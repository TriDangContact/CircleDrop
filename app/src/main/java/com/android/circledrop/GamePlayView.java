package com.android.circledrop;

import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class GamePlayView extends View {
    private final static String LOG_TAG = "GamePlayView";
    private final static int WHITE_CIRCLE_LIMIT = 10;    //better to have lower number
    private final static int DEFAULT_RADIUS = 40;       //40 is good
    private final static double SPEED_MULTIPLIER = 1.25;    //25% is good
    private final static float MAX_OBSTACLE_SPEED = 50; //50 is good
    private final static int MAX_OBSTACLE_SIZE = 2;     //screenwidth divided by this number
    private final static int INCREASE_RADIUS_SIZE = 1; //should it keep between 1-10
    private final static int POINTS_INCREASE = 1;
    private final static int PLAYER_MOVEMENT_SPEED = 10;    //10 is good

    private Paint mPaint;
    private Circle mPlayerCircle;
    private TimeAnimator mTimeAnimator;

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
        init();
    }

    public GamePlayView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mRadius = DEFAULT_RADIUS;
        createPlayer();
    }

    private void createPlayer() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLACK);
        int startX = (mScreenWidth/2);
        int startY = (mScreenHeight)/20 * 15;
        mPlayerCircle = new Circle(startX, startY, mRadius, mPaint);
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
                            "MESSAGE SENT= " +each.isLiveScoreUpdated());
            each.drawOn(canvas);
        }
        canvas.drawCircle(mPlayerCircle.getCenterX(), mPlayerCircle.getCenterY(),
                mPlayerCircle.getRadius(), mPlayerCircle.getPaint());
    }

    // gets called when finger is down in newgame state
    public Circle createObstacleCircle(float eventX, float eventY) { ;
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

    // called by the main activity during newgame state
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

    // called by main activity during startgame state
    public void movePlayerCircle(float eventX) {
        if (eventX < (mScreenWidth / 2)) {
            Log.d(LOG_TAG, "Start State, Action Down on left side");
            if (mPlayerCircle.getX() >= 10) {
                mPlayerCircle.setCenterX(mPlayerCircle.getCenterX() - PLAYER_MOVEMENT_SPEED);

            }
        } else if (eventX >= mScreenWidth / 2) {
            Log.d(LOG_TAG, "Start State, Action Down on right side");
            if (mPlayerCircle.getX() + mPlayerCircle.getWidth() <= mScreenWidth - 10) {
                mPlayerCircle.setCenterX(mPlayerCircle.getCenterX() + PLAYER_MOVEMENT_SPEED);
            }
        }
        invalidate();
    }

    // called by main activity during startgame state
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
            // limit the circle velocity to make game more playable
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

            // we need to make sure that live and score is only updated once per traversal
            if (circle.getCollision() == true) {
                if (circle.isLiveScoreUpdated() == false) {
                    Log.d(LOG_TAG, "/////SENDING MESSAGE");
                    mLives--;
                    circle.setLiveScoreUpdated(true);
                }
                else {
                    Log.d(LOG_TAG, "/////MESSAGE SENT IS TRUE");
                }
            }

            // we define the threshold as the point immediately after the player, and the point
            // immediately before the next position the obstacle will move to, therefore, only
            // allowing the obstacle a single chance to update the score.
            float currentY = circle.getY();
            float currentThreshold = mPlayerCircle.getY() + (mPlayerCircle.getRadius()*2);
            float nextThreshold = currentThreshold + change;
            if (currentY > currentThreshold && currentY < nextThreshold) {
                if (circle.getCollision() == false) {
                    mScore += circle.getPoints();
                    circle.setLiveScoreUpdated(true);
                }
            }
            // If the obstacle is completely outside of the view bounds (completed a
            // traversal) after updating it's position, recycle it.
            if (circle.getY() > mScreenHeight) {
                initializeCircle(circle);
            }
        }
    }

    private void initializeCircle(Circle circle) {
        double speed = circle.getSpeed() * SPEED_MULTIPLIER;
        circle.setPoints(circle.getPoints()+POINTS_INCREASE);
        circle.setCenterY(getTop()-(circle.getWidth()/2));
        circle.setCollision(false);
        circle.setLiveScoreUpdated(false);
        circle.setSpeed(speed);
    }

    public void pauseAnimation() {
        if (mTimeAnimator != null && mTimeAnimator.isRunning()) {
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

}
