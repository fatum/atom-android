package io.ironsourceatom.sdk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith(MockitoJUnitRunner.class)
public class ReportDataTest {

	@Test
	public void testSetters() {
		Report report = new Report();
		report.setEndpoint("bla");
		report.setTable("bla");
		report.setToken("bla");
		report.setData("bla");
		report.setBulk(true);
	}

	@Test
	public void testSend() {
		Report report = new Report();
		//report.send();
	}

	@Test
	public void testConstructor() {
		Report report = new Report();
		assertNotNull(report);
	}
}

