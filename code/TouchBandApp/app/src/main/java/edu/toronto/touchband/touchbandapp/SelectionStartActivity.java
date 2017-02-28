package edu.toronto.touchband.touchbandapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

/**
 * Displays the start button and the target item
 */
public class SelectionStartActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_start_screen);
    }

    public void start(View view){
        //TODO: launch Selection activity - you might want 3 separate ones
        //Intent intent = new Intent(this, SelectionActivity.class);
        //startActivity(intent);
    }
}
