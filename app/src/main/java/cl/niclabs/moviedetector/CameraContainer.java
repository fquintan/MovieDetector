package cl.niclabs.moviedetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by felipe on 21-09-15.
 */

public class CameraContainer extends SurfaceView implements SurfaceHolder.Callback  {
    private String TAG = CameraContainer.class.getSimpleName();

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
        if(mCamera != null){
            mCamera.setPreviewCallback(previewCallback);
        }
    }

    private Camera.PreviewCallback previewCallback;

    private int id;

//    private int width;
//    private int height;

    @SuppressWarnings("deprecation")
    public CameraContainer(Context context, Camera.PreviewCallback callback) {
        super(context);


        id = new Random().nextInt();
        TAG = TAG+id;
        mHolder = getHolder();
        if(mHolder == null){
            Log.e(TAG, "Error: couldn't get a surface holder in constructor");
        }
        mHolder.addCallback(this);
        previewCallback = callback;
//        setWillNotDraw(false);
//        startCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
//        this.width = width;
//        this.height = height;

        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        stopPreviewAndFreeCamera();
        Log.d(TAG, "surfaceDestroyed");
    }

    public void startCamera(){
        Log.d(TAG, "startCamera");
        if (mCamera != null){
            return;
        }
        mCamera = getCameraInstance();
    }

    public void startPreview(){
        Log.d(TAG, "startPreview");
        if (mHolder.getSurface() == null || mCamera == null){
            // preview surface does not exist
            Log.d(TAG, "Couldn't start preview, camera or surfaceView doesn't exist");
            return;
        }
        // if previous preview is active, stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        List<Camera.Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        Camera.Size selectedSize = getPreferredSize(previewSizes);
        Camera.Parameters p = mCamera.getParameters();
        Log.d(TAG, "setting preview parameters");
        p.setPreviewSize(selectedSize.width, selectedSize.height);
        p.setPreviewFormat(ImageFormat.NV21);
        mCamera.setParameters(p);

        // start preview with new settings
        try {
            Log.d(TAG, "setting preview display");
            mCamera.setPreviewDisplay(mHolder);
            Log.d(TAG, "setting preview callback");
            mCamera.setPreviewCallback(previewCallback);
            Log.d(TAG, "starting preview");
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private Camera.Size getPreferredSize(List<Camera.Size> previewSizes) {
        ArrayList<Camera.Size> preferredSizes = new ArrayList<Camera.Size>();
        preferredSizes.add(mCamera.new Size(480, 320));
        preferredSizes.add(mCamera.new Size(640, 480));
        preferredSizes.add(mCamera.new Size(800, 480));
        preferredSizes.add(mCamera.new Size(864, 480));

        Camera.Size selectedSize = null;
        for (Camera.Size size : preferredSizes){
            if (previewSizes.contains(size)){
                selectedSize = size;
                break;
            }
        }
        return selectedSize;
    }

    public void stopPreviewAndFreeCamera() {
        Log.d(TAG, "stopPreviewAndFreeCamera");

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
            mHolder.removeCallback(this);

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }

    @SuppressWarnings("deprecation")
    private Camera getCameraInstance(){
        Log.d(TAG, "getCameraInstance");
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
            Log.d(TAG, "camera created");
        }
        catch (Exception e){
            Log.d(TAG, "failed to initialize camera");
            e.printStackTrace();
            Log.d(TAG, "exception logged");
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


}
