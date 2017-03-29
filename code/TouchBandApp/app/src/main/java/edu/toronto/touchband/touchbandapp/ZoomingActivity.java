package edu.toronto.touchband.touchbandapp;

import android.accessibilityservice.AccessibilityService.MagnificationController;
import android.accessibilityservice.AccessibilityService.MagnificationController.OnMagnificationChangedListener;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.wearable.activity.WearableActivity;
import android.app.Activity;
import android.view.accessibility.AccessibilityManager;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.support.v4.view.MotionEventCompat;

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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ZoomingActivity extends WearableActivity {
    private ImageView iv;
    private Matrix matrix;
    float scale = 0.1f;
    private ScaleGestureDetector SGD;
    private MetricsManager mMetricsManager;

    // Stuff for touch input
    private RequestQueue mRequestQueue;
    private final String mUrl = "http://192.168.43.91:3000/touchInfo";

    private AtomicBoolean mTouch1 = new AtomicBoolean(false);
    private AtomicBoolean mTouch2 = new AtomicBoolean(false);

    private AtomicIntegerArray mTouchIndices = new AtomicIntegerArray(6);
    private AtomicInteger mPrevTouchLoc1 = new AtomicInteger(-1);
    private AtomicInteger mPrevTouchLoc2 = new AtomicInteger(-1);
    private AtomicInteger mDownLoc = new AtomicInteger(-1);
    private AtomicInteger mDownLoc1 = new AtomicInteger(-1);
    private AtomicInteger mDownLoc2 = new AtomicInteger(-1);
    private boolean mMoved1 = false;
    private boolean mMoved2 = false;
    private int yScale = 60;
    private int mThresh = 80;
    private float mImageScale = 1.0f;
    private long mStartTime = 0;
    private int mImageSize = 260;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zooming_activity);
        mMetricsManager = MetricsManager.getInstance();

        iv = (ImageView) findViewById(R.id.img);
        SGD = new ScaleGestureDetector(this, new ScaleListener());
        matrix = new Matrix();

        iv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                int action = MotionEventCompat.getActionMasked(ev);

                int pointers = ev.getPointerCount();
                int yindex2 = -1;
                int yindex1 = -1;
                final int pindex1 = ev.findPointerIndex(0);
                final int pindex2 = ev.findPointerIndex(1);

                if (pindex1 != -1) {
                    yindex1 = (int) ev.getAxisValue(MotionEvent.AXIS_X, pindex1);
                }
                if (pindex2 != -1) {
                    yindex2 = (int) ev.getAxisValue(MotionEvent.AXIS_Y, pindex2);
                }

                //System.out.println("Pointers: " + pointers);
                //System.out.println("mDownLoc1: " + mDownLoc1.get() + " yindex1: " + yindex1);
                //System.out.println("mDownLoc2: " + mDownLoc2.get() + " yindex2: " + yindex2);

                switch (action) {
                    case (MotionEvent.ACTION_POINTER_DOWN):
                    case (MotionEvent.ACTION_DOWN):
                       // System.out.println("Action DOWN");
                        if (pindex1 != -1) {
                            mDownLoc1.set(yindex1);
                            mPrevTouchLoc1.set(yindex1);
                            mMoved1 = false;
                        }
                        if (pindex2 != -1) {
                            mDownLoc2.set(yindex2);
                            mPrevTouchLoc2.set(yindex2);
                            mMoved2 = false;
                        }
                        return true;
                    case (MotionEvent.ACTION_MOVE):
                        //System.out.println("Action MOVE: " + yindex1 + ", " + yindex2);
                        if (pointers < 2) {
                            return true;
                        }

                        boolean top1 = true;
                        float factor = 1.0f;
                        if (yindex1 < yindex2) {
                            top1 = false;
                        }
                        float diff1 = ((float) mPrevTouchLoc1.get() - (float) yindex1);
                        float diff2 = ((float) mPrevTouchLoc2.get() - (float) yindex2);
                        if (diff1 != 0) {
                            mMoved1 = true;
                        }
                        if (diff2 != 0) {
                            mMoved2 = true;
                        }
                        // Ignore if not a pinch gesture
                        if ((diff1 > 0 && diff2 > 0) || (diff1 < 0 && diff2 < 0) || (diff1 == 0 && diff2 == 0)) {
                           // System.out.println("ignoring");
                            mPrevTouchLoc1.set(yindex1);
                            mPrevTouchLoc2.set(yindex2);
                            return true;
                        }
                        if (top1) {
                            // zoom out
                            if (diff1 >= 0 && diff2 <= 0) {
                                factor = -1.0f;
                            }
                        } else if (diff1 <= 0 && diff2 >= 0) {
                            factor = -1.0f;
                        }

                        float diff = factor * (Math.abs(diff1) + Math.abs(diff2));
                       // System.out.print("diff: " + diff);
                        float scale = mImageScale + (diff/ 60.f);
                        if (scale < 0) {
                            scale = 0.0f;
                        } else if (scale > 3) {
                            scale = 3.0f;
                        }
                        System.out.println("scale: " + scale + ", " + factor);
                        matrix.setScale(scale, scale);
                        mImageScale = scale;
                        iv.setImageMatrix(matrix);
                        mPrevTouchLoc1.set(yindex1);
                        mPrevTouchLoc2.set(yindex2);

                        return true;
                    case (MotionEvent.ACTION_POINTER_UP):
                    case (MotionEvent.ACTION_UP):
                        System.out.println("Action UP");
                        if ((((yindex1 != -1) && (yindex1 == mDownLoc1.get()) && !mMoved1)
                            || ((yindex2 != -1) && (yindex2 == mDownLoc2.get()) && !mMoved2))) {
                            //System.out.println("mDownLoc1: " + mDownLoc1.get() + " yindex1: " + yindex1);
                            //System.out.println("mDownLoc2: " + mDownLoc2.get() + " yindex2: " + yindex2);
                            // Record metrics
                            System.out.println("mImageScale: " + mImageScale);
                            //System.out.println("iv.width: " + iv.getWidth());
                            float acc = 1.0f - Math.abs((float) (360 - (mImageScale * mImageSize)) / 360.0f);
                            long time = System.currentTimeMillis() - mStartTime;
                            mMetricsManager.recordMetric("zooming", time, acc);
                            ZoomingActivity.this.finish();
                        } else {
                            if (pindex1 != -1) {
                                mDownLoc1.set(-1);
                                mPrevTouchLoc1.set(-1);
                                mMoved1 = false;
                            }
                            if (pindex2 != -1) {
                                mDownLoc2.set(-1);
                                mPrevTouchLoc2.set(-1);
                                mMoved2 = false;
                            }
                        }
                        return true;
                    case (MotionEvent.ACTION_CANCEL):
                        System.out.println("Action CANCEL or OUTSIDE");
                        if (pindex1 != -1) {
                            mDownLoc1.set(-1);
                            mPrevTouchLoc1.set(-1);
                        }
                        if (pindex2 != -1) {
                            mDownLoc2.set(-1);
                            mPrevTouchLoc2.set(-1);
                        }
                    case (MotionEvent.ACTION_OUTSIDE):
                        return true;
                    default:
                        System.out.println("DEFAULT");
                        return false;
                }
            }
        });

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
        mStartTime = System.currentTimeMillis();
        mRequestQueue.start();
    }

    @Override
    public void onDestroy() {
        mRequestQueue.stop();
        super.onDestroy();
    }

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(1.0f, Math.min(scale, 10.0f));
            System.out.println("scale: " + scale);
            mImageScale = scale;
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
        if ((eventType == MotionEvent.ACTION_DOWN) || (eventType == MotionEvent.ACTION_UP)) {
            motionEvent = MotionEvent.obtain(downTime, eventTime, eventType, 0, end, metaState);

            if (eventType == MotionEvent.ACTION_DOWN) {
                mDownLoc.set(end);
            } else if (eventType == MotionEvent.ACTION_UP) {
                if (end == mDownLoc.get()) {
                    // Record metrics
                    System.out.println("mImageScale: " + mImageScale);
                    System.out.println("iv.width: " + iv.getWidth());
                    float acc = 1.0f - Math.abs((float) (360 - (mImageScale * mImageSize)) / 360.0f);
                    long time = System.currentTimeMillis() - mStartTime;
                    mMetricsManager.recordMetric("zooming", time, acc);
                    ZoomingActivity.this.finish();
                }
                mDownLoc.set(-1);
            }
        } else if (eventType == MotionEvent.ACTION_MOVE) {
            float scale = mImageScale + (((float) start - (float) end) / 360.f);
            System.out.println("scale: " + scale);
            matrix.setScale(scale, scale);
            mImageScale = scale;
            iv.setImageMatrix(matrix);
        }

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
                                //System.out.println("arraystring: " + arrayString);
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

                                //int y = maxIndex;
                                //System.out.println("maxIndex1: " + maxIndex1); //+ ", maxIndex2: " + maxIndex2);
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
                                    //System.out.println("touchIndex1: " + touchIndex1 + "newTouchLoc1: " + newTouchLoc1);
                                }

                                // System.out.println("PrevTouchLoc1: " + mPrevTouchLoc1.get());
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
                mRequestQueue.add(createStringRequest());
            }
        });
        //stringRequest.setShouldCache(false);
        return stringRequest;
    }
}
