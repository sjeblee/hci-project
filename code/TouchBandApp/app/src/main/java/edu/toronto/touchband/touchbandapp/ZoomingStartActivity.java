package edu.toronto.touchband.touchbandapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;

/**
 * Displays the start button and the target item
 */
public class ZoomingStartActivity extends WearableActivity {
    private int mCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_start_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void start(View view){
        // launch Zooming activity
        mCounter++;
        Intent intent = new Intent(this, ZoomingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Quit after 10 iterations
        if (mCounter >= 10) {
            this.finish();
        }
    }
}
