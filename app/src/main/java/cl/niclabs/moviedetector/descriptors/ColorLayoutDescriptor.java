package cl.niclabs.moviedetector.descriptors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Created by felipe on 19-10-15.
 */
public class ColorLayoutDescriptor extends ImageDescriptor {


    private int height;
    private int width;

    public ColorLayoutDescriptor(double[] descriptor, long timestamp, int frameNumber) {
        super(descriptor, timestamp, frameNumber);
    }

    public ColorLayoutDescriptor(double[] descriptor, long timestamp, int frameNumber, int height, int width) {
        super(descriptor, timestamp, frameNumber);
        this.height = height;
        this.width = width;
    }

    @Override
    public String getType() {
        return "Keyframe";
    }

    @Override
    public String getSerializedOptions() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("height", height);
        jsonObject.addProperty("width", width);
        jsonObject.addProperty("quant", "1U");
        jsonObject.addProperty("colorspace", "RGB");
        return new Gson().toJson(jsonObject);
    }

}
