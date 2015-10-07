package cl.niclabs.moviedetector.descriptors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cl.niclabs.moviedetector.utils.GsonHelper;

/**
 * Created by felipe on 23-09-15.
 */
public class VideoDescriptor <T extends ImageDescriptor>{

    private ArrayList<T> imageDescriptors;
    private Gson gson;

    public VideoDescriptor() {
        this(new ArrayList<T>());
    }

    public VideoDescriptor(ArrayList<T> imageDescriptors) {
        this.imageDescriptors = imageDescriptors;
//        final GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.registerTypeHierarchyAdapter(ImageDescriptor.class, new VideoDescriptorJSONAdapter());
//        gsonBuilder.setPrettyPrinting();
//        gson = gsonBuilder.create();
        gson = new Gson();

    }

    public ArrayList<T> getImageDescriptors() {
        return imageDescriptors;
    }

    public void addDescriptor(T imageDescriptor){
        imageDescriptors.add(imageDescriptor);
    }

    public String toJSON(){
        return gson.toJson(imageDescriptors);
    }

    private class VideoDescriptorJSONAdapter extends TypeAdapter<ImageDescriptor> {


        @Override
        public void write(JsonWriter out, ImageDescriptor value) throws IOException {
            out.beginObject();
            out.name("timestamp").value(value.getTimestamp());
            out.name("descriptor").value(GsonHelper.customGson.toJson(value.getBytes()));
            out.endObject();
        }

        @Override
        public ImageDescriptor read(JsonReader in) throws IOException {
            return null;
        }
    }
}
