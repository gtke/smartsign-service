package com.example.smartsign.smartsignservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by drichmond on 10/16/15.
 */
public class ButtonOverlay extends Activity{
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Go to the settings page.
        Toast.makeText(getApplicationContext(), "Overlay Activity", Toast.LENGTH_SHORT).show();
    }
}
