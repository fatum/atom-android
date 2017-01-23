package io.ironsourceatom.trackertest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import io.ironsourceatom.sdk.ReportHandler;
import io.ironsourceatom.sdk.ReportIntent;
import io.ironsourceatom.sdk.ReportService;

/**
 * Created by valentine.pavchuk on 12/13/16.
 */

public class ReportServiceMock extends ReportService {
    public static ReportHandler reportHandler;

    public static void setReportHandler(ReportHandler reportHandler) {
        ReportJobServiceMock.reportHandler = reportHandler;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = ReportJobServiceMock.reportHandler;
    }

    @Override
    protected void setAlarm(long triggerMills) {
        ReportIntent report = new ReportIntentMock(this, SdkEvent.FLUSH_QUEUE);
        PendingIntent intent = PendingIntent.getService(this, 0, report.getIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(intent);
        alarmManager.set(AlarmManager.RTC, triggerMills, intent);
    }
}
