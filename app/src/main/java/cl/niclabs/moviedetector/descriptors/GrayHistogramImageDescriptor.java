package cl.niclabs.moviedetector.descriptors;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Created by felipe on 23-09-15.
 */
public class GrayHistogramImageDescriptor extends ImageDescriptor{

    private int[] histogram;
    private float[] descriptor ;
    private int frame;

    public GrayHistogramImageDescriptor(long timestamp, int[] histogram, float[] descriptor, int frame){
        super(timestamp);
        this.histogram = histogram;
        this.descriptor = descriptor;
        this.frame = frame;
    }

    @Override
    public int getSize() {
        return histogram.length*4;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(histogram.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(histogram);
        return byteBuffer.array();
    }
}
