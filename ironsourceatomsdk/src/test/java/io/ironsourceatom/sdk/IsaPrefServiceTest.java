package io.ironsourceatom.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IsaPrefServiceTest {

	final Context context = mock(MockContext.class);
	private IsaPrefService service;

	@Before
	public void setUp() {
		try {
			Field field = IsaPrefService.class.getDeclaredField("sInstance");
			field.setAccessible(true);
			field.set(null, null);
		} catch (Exception ex) {
		}
	}

	@Test
	public void testLoadIntDef() {
		service = IsaPrefService.getInstance(context);
		assertEquals(service.load("test key", 12), 12);

	}

	@Test
	public void testLoadLongDef() {
		service = IsaPrefService.getInstance(context);
		assertEquals(service.load("test key", 12L), 12L);
	}

	@Test
	public void testLoadIntException() {
		SharedPreferences sharedPrefs = mock(SharedPreferences.class);
		when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
		when(sharedPrefs.getString(anyString(), anyString())).thenReturn("test return");

		service = IsaPrefService.getInstance(context);
		assertEquals(service.load("test key", 12), 12);
	}

	@Test
	public void testLoadLongException() {
		SharedPreferences sharedPrefs = mock(SharedPreferences.class);
		when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
		when(sharedPrefs.getString(anyString(), anyString())).thenReturn("test return");

		service = IsaPrefService.getInstance(context);
		assertEquals(service.load("test key", 12L), 12L);
	}

	@Test
	public void testDeleteFalse() {
		service = IsaPrefService.getInstance(context);
		assertEquals(service.delete("test key"), false);
	}

	@Test
	public void testDeleteTrue() {
		SharedPreferences sharedPrefs = mock(SharedPreferences.class);

		SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);
		when(sharedPrefs.edit()).thenReturn(editor);
		when(editor.remove(anyString())).thenReturn(editor);
		when(editor.commit()).thenReturn(true);

		when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);

		service = IsaPrefService.getInstance(context);
		assertEquals(service.delete("test key"), true);
	}
}
