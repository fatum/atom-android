package io.ironsourceatom.sdk;

import android.content.Context;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BackOffTest {


	private long         currentMills = 0L;
	final   Context      context      = mock(MockContext.class);
	final   IsaPrefUtils sharedPref   = mock(IsaPrefUtils.class);
	BackOff backOff = new BackOff(context) {
		@Override
		protected IsaPrefUtils getPrefService(Context context) { return sharedPref; }

		@Override
		protected long currentTimeMillis() { return currentMills; }
	};

	@Before
	public void clearMocks() {
		reset(sharedPref);
	}

	@Test
	public void testHasNext() {
		assertEquals(backOff.hasNext(), true);
	}

	@Test
	public void testNext() {
		backOff.reset();
		when(sharedPref.load(anyString(), anyLong())).thenReturn(0L);
		long t1 = backOff.next();
		currentMills = t1;
		when(sharedPref.load(anyString(), anyLong())).thenReturn(t1);
		long t2 = backOff.next();
		currentMills = t2;
		when(sharedPref.load(anyString(), anyLong())).thenReturn(t2);
		long t3 = backOff.next();
		assertTrue(t1 < t2 && t2 < t3);
		verify(sharedPref, times(1)).save(anyString(), eq(t1));
		verify(sharedPref, times(1)).save(anyString(), eq(t2));
		verify(sharedPref, times(1)).save(anyString(), eq(t3));
		when(sharedPref.load(anyString(), anyLong())).thenReturn(t3);
	}

	@Test
	public void testReset() {
		backOff.reset();
		verify(sharedPref, times(1)).save(anyString(), eq(0));
		verify(sharedPref, times(1)).delete(anyString());
	}

	@Test
	public void getInstanceTest() {
		BackOff backOff1 = BackOff.getInstance(context);
		BackOff backOff2 = BackOff.getInstance(context);
		assertTrue(backOff1 == backOff2);

	}
}