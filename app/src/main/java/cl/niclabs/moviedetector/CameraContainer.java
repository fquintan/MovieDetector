package cl.niclabs.moviedetector;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.nfc.Tag;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felipe on 21-09-15.
 */

public class CameraContainer extends SurfaceView implements SurfaceHolder.Callback  {
    private String TAG = CameraContainer.class.getCanonicalName();

    private SurfaceHolder mHolder;
    private Camera mCamera;

    private int width;
    private int height;
    private boolean previewRunning;

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
        Log.d(TAG, "constructor");
        mHolder = getHolder();
        if(mHolder == null){
            Log.e(TAG, "Error: couldn't get a surface holder in constructor");
        }
        mHolder.addCallback(this);
        previewCallback = callback;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        startCamera();
//        startCameraPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        startCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        stopCameraPreview();
        releaseCamera();
    }

    public void startCamera(){
        Log.d(TAG, "startCamera");
        if (mCamera != null){
            Log.d(TAG, "Camera was already started");
            return;
        }
        mCamera = getCameraInstance();
    }

    public void startCameraPreview(){
        Log.d(TAG, "startCameraPreview");
        if (previewRunning){
            Log.d(TAG, "Preview already started");
//            return;
        }
        if (mHolder.getSurface() == null || mCamera == null){
            Log.d(TAG, "Couldn't start preview");
            return;
        }

        List<Camera.Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        Camera.Size selectedSize = getPreferredSize(previewSizes);
        Camera.Parameters p = mCamera.getParameters();
        Log.d(TAG, "setting preview parameters");
        p.setPreviewSize(selectedSize.width, selectedSize.height);
        p.setPreviewFormat(ImageFormat.NV21);
        mCamera.setParameters(p);

        // start preview with new settings
        try {
            mHolder.addCallback(this);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
            previewRunning = true;

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public int  getImageWidth() {
        return width;
    }

    public int getImageHeight() {
        return height;
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
                height = size.height;
                width = size.width;

                break;
            }
        }
        return selectedSize;
    }

    public void stopCameraPreview() {
        Log.d(TAG, "stopCameraPreview");
        if (mCamera != null && previewRunning) {
            mHolder.removeCallback(this);
//            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
        previewRunning = false;
    }

    public void releaseCamera(){
        Log.d(TAG, "release camera");
        if (mCamera != null){
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
