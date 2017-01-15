package io.ironsourceatom.sdk;

import android.content.Context;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Persistent exponential backoff service.
 */
public class BackOff {

	private static BackOff sInstance;

	private final String KEY_LAST_TICK   = "retry_last_tick";
	private final String KEY_RETRY_COUNT = "retry_count";

	protected final int MAX_RETRY_COUNT     = 8;
	protected final int INITIAL_RETRY_VALUE = 0;

	private int            retry;
	private IsaConfig      config;
	private IsaPrefService prefService;

	BackOff(Context context) {
		config = getConfig(context);
		prefService = getPrefService(context);
		retry = prefService.load(KEY_RETRY_COUNT, INITIAL_RETRY_VALUE);
	}

	public static synchronized BackOff getInstance(Context context) {
		if (null == sInstance) {
			sInstance = new BackOff(context);
		}

		return sInstance;
	}

	/**
	 * Calculates and returns the next retry time in milliseconds and advances the counter.
	 * nextTick - the next clock tick that the function returns
	 * lastTick - The last known tick (used to store the state)
	 *
	 * @return nextTick - next clock tick for backoff service
	 */
	synchronized long next() {
		long nextTick, currentTime = currentTimeMillis();
		long lastTick = prefService.load(KEY_LAST_TICK, currentTime);
		long temp = getMills(retry);

		nextTick = currentTime + temp; // set the nextTick
		if (currentTime > lastTick) {
			prefService.save(KEY_RETRY_COUNT, ++retry);
		}
		prefService.save(KEY_LAST_TICK, nextTick);
		return nextTick;
	}

	/**
	 * Get milliseconds number based on the given n (retry number)
	 *
	 * @param n retry number
	 * @return new retry time in milliseconds
	 */
	private long getMills(int n) {
		if (n <= INITIAL_RETRY_VALUE) {
			return config.getFlushInterval();
		}
		return (long) (new Random().nextDouble() * TimeUnit.MINUTES.toMillis((int) Math.pow(2, n) - 1));
	}

	/**
	 * Reset number of retries to INITIAL_RETRY_VALUE, and
	 * save current state in sharedPreferences.
	 */
	void reset() {
		retry = INITIAL_RETRY_VALUE;
		prefService.save(KEY_RETRY_COUNT, retry);
		prefService.delete(KEY_LAST_TICK);
	}

	public boolean hasNext() {
		return retry <= MAX_RETRY_COUNT;
	}

	/**
	 * For testing purpose. to allow mocking this behavior.
	 */
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	protected IsaPrefService getPrefService(Context context) {
		return IsaPrefService.getInstance(context);
	}

	protected IsaConfig getConfig(Context context) {
		return IsaConfig.getInstance(context);
	}

}