package cl.niclabs.moviedetector.descriptors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Created by felipe on 23-09-15.
 */
public class GrayHistogramDescriptor extends ImageDescriptor{

    private int zones_x;
    private int zones_y;
    private int bins;

    public GrayHistogramDescriptor(double[] descriptor, long timestamp, int frameNumber) {
        super(descriptor, timestamp, frameNumber);
    }

    public GrayHistogramDescriptor(double[] descriptor, long timestamp, int frameNumber, int zones_x, int zones_y, int bins) {
        super(descriptor, timestamp, frameNumber);
        this.zones_x = zones_x;
        this.zones_y = zones_y;
        this.bins = bins;
    }

    @Override
    public String getType() {
        return "GrayHistogram";
    }

    @Override
    public String getSerializedOptions() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("zones_x", zones_x);
        jsonObject.addProperty("zones_y", zones_y);
        jsonObject.addProperty("bins", bins);
        jsonObject.addProperty("quant", "4F");
        return new Gson().toJson(jsonObject);
    }
}
