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
import cl.niclabs.moviedetector.descriptors.KeyframeDescriptor;
import cl.niclabs.moviedetector.descriptors.KeyframeExtractor;
import cl.niclabs.moviedetector.descriptors.VideoDescriptor;
import cl.niclabs.moviedetector.http.SearchRequest;
import cl.niclabs.moviedetector.utils.ScreenBoundaries;


/**
 * A placeholder fragment containing a simple view.
 */
public class CameraPreviewFragment extends Fragment{
    private static final String TAG = CameraPreviewFragment.class.getSimpleName();

    CameraContainer cameraContainer;
    ImageDescriptorExtractor descriptorExtractor;
    ProgressBar progressBar;
    private Button recordButton;
    private DrawView screenLimits;


    private class VideoDescriptorExtractor implements Camera.PreviewCallback{
        private VideoDescriptor<KeyframeDescriptor, Integer> videoDescriptor;
        private long startTime;
        private long lastDescriptor = 0;
        private int frameCounter = -1;
        private static final long max_time = 5000;
        private static final long segmentation = 250;
        public VideoDescriptorExtractor(){
            videoDescriptor = new VideoDescriptor<KeyframeDescriptor, Integer>(Integer.class);
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
                    KeyframeDescriptor descriptor = (KeyframeDescriptor) descriptorExtractor.extract(data, timeRecorded, frameCounter);
                    videoDescriptor.addDescriptor(descriptor);
                }
            }
            else{
                cameraContainer.setPreviewCallback(new NullPreviewCallback());
                QueryResultsFragment queryResultsFragment = new QueryResultsFragment();
                new SearchRequest(videoDescriptor, queryResultsFragment).execute();

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
        cameraContainer = new CameraContainer(context, new NullPreviewCallback());
        screenLimits = (DrawView) view.findViewById(R.id.screen_limits);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
//        screenLimits.setupPoints(getActivity(), screenWidth/2, screenHeight/2, 300, 200);

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
//        descriptorExtractor = new GrayHistogramExtractor(getActivity(), cameraContainer.getImageWidth(), cameraContainer.getImageHeight());

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
        return new KeyframeExtractor(getActivity(), 10, 10,
                cameraContainer.getImageWidth(),
                cameraContainer.getImageHeight(),
                boundaries);
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
