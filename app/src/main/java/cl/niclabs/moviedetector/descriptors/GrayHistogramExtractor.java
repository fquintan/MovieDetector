package cl.niclabs.moviedetector.descriptors;

import android.content.Context;
import android.support.v8.renderscript.*;
import android.util.Log;

import cl.niclabs.moviedetector.ScriptC_GrayZoneHist;
import cl.niclabs.moviedetector.utils.ScreenBoundaries;
import fquintan.renderscripttest.ScriptC_decodeYUV;

/**
 * Created by felipe on 23-09-15.
 */
public class GrayHistogramExtractor implements ImageDescriptorExtractor{
    private static final String TAG = GrayHistogramExtractor.class.getSimpleName();

    public GrayHistogramExtractor(Context context, int imageHeight, int imageWidth, ScreenBoundaries croppingLimits) {
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        histogram = new int[histogramLength];
        screenLimits = croppingLimits;
        setupRenderscript(context);
    }

    private final int bins = 64;
    private final int horizontalZones = 2;
    private final int verticalZones = 2;
    private int imageHeight;
    private int imageWidth;

    ScreenBoundaries screenLimits;

    private final int histogramLength = bins * horizontalZones * verticalZones;

    private final int[] emptyHist = new int[histogramLength];
    private int[] histogram;

    private RenderScript scriptContext;
    private ScriptC_decodeYUV yuvToGrayDecoder;
    private ScriptC_GrayZoneHist histogramExtractor;

    private Allocation decoderOutAllocation;
    private Allocation croppedAllocation;
    private Allocation histOutAllocation;

    @Override
    public ImageDescriptor extract(byte[] frame, long timestamp, int frameNumber) {
        decoderOutAllocation.copy2DRangeFrom(0, 0, imageWidth, imageHeight, frame);
        croppedAllocation.copy2DRangeFrom(0, 0, screenLimits.getWidth(), screenLimits.getHeight(),
                decoderOutAllocation, screenLimits.left, screenLimits.top);
        histOutAllocation.copyFrom(emptyHist);
        histogramExtractor.set_gIn(decoderOutAllocation);
        histogramExtractor.set_gOut(histOutAllocation);
        histogramExtractor.bind_gOutarray(histOutAllocation);
        histogramExtractor.set_gScript(histogramExtractor);
        histogramExtractor.invoke_compute_histogram();

        histOutAllocation.copyTo(histogram);
        int zone_total = imageHeight * imageWidth / 4;
        double[] descriptor = new double[histogram.length];
        for(int i = 0; i < histogram.length; i ++){
            descriptor[i] = ((float) histogram[i]) / zone_total;
        }
        return new GrayHistogramDescriptor(descriptor, timestamp, frameNumber, horizontalZones, verticalZones, bins);
    }

    private void setupRenderscript(Context context) {
        Log.d(TAG, "setting up renderscript");
        scriptContext = RenderScript.create(context);
        histogramExtractor = new ScriptC_GrayZoneHist(scriptContext);

        Element decoderOutElement = Element.U8(scriptContext);
        Type.Builder decoderOutType = new Type.Builder(scriptContext, decoderOutElement);

        decoderOutAllocation = Allocation.createTyped(scriptContext,
                decoderOutType.setX(imageWidth).setY(imageHeight).create(),   // Allocation Type
                Allocation.MipmapControl.MIPMAP_NONE,    // No MIPMAP
                Allocation.USAGE_SCRIPT                 // will be used by a script
        );
        Type.Builder croppedTB = new Type.Builder(scriptContext, Element.U8(scriptContext));
        croppedTB.setX(screenLimits.getWidth());
        croppedTB.setY(screenLimits.getHeight());
        croppedAllocation = Allocation.createTyped(scriptContext, croppedTB.create(), Allocation.USAGE_SCRIPT);

        Type.Builder histOutType = new Type.Builder(scriptContext, Element.I32(scriptContext));
        histOutAllocation = Allocation.createTyped(scriptContext, histOutType.setX(histogramLength).create(),
                Allocation.USAGE_SCRIPT);

        histogramExtractor.invoke_setup_histogram(horizontalZones, verticalZones,
                screenLimits.getWidth(), screenLimits.getHeight(), bins);
    }


}
