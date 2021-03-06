package edu.toronto.touchband.touchbandapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;

/**
 * Displays the task options: selection or scrolling, and the finish button
 */
public class TaskActivity extends WearableActivity {

    MetricsManager mMetricsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mMetricsManager = MetricsManager.getInstance();
    }

    public void startZoomingActivity(View view) {
        Intent intent = new Intent(this, ZoomingStartActivity.class);
        startActivity(intent);
    }

    public void startScrollingActivity(View view) {
        Intent intent = new Intent(this, ScrollingStartActivity.class);
        startActivity(intent);
    }

    public void endSession(View view) {

        // Dump metrics
        if (mMetricsManager != null) {
            mMetricsManager.printMetrics();
        }
        this.finish();
    }
}
