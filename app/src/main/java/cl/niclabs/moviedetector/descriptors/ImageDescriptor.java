package cl.niclabs.moviedetector.descriptors;

import com.google.gson.annotations.Expose;

/**
 * Created by felipe on 23-09-15.
 */
public abstract class ImageDescriptor {

    public long getTimestamp() {
        return timestamp;
    }

    @Expose
    protected double[] descriptor;
    @Expose
    protected long timestamp;
    @Expose
    protected int frameNumber;

    public abstract String getType();
    public abstract String getSerializedOptions();

    public ImageDescriptor(double[] descriptor, long timestamp, int frameNumber) {
        this.descriptor = descriptor;
        this.timestamp = timestamp;
        this.frameNumber = frameNumber;
    }
}
