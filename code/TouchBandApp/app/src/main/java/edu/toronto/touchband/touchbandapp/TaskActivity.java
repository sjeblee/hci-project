package edu.toronto.touchband.touchbandapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

/**
 * Displays the task options: selection or scrolling, and the finish button
 */
public class TaskActivity extends WearableActivity {

    MetricsManager mMetricsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);
        mMetricsManager = MetricsManager.getInstance();
    }

    public void startZoomingActivity(View view){
        Intent intent = new Intent(this, ZoomingStartActivity.class);
        startActivity(intent);
    }

    public void startScrollingActivity(View view){
        //TODO: pass participant id as an extra
        Intent intent = new Intent(this, ScrollingStartActivity.class);
        startActivity(intent);
    }

    public void endSession(View view) {
        // TODO: display user id

        // Dump metrics
        mMetricsManager.printMetrics();
        this.finish();
    }
}
