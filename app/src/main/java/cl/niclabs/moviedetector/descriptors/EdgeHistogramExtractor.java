package cl.niclabs.moviedetector.descriptors;

import android.content.Context;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Type;

import cl.niclabs.moviedetector.ScriptC_borderDetector;
import cl.niclabs.moviedetector.ScriptC_edgeHist;
import cl.niclabs.moviedetector.ScriptC_reducer;
import cl.niclabs.moviedetector.utils.ScreenBoundaries;

/**
 * Created by felipe on 13-11-15.
 */
public class EdgeHistogramExtractor implements ImageDescriptorExtractor {

    int descriptorLength;

    int imageWidth;
    int imageHeight;

    int croppedImageWidth;
    int croppedImageHeight;

    int numberOfBlocksW = 8;
    int numberOfBlocksH = 8;

    int numberOfZonesW = 4;
    int numberOfZonesH = 4;

    int totalBlocksW = numberOfBlocksW * numberOfZonesW;
    int totalBlocksH = numberOfBlocksH * numberOfZonesH;
    int totalBlocks = totalBlocksH * totalBlocksW;

    int totalSubBlocksW = totalBlocksW * 2;
    int totalSubBlocksH = totalBlocksH * 2;
    int totalSubBlocks = totalSubBlocksH * totalSubBlocksW;

    int threshold = 5;

    int zoneHeight;
    int zoneWidth;

    int blockHeight;
    int blockWidth;

    int subBlockHeight;
    int subBlockWidth;

    ScreenBoundaries screenLimits;

    private RenderScript rs;
    private ScriptC_reducer reducer;
    private ScriptC_borderDetector detector;
    private ScriptC_edgeHist edgeHist;

//    private Allocation yuvAllocation;
    private Allocation grayAllocation;
    private Allocation grayCroppedAllocation;
    private Allocation reducedImageAllocation;
    private Allocation reducedImage2DAllocation;
    private Allocation blockEnergyAllocation;
    private Allocation histogramAllocation;

    private int[] emptyDescriptor;
    private int[] descriptor;

    public EdgeHistogramExtractor(Context context, int imageWidth, int imageHeight, ScreenBoundaries screenLimits) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.screenLimits = screenLimits;

        subBlockHeight = screenLimits.getHeight() / totalSubBlocksH;
        subBlockWidth = screenLimits.getWidth() / totalSubBlocksW;
        blockHeight = subBlockHeight * 2;
        blockWidth = subBlockWidth * 2;
        zoneHeight = blockHeight * numberOfBlocksW;
        zoneWidth = blockWidth * numberOfBlocksH;
        croppedImageHeight = zoneHeight * numberOfZonesH;
        croppedImageWidth = zoneWidth * numberOfZonesW;

        descriptorLength = numberOfZonesH * numberOfBlocksW * 5;
        emptyDescriptor = new int[descriptorLength];
        setupRenderscript(context);
    }


    @Override
    public ImageDescriptor extract(byte[] frame, long timestamp, int frameNumber) {
        grayAllocation.copy2DRangeFrom(0, 0, imageWidth, imageHeight, frame);
        grayCroppedAllocation.copy2DRangeFrom(0, 0, croppedImageWidth, croppedImageHeight,
                grayAllocation, screenLimits.left, screenLimits.top);
        reducedImageAllocation.copyFrom(new int[totalSubBlocks]);
        reducer.set_gIn(grayCroppedAllocation);
        reducer.set_gOut(reducedImageAllocation);
        reducer.bind_gOutarray(reducedImageAllocation);
        reducer.invoke_compute_reduce();
        int[] reducedImage = new int[totalSubBlocks];
        reducedImageAllocation.copyTo(reducedImage);
        for (int i = 0; i < reducedImage.length; i++) {
            reducedImage[i] = reducedImage[i] / (subBlockWidth * subBlockHeight);
        }
        reducedImage2DAllocation.copy2DRangeFrom(0,0,totalSubBlocksW, totalSubBlocksH, reducedImage);

        detector.set_gIn(reducedImage2DAllocation);
        detector.set_gOut(blockEnergyAllocation);
        detector.invoke_compute_reduce();

        edgeHist.set_gIn(blockEnergyAllocation);
        edgeHist.set_gOut(histogramAllocation);
        edgeHist.bind_gOutarray(histogramAllocation);
        edgeHist.invoke_compute_edge_histogram();





        return null;
    }

    private void setupRenderscript(Context context){
        rs = RenderScript.create(context);

        reducer = new ScriptC_reducer(rs);
        reducer.invoke_setup_reducer(totalSubBlocksW, totalSubBlocksH, croppedImageWidth, croppedImageHeight);
        reducer.set_gScript(reducer);

        detector = new ScriptC_borderDetector(rs);
        detector.invoke_setup_detector(threshold);
        detector.set_gScript(detector);

        edgeHist = new ScriptC_edgeHist(rs);
        edgeHist.invoke_setup_edge_histogram(numberOfZonesW, numberOfZonesH, totalBlocksW, totalBlocksH);
        edgeHist.set_gScript(edgeHist);

        Element decoderOutElement = Element.U8(rs);
        Type.Builder decoderOutType = new Type.Builder(rs, decoderOutElement);
        grayAllocation = Allocation.createTyped(rs,
                decoderOutType.setX(imageWidth).setY(imageHeight).create(),   // Allocation Type
                Allocation.MipmapControl.MIPMAP_NONE,    // No MIPMAP
                Allocation.USAGE_SCRIPT                 // will be used by a script
        );
        Type.Builder tbGrayCrop = new Type.Builder(rs, Element.U8(rs));
        tbGrayCrop.setX(croppedImageWidth);
        tbGrayCrop.setY(croppedImageHeight);
        grayCroppedAllocation = Allocation.createTyped(rs, tbGrayCrop.create(), Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        Type.Builder tbReduced = new Type.Builder(rs, Element.I32(rs));
        tbReduced.setX(totalSubBlocks);
        reducedImageAllocation = Allocation.createTyped(rs, tbReduced.create(), Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        Type.Builder tbReduced2D = new Type.Builder(rs, Element.I32(rs));
        tbReduced2D.setX(totalSubBlocksW);
        tbReduced2D.setY(totalSubBlocksH);
        reducedImage2DAllocation = Allocation.createTyped(rs, tbReduced2D.create(), Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        Type.Builder tbEnergy = new Type.Builder(rs, Element.U8(rs));
        tbEnergy.setX(totalBlocksW);
        tbEnergy.setY(totalBlocksH);
        blockEnergyAllocation = Allocation.createTyped(rs, tbEnergy.create(), Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        Type.Builder tbHist = new Type.Builder(rs, Element.U8(rs));
        tbHist.setX(descriptorLength);
        histogramAllocation = Allocation.createTyped(rs, tbHist.create(), Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

    }


}
