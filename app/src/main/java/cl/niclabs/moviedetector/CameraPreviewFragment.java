package cl.niclabs.moviedetector;

import android.content.Context;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import cl.niclabs.moviedetector.descriptors.GrayHistogramExtractor;
import cl.niclabs.moviedetector.descriptors.GrayHistogramImageDescriptor;
import cl.niclabs.moviedetector.descriptors.VideoDescriptor;
import cl.niclabs.moviedetector.http.SearchRequest;


/**
 * A placeholder fragment containing a simple view.
 */
public class CameraPreviewFragment extends Fragment{
    private static final String TAG = CameraPreviewFragment.class.getSimpleName();

    CameraContainer cameraContainer;
    GrayHistogramExtractor descriptorExtractor;
    ProgressBar progressBar;
    private Button recordButton;


    private class VideoDescriptorExtractor implements Camera.PreviewCallback{
        private VideoDescriptor<GrayHistogramImageDescriptor> videoDescriptor;
        private long startTime;
        private long lastDescriptor = 0;
        private int frameCounter = -1;
        private static final long max_time = 5000;
        private static final long segmentation = 250;
        public VideoDescriptorExtractor(){
            videoDescriptor = new VideoDescriptor<GrayHistogramImageDescriptor>();
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
                    GrayHistogramImageDescriptor descriptor = (GrayHistogramImageDescriptor) descriptorExtractor.extract(data, timeRecorded, frameCounter);
                    videoDescriptor.addDescriptor(descriptor);
                }
            }
            else{
                Toast.makeText(getActivity(), "Computed descriptors", Toast.LENGTH_SHORT).show();
                cameraContainer.setPreviewCallback(new NullPreviewCallback());
                QueryResultsFragment queryResultsFragment = new QueryResultsFragment();
//                queryResultsFragment.setEmptyText("Esperando resultados");

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
                    Toast.makeText(getActivity(), "Computing descriptors", Toast.LENGTH_SHORT).show();
                    recordButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
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
