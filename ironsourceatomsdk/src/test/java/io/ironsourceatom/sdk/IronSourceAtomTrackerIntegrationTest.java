package io.ironsourceatom.sdk;

/**
 * Created by g8y3e on 12/13/16.
 *
 * @RunWith(AndroidJUnit4.class)
 * @SmallTest //@Config(constants = BuildConfig.class, emulateSdk = 21, manifest = Config.NONE)
 * public class IronSourceAtomTrackerIntegrationTest {
 * final TestsUtils.MockPoster httpClient = new TestsUtils.MockPoster();
 * <p>
 * final IsaConfig isaConfig = IsaConfig.getInstance(InstrumentationRegistry.getTargetContext());
 * final StorageService dbAdapter = new DbAdapter(InstrumentationRegistry.getTargetContext());
 * final ReportHandler reportHandler = new ReportHandler(InstrumentationRegistry.getTargetContext()) {
 * @Override protected StorageService getStorage(Context context) {
 * return dbAdapter;
 * }
 * @Override protected RemoteService getHttpClient() {
 * return httpClient;
 * }
 * };
 * <p>
 * final String authKey = "";
 * <p>
 * class ReportJobServiceMock extends ReportJobService {
 * /* @Override
 * public Context getApplicationContext() {
 * return InstrumentationRegistry.getTargetContext();
 * }
 * @Override public void onCreate() {
 * super.onCreate();
 * <p>
 * reportService = reportHandler;
 * }
 * };
 * <p>
 * class ReportJobIntentMock extends ReportJobIntent {
 * ReportJobIntentMock(Context context, int sdkEvent) {
 * super(context, sdkEvent);
 * intent = new Intent(context, ReportJobServiceMock.class);
 * intent.putExtra(EXTRA_SDK_EVENT, sdkEvent);
 * }
 * }
 * <p>
 * final IronSourceAtomTracker tracker = new IronSourceAtomTracker(InstrumentationRegistry.getTargetContext(), authKey) {
 * @Override protected Report newReport(Context context, int event_code) {
 * int currentApiVersion = android.os.Build.VERSION.SDK_INT;
 * <p>
 * if (currentApiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
 * return new ReportJobIntentMock(context, event_code);
 * } else {
 * return new ReportIntent(context, event_code);
 * }
 * }
 * };
 * @Rule public final ServiceTestRule mServiceRule = new ServiceTestRule();
 * @Before public void reset() {
 * httpClient.mBackedMock.clear();
 * }
 * @Test public void trackError() {
 * tracker.track("test", "{\"data\": 1}", true);
 * }
 * }
 */
