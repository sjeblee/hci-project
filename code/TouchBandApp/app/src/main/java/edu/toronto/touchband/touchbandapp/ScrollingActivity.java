package edu.toronto.touchband.touchbandapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Displays the list for the scrolling task
 */
public class ScrollingActivity extends WearableActivity {

    private MetricsManager mMetricsManager;
    private long mStartTime;
    private int mWrongSelections = 0;
    private String mTargetItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

        // Get the target item
        Intent intent = this.getIntent();
        mTargetItem = intent.getStringExtra("target");
        System.out.println("got target from extra: " + mTargetItem);

        mMetricsManager = MetricsManager.getInstance();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_launcher_view);

        // Load list items from xml and shuffle it
        ArrayList<String> itemList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.items)));
        long seed = System.nanoTime();
        Collections.shuffle(itemList, new Random(seed));

        final RecyclerView.Adapter mAdapter = new RecyclerListAdapter(itemList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(ScrollingActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        long endtime = System.currentTimeMillis();
                        String item = ((RecyclerListAdapter) mAdapter).getItem(position);

                        // Check to see if it's the right item, if so, return to start screen
                        if (item.equals(mTargetItem)) {
                            long time = endtime - mStartTime;
                            double acc = 1.0/(mWrongSelections+1);
                            recordMetric(time, acc);
                            ScrollingActivity.this.finish();
                        } else {
                            mWrongSelections++;
                        }
                    }
                })
        );

        mStartTime = System.currentTimeMillis();
    }

    private void recordMetric(long time, double acc) {
        mMetricsManager.recordMetric("scrolling", time, acc);
    }
}
