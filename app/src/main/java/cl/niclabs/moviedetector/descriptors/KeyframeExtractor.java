package cl.niclabs.moviedetector.descriptors;

import android.content.Context;
import android.graphics.ImageFormat;
import android.support.v8.renderscript.*;
import android.util.Log;

import cl.niclabs.moviedetector.ScriptC_keyframe;
import cl.niclabs.moviedetector.utils.ScreenBoundaries;

/**
 * Created by felipe on 20-10-15.
 */
public class KeyframeExtractor implements ImageDescriptorExtractor{

    int descriptorHeight;
    int descriptorWidth;
    int descriptorLength;

    int imageWidth;
    int imageHeight;

    int zoneHeight;
    int zoneWidth;

    ScreenBoundaries screenLimits;

    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRGB;
    private ScriptC_keyframe keyframeScript;

    private Allocation yuvAllocation;
    private Allocation rgbAllocation;
    private Allocation rgbCroppedAllocation;
    private Allocation keyFrameAllocation;
    private int[] emptyDescriptor;
    private int[] descriptor;

    public KeyframeExtractor(Context context, int descriptorHeight, int descriptorWidth, int imageWidth, int imageHeight, ScreenBoundaries croppingLimits) {
        this.descriptorHeight = descriptorHeight;
        this.descriptorWidth = descriptorWidth;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        descriptorLength = descriptorHeight*descriptorWidth*3;
        emptyDescriptor = new int[descriptorLength];
        descriptor = new int[descriptorLength];
        zoneHeight = imageHeight / descriptorHeight;
        zoneWidth = imageWidth / descriptorWidth;
        this.screenLimits = croppingLimits;

        setupRenderscript(context);
    }

    @Override
    public ImageDescriptor extract(byte[] frame, long timestamp, int frameNumber) {
        yuvAllocation.copyFrom(frame);
//        yuvAllocation.copy2DRangeFrom(0,0,imageWidth,imageHeight,frame);
        yuvToRGB.setInput(yuvAllocation);
        yuvToRGB.forEach(rgbAllocation);
        Log.d("KeyframeExtractor", "Attempting to crop allocation");

        rgbCroppedAllocation.copy2DRangeFrom(0, 0, screenLimits.getWidth(), screenLimits.getHeight(),
                                             rgbAllocation, screenLimits.left, screenLimits.top);
        Log.d("KeyframeExtractor", "cropped allocation");
        keyFrameAllocation.copyFrom(emptyDescriptor);
        keyframeScript.set_gIn(rgbCroppedAllocation);
        keyframeScript.set_gOut(keyFrameAllocation);
        keyframeScript.bind_gOutarray(keyFrameAllocation);

//        keyframeScript.set_gScript(keyframeScript);
        keyframeScript.invoke_compute_keyframe();
        rs.finish();

        keyFrameAllocation.copyTo(descriptor);

        double[] descriptor_as_double = new double[descriptorLength];
        int count = 0;
        for (int i = 0; i < descriptorLength; i++){
            descriptor_as_double[i] = descriptor[i] / (zoneHeight*zoneWidth);
            count += descriptor[i];
        }
        return new KeyframeDescriptor(descriptor_as_double, timestamp, frameNumber,
                                        descriptorHeight, descriptorWidth);
    }

    private void setupRenderscript(Context context){
        rs = RenderScript.create(context);
        yuvToRGB = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        keyframeScript = new ScriptC_keyframe(rs);

        Type.Builder tbYUV = new Type.Builder(rs, Element.U8(rs));
        tbYUV.setX(imageWidth);
        tbYUV.setY(imageHeight);
        tbYUV.setYuvFormat(ImageFormat.NV21);

        Type.Builder tbRGB = new Type.Builder(rs, Element.U8_4(rs));
        tbRGB.setX(imageWidth);
        tbRGB.setY(imageHeight);

        Type.Builder tbRGBCrop = new Type.Builder(rs, Element.U8_4(rs));
        tbRGBCrop.setX(screenLimits.getWidth());
        tbRGBCrop.setY(screenLimits.getHeight());

        Type.Builder tbKeyframe = new Type.Builder(rs, Element.I32(rs));
        tbKeyframe.setX(descriptorLength);

        yuvAllocation = Allocation.createTyped(rs, tbYUV.create(), Allocation.MipmapControl.MIPMAP_NONE,  Allocation.USAGE_SCRIPT & Allocation.USAGE_SHARED);
        rgbAllocation = Allocation.createTyped(rs, tbRGB.create(), Allocation.MipmapControl.MIPMAP_NONE,  Allocation.USAGE_SCRIPT );
        rgbCroppedAllocation = Allocation.createTyped(rs, tbRGBCrop.create(), Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        keyFrameAllocation = Allocation.createTyped(rs, tbKeyframe.create(), Allocation.USAGE_SCRIPT);

//        keyframeScript.invoke_setup2(descriptorWidth, descriptorHeight, imageWidth, imageHeight);
        keyframeScript.invoke_setup2(descriptorWidth, descriptorHeight, screenLimits.getWidth(), screenLimits.getHeight());
        keyframeScript.set_gScript(keyframeScript);

    }
}
