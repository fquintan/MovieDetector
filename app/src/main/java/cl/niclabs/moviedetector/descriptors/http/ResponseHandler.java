package cl.niclabs.moviedetector.descriptors.http;

import java.io.InputStream;

/**
 * Created by felipe on 29-09-15.
 */
public interface ResponseHandler {
    public void onResponse(String responseText);
}
