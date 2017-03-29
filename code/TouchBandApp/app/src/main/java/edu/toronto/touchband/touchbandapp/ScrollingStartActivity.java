package edu.toronto.touchband.touchbandapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Displays the start button and the target item
 */
public class ScrollingStartActivity extends WearableActivity {

    private String mTargetItem;
    private int mCounter = 0;
    private ArrayList<String> used = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Quit after 10 iterations
        if (mCounter >= 5) {
            this.finish();
        }

        // Generate target item and pass as an extra
        ArrayList<String> itemList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.items)));
        long seed = System.nanoTime();
        Collections.shuffle(itemList, new Random(seed));

        // Pick a random item, make sure we haven't used it before
        int i = 0;
        mTargetItem = itemList.get(i);
        while (used.contains(mTargetItem)) {
            i++;
            mTargetItem = itemList.get(i);
        }
        System.out.println("Target item: " + mTargetItem);

        TextView textView = (TextView) findViewById(R.id.targetitemtext);
        textView.setText(mTargetItem);
        used.add(mTargetItem);
    }

    public void start(View view){
        mCounter++;
        Intent intent = new Intent(this, ScrollingActivity.class);
        intent.putExtra("target", mTargetItem);
        startActivity(intent);
    }
}
