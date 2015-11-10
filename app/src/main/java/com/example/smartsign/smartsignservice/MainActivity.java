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
                getYoutubeId(word,getApplicationContext());
            } else {
                setContentView(R.layout.activity_main); // Handle invalid sent data type here...
            }
        } else {
            setContentView(R.layout.activity_main); // Go to the settings page.
        }
        Switch s = (Switch) findViewById(R.id.phonelistenswitch);

        if (s != null) {
            s.setOnCheckedChangeListener(new listenSwitchListener(this));
        }
        RadioGroup radioSexGroup = (RadioGroup) findViewById(R.id.radioGroup1);

        radioSexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int id) {
                if(id == R.id.radioButton){
                    MainService.setImage(MainService.SMARTSIGN_IMAGE);
                } else if(id == R.id.radioButton2){
                    MainService.setImage(MainService.CAT_IMAGE);
                }

            }
    });
        Intent serviceIntent = new Intent(this, MainService.class);
        startService(serviceIntent);
        serviceIntent.getComponent();
    }

    public static void getYoutubeId(String word,android.content.Context callContext){
        AsyncHttpClient smartSignClient = new AsyncHttpClient();
        final android.content.Context context = callContext;
        final String lookupWord = word;
        Toast toast = Toast.makeText(context,"Find: " + word, Toast.LENGTH_SHORT);
        toast.show();
        smartSignClient.get(BASE_URL + word, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray wordObject) {
                JSONObject first_word_object;
                try {
                    first_word_object = (JSONObject) wordObject.get(0);
                    youtubeId = first_word_object.getString("id");
                    // Open video in Youtube App or Browser.
                    playYoutubeVideo(youtubeId,context);
                } catch (JSONException e) {
                    Toast.makeText(context,"Failed to find translation for " + lookupWord,Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onFinish() {
                Log.d("onFinish: ", youtubeId);
            }
        });
    }


    private String getSharedWord(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Toast toast = Toast.makeText(getApplicationContext(), sharedText, Toast.LENGTH_SHORT);
            toast.show();
        }
        return sharedText;
    }

    private static void playYoutubeVideo(String youtubeId, android.content.Context context){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + youtubeId));
            context.startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v="+youtubeId));
            context.startActivity(intent);
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
class listenSwitchListener implements CompoundButton.OnCheckedChangeListener{

    private MainActivity activity;
    public listenSwitchListener(MainActivity activity){
        this.activity = activity;
    }
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        MainService.setIsListening(isChecked);
    }
}

