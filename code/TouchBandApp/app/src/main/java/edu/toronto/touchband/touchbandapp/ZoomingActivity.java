package edu.toronto.touchband.touchbandapp;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

public class ZoomingActivity extends WearableActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zooming_activity);
    }
}
