package io.ironsourceatom.sdk;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IronSourceAtomTest {
    private final Context context = mock(Context.class);
    private final String auth = "hdhdhhd";
    private final IronSourceAtom atom = mock(IronSourceAtom.class);
    private SimpleReportIntent reportIntent = mock(SimpleReportIntent.class);


    @Test
    public void testConstructor() {
        IronSourceAtom myAtom = new IronSourceAtom(context, auth);
        assertNotNull(myAtom);

    }

    @Test
    public void testPutEvent() {
        IronSourceAtom myAtom = new IronSourceAtom(context, auth);
        myAtom.putEvent("hfhhf", "djdjdj");

    }

    @Test
    public void testPutEvents() {
        IronSourceAtom myAtom = new IronSourceAtom(context, auth);
        myAtom.putEvents("hfhhf", "djdjdj");

    }

    @Test
    public void testOpenReport() {
        IronSourceAtom myAtom = new IronSourceAtom(context, auth);
        myAtom.openReport(context);
        Report report = myAtom.openReport(context);
        report.setData("data");
        report.setBulk(true);
        report.setEndPoint("data");
        report.setTable("data");
        report.setToken("data");


    }

    @Test(expected = IllegalArgumentException.class)
    public void setEndpointNullTest() {
        IronSourceAtom myAtom = new IronSourceAtom(context, auth);
        myAtom.setEndPoint(null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void setEndpointEmptyTest() {
        IronSourceAtom myAtom = new IronSourceAtom(context, auth);
        myAtom.setEndPoint("");

    }

}

