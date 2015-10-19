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
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(VideoDescriptor.class, new VideoDescriptorJSONAdapter());
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
//        gson = new Gson();

    }

    public ArrayList<T> getImageDescriptors() {
        return imageDescriptors;
    }

    public void addDescriptor(T imageDescriptor){
        imageDescriptors.add(imageDescriptor);
    }

    public String toJSON(){
        return gson.toJson(this);
    }

    private class VideoDescriptorJSONAdapter extends TypeAdapter<VideoDescriptor<T>> {

        @Override
        public void write(JsonWriter out, VideoDescriptor<T> value) throws IOException {
            T firstDescriptor = value.getImageDescriptors().get(0);
            out.beginObject();
            out.name("type").value(firstDescriptor.getType());
            out.name("options").value(firstDescriptor.getSerializedOptions());
            out.name("length").value(value.getImageDescriptors().size());
            Gson imageDescriptorSerializer = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            out.name("descriptors").value(imageDescriptorSerializer.toJson(value.getImageDescriptors()));
            out.endObject();
        }

        @Override
        public VideoDescriptor read(JsonReader in) throws IOException {
            return null;
        }
    }
}
