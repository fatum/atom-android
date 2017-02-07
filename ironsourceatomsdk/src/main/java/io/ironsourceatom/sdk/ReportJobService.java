package io.ironsourceatom.sdk;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

/**
 * Created on 08/11/2016.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ReportJobService
        extends JobService {

    private static final String TAG = "ReportJobService";

    public static final int JOB_ID = 1700;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Logger.log(TAG, "Requesting ReportService to flush events", Logger.SDK_DEBUG);
        ReportService.sendReport(this, new ReportData(SdkEvent.FLUSH_QUEUE));

        // The only purpose of this job is to start the service - so our work is done
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        // Return true to retry completing the job
        return true;
    }
}
