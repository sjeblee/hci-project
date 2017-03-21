package edu.toronto.touchband.touchbandapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.wearable.activity.WearableActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends WearableActivity {

    private final String mUrl = "http://www.yahoo.com";
    private static final String sUserIdKey = "user_id";
    private RequestQueue mRequestQueue;
    private MetricsManager mMetricsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context mContext = this;

        // Start the HTTP request queue
        mRequestQueue = Volley.newRequestQueue(mContext);
        StringRequest stringRequest = createStringRequest();
        mRequestQueue.add(stringRequest);

        MetricsManager.initInstance(this);
        mMetricsManager = MetricsManager.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView textview = (TextView) findViewById(R.id.userid);
        textview.setText("Thank you, your id is " + mMetricsManager.getId());
    }

    @Override
    public void onDestroy() {
        mMetricsManager.shutdown();
        super.onDestroy();
    }

    public void startNewSession(View view){
        // Generate a new participant id
        int id = mMetricsManager.getNewId();
        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra(sUserIdKey, id);
        startActivity(intent);
    }

    public void generateTouch(int startx, int starty, int endx, int endy) {
        // TODO: generate a touch event on the main thread
        //System.out.println("Generated touch event (not really)");

        // TODO: get the current view
        View view = getWindow().getCurrentFocus();

        // Obtain MotionEvent object
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = 0.0f;
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        int metaState = 0;
        MotionEvent motionEvent = null;
        if ((startx == endx) && (starty == endy)) {
            motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, metaState);
        } else {
            motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y, metaState);
        }


        // Dispatch touch event to view
        //view.dispatchTouchEvent(motionEvent);
    }

    /**
     * Create a new HTTP GET request that generates another request on the successful completion of this one
     * @return the StringRequest
     */
    public StringRequest createStringRequest() {
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, mUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //System.err.println("Response is: " + response);
                        // TODO: get touch info from the response
                        // TODO: figure out if an up, down, or move event has happened
                        generateTouch(0, 0, 0, 0);

                        // Send the next request
                        mRequestQueue.add(createStringRequest());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("Error from Volley GET request!");
            }
        });
        return stringRequest;
    }
}
