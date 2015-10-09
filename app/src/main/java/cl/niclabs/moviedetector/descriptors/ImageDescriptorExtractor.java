package cl.niclabs.moviedetector.descriptors;

/**
 * Created by felipe on 23-09-15.
 */
public interface ImageDescriptorExtractor {

    public ImageDescriptor extract(byte[] frame, long timestamp, int frameNumber);

}
