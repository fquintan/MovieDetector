package cl.niclabs.moviedetector.http;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cl.niclabs.moviedetector.descriptors.VideoDescriptor;

/**
 * Created by felipe on 29-09-15.
 */
public class SearchRequest {

    private static final String TAG = SearchRequest.class.getSimpleName();

//    private final String queryURL = "http://172.30.65.34:5000/search/api/search_by_descriptor";
    private final String queryURL = "http://172.30.65.34:5000/search/api/search_by_descriptor";
    private VideoDescriptor videoDescriptor;
    private ResponseHandler responseHandler;

    public SearchRequest(VideoDescriptor videoDescriptor, ResponseHandler responseHandler) {
        this.videoDescriptor = videoDescriptor;
        this.responseHandler = responseHandler;
    }

    public void execute(){
        new RequestAsyncTask(responseHandler).execute(videoDescriptor);
    }

    private class RequestAsyncTask extends AsyncTask<VideoDescriptor, Void, String>{
        private ResponseHandler responseHandler;

        public RequestAsyncTask(ResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
        }

        @Override
        protected String doInBackground(VideoDescriptor... params) {
            URL urlToRequest;
            StringBuilder response = new StringBuilder();
            HttpURLConnection urlConnection = null;
            try {
                urlToRequest = new URL(queryURL);
                urlConnection = (HttpURLConnection) urlToRequest.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
//                urlConnection.setChunkedStreamingMode(0);

                DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
//                writeStream(out);
                //Create JSONObject here
                String json = videoDescriptor.toJSON();
                out.writeBytes(json);
                out.flush();
                out.close();
                urlConnection.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }


            }
            catch (MalformedURLException e) {
                Log.d(TAG, queryURL + " is not a valid URL");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            responseHandler.onResponse(response);
        }
    }
}
