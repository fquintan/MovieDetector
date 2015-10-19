package cl.niclabs.moviedetector.descriptors;

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
        return "options";
    }
}
