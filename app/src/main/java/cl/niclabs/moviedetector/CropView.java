package cl.niclabs.moviedetector;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CropView extends View {

    static final String TAG = CropView.class.getSimpleName();

    Point point1, point3;
    Point point2, point4;

    private final int defaultWidth = 600;
    private final int defaultHeight = 400;
    private final int defaultCenterX = defaultWidth/2;
    private final int defaultCenterY = defaultHeight/2;
    /**
     * point1 and point 3 are of same group and same as point 2 and point4
     */
    int groupId = -1;
    private ArrayList<ColorBall> colorballs = new ArrayList<ColorBall>();
    // array that holds the balls
    private int balID = 0;
    // variable to know what ball is being dragged
    Paint paint;
    Canvas canvas;
    private Bitmap bitmap;

    public void setupPoints(int centerX, int centerY, int width, int height){
        paint = new Paint();
        setFocusable(true); // necessary for getting the touch events
        canvas = new Canvas();

//        // setting the start point for the balls
        point1 = new Point(centerX - (width/2), centerY - (height/2));
        point2 = new Point(centerX + (width/2), centerY - (height/2));
        point3 = new Point(centerX + (width/2), centerY + (height/2));
        point4 = new Point(centerX - (width/2), centerY + (height/2));
//        point1 = new Point(20, 20);
//        point2 = new Point(1059, 20);
//        point3 = new Point(1050, 500);
//        point4 = new Point(20, 500);

        // declare each ball with the ColorBall class
        colorballs.clear();
        colorballs.add(new ColorBall(bitmap, point1, 0));
        colorballs.add(new ColorBall(bitmap, point2, 1));
        colorballs.add(new ColorBall(bitmap, point3, 2));
        colorballs.add(new ColorBall(bitmap, point4, 3));
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.setupPoints(w/2, h/2, defaultWidth, defaultHeight);
    }

    public CropView(Context context) {
        super(context);
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.red_dot);
        setupPoints(defaultCenterX, defaultCenterY, defaultWidth, defaultHeight);
    }

    public CropView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.red_dot);
        setupPoints(defaultCenterX, defaultCenterY, defaultWidth, defaultHeight);
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.red_dot);
        setupPoints(defaultCenterX, defaultCenterY, defaultWidth, defaultHeight);
    }

    // the method that draws the balls
    @Override
    protected void onDraw(Canvas canvas) {
        // canvas.drawColor(0xFFCCCCCC); //if you want another background color

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.parseColor("#55000000"));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeJoin(Paint.Join.ROUND);
        // mPaint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);

        canvas.drawPaint(paint);
        paint.setColor(Color.parseColor("#55FFFFFF"));

        if (groupId == 1) {
            canvas.drawRect(point1.x + colorballs.get(0).getWidthOfBall() / 2,
                    point3.y + colorballs.get(2).getWidthOfBall() / 2, point3.x
                            + colorballs.get(2).getWidthOfBall() / 2, point1.y
                            + colorballs.get(0).getWidthOfBall() / 2, paint);
        } else {
            canvas.drawRect(point2.x + colorballs.get(1).getWidthOfBall() / 2,
                    point4.y + colorballs.get(3).getWidthOfBall() / 2, point4.x
                            + colorballs.get(3).getWidthOfBall() / 2, point2.y
                            + colorballs.get(1).getWidthOfBall() / 2, paint);
        }

        // draw the balls on the canvas
        for (ColorBall ball : colorballs) {
            canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(),
                    new Paint());
        }
        shade_region_between_points();

    }


    // events when touching the screen
    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();

        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch (eventAction) {

            case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on
                // a ball
                balID = -1;
                groupId = -1;
                for (ColorBall ball : colorballs) {
                    // check if inside the bounds of the ball (circle)
                    // get the center for the ball
                    int centerX = ball.getX() + ball.getWidthOfBall();
                    int centerY = ball.getY() + ball.getHeightOfBall();
//                    paint.setColor(Color.CYAN);
                    // calculate the radius from the touch to the center of the ball
                    double radCircle = Math
                            .sqrt((double) (((centerX - X) * (centerX - X)) + (centerY - Y)
                                    * (centerY - Y)));

                    if (radCircle < ball.getWidthOfBall()) {

                        balID = ball.getID();
                        if (balID == 1 || balID == 3) {
                            groupId = 2;
                            canvas.drawRect(point1.x, point3.y, point3.x, point1.y,
                                    paint);
                        } else {
                            groupId = 1;
                            canvas.drawRect(point2.x, point4.y, point4.x, point2.y,
                                    paint);
                        }
                        invalidate();
                        break;
                    }
                    invalidate();
                }

                break;

            case MotionEvent.ACTION_MOVE: // touch drag with the ball
                // move the balls the same as the finger
                if (balID > -1) {
//                    Utils.logd("Moving Ball : " + balID);

                    colorballs.get(balID).setX(X);
                    colorballs.get(balID).setY(Y);

//                    paint.setColor(Color.CYAN);

                    if (groupId == 1) {
                        colorballs.get(1).setX(colorballs.get(0).getX());
                        colorballs.get(1).setY(colorballs.get(2).getY());
                        colorballs.get(3).setX(colorballs.get(2).getX());
                        colorballs.get(3).setY(colorballs.get(0).getY());
                        canvas.drawRect(point1.x, point3.y, point3.x, point1.y,
                                paint);
//                        shade_region_between_points();
                    } else {
                        colorballs.get(0).setX(colorballs.get(1).getX());
                        colorballs.get(0).setY(colorballs.get(3).getY());
                        colorballs.get(2).setX(colorballs.get(3).getX());
                        colorballs.get(2).setY(colorballs.get(1).getY());
                        canvas.drawRect(point2.x, point4.y, point4.x, point2.y,
                                paint);
                    }

                    invalidate();
                }

                break;

            case MotionEvent.ACTION_UP:
                // touch drop - just do things here after dropping

                break;
        }
        // redraw the canvas
        invalidate();
        return true;

    }

    public void shade_region_between_points() {
//        canvas.drawRect(point1.x, point3.y, point3.x, point1.y, paint);
        canvas.drawRect(getLeftLimit(), getTopLimit(), getRightLimit(), getBottomLimit(), paint);
    }

    public int getTopLimit(){
        return Math.min(point1.y, point3.y);
    }
    public int getBottomLimit(){
        return Math.max(point1.y, point3.y);
    }
    public int getRightLimit(){
        return Math.max(point1.x, point3.x);
    }
    public int getLeftLimit(){
        return Math.min(point1.x, point3.x);
    }

}