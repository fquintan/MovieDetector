package cl.niclabs.moviedetector.descriptors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Created by felipe on 05-11-15.
 */
public class EdgeHistogramDescriptor extends ImageDescriptor {

    private int zones_x;
    private int zones_y;

    private int subdivisions_x;
    private int subdivisions_y;

    private int threshold;

    public EdgeHistogramDescriptor(double[] descriptor, long timestamp, int frameNumber) {
        super(descriptor, timestamp, frameNumber);
    }

    @Override
    public String getType() {
        return "EdgeHistogram";
    }

    @Override
    public String getSerializedOptions() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("zones_x", zones_x);
        jsonObject.addProperty("zones_y", zones_y);
        jsonObject.addProperty("subdivisions_x", subdivisions_x);
        jsonObject.addProperty("subdivisions_y", subdivisions_y);
        jsonObject.addProperty("threshold", threshold);
        jsonObject.addProperty("quant", "4F");
        return new Gson().toJson(jsonObject);
    }
}
