package edu.toronto.touchband.touchbandapp;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Records and manages app metrics on a separate thread.
 */
public class MetricsManager {
    private final String mFilename = "touchband_metrics.csv";

    private Context mContext;
    private AtomicInteger mId = new AtomicInteger(0);
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private static MetricsManager mInstance;

    public static MetricsManager getInstance() {
        return mInstance;
    }

    private MetricsManager(Context context) {
        mContext = context;

        mHandlerThread = new HandlerThread("MetricsThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Create the data file if it doesn't exist yet
                File file = new File(mContext.getFilesDir(), mFilename);
                try {
                    if (file.exists()) {
                        // Get the last used id number
                        InputStream inputStream = mContext.openFileInput(mFilename);
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        String line = br.readLine();
                        while (line != null) {
                            System.out.println(line);
                            if (line.contains(",")) {
                                String idString = line.substring(0, line.indexOf(','));
                                if (!idString.equals("id")) {
                                    try {
                                        int id = Integer.parseInt(idString);
                                        if (id != -1) mId.set(id);
                                    } catch (NumberFormatException e) {
                                        System.err.println(e.getMessage());
                                    }
                                }
                            }
                            line = br.readLine();
                        }
                        inputStream.close();
                        System.out.println("Last used id: " + mId.get());

                    } else {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mContext.openFileOutput(mFilename, Context.MODE_PRIVATE));
                        outputStreamWriter.write("id,task,time,accuracy\n");
                        outputStreamWriter.close();
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        };

        mHandler.post(runnable);
    }

    public void shutdown() {
        try {
            mHandlerThread.quitSafely();
            mHandlerThread.join(1000);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void initInstance(Context context) {
        mInstance = new MetricsManager(context);
    }

    /**
     * Thread-safe
     * @return mId
     */
    public int getNewId() {
        return mId.incrementAndGet();
    }

    /**
     * Thread-safe
     * @return mId
     */
    public int getId() {
        return mId.get();
    }

    /**
     * Record a metric for a task
     * @param task the name of the task, i.e. "scrolling" or "selection"
     * @param time the time of the trial in ms
     * @param accuracy the accuracy
     */
    public void recordMetric(final String task, final long time, final double accuracy) {
        final int id = getId();
        System.out.println("Writing metric: " + getId() + ", " + task + ", " + time + ", " + accuracy);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Append metric to file
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mContext.openFileOutput(mFilename, Context.MODE_APPEND));
                    outputStreamWriter.write(id + "," + task + "," + time + "," + accuracy + "\n");
                    outputStreamWriter.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        };
        mHandler.post(runnable);
    }

    public void printMetrics() {
        System.out.println("Metrics:");
        // Get the last used id number
        try {
            InputStream inputStream = mContext.openFileInput(mFilename);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = br.readLine();
            while (line != null) {
                System.out.println(line);
                line = br.readLine();
            }
            inputStream.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
