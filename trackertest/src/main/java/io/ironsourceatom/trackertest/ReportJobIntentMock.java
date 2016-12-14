package io.ironsourceatom.trackertest;

import android.content.Context;
import android.content.Intent;

import io.ironsourceatom.sdk.ReportJobIntent;

/**
 * Created by g8y3e on 12/13/16.
 */
class ReportJobIntentMock extends ReportJobIntent {
    ReportJobIntentMock(Context context, int sdkEvent) {
        super(context, sdkEvent);
        intent = new Intent(context, ReportJobServiceMock.class);
        intent.putExtra(EXTRA_SDK_EVENT, sdkEvent);
    }
}