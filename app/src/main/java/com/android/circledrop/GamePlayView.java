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
    private final static int WHITE_CIRCLE_LIMIT = 5;    //better to have lower number
    private final static int DEFAULT_RADIUS = 40;       //40 is good
    private final static double SPEED_MULTIPLIER = 1.25;    //25% is good
    private final static float MAX_OBSTACLE_SPEED = 50; //50 is good
    private final static int MAX_OBSTACLE_SIZE = 2;     //screenwidth divided by this number
    private final static int INCREASE_RADIUS_SIZE = 10; //10 is good
    private final static int POINTS_INCREASE = 1;
    private final static int PLAYER_MOVEMENT_SPEED = 10;    //10 is good

    private Paint mPaint;
    private Circle mPlayerCircle;
    private MessageSentListener mMessageSentListener;
    private TimeAnimator mTimeAnimator;
    private long mCurrentPlayTime;

    private int mRadius;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScore;
    private int mLives;

    ArrayList<Circle> mCircleArrayList = new ArrayList<Circle>();

    public GamePlayView(Context context, int width, int height, int score, int lives) {
        super(context);
        mScreenWidth = width;
        mScreenHeight = height;
        mScore = score;
        mLives = lives;
        this.mMessageSentListener = null;
        init();
        Log.d(LOG_TAG, "GamePlayview constructor1 called");
    }

    public GamePlayView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public interface MessageSentListener {
        void onMessageSent();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mRadius = DEFAULT_RADIUS;
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
                            "points= " +each.getPoints()+ " COLLISION= " +each.getCollision()+ " " +
                            "MESSAGE SENT= " +each.isMessageSent());
            each.drawOn(canvas);
        }
        canvas.drawCircle(mPlayerCircle.getCenterX(), mPlayerCircle.getCenterY(),
                mPlayerCircle.getRadius(),
                mPlayerCircle.getPaint());
    }


    // gets called when finger is down
    public Circle createObstacleCircle(float eventX, float eventY) {
        if (mMessageSentListener == null) {
            mMessageSentListener = new MessageSentListener() {
                @Override
                public void onMessageSent() {
                    Log.d(LOG_TAG, "/////INSTANTIATED MESSAGE LISTENER");
                }
            };
        }
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
            if ((each.getCenterX() == circle.getCenterX()) &&
                    (each.getCenterY() == circle.getCenterY()) &&
                    (circle.getWidth() < (mScreenWidth/MAX_OBSTACLE_SIZE))) {
                circle.setRadius(circle.getRadius()+INCREASE_RADIUS_SIZE);
            }
        }
        invalidate();
    }

    // called by main method during startgame state
    public void movePlayerCircle(float eventX) {
        if (eventX < (mScreenWidth / 2)) {
            Log.d(LOG_TAG, "Start State, Action Down on left side");
            if (mPlayerCircle.getX() >= mPlayerCircle.getWidth()) {
                mPlayerCircle.setCenterX(mPlayerCircle.getCenterX() - PLAYER_MOVEMENT_SPEED);

            }
        } else if (eventX >= mScreenWidth / 2) {
            Log.d(LOG_TAG, "Start State, Action Down on right side");
            if (mPlayerCircle.getX() + mPlayerCircle.getWidth() <= mScreenWidth) {
                mPlayerCircle.setCenterX(mPlayerCircle.getCenterX() + PLAYER_MOVEMENT_SPEED);
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
                circle.setCenterY(circle.getCenterY() + change);
            }
            else {
                circle.setCenterY(circle.getCenterY() + MAX_OBSTACLE_SPEED);
            }
            // this is where we detect collision, since the object's position have been
            // updated to their current position
            calculateCollision(circle);

            if (circle.getCollision() == true) {
                if (circle.isMessageSent() == false) {
                    Log.d(LOG_TAG, "/////SENDING MESSAGE");
                    mLives--;
                    Log.d(LOG_TAG, "/////LIVES= " +mLives);
                    if (mMessageSentListener != null) {
                        mMessageSentListener.onMessageSent();
                        Log.d(LOG_TAG, "/////MESSAGE SENT");
                        circle.setMessageSent(true);
                    }
                    else {
                        Log.d(LOG_TAG, "/////ERROR: MessageSentListener is NULL");
                    }
                }
                else {
                    Log.d(LOG_TAG, "/////MESSAGE SENT IS TRUE");
                }
            }
            // immediately after the obstacle has entirely passed the player, update the live
            // score and send the message to Activity. Then we immediately close the door to
            // prevent any more message for the current traversal since we only need the score to
            // be sent once per traversal.
            float currentY = circle.getY();
            float currentThreshold = mPlayerCircle.getY() + (mPlayerCircle.getRadius()*2);
            float nextThreshold = currentThreshold + change;
            if (currentY > currentThreshold && currentY < nextThreshold) {
                if (circle.getCollision() == false) {
                    Log.d(LOG_TAG, "/////SENDING MESSAGE AFTER THRESHOLD");
                    Log.d(LOG_TAG,
                            "CurrentY= " +currentY+ " currentThreshold= " +currentThreshold+ " " +
                                    " nextThreshold= " +nextThreshold);
                    Log.d(LOG_TAG, "/////PLAYER CIRCLE "+mPlayerCircle.toString());
                    Log.d(LOG_TAG, "///OBSTACLE CIRCLE " +circle.toString());
                    mScore += circle.getPoints();
                    Log.d(LOG_TAG, "This circle have not collided, SCORE = " +mScore);
                    if (mMessageSentListener != null) {
                        mMessageSentListener.onMessageSent();
                        Log.d(LOG_TAG, "/////MESSAGE SENT AFTER THRESHOLD");
                        circle.setMessageSent(true);
                    }
                    else {
                        Log.d(LOG_TAG, "/////ERROR: MessageSentListener is NULL AFTER THRESHOLD");
                    }
                }
            }

            // If the obstacle is completely outside of the view bounds after
            // updating it's position, recycle it.
            if (circle.getY() > mScreenHeight) {
                // if circle detects collision, send a message to Activity
                Log.d(LOG_TAG, "Circle traveled outside of screen");
//                if (circle.getCollision() == false) {
//                    mScore += circle.getPoints();
//                    Log.d(LOG_TAG, "This circle have not collided, SCORE = " +mScore);
//                }
//                if (mMessageSentListener != null) {
//                    mMessageSentListener.onMessageSent();
//                }
                initializeCircle(circle);
            }
        }
    }

    private void initializeCircle(Circle circle) {
        double speed = circle.getSpeed() * SPEED_MULTIPLIER;
        circle.setPoints(circle.getPoints()+POINTS_INCREASE);
        circle.setCenterY(getTop()-(circle.getWidth()/2));
        circle.setCollision(false);
        circle.setMessageSent(false);
        circle.setSpeed(speed);
    }

    public void pauseAnimation() {
        if (mTimeAnimator != null && mTimeAnimator.isRunning()) {
            // Store the current play time for later.
            mCurrentPlayTime = mTimeAnimator.getCurrentPlayTime();
            mTimeAnimator.pause();
        }
    }

    // uses existing algorithm for circle collision
    public void calculateCollision(Circle circle) {
        Circle firstBall = mPlayerCircle;
        Circle secondBall = circle;
        float distance = (float) Math.sqrt(
            (
                (firstBall.getCenterX() - secondBall.getCenterX()) *
                (firstBall.getCenterX() - secondBall.getCenterX())
            ) +
            (
                (firstBall.getCenterY() - secondBall.getCenterY()) *
                (firstBall.getCenterY() - secondBall.getCenterY())
            )
        );
        if (distance < firstBall.getRadius() + secondBall.getRadius())
        {
            Log.d(LOG_TAG, "////COLLISION OCCURRED/////: " +circle.toString());
            Log.d(LOG_TAG, "////PLAYER CIRCLE/////: " +firstBall.toString());
            circle.setCollision(true);
        }
    }

    public int getScore() {
        return mScore;
    }

    public int getLives() {
        return mLives;
    }

    public void setMessageSentListener(MessageSentListener listener) {
        this.mMessageSentListener = listener;
    }

}
