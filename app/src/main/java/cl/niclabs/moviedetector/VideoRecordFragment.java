package cl.niclabs.moviedetector;

/**
 * Created by felipe on 21-11-15.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;

import cl.niclabs.moviedetector.http.FromVideoSearchRequest;
import cl.niclabs.moviedetector.http.ResponseHandler;

/**
 * A placeholder fragment containing a simple view.
 */
public class VideoRecordFragment extends Fragment {
    private static final String TAG = VideoRecordFragment.class.getCanonicalName();
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 1;

    private Button recordButton;
    private final String appName = "MovieDetector";
    private Uri videoFileUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.video_record_fragment, container, false);
        recordButton = (Button) view.findViewById(R.id.start_recording_intent_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                videoFileUri = getOutputMediaFileUri();

                intent.putExtra(MediaStore.EXTRA_OUTPUT, videoFileUri);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

                startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE){
            if (resultCode == getActivity().RESULT_OK){
                Toast.makeText(getActivity(), "Sending video", Toast.LENGTH_LONG).show();
                QueryResultsFragment queryResultsFragment = new QueryResultsFragment();
                new FromVideoSearchRequest(videoFileUri, queryResultsFragment).execute();

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, queryResultsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }

    }

    private Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), appName);

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Toast.makeText(getActivity(), "Failed to create directory MyCameraVideo.",
                        Toast.LENGTH_LONG).show();
                Log.d("MyCameraVideo", "Failed to create directory MyCameraVideo.");
                return null;
            }
        }

        java.util.Date date = new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());

        File mediaFile;
        // For unique video file name appending current timeStamp with file name
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                "VID_" + timeStamp + ".mp4");
            "VID_" + ".mp4");

        return mediaFile;
    }


}
