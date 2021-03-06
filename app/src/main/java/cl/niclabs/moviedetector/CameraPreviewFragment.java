package cl.niclabs.moviedetector;

import android.content.Context;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import cl.niclabs.moviedetector.descriptors.ImageDescriptorExtractor;
import cl.niclabs.moviedetector.descriptors.ColorLayoutDescriptor;
import cl.niclabs.moviedetector.descriptors.ColorLayoutExtractor;
import cl.niclabs.moviedetector.descriptors.VideoDescriptor;
import cl.niclabs.moviedetector.http.FromDescriptorsSearchRequest;
import cl.niclabs.moviedetector.utils.ScreenBoundaries;


/**
 * A placeholder fragment containing a simple view.
 */
public class CameraPreviewFragment extends Fragment{
    private static final String TAG = CameraPreviewFragment.class.getCanonicalName();

    CameraContainer cameraContainer;
    ImageDescriptorExtractor descriptorExtractor;
    ProgressBar progressBar;
    private Button recordButton;
    private CropView screenLimits;


    private class VideoDescriptorExtractor implements Camera.PreviewCallback{
//        private VideoDescriptor<EdgeHistogramDescriptor, Double> videoDescriptor;
//        private VideoDescriptor<GrayHistogramDescriptor, Double> videoDescriptor;
        private VideoDescriptor<ColorLayoutDescriptor, Integer> videoDescriptor;
        private long startTime;
        private long lastDescriptor = 0;
        private int frameCounter = -1;
        private static final long max_time = 5000;
        private static final long segmentation = 250;
        public VideoDescriptorExtractor(){
//            videoDescriptor = new VideoDescriptor<EdgeHistogramDescriptor, Double>(Double.class);
//            videoDescriptor = new VideoDescriptor<GrayHistogramDescriptor, Double>(Double.class);
            videoDescriptor = new VideoDescriptor<ColorLayoutDescriptor, Integer>(Integer.class);
            startTime = System.currentTimeMillis();
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            frameCounter++;
            long currentTime = System.currentTimeMillis();
            long timeRecorded = currentTime - startTime;
            if (timeRecorded < max_time){
                progressBar.setProgress((int) timeRecorded);
                if (currentTime - lastDescriptor > segmentation){
                    lastDescriptor = currentTime;
//                    GrayHistogramDescriptor descriptor = (GrayHistogramDescriptor) descriptorExtractor.extract(data, timeRecorded, frameCounter);
                    ColorLayoutDescriptor descriptor = (ColorLayoutDescriptor) descriptorExtractor.extract(data, timeRecorded, frameCounter);
//                    EdgeHistogramDescriptor descriptor = (EdgeHistogramDescriptor) descriptorExtractor.extract(data, timeRecorded, frameCounter);
                    videoDescriptor.addDescriptor(descriptor);
                }
            }
            else{
                cameraContainer.setPreviewCallback(new NullPreviewCallback());
                QueryResultsFragment queryResultsFragment = new QueryResultsFragment();
                new FromDescriptorsSearchRequest(videoDescriptor, queryResultsFragment).execute();

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, queryResultsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

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

        recordButton = (Button) view.findViewById(R.id.start_recording_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager)
                        getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // fetch data
                    recordButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    descriptorExtractor = initializeDescriptorExtractor();
                    cameraContainer.setPreviewCallback(new VideoDescriptorExtractor());
                } else {
                    // display error
                    Toast.makeText(getActivity(), "Couldn't access network", Toast.LENGTH_SHORT).show();
                }
            }
        });
        progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        progressBar.setIndeterminate(false);
        progressBar.setMax((int) VideoDescriptorExtractor.max_time);
        Context context = getActivity();
        if (cameraContainer != null){
            cameraContainer.stopCameraPreview();
            cameraContainer.releaseCamera();
        }
        cameraContainer = new CameraContainer(context, new NullPreviewCallback());
        FrameLayout cameraPreview = (FrameLayout) view.findViewById(R.id.camera_preview);
        cameraPreview.addView(cameraContainer);
        screenLimits = (CropView) view.findViewById(R.id.screen_limits);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
//        screenLimits.setupPoints(getActivity(), screenWidth/2, screenHeight/2, 300, 200);

        return view;
    }



    private ImageDescriptorExtractor initializeDescriptorExtractor(){
        int left = screenLimits.getLeftLimit();
        int right = screenLimits.getRightLimit();
        int top = screenLimits.getTopLimit();
        int bottom = screenLimits.getBottomLimit();

        int screenWidth = cameraContainer.getRight();
        int screenHeight = cameraContainer.getBottom();
        int cameraWidth = cameraContainer.getImageWidth();
        int cameraHeight = cameraContainer.getImageHeight();
        left = (int) (cameraWidth / ((double) screenWidth) * left);
        right = (int) (cameraWidth / ((double) screenWidth) * right);
        top = (int) (cameraHeight / ((double) screenHeight) * top);
        bottom = (int) (cameraHeight / ((double) screenHeight) * bottom);
        ScreenBoundaries boundaries = new ScreenBoundaries(left, right, top, bottom);

        return new ColorLayoutExtractor(getActivity(), 10, 10,
                cameraContainer.getImageWidth(),
                cameraContainer.getImageHeight(),
                boundaries);
//        return new GrayHistogramExtractor(getActivity(),
//                cameraContainer.getImageHeight(),
//                cameraContainer.getImageHeight(),
//                boundaries);
//        return new EdgeHistogramExtractor(getActivity(),
//                cameraContainer.getImageWidth(),
//                cameraContainer.getImageHeight(),
//                boundaries);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        cameraContainer.stopCameraPreview();
        cameraContainer.releaseCamera();
        cameraContainer = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause");
        cameraContainer.stopCameraPreview();
        cameraContainer.releaseCamera();
//        cameraContainer.setVisibility(View.GONE);
        View view = getView();
        FrameLayout cameraPreview = (FrameLayout) view.findViewById(R.id.camera_preview);
        cameraPreview.removeAllViews();
//        cameraContainer = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        FrameLayout cameraPreview = (FrameLayout) getView().findViewById(R.id.camera_preview);
        if (cameraPreview.getChildCount() == 0){
            cameraPreview.addView(cameraContainer);
        }
//        cameraContainer.setVisibility(View.VISIBLE);
        cameraContainer.startCamera();
        cameraContainer.startCameraPreview();
    }
}
