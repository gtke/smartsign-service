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


public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://smartsign.imtc.gatech.edu/videos?keywords=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        setContentView(R.layout.activity_main);
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


    public void getYoutubeList(String word){
        if(word.length() < 0){
            return;
        }
        AsyncHttpClient smartSignClient = new AsyncHttpClient();
        Toast toast = Toast.makeText(getApplicationContext(),"Find: " + word, Toast.LENGTH_SHORT);
        toast.show();
        final Activity activity = this;
        smartSignClient.get(BASE_URL + word, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    List<Map<String,String>> data = new ArrayList<Map<String,String>>();
                    PriorityQueue<Data> primaryData = new PriorityQueue<Data>();
                    for(int i = 0;i<response.length();i++) {
                        JSONObject object = (JSONObject) response.get(i);
                        String youtubeId = object.getString("id");
                        String title = object.getString("title");
                        String thumbnail = object.getString("thumbnail");

                        Map<String,String> item = new HashMap<String,String>();
                        item.put("title",title);
                        item.put("thumbnail",thumbnail);
                        item.put("youtubeId",youtubeId);

                        String keywords = "";
                        JSONArray keywordsObject = object.getJSONArray("keywords");
                        for(int j = 0;j<keywordsObject.length();j++){
                            String ans = keywordsObject.getString(j);
                            keywords += ans;
                        }
                        Pattern pat = Pattern.compile("(\\{)(\\d)(\\})");
                        Matcher m = pat.matcher(keywords);
                        if(m.find()){
                            try{
                                int index = Integer.parseInt(m.group(2));
                                primaryData.add(new Data(index,item));
                            } catch(Exception e){
                                data.add(item);
                            }
                        } else {
                            data.add(item);
                        }
                    }
                    int primaryIndex = 0;
                    while(!primaryData.isEmpty()){
                       data.add(primaryIndex,primaryData.poll().hashmap);
                        primaryIndex++;
                    }

                    ListView list=(ListView)findViewById(R.id.listView);

                    // Getting adapter by passing xml data ArrayList
                    ListPopulater populater=new ListPopulater(activity, data);
                    list.setAdapter(populater);

//                    // Click event for single list row
                    list.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            TextView text = (TextView)view.findViewById(R.id.youtube_id);
                            String youtubeId = text.getText().toString();
                            playYoutubeVideo(youtubeId);
                        }
                    });

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Failed to populate list",Toast.LENGTH_SHORT);
                    e.printStackTrace();
                }

                // Open video in Youtube App or Browser.
            }

            @Override
            public void onFinish() {
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


