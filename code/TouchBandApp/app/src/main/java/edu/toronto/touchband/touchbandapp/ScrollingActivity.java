package edu.toronto.touchband.touchbandapp;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list for the scrolling task
 */
public class ScrollingActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_launcher_view);

        List<String> itemList = new ArrayList<String>();
        itemList.add("One");
        itemList.add("Two");
        itemList.add("Three");
        itemList.add("Four");

        RecyclerView.Adapter mAdapter = new RecyclerListAdapter(itemList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        // TODO: add timing
    }
}
