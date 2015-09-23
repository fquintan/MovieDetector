package cl.niclabs.moviedetector.descriptors;

/**
 * Created by felipe on 23-09-15.
 */
public class GrayHistogramExtractor implements ImageDescriptorExtractor{



    @Override
    public ImageDescriptor extract(byte[] frame, long timestamp) {
        int[] histogram = {1,2,3,4,5,6,7,8};
        return new GrayHistogramImageDescriptor(timestamp, histogram);
    }
}
