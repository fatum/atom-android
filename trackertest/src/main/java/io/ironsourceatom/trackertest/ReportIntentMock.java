package io.ironsourceatom.trackertest;

import android.content.Context;
import android.content.Intent;

import io.ironsourceatom.sdk.ReportIntent;

/**
 * Created by valentine.pavchuk on 12/13/16.
 */

public class ReportIntentMock extends ReportIntent {
    ReportIntentMock(Context context, int sdkEvent) {
        super(context, sdkEvent);
        intent = new Intent(context, ReportServiceMock.class);
        intent.putExtra(EXTRA_SDK_EVENT, sdkEvent);
    }
}