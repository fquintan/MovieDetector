package cl.niclabs.moviedetector.descriptors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by felipe on 23-09-15.
 */
public class VideoDescriptor <D extends ImageDescriptor, T extends Number>{

    private final Class<T> numericType;
    private ArrayList<D> imageDescriptors;
    private Gson gson;

    public VideoDescriptor(Class<T> numericType) {
        this(numericType, new ArrayList<D>());
    }

    public VideoDescriptor(Class<T> numericType, ArrayList<D> imageDescriptors) {
        this.numericType = numericType;

        this.imageDescriptors = imageDescriptors;
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(VideoDescriptor.class, new VideoDescriptorJSONAdapter());
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
//        gson = new Gson();

    }

    public ArrayList<D> getImageDescriptors() {
        return imageDescriptors;
    }

    public void addDescriptor(D imageDescriptor){
        imageDescriptors.add(imageDescriptor);
    }

    public String toJSON(){
        return gson.toJson(this);
    }

    private class VideoDescriptorJSONAdapter extends TypeAdapter<VideoDescriptor<D,T>> {

        @Override
        public void write(JsonWriter out, VideoDescriptor<D,T> value) throws IOException {
            D firstDescriptor = value.getImageDescriptors().get(0);
            out.beginObject();
            out.name("type").value(firstDescriptor.getType());
            out.name("options").value(firstDescriptor.getSerializedOptions());
            out.name("length").value(value.getImageDescriptors().size());
            GsonBuilder gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
            if (numericType.equals(Integer.class)){
                gsonBuilder.registerTypeAdapter(Double.class, new DoubleToIntSerializer());
            }
            else{
                gsonBuilder.registerTypeAdapter(Double.class, new DoubleToFloatSerializer());
            }
            Gson imageDescriptorSerializer = gsonBuilder.create();
            out.name("descriptors").value(imageDescriptorSerializer.toJson(value.getImageDescriptors()));
            out.endObject();
        }

        @Override
        public VideoDescriptor read(JsonReader in) throws IOException {
            return null;
        }
    }

    private class DoubleToIntSerializer implements JsonSerializer<Double> {
        @Override
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.intValue());
        }
    }
    private class DoubleToFloatSerializer implements JsonSerializer<Double> {
        @Override
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.floatValue());
        }
    }


}
