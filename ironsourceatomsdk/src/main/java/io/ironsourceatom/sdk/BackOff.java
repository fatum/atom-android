package io.ironsourceatom.sdk;

import android.content.Context;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Persistence exponential backoff service.
 */
class BackOff {

    private IsaPrefService prefService;
    private final String KEY_LAST_TICK = "retry_last_tick";
    private final String KEY_RETRY_COUNT = "retry_count";

    protected final int MAX_RETRY_COUNT = 8;
    private int retry;

    private static BackOff sInstance;

    BackOff(Context context) {
        prefService = getPrefService(context);
        retry = prefService.load(KEY_RETRY_COUNT, 0);
    }

    public static synchronized BackOff getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BackOff(context);
        }

        return sInstance;
    }

    /**
     * Calculates and returns the next milliseconds and advances the counter.
     * nextTick - the next clock tick that the function returns
     * scheduledNextTick - The last known nextTick (used to store the state)
     *
     * @return nextTick - next clock tick.
     */
    synchronized long next() {
        final long currentTime = currentTimeMillis();
        final long scheduledNextTick = prefService.load(KEY_LAST_TICK, 0L);

        // Increment counter only if last backoff expired
        if (currentTime > scheduledNextTick) {
            prefService.save(KEY_RETRY_COUNT, ++retry);
        }

        final long nextTick = currentTime + getMills(retry); // set the nextTick
        prefService.save(KEY_LAST_TICK, nextTick);
        return nextTick;
    }

    long getNextBackoffTime() {
        return prefService.load(KEY_LAST_TICK, 0L);
    }

    /**
     * Get milliseconds number based on the given n - jitter.
     *
     * @param n The retry counter
     * @return A value between (2 power n-1) and (2 power n)
     */
    private long getMills(int n) {
        final long basicBackoff = TimeUnit.MINUTES.toMillis((int) Math.pow(2, n));
        final long jitter = (long) (new Random().nextDouble() * (basicBackoff / 2));
        return basicBackoff - jitter;
    }

    /**
     * Reset number of retries to INITIAL_RETRY_VALUE, and
     * save current state in sharedPreferences.
     */
    void reset() {
        retry = 0;
        prefService.save(KEY_RETRY_COUNT, retry);
        prefService.delete(KEY_LAST_TICK);
    }

    public boolean hasNext() {
        return retry <= MAX_RETRY_COUNT;
    }

    /**
     * For testing purpose. to allow mocking this behavior.
     */
    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    protected IsaPrefService getPrefService(Context context) {
        return IsaPrefService.getInstance(context);
    }
}