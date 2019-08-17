package com.example.checkmate.misc;

import androidx.lifecycle.Observer;
import java.util.ArrayList;
import com.example.checkmate.itemSelection.ItemSelectionViewModel;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import android.os.AsyncTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.io.DataOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import android.view.MotionEvent;
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
import com.example.checkmate.itemSelection.ItemSelectionActivity;
import com.example.checkmate.data.JoinData;
import com.example.checkmate.data.JoinDataItem;
import com.example.checkmate.data.model.ItemStatus;
import com.example.checkmate.data.model.ItemPaymentDetails;
import com.example.checkmate.data.model.ItemPaymentDetails;
import com.example.checkmate.data.model.BillDetailsResponse;

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
    public void setBitmapFactory(BitmapFactory bitMapFac) {
        this.bitMapFac = bitMapFac;
    }
    
    ItemSelectionActivity lifecycleOwner = null;
    public void setLifecycleOwner(ItemSelectionActivity lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    JoinData joinData;
    public void setJoinData(JoinData joinData) {
        this.joinData = joinData;
    }

    ItemSelectionViewModel viewModel;
    public void setViewModel(ItemSelectionViewModel viewModel) {
        this.viewModel = viewModel;
//        viewModel.getItemDetails().observe(this.lifecycleOwner, new Observer<BillDetailsResponse>() { 
//            @Override
//            public void onChanged(BillDetailsResponse bdr) {
//                if (CustomView.this.joinData.getItems()) == null) {
//                    return;
//                }
//                System.out.println(">>> Got one!");
//                for (ItemStatus item : bdr.getItems()) {
//                    for (JoinDataItem joinItem : CustomView.this.joinData.getItems()) {
//                        if (item.getName() == joinItem.getName()) {
//                            for (ItemPaymentDetails detail : item.getDetails()) {
//                                CustomView.this.addColor(joinItem, detail.getColor());
//                            }
//                        }
//                    }
//                }
//            }
//        });
    }

    public void onChanged(BillDetailsResponse bdr) {
        if (this.joinData.getItems() == null) {
            return;
        }
        if (bdr == null || bdr.getItems() == null) {
            return;
        }
        for (ItemStatus item : bdr.getItems()) {
            for (JoinDataItem joinItem : this.joinData.getItems()) {
                if (item.getName().toUpperCase().equals(joinItem.getName().toUpperCase())) {
                    // System.out.println(">>> Got one! " + item.getName());
                    for (ItemPaymentDetails detail : item.getDetails()) {
                        if (detail.getColor() == null ||
                            detail.getColor().equals(this.joinData.getColor())) {
                            continue;
                        }
                        this.addColor(joinItem, detail.getColor());
                    }
                }
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println(">>> action " + event.getAction());
        if (event.getAction() == MotionEvent.ACTION_UP) {
            try {
                Bitmap bmp = ((BitmapDrawable) this.getDrawable()).getBitmap();
                float hr = /* 4 * */ getWidth() / (float)bmp.getWidth();
                float vr = /* 4 * */getHeight() / (float)bmp.getHeight();
                System.out.println(">>> touch " + event.getX() + " " + event.getY());
                System.out.println(">>> "
                + (joinData.getItems()[0].getPosition().getX() * hr) + " " +
                + (joinData.getItems()[0].getPosition().getY() * vr) + " " +
                + ((joinData.getItems()[0].getPosition().getX() * hr)  + joinData.getItems()[0].getPosition().getW()* hr) + " " +
                + ((joinData.getItems()[0].getPosition().getY() * vr)  + joinData.getItems()[0].getPosition().getH() * vr));

                for (JoinDataItem joinItem : joinData.getItems()) {
                    if (joinItem.getPosition().getX() * hr <= event.getX() &&
                        joinItem.getPosition().getY() * vr <= event.getY() &&
                        (joinItem.getPosition().getX() + joinItem.getPosition().getW())*hr >= event.getX() &&
                        (joinItem.getPosition().getY() + joinItem.getPosition().getH())*vr >= event.getY())
                    {
                        this.addColor(joinItem, joinData.getColor());
                        lifecycleOwner.addTotal(joinItem.getUnitPrice());
                        viewModel.selectItem(joinData.getId(), joinItem.getName(), joinData.getColor());

                    }
                }

            }
            catch (Exception ex) {
                System.out.println(">>> touch " + ex);
                ex.printStackTrace();
            }
        }
        return true;
    }
    
    private void addColor(JoinDataItem joinItem, String color) {
        if (joinItem.getClicked() == null) {
            joinItem.setClicked(new ArrayList<String>());
        }

        Boolean wasntClicked = joinItem.getClicked().size() < joinItem.getQuantity();
        if (wasntClicked) {

            joinItem.getClicked().add(color);

            // System.out.println(">>> " + joinData.getId() + " " + joinItem.getName() + " " + joinData.getColor());
            // new OnClickPostAsyncTask().execute(joinItem);
        }
    }

    private class OnClickPostAsyncTask extends AsyncTask<JoinDataItem, Void, String> {
        @Override
        protected String doInBackground(JoinDataItem... params) {
            try {
                System.out.println(">>> CustomView post");
                //URL url = new URL("http://give-a-shit-check-mate.herokuapp.com/onClick");
                URL url = new URL("http://192.168.0.100:17723/onClick");

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                connection.setRequestMethod("POST");

                ObjectMapper objMapper = new ObjectMapper();
                byte[] data = objMapper
                    .writeValueAsString(params[0])
                    .getBytes(StandardCharsets.UTF_8);

                try(DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                    wr.write(data);
                }

                connection.connect();
                System.out.println(">>> " + new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine());

            }
            catch (Exception ex) {
                System.out.println(">>> touch " + ex);
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
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
            new Rect(0, 0, bmp.getWidth(), bmp.getHeight()),
            new Rect(0, 0, getWidth(), getHeight()),
            null);

        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(60);
        float hr = /*4 * */getWidth() / (float)bmp.getWidth();
        float vr = /*4 * */getHeight() / (float)bmp.getHeight();
        //System.out.println(">>> " + vr + " " + getHeight() + " " + bmp.getHeight());

        paint.setAlpha(60);

        //canvas.drawRect(1, 1, getWidth() - 1, getHeight() - 1, paint);

        for (JoinDataItem joinItem : joinData.getItems()) {
            ArrayList<String> colors = joinItem.getClicked();
            if (colors == null) {
                colors = new ArrayList<String>();
            }
            colors = (ArrayList<String>)colors.clone();
            while (colors.size() < joinItem.getQuantity()) {
                colors.add("#000000");
            }
            
            //System.out.println(">>> joinItem "  + joinItem.getPosition().getX());
            Float ix = -1.0f;
            for (String color : colors) {
                ix += 1;
                int r = Integer.valueOf(color.substring(1, 3), 16);
                int g = Integer.valueOf(color.substring(3, 5), 16);
                int b = Integer.valueOf(color.substring(5, 7), 16);
                paint.setColor(Color.argb(60, r, g, b));
                float leftX = joinItem.getPosition().getX() * hr +
                        (ix / colors.size()) * joinItem.getPosition().getW() * hr;
                // System.out.println(">>> foo " + leftX);
                canvas.drawRect(
                    leftX,
                    joinItem.getPosition().getY() * vr,
                    leftX + (joinItem.getPosition().getW() / (colors.size())) * hr,
                    (joinItem.getPosition().getH() + joinItem.getPosition().getY()) * vr,
                    paint);

            }

    //        System.out.println(">>> painting " +             joinItem.getPosition().getX() * hr + " " +
    //            joinItem.getPosition().getY() * vr + " " +
    //            (joinItem.getPosition().getW() + joinItem.getPosition().getX()) * hr + " " +
    //            (joinItem.getPosition().getH() + joinItem.getPosition().getY()) * vr);


        }
    //    canvas.drawRect(150, 825, 1000, 925, paint);
    //
    //    paint.setColor(Color.RED);
    //    canvas.drawRect(150, 965, 1000, 1065, paint);
    //
    //    paint.setColor(Color.GREEN);
    //    canvas.drawRect(150, 1105, 1000, 1205, paint);
    //
    //    paint.setPathEffect(new DashPathEffect(new float[] { 10f, 10f }, 0));
    //    paint.setColor(Color.BLACK);
    //    canvas.drawRect(150, 1245, 1000, 1345, paint);
    //    canvas.drawRect(150, 1385, 1000, 1485, paint);

        //you need to call postInvalidate so that the system knows that it should redraw your custom ImageView
        this.postInvalidate();
    }
}