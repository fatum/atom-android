package io.ironsourceatom.sdk;

import android.annotation.TargetApi;
import android.app.PendingIntent;
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

    private static HandlerThread handlerThread = new HandlerThread(TAG);

    static {
        handlerThread.start();
    }

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
        handlerLooper.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (handler.handleReport(intent) == ReportHandler.HandleStatus.RETRY &&
                            backOff.hasNext()) {
                        setJob(backOff.next());
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

    protected void setJob(long triggerMills) {
        Logger.log(TAG, "Setting alarm, Will send in: " + (triggerMills - backOff.currentTimeMillis()) + "ms", Logger.SDK_DEBUG);

        ComponentName serviceComponent = new ComponentName(this.getApplicationContext(), ReportJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setOverrideDeadline(triggerMills - backOff.currentTimeMillis());

        JobScheduler tm =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(builder.build());
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        // We don't do any real 'work' in this sample app. All we'll
        // do is track which jobs have landed on our service, and
        // update the UI accordingly.
        jobParamsMap.add(params);

        ReportJobIntent report = new ReportJobIntent(this, SdkEvent.FLUSH_QUEUE);
        report.send();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // Stop tracking these job parameters, as we've 'finished' executing.
        jobParamsMap.remove(params);

        return true;
    }

}
