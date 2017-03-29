package edu.toronto.touchband.touchbandapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

    private static final String sUserIdKey = "user_id";
    private MetricsManager mMetricsManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

}
