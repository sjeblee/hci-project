package edu.toronto.touchband.touchbandapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

/**
 * Displays the task options: selection or scrolling, and the finish button
 */
public class TaskActivity extends WearableActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);
    }

    public void startSelectionActivity(View view){
        // TODO: launch SelectionActivity
    }

    public void startScrollingActivity(View view){
        //TODO: pass participant id as an extra
        Intent intent = new Intent(this, ScrollingStartActivity.class);
        startActivity(intent);
    }

    public void endSession(View view){
        // TODO: end the session
        // TODO: display the participant id
    }
}
