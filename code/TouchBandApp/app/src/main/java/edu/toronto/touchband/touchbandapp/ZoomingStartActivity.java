package edu.toronto.touchband.touchbandapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.app.Activity;
import android.view.View;

/**
 * Displays the start button and the target item
 */
public class ZoomingStartActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_start_screen);
    }

    public void start(View view){
        //TODO: launch Zooming activity
        Intent intent = new Intent(this, ZoomingActivity.class);
        startActivity(intent);
    }
}
