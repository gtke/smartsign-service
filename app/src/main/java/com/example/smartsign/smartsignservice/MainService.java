    package com.example.smartsign.smartsignservice;

    import android.accessibilityservice.AccessibilityService;
    import android.accessibilityservice.AccessibilityServiceInfo;
    import android.app.*;
    import android.content.*;
    import android.content.res.Resources;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.graphics.Canvas;
    import android.graphics.Color;
    import android.graphics.Paint;
    import android.graphics.PixelFormat;
    import android.graphics.drawable.Drawable;
    import android.net.Uri;
    import android.os.Bundle;
    import android.os.IBinder;
    import android.view.Gravity;
    import android.view.KeyEvent;
    import android.view.LayoutInflater;
    import android.view.MotionEvent;
    import android.view.View;
    import android.view.ViewGroup;
    import android.view.WindowManager;
    import android.view.accessibility.AccessibilityEvent;
    import android.widget.ImageView;
    import android.widget.Toast;
    import android.util.*;
    import android.support.v4.view.*;

    import com.loopj.android.http.AsyncHttpClient;
    import com.loopj.android.http.JsonHttpResponseHandler;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.util.List;

    import cz.msebera.android.httpclient.Header;

    /**
     * Created by drichmond on 10/15/15.
     */
    public class MainService extends AccessibilityService {

        HUDView mView;
        private static final String BASE_URL = "http://smartsign.imtc.gatech.edu/videos?keywords=";
        private static String youtubeId = "";
        private static String selectedText = "---";
        private WindowManager.LayoutParams buttonViewParams;
        private boolean showingWindow = false;
        @Override
        public void onCreate() {
            super.onCreate();
            Bitmap image = BitmapFactory.decodeResource(getResources(),
                    R.drawable.smartsigndictionary);
            mView = new HUDView(this,this,image); //replace first with getApplicationContext()
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    200,100,0,0,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.BOTTOM;
            params.setTitle("Load Average");
            buttonViewParams = params;
        }
        public void showButton(){
            if(!showingWindow) {
                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                wm.addView(mView, buttonViewParams);
                showingWindow = true;
            }
        }
        public void hideButton() {
            if(showingWindow) {
                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                wm.removeView(mView);
                showingWindow = false;
            }
        }
        @Override
        public void onDestroy() {
            super.onDestroy();
            if(mView != null)
            {
                ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mView);
                mView = null;
            }
        }

        @Override
        public void onInterrupt(){

        }
        @Override
        public void onAccessibilityEvent(AccessibilityEvent event) {
            int eventType = event.getEventType();
            //just trying to filter down the events we care about to reduce delay/lag
            if(eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                    eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
                    eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
                    eventType == AccessibilityEvent.TYPE_VIEW_SELECTED) {

                List<CharSequence> textList = event.getText();
                if (textList.size() > 0) {
                    String text = (textList.get(0)).toString();
                    int start = event.getFromIndex();
                    int end = event.getToIndex();
                    if (start != -1 && end > start && text != null && text.length() > end) {
                        //i don't think we should do anything in this case
                    } else {
                        hideButton();
                    }
                } else {
                    hideButton();
                }
            }
            List<CharSequence> textList = event.getText();
            if (textList.size() > 0) {
                String text = (textList.get(0)).toString();
                int start = event.getFromIndex();
                int end = event.getToIndex();
                if (start != -1 && end > start && text != null && text.length() >= end) {
                    selectedText = text.substring(start, end);
                    showButton();
                }

            }
        }
        public int onStartCommand(Intent intent,int flags,int startId){
            return super.onStartCommand(intent,flags,startId);
        }
        protected void onServiceConnected() {
            AccessibilityServiceInfo info = new AccessibilityServiceInfo();
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
            info.notificationTimeout = 100;
            setServiceInfo(info);
        }
        public String getSelectedText(){
            return selectedText;
        }



    }

    class HUDView extends ViewGroup {
        private Paint mLoadPaint;
        private MainService service;
        private Bitmap image;

        public HUDView(Context context,MainService service,Bitmap image) {
            super(context);
            this.service = service;
            mLoadPaint = new Paint();
            mLoadPaint.setAntiAlias(true);
            mLoadPaint.setTextSize(27);
            mLoadPaint.setARGB(255, 0, 150, 200);
            this.image = image;
        }
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.BLACK);

            canvas.drawBitmap(image, 0, 0, null);

            //canvas.drawText("Translate", 0, 60, mLoadPaint);
        }

        @Override
        protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            //return super.onTouchEvent(event);
            if(event.getAction() != MotionEvent.ACTION_OUTSIDE) {
                String str = service.getSelectedText();
                Intent intent = new Intent(service,MainActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT,str);
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                service.startActivity(intent);
            } else{

            }
            return true;
        }
    }