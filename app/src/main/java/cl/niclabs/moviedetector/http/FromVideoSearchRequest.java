package cl.niclabs.moviedetector.http;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by felipe on 21-11-15.
 */
public class FromVideoSearchRequest {

    private static final String TAG = FromVideoSearchRequest.class.getSimpleName();
    private static final String FAIL = "FAIL";
    private Uri videoFile;
    private ResponseHandler responseHandler;

    public FromVideoSearchRequest(Uri videoFile, ResponseHandler responseHandler) {
        this.videoFile = videoFile;
        this.responseHandler = responseHandler;
    }

//    private final String queryURL = "http://192.168.0.10:5000/search/api/search_by_video_file";
    private final String queryURL = "http://172.30.65.34:5000/search/api/search_by_video_file";
    public void execute(){new RequestAsyncTask(responseHandler).execute(videoFile);}

    private class RequestAsyncTask extends AsyncTask<Uri, Integer, String>{
        private ResponseHandler responseHandler;
        private int maxBufferSize = 1024 * 1024;

        public RequestAsyncTask(ResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
        }

        @Override
        protected String doInBackground(Uri... params) {
            Log.d(TAG, "doInBackground");
            URL urlToRequest = null;
            StringBuilder response = new StringBuilder();
            HttpURLConnection connection = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            String fileName = params[0].getLastPathSegment();

            try {
                urlToRequest = new URL(queryURL);
            }
            catch (MalformedURLException e) {
                Log.d(TAG, queryURL + " is not a valid URL");
                this.cancel(true);
                return FAIL;
            }

            try {
                Log.d(TAG, "Finding server");
                connection = (HttpURLConnection) urlToRequest.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                Log.d(TAG, "Attempting connection with server");
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                Log.d(TAG, "Established connection with server");

                out.writeBytes(twoHyphens + boundary + lineEnd);
                out.writeBytes("Content-Disposition: form-data; name=\"descriptor\"" + lineEnd + lineEnd);
                out.writeBytes("KF_10x10_RGB_1U" + lineEnd);

                out.writeBytes(twoHyphens + boundary + lineEnd);
                out.writeBytes("Content-Disposition: form-data; name=\"alias\"" + lineEnd + lineEnd);
                out.writeBytes("kf" + lineEnd);

                out.writeBytes(twoHyphens + boundary + lineEnd);
                out.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                + fileName + "\"" + lineEnd);
                out.writeBytes("Content-type: video/mp4" + lineEnd);
                out.writeBytes(lineEnd);
                Log.d(TAG, "Wrote file preamble");

                // create a buffer of  maximum size
                FileInputStream fileInputStream = new FileInputStream(new File(params[0].getEncodedPath()));
                int bytesAvailable = fileInputStream.available();

                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer = new byte[bufferSize];

                // read file and write it into form...
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                Log.d(TAG, "Preparing to write file");

                while (bytesRead > 0) {

                    out.write(buffer, 0, bufferSize);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }
                Log.d(TAG, "Finished writing file");


                // send multipart form data necesssary after file data...
                out.writeBytes(lineEnd);
                out.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                out.flush();
                out.close();
                Log.d(TAG, "Waiting for server response");
                connection.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while((line = in.readLine()) != null){
                    response.append(line);
                }
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                cancel(true);
                return FAIL;
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals(FAIL)){
                responseHandler.onFailure();
                return;
            }
            responseHandler.onSuccessResponse(s);
        }

        @Override
        protected void onCancelled(String s) {
            responseHandler.onFailure();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.d(TAG, "progress: " + values[0]);
        }

    }

}
