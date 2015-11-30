package com.example.smartsign.smartsignservice;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Map;
/**
 * Created by drichmond on 11/29/15.
 */
public class ListPopulater extends BaseAdapter {



    private Activity activity;
    private List<Map<String,String>> data;
    private HashMap<String,Bitmap> imageStore;
    private static LayoutInflater inflater=null;

    public ListPopulater(Activity a, List<Map<String,String>> data) {
        activity = a;
        this.data=data;
        imageStore = new HashMap<String,Bitmap>();
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view =convertView;
        if(convertView==null)
            view = inflater.inflate(R.layout.list_item, null);

        TextView title = (TextView)view.findViewById(R.id.title);
        TextView text = (TextView)view.findViewById(R.id.youtube_id);
        final ImageView thumb_image=(ImageView)view.findViewById(R.id.list_image);

        Map<String,String> listData = this.data.get(position);

        // Setting all values in listview
        title.setText(listData.get("title"));
        text.setText(listData.get("youtubeId"));
        final String url = listData.get("thumbnail");
        if(url != null) {
            new Thread(new Runnable() {
                public void run() {
                    loadImage(thumb_image, url);
                }
            }).start();
        }

        return view;
    }
    public void clear(){
        imageStore.clear();
    }
    private void loadImage(ImageView thumb_image,String str){
        Bitmap bmp = null;
        final ImageView thumb_image2 = thumb_image;
        try {
            if(imageStore.containsKey(str)){
                bmp = imageStore.get(str);
            } else {
                URL url = new URL(str);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                imageStore.put(str,bmp);
            }

        } catch(MalformedURLException e){

        } catch(IOException e){

        }

        if(bmp != null) {
            final Bitmap bmp2 = bmp;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    thumb_image2.setImageBitmap(bmp2);
                }
            });

        } else{
            //show default image
            Bitmap bmp2 = BitmapFactory.decodeResource(activity.getResources(), R.drawable.smartsigndictionary);
            thumb_image2.setImageBitmap(bmp2);
        }
    }
}

