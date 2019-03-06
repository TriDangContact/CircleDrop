package com.android.circledrop;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class Circle {
    private float mX;
    private float mY;
    private int mRadius;
    private int mPoints;
    private Paint mPaint;
    private double mSpeed;
    private boolean mCollision;

    public Circle(float x, float y, int radius, Paint paint) {
        mX = x;
        mY = y;
        mRadius = radius;
        mPoints = 1;
        mPaint = paint;
        mSpeed = 200;
        mCollision = false;
    }
    public Circle(float x, float y, int radius) {
        mX = x;
        mY = y;
        mRadius = radius;
    }

    public Circle(float x, float y, int radius, boolean points, Paint paint) {
        mX = x;
        mY = y;
        mRadius = radius;
        mPaint = paint;
        if (points) {
            mPoints = 1;
        }
    }

    public void drawOn(Canvas canvas) {
        canvas.drawCircle(mX, mY, mRadius, mPaint);
    }

    public float getX() {
        return mX;
    }

    public void setX(float x) {
        mX = x;
    }

    public float getY() {
        return mY;
    }

    public void setY(float y) { mY = y; }

    public float getCenterX() { return getX()+getRadius(); }

    public float getCenterY() {
        return getY()+getRadius();
    }

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

    public boolean isCollision() { return mCollision; }

    public void setCollision(boolean collision) { mCollision = collision; }

    public String toString() {
        return "X= " +mX+ ", Y= " +mY+ ", radius= " +mRadius;
    }

}
