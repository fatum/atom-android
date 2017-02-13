package io.ironsourceatom.sdk;

public class SdkEvent {

	public static final int ENQUEUE      = 0;  // save report
	public static final int FLUSH_QUEUE  = 1;  // send all reports from storage
	public static final int POST_SYNC    = 2;  // send report immediately
	public static final int ERROR        = 10; // error on report
	public static final int REPORT_ERROR = 12; // report and error to sdk error stream
}
