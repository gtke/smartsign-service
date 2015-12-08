package com.example.smartsign.smartsignservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by drichmond on 10/16/15.
 * This class represents the button overlay functionality where an overlay button is placed over the entire android UI
 * no matter what app is currently running.  This functionality has been removed.
 * This class is only being kept because it represents previously needed functionality.  Being kept for historical purposes
 * in case it is determined that the buttonOverlay functionality is advantageous to this app at a later point in time.
 */
public class ButtonOverlay extends Activity{
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Go to the settings page.
    }
}
