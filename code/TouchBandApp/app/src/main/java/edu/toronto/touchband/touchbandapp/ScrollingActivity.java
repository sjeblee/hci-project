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
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;

/**
 * Displays the list for the scrolling task
 */
public class ScrollingActivity extends WearableActivity {

    private MetricsManager mMetricsManager;
    private long mStartTime;
    private int mWrongSelections = 0;
    private String mTargetItem;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

        // Get the target item
        Intent intent = this.getIntent();
        mTargetItem = intent.getStringExtra("target");
        System.out.println("got target from extra: " + mTargetItem);

        mMetricsManager = MetricsManager.getInstance();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_launcher_view);

        // Load list items from xml and shuffle it
        ArrayList<String> itemList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.items)));
        itemList.remove(itemList.indexOf(mTargetItem));
        int numItems = itemList.size();
        long seed = System.nanoTime();
        Collections.shuffle(itemList, new Random(seed));
        int index = ThreadLocalRandom.current().nextInt(5, numItems+1);
        itemList.add(index, mTargetItem);

        // Pad the list with extra values so the last one can be reached
        for (int i=0; i<3; i++) {
            itemList.add("");
        }

        final RecyclerView.Adapter mAdapter = new RecyclerListAdapter(itemList);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(ScrollingActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        long endtime = System.currentTimeMillis();
                        String item = ((RecyclerListAdapter) mAdapter).getCurrentItem();

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

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //System.out.println("scroll state changed");
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int selectedIndex = ((RecyclerListAdapter) mAdapter).getSelectedIndex();
                int itemIndex = ((RecyclerListAdapter) mAdapter).onScrolled(dy);
                // Update item highlight
                if (selectedIndex != itemIndex) {
                    RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
                    View oldView = layoutManager.findViewByPosition(selectedIndex);
                    oldView.setSelected(false);
                    View itemView = layoutManager.findViewByPosition(itemIndex);
                    itemView.setSelected(true);
                }
            }
        });

        mStartTime = System.currentTimeMillis();
    }

    private void recordMetric(long time, double acc) {
        mMetricsManager.recordMetric("scrolling", time, acc);
    }
}
