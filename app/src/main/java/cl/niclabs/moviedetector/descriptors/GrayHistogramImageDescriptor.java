package cl.niclabs.moviedetector.descriptors;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

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
