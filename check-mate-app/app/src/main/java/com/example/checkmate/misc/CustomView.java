package com.example.checkmate.misc;

import android.widget.ImageView;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Canvas;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.DashPathEffect;

public class CustomView extends ImageView {

public CustomView(Context context) {
    super(context);
}

public CustomView(Context context, AttributeSet attrst) {
    super(context, attrst);
}

public CustomView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
}

BitmapFactory bitMapFac = null;
public void setBitmapFactory(BitmapFactory bitMapFac)
{
    this.bitMapFac = bitMapFac;
}

@Override
public void onDraw(Canvas canvas) {

    canvas.drawColor(Color.TRANSPARENT);

    /*instantiate a bitmap and draw stuff here, it could well be another
    class which you systematically update via a different thread so that you can get a fresh updated
    bitmap from, that you desire to be updated onto the custom ImageView.
    That will happen everytime onDraw has received a call i.e. something like:*/

    Bitmap bmp = ((BitmapDrawable) this.getDrawable()).getBitmap(); //bitMapFac.update(); //where update returns the most up to date Bitmap

    //here you set the rectangles in which you want to draw the bitmap and pass the bitmap
    canvas.drawBitmap(
        bmp,
        new Rect(0,0,bmp.getWidth(),bmp.getHeight()),
        new Rect(0,0,getWidth(),getHeight()),
        null);
    super.onDraw(canvas);

    Paint paint = new Paint();
    paint.setStrokeWidth(10);
    paint.setStyle(Paint.Style.STROKE);

    paint.setColor(Color.BLUE);
    canvas.drawRect(150, 825, 1000, 925, paint);

    paint.setColor(Color.RED);
    canvas.drawRect(150, 965, 1000, 1065, paint);

    paint.setColor(Color.GREEN);
    canvas.drawRect(150, 1105, 1000, 1205, paint);

    paint.setPathEffect(new DashPathEffect(new float[] { 10f, 10f }, 0));
    paint.setColor(Color.BLACK);
    canvas.drawRect(150, 1245, 1000, 1345, paint);
    canvas.drawRect(150, 1385, 1000, 1485, paint);

    //you need to call postInvalidate so that the system knows that it should redraw your custom ImageView
    this.postInvalidate();
}
}