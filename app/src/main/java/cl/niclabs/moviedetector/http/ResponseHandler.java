package cl.niclabs.moviedetector.http;

/**
 * Created by felipe on 29-09-15.
 */
public interface ResponseHandler {
    void onSuccessResponse(String responseText);
    void onFailure();
}
