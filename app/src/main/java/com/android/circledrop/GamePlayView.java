package com.android.circledrop;

import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import java.util.ArrayList;

public class GamePlayView extends View {
    private final static String LOG_TAG = "GamePlayView";
    private final static int WHITE_CIRCLE_LIMIT = 5;
    private final static int DEFAULT_RADIUS = 30;
    private final static double SPEED_MULTIPLIER = 1.25;
    private final static float MAX_OBSTACLE_SPEED = 50;
    private final static int POINTS_INCREASE = 1;

    private Paint mPaint;
    private Circle mPlayerCircle;

    private TimeAnimator mTimeAnimator;
    private long mCurrentPlayTime;

    private int mRadius;
    private int mScreenWidth;
    private int mScreenHeight;
    private boolean mCollision;
    private boolean mClearGame;
    private int mScore;
    private int mLives;

    ArrayList<Circle> mCircleArrayList = new ArrayList<Circle>();

    public GamePlayView(Context context, int width, int height, int score, int lives) {
        super(context);
        mScreenWidth = width;
        mScreenHeight = height;
        mScore = score;
        mLives = lives;
        init();
        Log.d(LOG_TAG, "GamePlayview constructor1 called");
    }

    public GamePlayView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mRadius = DEFAULT_RADIUS;
        mClearGame = false;
        createPlayer();
    }

    private void createPlayer() {
        Log.d(LOG_TAG, "createPlayer() called");
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLACK);
        Log.d(LOG_TAG, "width= " +mScreenWidth+ ", height= " +mScreenHeight);
        int startX = (mScreenWidth/2);
        int startY = (mScreenHeight)/20 * 15;
        mPlayerCircle = new Circle(startX, startY, mRadius, mPaint);
        Log.d(LOG_TAG, mPlayerCircle.toString());
    }

    //needs to be able to dynamically get the current X/Y and add to it to change the position of
    // the circle
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Circle each : mCircleArrayList) {
            Log.d(LOG_TAG,
                    "x= " +each.getX()+ ", y= "+each.getY()+", centerX= "+each.getCenterX()+
                            "," +
                            " " +
                            "centerY= "+each.getCenterY()+ ", radius= " +each.getRadius()+ ", " +
                            "points= " +each.getPoints());
            each.drawOn(canvas);
        }
        canvas.drawCircle(mPlayerCircle.getX(), mPlayerCircle.getY(), mPlayerCircle.getRadius(),
                mPlayerCircle.getPaint());
//        if (mClearGame) {
//            canvas.drawColor(Color.WHITE, PorterDuff.Mode.MULTIPLY);
//            mClearGame = false;
//        }
    }


    // gets called when finger is down
    public Circle createObstacleCircle(float eventX, float eventY) {
        if (mCircleArrayList.size() < WHITE_CIRCLE_LIMIT) {
            Log.d(LOG_TAG, "createCircle() called");
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            int color = ContextCompat.getColor(getContext(), R.color.colorEgg);
            paint.setColor(color);
            Circle mObstacleCircle = new Circle(eventX, eventY, mRadius, paint);
            mCircleArrayList.add(mObstacleCircle);
            return mObstacleCircle;
        }
        return null;
    }

    //takes a circle object and increase its radius
    public void increaseRadius(Circle circle) {
        Log.d(LOG_TAG, "increaseRadius() called");
        for (Circle each : mCircleArrayList) {
            if ((each.getX() == circle.getX()) &&
                    (each.getY() == circle.getY()) &&
                    (circle.getWidth() < mScreenWidth/2)) {
                circle.setRadius(circle.getRadius()+10);
            }
        }
        invalidate();
    }


    // called by main method during startgame state
    public void movePlayerCircle(float eventX) {
        if (eventX < (mScreenWidth / 2)) {
            Log.d(LOG_TAG, "Start State, Action Down on left side");
            if (mPlayerCircle.getX() >= mPlayerCircle.getWidth()) {
                mPlayerCircle.setX(mPlayerCircle.getX() - 10);

            }
        } else if (eventX >= mScreenWidth / 2) {
            Log.d(LOG_TAG, "Start State, Action Down on right side");
            if (mPlayerCircle.getX() + mPlayerCircle.getWidth() <= mScreenWidth) {
                mPlayerCircle.setX(mPlayerCircle.getX() + 10);
            }
        }
        invalidate();
    }

    public void startAnimation() {
        mTimeAnimator = new TimeAnimator();
        mTimeAnimator.setTimeListener(new TimeAnimator.TimeListener() {
            @Override
            public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
                updateObstacleState(deltaTime);
                invalidate();
            }
        });
        mTimeAnimator.start();
    }

    private void updateObstacleState(float deltaMs) {
        // Converting to seconds since PX/S constants are easier to understand
        final float deltaSeconds = deltaMs / 1000f;

        for (final Circle circle : mCircleArrayList) {
            // Move the circle based on the elapsed time and it's speed
            float change = (float) circle.getSpeed() * deltaSeconds;
            if (change < MAX_OBSTACLE_SPEED) {
                Log.d(LOG_TAG, "change= " + change);
                circle.setY(circle.getY() + change);
            }
            else {
                circle.setY(circle.getY() + MAX_OBSTACLE_SPEED);
            }


            // If the obstacle is completely outside of the view bounds after
            // updating it's position, recycle it.
            if (circle.getY() > mScreenHeight) {
                // if circle detects collision, subtract a live
                Log.d(LOG_TAG, "Circle traveled outside of screen");
                if (checkCollision(circle)) {
                    //somehow send message back to main class
                    //possibly call a thread to send message back to Main class
                    circle.setCollision(true);
                }
                // otherwise, add points
                else {
                    //somehow send message back to main class
                    //possibly call a thread to send message back to Main class
                    mCollision = false;
                }
                initializeCircle(circle);
            }
        }
    }

    private void initializeCircle(Circle circle) {
        double speed = circle.getSpeed() * SPEED_MULTIPLIER;
        circle.setPoints(circle.getPoints()+POINTS_INCREASE);
        circle.setY(getTop()-circle.getWidth());
        circle.setCollision(false);
        circle.setSpeed(speed);
    }

    public void pauseAnimation() {
        if (mTimeAnimator != null && mTimeAnimator.isRunning()) {
            // Store the current play time for later.
            mCurrentPlayTime = mTimeAnimator.getCurrentPlayTime();
            mTimeAnimator.pause();
        }
    }

    // this method is called in main activity to check if there was collision
    public boolean checkCollision(Circle circle) {
        if (circle.getX() == mPlayerCircle.getX() && circle.getY() == mPlayerCircle.getY()) {
            return true;
        }
        // if the obstacle circle passes the player circle at one point in its travel
        // decrease live
        return false;
    }

    public boolean getCollision() {
        return mCollision;
    }

//    public void resume() {
//        if (mTimeAnimator != null && mTimeAnimator.isPaused()) {
//            mTimeAnimator.start();
//            mTimeAnimator.setCurrentPlayTime(mCurrentPlayTime);
//        }
//    }

//    public void clearGamePlay() {
//        mClearGame = true;
//    }

    public int getScore() {
        return mScore;
    }

    public int getLives() {
        return mLives;
    }

}
