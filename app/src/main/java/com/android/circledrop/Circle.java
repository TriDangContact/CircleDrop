package com.android.circledrop;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Circle {
    private float mCenterX;
    private float mCenterY;
    private int mRadius;
    private int mPoints;
    private Paint mPaint;
    private double mSpeed;
    private boolean mCollision;
    private boolean mLiveScoreUpdated;

    public Circle(float centerX, float centerY, int radius, Paint paint) {
        mCenterX = centerX;
        mCenterY = centerY;
        mRadius = radius;
        mPoints = 1;
        mPaint = paint;
        mSpeed = 200;           //anything above 150 is good starting speed
        mCollision = false;
        mLiveScoreUpdated = false;
    }

    public void drawOn(Canvas canvas) {
        canvas.drawCircle(this.getCenterX(), this.getCenterY(), mRadius, mPaint);
    }

    public float getX() {
        return mCenterX - mRadius;
    }

    public float getY() {
        return mCenterY - mRadius;
    }

    public float getCenterX() { return mCenterX; }

    public void setCenterX(float x) { mCenterX = x; }

    public float getCenterY() {
        return mCenterY;
    }

    public void setCenterY(float y) { mCenterY = y; }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int radius) {
        mRadius = radius;
    }

    public int getPoints() {
        return mPoints;
    }

    public void setPoints(int points) {
        mPoints = points;
    }

    public Paint getPaint() { return mPaint; }

    public void setPaint(Paint paint) { mPaint = paint; }

    public int getWidth() { return mRadius*2; }

    public int getHeight() { return mRadius*2; }

    public double getSpeed() { return mSpeed; }

    public void setSpeed(double speed) { mSpeed = speed; }

    public void setCollision(boolean collision) { mCollision = collision; }

    public boolean getCollision() { return mCollision; }

    public boolean isLiveScoreUpdated() { return mLiveScoreUpdated; }

    public void setLiveScoreUpdated(boolean liveScoreUpdated) { mLiveScoreUpdated = liveScoreUpdated; }

    public String toString() {
        return "centerX= " +
                "=" +mCenterX+ ", centerY= " +mCenterY+", X= " +this.getX()+ ", Y= " +this.getY()+
                ", " +
                "radius= " +mRadius+ ", height= " +this.getHeight();
    }

}
