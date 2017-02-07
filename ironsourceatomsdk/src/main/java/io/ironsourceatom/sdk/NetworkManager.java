package io.ironsourceatom.sdk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * A connectivity utility class for internal use in this library.
 */
public class NetworkManager {

    private static final String TAG = "NetworkManager";

    private static NetworkManager sInstance;

    private Context context;

    NetworkManager(Context context) {
        this.context = context;
    }

    public static synchronized NetworkManager getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new NetworkManager(context);
        }

        return sInstance;
    }

    /**
     * Detect whether there's an Internet connection available.
     *
     * @return boolean
     */
    public boolean isOnline() {
        boolean isOnline;
        try {
            final NetworkInfo netInfo = getNetworkInfo();
            isOnline = netInfo != null && netInfo.isConnected();
        } catch (final SecurityException e) {
            isOnline = true;
        }
        return isOnline;
    }

    /**
     * Return a human-readable name describe the type of the network.
     *
     * @return String
     */
    public String getConnectedNetworkType() {
        NetworkInfo info = getNetworkInfo();
        return info != null && info.isConnected() ? info.getTypeName() : "unknown";
    }

    /**
     * Indicates whether the device is currently roaming on this network.
     *
     * @return boolean
     */
    public boolean isDataRoamingEnabled() {
        NetworkInfo info = getNetworkInfo();
        return info != null && info.isRoaming();
    }

    /**
     * Get IronSourceAtomFactory network type based on the returned connectivity
     * network type.
     *
     * @return int
     */
    public int getNetworkAtomType() {
        NetworkInfo info = getNetworkInfo();
        int networkType = info != null ? info.getType() : 0;
        switch (networkType) {
            case ConnectivityManager.TYPE_MOBILE:
                return IronSourceAtomFactory.NETWORK_MOBILE;
            case ConnectivityManager.TYPE_WIFI:
                return IronSourceAtomFactory.NETWORK_WIFI;
            default:
                return 0;
        }
    }

    private NetworkInfo getNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }
}
