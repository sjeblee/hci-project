package edu.toronto.touchband.touchbandapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

/**
 * Displays the start button and the target item
 */
public class ScrollingStartActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);
    }

    public void start(View view){
        //TODO: generate target item and pass as an extra
        Intent intent = new Intent(this, ScrollingActivity.class);
        startActivity(intent);
    }
}
