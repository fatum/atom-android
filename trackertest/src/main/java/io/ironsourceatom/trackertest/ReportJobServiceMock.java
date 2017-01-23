package io.ironsourceatom.trackertest;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.content.ComponentName;
import android.os.Build;

import io.ironsourceatom.sdk.ReportHandler;
import io.ironsourceatom.sdk.ReportJobService;

/**
 * Created by g8y3e on 12/13/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ReportJobServiceMock extends ReportJobService {
    public static ReportHandler reportHandler;

    public static void setReportHandler(ReportHandler reportHandler) {
        ReportJobServiceMock.reportHandler = reportHandler;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = ReportJobServiceMock.reportHandler;
    }

    protected void createJob(long triggerMills) {
        long deltaTime = (triggerMills - backOff.currentTimeMillis());

        ComponentName serviceComponent = new ComponentName(this.getApplicationContext(), ReportJobServiceMock.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(deltaTime);
        builder.setOverrideDeadline(deltaTime);

        jobScheduler.schedule(builder.build());
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        jobParamsMap.add(params);

        ReportJobIntentMock report = new ReportJobIntentMock(this, SdkEvent.FLUSH_QUEUE);
        report.send();

        return true;
    }
}