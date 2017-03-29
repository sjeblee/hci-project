package edu.toronto.touchband.touchbandapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.*;

//import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

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

    private RequestQueue mRequestQueue;
    private final String mUrl = "http://192.168.43.91:3000/touchInfo";

    private AtomicBoolean mTouch = new AtomicBoolean(false);
    private AtomicIntegerArray mTouchIndices = new AtomicIntegerArray(6);
    private AtomicInteger mPrevTouchLoc = new AtomicInteger(-1);
    private int yScale = 60;
    private int mThresh = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Start the HTTP request queue
        final Context mContext = this;
        DiskBasedCache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();

        // Clear the cache
        mRequestQueue.add(new ClearCacheRequest(cache, null));

        // Start sending get requests
        StringRequest stringRequest = createStringRequest();
        mRequestQueue.add(stringRequest);

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
        int index = ThreadLocalRandom.current().nextInt(5, numItems + 1);
        itemList.add(index, mTargetItem);

        // Pad the list with extra values so the last one can be reached
        for (int i = 0; i < 5; i++) {
            itemList.add("");
        }

        final RecyclerView.Adapter mAdapter = new RecyclerListAdapter(itemList);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(ScrollingActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        long endtime = System.currentTimeMillis();
                        String item = ((RecyclerListAdapter) mAdapter).getCurrentItem();

                        // Check to see if it's the right item, if so, return to start screen
                        if (item.equals(mTargetItem)) {
                            long time = endtime - mStartTime;
                            double acc = 1.0 / (mWrongSelections + 1);
                            recordMetric(time, acc);
                            ScrollingActivity.this.finish();
                        } else {
                            mWrongSelections++;
                        }
                    }
                })
        );

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //System.out.println("scroll state changed");
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                System.out.println("Scrolled by " + dy);
                int selectedIndex = ((RecyclerListAdapter) mAdapter).getSelectedIndex();
                int itemIndex = ((RecyclerListAdapter) mAdapter).onScrolled(dy);
                // Update item highlight
                if (selectedIndex != itemIndex) {
                    RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
                    View oldView = layoutManager.findViewByPosition(selectedIndex);
                    if (oldView != null) {
                        oldView.setSelected(false);
                    }
                    View itemView = layoutManager.findViewByPosition(itemIndex);
                    itemView.setSelected(true);
                }
            }
        });

        mStartTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        mRequestQueue.stop();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRequestQueue.start();
    }

    @Override
    public void onDestroy() {
        mRequestQueue.stop();
        super.onDestroy();
    }

    private void recordMetric(long time, double acc) {
        mMetricsManager.recordMetric("scrolling", time, acc);
    }

    public void generateTouch(int eventType, int start, int end) {
        System.out.println("Generating touch event " + eventType + ", " + start + ", " + end);

        // Obtain MotionEvent object
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = 0.0f;
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        int metaState = 0;
        MotionEvent motionEvent = null;
        if ((eventType == MotionEvent.ACTION_DOWN) || (eventType == MotionEvent.ACTION_UP)) {
            motionEvent = MotionEvent.obtain(downTime, eventTime, eventType, 0, end, metaState);
        } else if (eventType == MotionEvent.ACTION_MOVE) {
            System.out.println("Creating move event");
            MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[1];
            MotionEvent.PointerCoords pc = new MotionEvent.PointerCoords();
            pc.setAxisValue(MotionEvent.AXIS_Y, end);
            pc.setAxisValue(MotionEvent.AXIS_X, 0);
            pointerCoords[0] = pc;
            MotionEvent.PointerProperties pointerProps = new MotionEvent.PointerProperties();
            pointerProps.id = 0;
            MotionEvent.PointerProperties[] pointerProperties = {pointerProps};
            motionEvent = MotionEvent.obtain(downTime, eventTime, eventType, 1, pointerProperties, pointerCoords, metaState, 0,
                    0.01f, 0.01f, 0, 0, 0, 0);
        }

        // Dispatch touch event to view
        if (motionEvent != null) {
            // Get the current view
            //View view = getWindow().getCurrentFocus();
            System.out.println("dispatch touch event");
            this.dispatchTouchEvent(motionEvent);
        }
    }

    /**
     * Create a new HTTP GET request that generates another request on the successful completion of this one
     *
     * @return the StringRequest
     */
    public StringRequest createStringRequest() {
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, mUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Get touch info from the response
                        if (response.length() > 2) {
                            // Split off the []
                            String string = response.substring(1, response.length() - 1);
                            //System.out.println("string: " + string);
                            String[] arrays = string.split(",");
                            // Figure out if an up, down, or move event has happened
                            for (String arrayString : arrays) {
                                //String arrayString = arrays[arrays.length - 1];
                                arrayString = arrayString.substring(1, arrayString.length() - 1);
                                System.out.println("arraystring: " + arrayString);
                                int[] array = new int[6];
                                String[] sarray = arrayString.split(" ");
                                for (int k = 0; k < 6; k++) {
                                    array[k] = Integer.parseInt(sarray[k]);
                                }

                                boolean moved = false;
                                boolean started = false;
                                int newTouchLoc = -1;
                                int maxIndex = -1;
                                int val = mThresh;

                                for (int y = 0; y < array.length; y++) {
                                    if (array[y] > val) {
                                        maxIndex = y;
                                        val = array[y];
                                    }
                                } // end for

                                int y = maxIndex;
                                System.out.println("maxIndex: " + maxIndex);
                                double touchIndex = -1.0;

                                if (maxIndex != -1) {
                                    int totalval = val;
                                    int sum = val * maxIndex;

                                    if (maxIndex - 1 >= 0) {
                                        int z = maxIndex - 1;
                                        if (array[z] >= mThresh) {
                                            totalval += array[z];
                                            sum += (z * array[z]);
                                        }
                                    }
                                    if (maxIndex + 1 < 6) {

                                        int r = maxIndex + 1;
                                        if (array[r] >= mThresh) {
                                            totalval += array[r];
                                            sum += (r * array[r]);
                                        }
                                    }

                                    touchIndex = (double) sum / (double) totalval;
                                    newTouchLoc = (int) (touchIndex * (double) yScale);
                                    System.out.println("touchIndex: " + touchIndex + "newTouchLoc: " + newTouchLoc);
                                }


                                System.out.println("PrevTouchLoc: " + mPrevTouchLoc.get());
                                if (mTouch.get() && (newTouchLoc != (mPrevTouchLoc.get())) && (maxIndex != -1)) {
                                    moved = true;
                                } else if ((maxIndex != -1) && !mTouch.get()) {
                                    started = true;
                                }

                                // Check for ACTION_UP
                                if (mTouch.get() && maxIndex < 0) {
                                    generateTouch(MotionEvent.ACTION_UP, mPrevTouchLoc.get(), mPrevTouchLoc.get());
                                    mTouch.compareAndSet(true, false);
                                } else { // ACTION_DOWN or ACTION_MOVE
                                    if (started) {
                                        mTouch.compareAndSet(false, true);
                                        generateTouch(MotionEvent.ACTION_DOWN, newTouchLoc, newTouchLoc);
                                    } else if (moved) {
                                        generateTouch(MotionEvent.ACTION_MOVE, mPrevTouchLoc.get(), newTouchLoc);
                                    }
                                }

                                // Save new touch info
                                for (int j = 0; j < array.length; j++) {
                                    mTouchIndices.set(j, array[j]);
                                }
                                mPrevTouchLoc.set(newTouchLoc);
                            }
                        }
                        // Send the next request
                        mRequestQueue.add(createStringRequest());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("Error from Volley GET request!");
                mRequestQueue.add(createStringRequest());
            }
        });
        stringRequest.setShouldCache(false);
        return stringRequest;
    }
}
