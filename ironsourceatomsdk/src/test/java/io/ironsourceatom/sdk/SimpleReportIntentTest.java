package io.ironsourceatom.sdk;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class SimpleReportIntentTest {


    @Mock
    Context context;


    private SimpleReportIntent report;


    @Test
    public void testSetEndPoint(){

        report = new SimpleReportIntent(context);
        report.setEndPoint("bla");
        report.setData("bla");
        report.setTable("bla");
        report.setToken("bla");
        report.setBulk(true);
        assertNull(report.getIntent().getExtras());

    }

}
