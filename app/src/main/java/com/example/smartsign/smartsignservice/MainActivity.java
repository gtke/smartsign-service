package com.example.smartsign.smartsignservice;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.graphics.PixelFormat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.provider.*;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://smartsign.imtc.gatech.edu/videos?keywords=";
    private static String youtubeId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String word = getSharedWord(intent); // Handle text being sent here...
                getYoutubeId(word);
            } else {
                setContentView(R.layout.activity_main); // Handle invalid sent data type here...
            }
        } else {
            setContentView(R.layout.activity_main); // Go to the settings page.
        }
        Intent serviceIntent = new Intent(this, MainService.class);
        startService(serviceIntent);
    }


    public void getYoutubeId(String word){
        AsyncHttpClient smartSignClient = new AsyncHttpClient();
        Toast toast = Toast.makeText(getApplicationContext(),"Find: " + word, Toast.LENGTH_SHORT);
        toast.show();
        smartSignClient.get(BASE_URL + word, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray wordObject) {
                JSONObject first_word_object;
                try {
                    first_word_object = (JSONObject) wordObject.get(0);
                    youtubeId = first_word_object.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println(youtubeId);
                Log.d("YoutubeID: ", youtubeId);

                // Open video in Youtube App or Browser.
                playYoutubeVideo(youtubeId);
            }

            @Override
            public void onFinish() {
                Log.d("onFinish: ", youtubeId);
            }
        });
    }


    public String getSharedWord(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Toast toast = Toast.makeText(getApplicationContext(), sharedText, Toast.LENGTH_SHORT);
            toast.show();
        }
        return sharedText;
    }

    public void playYoutubeVideo(String youtubeId){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + youtubeId));
            startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v="+youtubeId));
            startActivity(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


