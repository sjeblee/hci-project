package edu.toronto.touchband.touchbandapp;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Displays the list for the scrolling task
 */
public class ScrollingActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

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

                        // TODO: check to see if it's the right item, if so, return to start screen
                        if (item.equals("bananas")) {
                            ScrollingActivity.this.finish();
                        }
                    }
                })
        );

        long starttime = System.currentTimeMillis();
    }
}
