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

/**
 * this class represents the only visual activity of this app.  It allows the user to enter in a word, and search
 * for a sign language translation.  It displays the results in a listview with thumbnail images.  Also, if a word is shared
 * with the app, it will perform the same task of looking up the word and displaying the results to the user in the activity
 */
public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://smartsign.imtc.gatech.edu/videos?keywords=";
    /**
     * Checks to see if a word was shared with the app.  If so, the app perfors a lookup of the word using 
     * getYoutubeList(string); Also, sets onClick listener for the search button to get text from textView, and perform
     * getYoutubeList(string) on whatever text was in the textView.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    /**
     * This method's primary purpose is to get and parse information from SmartSign database.
     * looks up specified word using API call to SmartSign database.  Then returns.  If the lookup is successful, then the
     * method parses the returned JSON, and generates a List<Map<String, String>> which represents the returned data.  The list
     * is in priority order as specified by the returned JSON.  Then the method specifies an adapter to be used for the listView, 
     * and allows the adapter to handle the rest of populating the listview.
     * @param word the english word to be used in the query to the SmartSign databse.
     */
    public void getYoutubeList(String word){
        if(word == null || word.length() < 0){
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
            }

            @Override
            public void onFinish() {
            }
        });
    }
    /**
     * stop displaying the progress bar spinner
     */
    private void stopSpinner(){
        ProgressBar spinner;
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        ListView list = (ListView) findViewById(R.id.listView);
        list.setVisibility(View.VISIBLE);
    }

    /**
     * get the string that was shared with the app when it was launched, if any text was shared.
     */
    public String getSharedWord(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Toast toast = Toast.makeText(getApplicationContext(), sharedText, Toast.LENGTH_SHORT);
            //toast.show();
        }
        return sharedText;
    }
    /**
     * launch another activity to play the youtube video with the specified youtubeId.  The app used
     * to play the youtube video will depend on preferences of the specific android device, the player is
     * not part of this app.
     */
    public void playYoutubeVideo(String youtubeId){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + youtubeId));
            startActivity(intent);
        } catch (ActivityNotFoundException ex){
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
    
    /**
     * class used to represent data given by the SmartSign database.  Holds information, and specifies how
     * to compare the information so it can be put in priority order.
     */
    class Data implements Comparable<Data> {
        public final Map<String,String> hashmap;
        public final int priority;

        /**
         * constructor
         * @priority the number representing the place in priority order of the item.  The lower the number the more important the item is.
         * @hashmap map of information being represented by this object.
         */
        public Data(int priority, Map<String,String> hashmap) {
            this.hashmap = hashmap;
            this.priority = priority;
        }

        /**
         * compares items based on their priority variable.
         * @param other the other Data object this data object is being compared to.
         */
        @Override
        public int compareTo(Data other) {
            return Integer.valueOf(priority).compareTo(other.priority);
        }
    }
}


