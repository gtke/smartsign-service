package com.example.smartsign.smartsignservice;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
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
import android.graphics.Bitmap;
import java.net.*;
import android.graphics.BitmapFactory;
import java.io.*;
import java.util.*;
import cz.msebera.android.httpclient.Header;
import android.widget.AdapterView.*;
import android.app.Activity;

import java.util.regex.*;
import android.view.View.OnClickListener;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import android.graphics.Matrix;
import android.widget.ImageView;
import android.graphics.RectF;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://smartsign.imtc.gatech.edu/videos?keywords=";
    private boolean isLoaded;

    private Lock loadMutex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadMutex = new ReentrantLock(true);
        loadMutex.lock();
        isLoaded = false;
        loadMutex.unlock();
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        setContentView(R.layout.activity_main);
        ProgressBar spinner = (ProgressBar)(findViewById(R.id.progressBar1));
        spinner.setVisibility(View.GONE);
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String word = getSharedWord(intent); // Handle text being sent here...
                if(word != null && word.length() > 0) {
                    EditText text = (EditText) findViewById(R.id.editText);
                    text.setText(word);
                    getYoutubeList(word);
                }

            }
        }
        Button search = (Button) findViewById(R.id.button2);
        search.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                EditText text = (EditText) findViewById(R.id.editText);
                String lookupWord = text.getText().toString();
                getYoutubeList(lookupWord);
            }
        });
    }

    public boolean getIsLoaded(){
        loadMutex.lock();
        boolean ans = this.isLoaded;
        loadMutex.unlock();
        return ans;
    }

    public void getYoutubeList(String word){
        loadMutex.lock();
        isLoaded = false;
        loadMutex.unlock();
        if(word.length() < 0){
            return;
        }
        AsyncHttpClient smartSignClient = new AsyncHttpClient();
        Toast toast = Toast.makeText(getApplicationContext(),"Find: " + word, Toast.LENGTH_SHORT);
        //toast.show();
        final MainActivity activity = this;
        ProgressBar spinner;
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
        ListView list = (ListView) findViewById(R.id.listView);
        list.setVisibility(View.INVISIBLE);
        //networking stuff
        smartSignClient.get(BASE_URL + word, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
                    PriorityQueue<Data> primaryData = new PriorityQueue<Data>();
                    //Display no videos text if nothing is found
                    if (response.length() == 0) {

                        TextView text = (TextView) findViewById(R.id.noResults);
                        text.setText(getResources().getString(R.string.no_videos));
                    }

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject object = (JSONObject) response.get(i);
                        String youtubeId = object.getString("id");
                        String title = object.getString("title");
                        String thumbnail = object.getString("thumbnail");

                        Map<String, String> item = new HashMap<String, String>();
                        item.put("title", title);
                        item.put("thumbnail", thumbnail);
                        item.put("youtubeId", youtubeId);

                        String keywords = "";
                        JSONArray keywordsObject = object.getJSONArray("keywords");
                        for (int j = 0; j < keywordsObject.length(); j++) {
                            String ans = keywordsObject.getString(j);
                            keywords += ans;
                        }
                        Pattern pat = Pattern.compile("(\\{)(\\d)(\\})");
                        Matcher m = pat.matcher(keywords);
                        if (m.find()) {
                            try {
                                int index = Integer.parseInt(m.group(2));
                                primaryData.add(new Data(index, item));
                            } catch (Exception e) {
                                data.add(item);
                            }
                        } else {
                            data.add(item);
                        }
                    }
                    int primaryIndex = 0;
                    while (!primaryData.isEmpty()) {
                        data.add(primaryIndex, primaryData.poll().hashmap);
                        primaryIndex++;
                    }

                    ListView list = (ListView) findViewById(R.id.listView);

                    // Getting adapter by passing xml data ArrayList
                    ListPopulater populater = new ListPopulater(activity, data);
                    list.setAdapter(populater);

//                    // Click event for single list row
                    list.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            TextView text = (TextView) view.findViewById(R.id.youtube_id);
                            String youtubeId = text.getText().toString();
                            playYoutubeVideo(youtubeId);
                        }
                    });
                    stopSpinner();

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Failed to populate list", Toast.LENGTH_SHORT);
                    stopSpinner();
                    e.printStackTrace();
                }

                // Open video in Youtube App or Browser.
            }

            @Override
            public void onFinish() {
            }
        });
    }
    private void stopSpinner(){
        ProgressBar spinner;
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        ListView list = (ListView) findViewById(R.id.listView);
        list.setVisibility(View.VISIBLE);
    }

    public String getSharedWord(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Toast toast = Toast.makeText(getApplicationContext(), sharedText, Toast.LENGTH_SHORT);
            //toast.show();
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
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(getResources().getString(R.string.instructions));
            alertDialog.setMessage(getResources().getString(R.string.instructions_text));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class Data implements Comparable<Data> {
        public final Map<String,String> hashmap;
        public final int priority;

        public Data(int priority, Map<String,String> hashmap) {
            this.hashmap = hashmap;
            this.priority = priority;
        }

        @Override
        public int compareTo(Data other) {
            return Integer.valueOf(priority).compareTo(other.priority);
        }
    }
}


