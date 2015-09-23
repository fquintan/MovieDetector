package cl.niclabs.moviedetector.descriptors;

import java.util.ArrayList;

/**
 * Created by felipe on 23-09-15.
 */
public class VideoDescriptor <T extends ImageDescriptor>{

    private ArrayList<T> imageDescriptors;

    public VideoDescriptor() {
        imageDescriptors = new ArrayList<T>();
    }

    public VideoDescriptor(ArrayList<T> imageDescriptors) {
        this.imageDescriptors = imageDescriptors;
    }

    public ArrayList<T> getImageDescriptors() {
        return imageDescriptors;
    }

    public void addDescriptor(T imageDescriptor){
        imageDescriptors.add(imageDescriptor);
    }
}
