package com.android.circledrop;

import android.graphics.PointF;

public class Circle {
    private float mX;
    private float mY;
    private float mCenterX;
    private float mCenterY;
    private int mRadius;
    private int mPoints;

    public Circle(float x, float y, int radius, float centerX, float centerY) {
        mX = x;
        mY = y;
        mRadius = radius;
        mCenterX = centerX;
        mCenterY = centerY;
    }

    public Circle(float x, float y, int radius, float centerX, float centerY, boolean points) {
        mX = x;
        mY = y;
        mRadius = radius;
        mCenterX = centerX;
        mCenterY = centerY;
        if (points) {
            mPoints = 1;
        }
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

    public void setY(float y) {
        mY = y;
    }

    public float getCenterX() {
        return mCenterX;
    }

    public void setCenterX(float centerX) {
        mCenterX = centerX;
    }

    public float getCenterY() {
        return mCenterY;
    }

    public void setCenterY(float centerY) {
        mCenterY = centerY;
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
}
