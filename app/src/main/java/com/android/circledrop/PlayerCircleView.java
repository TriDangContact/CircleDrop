package com.android.circledrop;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.w3c.dom.Attr;

public class PlayerCircleView extends View {
    private final static String LOG_TAG = "PlayerCircleView";
    private final static int RADIUS = 50;
    private Paint paint;
    private Circle circle;
    private float x;
    private float y;
    private float centerX;
    private float centerY;
    private float marginX;
    private float marginY;


    public PlayerCircleView(Context context) {
        super(context);
        init();
    }

    public PlayerCircleView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        //hardcoded for now
        marginY = 30;
        x = 540;
        y = 1418 - RADIUS - marginY;
        centerX = x + RADIUS;
        centerY = y + RADIUS;
        circle = new Circle(x, y, RADIUS, centerX, centerY);
    }
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(LOG_TAG,
                "x= " +circle.getX()+ ", y= "+circle.getY()+", centerX= "+circle.getCenterX()+", " +
                        "centerY= "+circle.getCenterY());
        canvas.drawCircle(circle.getX(), circle.getY(), circle.getRadius(), paint);
    }

}
