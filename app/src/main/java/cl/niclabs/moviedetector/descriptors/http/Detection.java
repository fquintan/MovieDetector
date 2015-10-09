package cl.niclabs.moviedetector.descriptors.http;

/**
 * Created by felipe on 09-10-15.
 */
public class Detection {

    private float score;
    private String reference;

    public Detection(float score, String reference) {
        this.score = score;
        this.reference = reference;
    }

    public Detection() {
    }

    public float getScore() {

        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
