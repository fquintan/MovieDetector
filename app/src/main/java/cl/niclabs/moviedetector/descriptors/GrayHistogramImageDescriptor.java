package cl.niclabs.moviedetector.descriptors;

/**
 * Created by felipe on 23-09-15.
 */
public class GrayHistogramImageDescriptor extends ImageDescriptor{

    private int[] histogram;

    public GrayHistogramImageDescriptor(long timestamp, int[] histogram){
        super(timestamp);
        this.histogram = histogram;
    }

    @Override
    protected int getSize() {
        return 0;
    }

    @Override
    byte[] getBytes() {
        return new byte[0];
    }
}
