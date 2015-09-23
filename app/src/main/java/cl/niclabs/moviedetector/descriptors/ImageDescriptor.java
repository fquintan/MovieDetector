package cl.niclabs.moviedetector.descriptors;

/**
 * Created by felipe on 23-09-15.
 */
public abstract class ImageDescriptor {

    public ImageDescriptor(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private long timestamp;

    public abstract int getSize();
    public abstract byte[] getBytes();

}
