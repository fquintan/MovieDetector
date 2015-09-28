package cl.niclabs.moviedetector;

import android.content.Context;
import android.hardware.Camera;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import cl.niclabs.moviedetector.descriptors.GrayHistogramExtractor;
import cl.niclabs.moviedetector.descriptors.ImageDescriptor;


/**
 * A placeholder fragment containing a simple view.
 */
public class CameraPreviewFragment extends Fragment {
    private static final String TAG = CameraPreviewFragment.class.getSimpleName();

    CameraContainer cameraContainer;
    GrayHistogramExtractor descriptorExtractor;

    private class VideoDescriptorExtractor implements Camera.PreviewCallback{
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            long currentTime = System.currentTimeMillis();
            ImageDescriptor descriptor = descriptorExtractor.extract(data, currentTime);

        }
    }
    private class NullPreviewCallback implements Camera.PreviewCallback{

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.camera_preview_fragment, container, false);
        Button recordButton = (Button) view.findViewById(R.id.start_recording_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraContainer.setPreviewCallback(new VideoDescriptorExtractor());
            }
        });
        Context context = getActivity();
        cameraContainer = new CameraContainer(context, new NullPreviewCallback());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        FrameLayout cameraPreview = (FrameLayout) getView().findViewById(R.id.camera_preview);
        cameraPreview.addView(cameraContainer);
        cameraContainer.startCamera();
        cameraContainer.startPreview();
        descriptorExtractor = new GrayHistogramExtractor(getActivity(), 480, 320);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause");
        cameraContainer.stopPreviewAndFreeCamera();
        View view = getView();
        FrameLayout cameraPreview = (FrameLayout) view.findViewById(R.id.camera_preview);
        cameraPreview.removeAllViews();
//        cameraContainer = null;

    }
}
