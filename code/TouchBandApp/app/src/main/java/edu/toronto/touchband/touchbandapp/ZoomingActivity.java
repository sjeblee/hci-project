package edu.toronto.touchband.touchbandapp;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.wearable.activity.WearableActivity;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.ClearCacheRequest;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ZoomingActivity extends WearableActivity {
    private ImageView iv;
    private Matrix matrix;
    float scale = 0.1f;
    private ScaleGestureDetector SGD;

    // Stuff for touch input
    private RequestQueue mRequestQueue;
    private final String mUrl = "http://172.31.35.62:3000/touchInfo";

    private AtomicBoolean mTouch1 = new AtomicBoolean(false);
    private AtomicBoolean mTouch2 = new AtomicBoolean(false);

    private AtomicIntegerArray mTouchIndices = new AtomicIntegerArray(6);
    private AtomicInteger mPrevTouchLoc1 = new AtomicInteger(-1);
    private AtomicInteger mPrevTouchLoc2 = new AtomicInteger(-1);
    private int yScale = 60;
    private int mThresh = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zooming_activity);

        iv = (ImageView) findViewById(R.id.img);
        SGD = new ScaleGestureDetector(this, new ScaleListener());
        matrix = new Matrix();

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
        super.onDestroy();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        SGD.onTouchEvent(ev);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(1.0f, Math.min(scale, 10.0f));
            matrix.setScale(scale, scale);
            iv.setImageMatrix(matrix);
            return true;
        }
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
        /*if ((eventType == MotionEvent.ACTION_DOWN) || (eventType == MotionEvent.ACTION_UP)) {
            motionEvent = MotionEvent.obtain(downTime, eventTime, eventType, 0, end/2, metaState);
        } else if (eventType == MotionEvent.ACTION_MOVE) {*/
        System.out.println("Creating move event");
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[2];

        // First pointer
        MotionEvent.PointerCoords pc = new MotionEvent.PointerCoords();
        pc.setAxisValue(MotionEvent.AXIS_Y, end / 2);
        System.out.println("Point 1: " + end / 2);
        pc.setAxisValue(MotionEvent.AXIS_X, 160);
        pointerCoords[0] = pc;
        MotionEvent.PointerProperties pointerProps = new MotionEvent.PointerProperties();
        pointerProps.id = 0;

        // Second pointer
        MotionEvent.PointerCoords pc1 = new MotionEvent.PointerCoords();
        pc1.setAxisValue(MotionEvent.AXIS_Y, (360 - (end / 2)));
        System.out.println("Point 2: " + (360 - (end / 2)));
        pc1.setAxisValue(MotionEvent.AXIS_X, 160);
        pointerCoords[1] = pc1;
        MotionEvent.PointerProperties pointerProps1 = new MotionEvent.PointerProperties();
        pointerProps.id = 1;

        MotionEvent.PointerProperties[] pointerProperties = {pointerProps, pointerProps1};
        motionEvent = MotionEvent.obtain(downTime, eventTime, eventType, 1, pointerProperties, pointerCoords, metaState, 0,
                0.01f, 0.01f, 0, 0, 0, 0);
        // }

        // Dispatch touch event to view
        if (motionEvent != null) {
            // Get the current view
            //System.out.println("dispatch touch event");
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
                            int start = 0;
                            if (arrays.length > 10) {
                                start = arrays.length - 10;
                            }
                            // Figure out if an up, down, or move event has happened
                            for (int x = start; x < arrays.length; x++) {
                                String arrayString = arrays[x];
                                //String arrayString = arrays[arrays.length - 1];
                                arrayString = arrayString.substring(1, arrayString.length() - 1);
                                System.out.println("arraystring: " + arrayString);
                                int[] array = new int[6];
                                String[] sarray = arrayString.split(" ");
                                for (int k = 0; k < 6; k++) {
                                    array[k] = Integer.parseInt(sarray[k]);
                                }

                                boolean moved1 = false, moved2 = false;
                                boolean started1 = false, started2 = false;
                                int newTouchLoc1 = -1;
                                int newTouchLoc2 = -1;
                                int maxIndex1 = -1;
                                int maxIndex2 = -1;
                                int val1 = mThresh;
                                int val2 = mThresh;

                                // Look for two highest touch points, unless they're together
                                for (int y = 0; y < 6; y++) {
                                    if (array[y] > val1) {
                                        maxIndex1 = y;
                                        val1 = array[y];
                                    }
                                }
                                /*for (int y = 3; y < 6; y++) {
                                    if (array[y] > val2) {
                                        maxIndex2 = y;
                                        val2 = array[y];
                                    }
                                } */// end for

                                //int y = maxIndex;
                                System.out.println("maxIndex1: " + maxIndex1); //+ ", maxIndex2: " + maxIndex2);
                                double touchIndex1 = -1.0;
                                double touchIndex2 = -1.0;

                                // First touch
                                if (maxIndex1 != -1) {
                                    int totalval = val1;
                                    int sum = val1 * maxIndex1;

                                    if (maxIndex1 - 1 >= 0) {
                                        int z = maxIndex1 - 1;
                                        if (array[z] >= mThresh) {
                                            totalval += array[z];
                                            sum += (z * array[z]);
                                        }
                                    }

                                    if (maxIndex1 + 1 < 6) {
                                        int r = maxIndex1 + 1;
                                        if (array[r] >= mThresh) {
                                            totalval += array[r];
                                            sum += (r * array[r]);
                                        }
                                    }

                                    touchIndex1 = (double) sum / (double) totalval;
                                    newTouchLoc1 = (int) (touchIndex1 * (double) yScale);
                                    System.out.println("touchIndex1: " + touchIndex1 + "newTouchLoc1: " + newTouchLoc1);
                                }

                                // Second touch
                                /*if (maxIndex2 != -1) {
                                    int totalval = val2;
                                    int sum = val2 * maxIndex2;

                                    if (maxIndex2 - 1 >= 0) {
                                        int z = maxIndex2 - 1;
                                        if (array[z] >= mThresh) {
                                            totalval += array[z];
                                            sum += (z * array[z]);
                                        }
                                    }

                                    if (maxIndex2 + 1 < 6) {
                                        int r = maxIndex2 + 1;
                                        if (array[r] >= mThresh) {
                                            totalval += array[r];
                                            sum += (r * array[r]);
                                        }
                                    }

                                    touchIndex2 = (double) sum / (double) totalval;
                                    newTouchLoc2 = (int) (touchIndex2 * (double) yScale);
                                    System.out.println("touchIndex2: " + touchIndex2 + "newTouchLoc2: " + newTouchLoc2);
                                }*/


                                System.out.println("PrevTouchLoc1: " + mPrevTouchLoc1.get());
                                if (mTouch1.get() && (newTouchLoc1 != (mPrevTouchLoc1.get())) && (maxIndex1 != -1)) {
                                    moved1 = true;
                                } else if ((maxIndex1 != -1) && !mTouch1.get()) {
                                    started1 = true;
                                }

                                // Check for ACTION_UP
                                if (mTouch1.get() && maxIndex1 < 0) {
                                    generateTouch(MotionEvent.ACTION_UP, mPrevTouchLoc1.get(), mPrevTouchLoc1.get());
                                    mTouch1.compareAndSet(true, false);
                                } else { // ACTION_DOWN or ACTION_MOVE
                                    if (started1) {
                                        mTouch1.compareAndSet(false, true);
                                        generateTouch(MotionEvent.ACTION_DOWN, newTouchLoc1, newTouchLoc1);
                                    } else if (moved1) {
                                        generateTouch(MotionEvent.ACTION_MOVE, mPrevTouchLoc1.get(), newTouchLoc1);
                                    }
                                }

                               /* System.out.println("PrevTouchLoc2: " + mPrevTouchLoc2.get());
                                if (mTouch2.get() && (newTouchLoc2 != (mPrevTouchLoc2.get())) && (maxIndex2 != -1)) {
                                    moved2 = true;
                                } else if ((maxIndex2 != -1) && !mTouch2.get()) {
                                    started2 = true;
                                }

                                // Check for ACTION_UP
                                if (mTouch2.get() && maxIndex2 < 0) {
                                    generateTouch(MotionEvent.ACTION_UP, mPrevTouchLoc2.get(), mPrevTouchLoc2.get());
                                    mTouch2.compareAndSet(true, false);
                                } else { // ACTION_DOWN or ACTION_MOVE
                                    if (started2) {
                                        mTouch2.compareAndSet(false, true);
                                        generateTouch(MotionEvent.ACTION_DOWN, newTouchLoc2, newTouchLoc2);
                                    } else if (moved2) {
                                        generateTouch(MotionEvent.ACTION_MOVE, mPrevTouchLoc2.get(), newTouchLoc2);
                                    }
                                }*/

                                // Save new touch info
                                for (int j = 0; j < array.length; j++) {
                                    mTouchIndices.set(j, array[j]);
                                }
                                mPrevTouchLoc1.set(newTouchLoc1);
                                // mPrevTouchLoc2.set(newTouchLoc2);
                            }
                        }
                        // Send the next request
                        mRequestQueue.add(createStringRequest());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("Error from Volley GET request!");
            }
        });
        stringRequest.setShouldCache(false);
        return stringRequest;
    }
}
