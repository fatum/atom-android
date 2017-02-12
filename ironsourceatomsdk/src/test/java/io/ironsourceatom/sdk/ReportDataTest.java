package io.ironsourceatom.sdk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith(MockitoJUnitRunner.class)
public class ReportDataTest {

	@Test
	public void testSetters() {
		ReportData report = new ReportData();
		report.setEndPoint("bla");
		report.setTable("bla");
		report.setToken("bla");
		report.setData("bla");
		report.setBulk(true);
	}

	@Test
	public void testSend() {
		ReportData report = new ReportData();
		//report.send();
	}

	@Test
	public void testConstructor() {
		ReportData report = new ReportData();
		assertNotNull(report);
	}
}

