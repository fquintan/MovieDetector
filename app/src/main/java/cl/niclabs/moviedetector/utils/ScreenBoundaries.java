package cl.niclabs.moviedetector.utils;

/**
 * Created by felipe on 29-10-15.
 */
public class ScreenBoundaries {
    public int left;
    public int right;
    public int top;
    public int bottom;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScreenBoundaries)) return false;

        ScreenBoundaries that = (ScreenBoundaries) o;

        if (left != that.left) return false;
        if (right != that.right) return false;
        if (top != that.top) return false;
        if (bottom != that.bottom) return false;

        return true;
    }

    public ScreenBoundaries(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public int getWidth(){
        return Math.abs(right - left);
    }

    public int getHeight(){
        return Math.abs(bottom - top);
    }
}
