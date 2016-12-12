package io.ironsourceatom.sdk;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.LinkedList;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ReportJobService extends JobService {
    private static final String TAG = "ReportJobService";

    private JobScheduler jobScheduler;
    private ReportHandler handler;
    private BackOff backOff;

    private final LinkedList<JobParameters> jobParamsMap = new LinkedList<JobParameters>();

    // Handler thread - used to start a thread with a Looper (to run msgs in a queue)
    private static HandlerThread handlerThread = new HandlerThread(TAG);

    static {
        handlerThread.start();
    }

    // Handler thread to process Runnables from the looper
    private Handler handlerLooper = new Handler(handlerThread.getLooper());

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = this.getApplicationContext();
        jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        handler = new ReportHandler(context);
        backOff = BackOff.getInstance(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        // called on intent.startService(intent) at ReportJobIntent
        handlerLooper.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (handler.handleReport(intent) == ReportHandler.HandleStatus.RETRY &&
                            backOff.hasNext()) {
                        createJob(backOff.next());
                    } else {
                        backOff.reset();
                    }
                } catch (Throwable th) {
                    Logger.log(TAG, "failed to handle intent: " + th, Logger.SDK_ERROR);
                }
            }
        });

        return START_NOT_STICKY;
    }

    /**
     * Create a job at the JobScheduler class,
     *
     * @param triggerMills time to be executed in
     */
    protected void createJob(long triggerMills) {
        Logger.log(TAG, "Setting alarm, Will send in: " + (triggerMills - backOff.currentTimeMillis()) + "ms", Logger.SDK_DEBUG);

        long deltaTime = (triggerMills - backOff.currentTimeMillis());

        ComponentName serviceComponent = new ComponentName(this.getApplicationContext(), ReportJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(deltaTime);
        builder.setOverrideDeadline(deltaTime);

        jobScheduler.schedule(builder.build());
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        jobParamsMap.add(params);

        ReportJobIntent report = new ReportJobIntent(this, SdkEvent.FLUSH_QUEUE);
        report.send();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobParamsMap.remove(params);

        return true;
    }

}
