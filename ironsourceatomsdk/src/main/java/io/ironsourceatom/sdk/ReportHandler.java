package io.ironsourceatom.sdk;

import io.ironsourceatom.sdk.StorageService.*;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.ceil;

/**
 * Class responsible for tracker logic
 */
class ReportHandler {


    enum SendStatus {SUCCESS, DELETE, RETRY}

    enum HandleStatus {HANDLED, RETRY}

    private static final String TAG = "ReportHandler";
    private NetworkManager networkManager;
    private StorageService storage;
    private RemoteService client;
    private IsaConfig config;

    public ReportHandler(Context context) {
        client = getClient();
        config = getConfig(context);
        storage = getStorage(context);
        networkManager = getNetManager(context);
    }

    /**
     * handleReport responsible to handle the given ReportIntent based on the
     * event-type(that could be one of the 3: FLUSH, ENQUEUE or POST_SYNC).
     *
     * @param intent
     * @return result of the handleReport if success true or failed false
     */
    public synchronized HandleStatus handleReport(Intent intent) {
        HandleStatus status = HandleStatus.HANDLED;
        boolean isOnline = networkManager.isOnline() && canUseNetwork();
        try {
            if (null == intent.getExtras()) return status;
            int sdkEvent = intent.getIntExtra(ReportIntent.EXTRA_SDK_EVENT, SdkEvent.ERROR);
            Bundle bundle = intent.getExtras();
            JSONObject dataObject = new JSONObject();
            try {
                String[] fields = {ReportIntent.TABLE, ReportIntent.TOKEN, ReportIntent.DATA};
                for (String key : fields) {
                    Object value = bundle.get(key);
                    dataObject.put(key, value);
                }
            } catch (Exception e) {
                Logger.log(TAG, "Failed extracting the data from Intent", Logger.SDK_DEBUG);
            }
            List<Table> tablesToFlush = new ArrayList<>();
            switch (sdkEvent) {
                case SdkEvent.FLUSH_QUEUE:
                    if (isOnline) {
                        tablesToFlush = storage.getTables();
                        break;
                    }
                    return HandleStatus.RETRY;
                case SdkEvent.POST_SYNC:
                case SdkEvent.REPORT_ERROR:
                    if (isOnline) {
                        String message = createMessage(dataObject, false);
                        String url = config.getAtomEndPoint(dataObject.getString(ReportIntent.TOKEN));
                        if (send(message, url) != SendStatus.RETRY || sdkEvent == SdkEvent.REPORT_ERROR)
                            break;
                    }
                case SdkEvent.ENQUEUE:
                    Table table = new Table(dataObject.getString(ReportIntent.TABLE),
                            dataObject.getString(ReportIntent.TOKEN));
                    int nRows = storage.addEvent(table, dataObject.getString(ReportIntent.DATA));
                    if (isOnline && config.getBulkSize() <= nRows) {
                        tablesToFlush.add(table);
                    } else {
                        return HandleStatus.RETRY;
                    }
            }
            // If there's something to flush, it'll not be empty.
            for (Table table : tablesToFlush) flush(table);
        } catch (Exception e) {
            status = HandleStatus.RETRY;
            Logger.log(TAG, e.getMessage(), Logger.SDK_DEBUG);
        }
        return status;
    }

    /**
     * First, we peek the batch the fits with the `MaximumRequestLimit`
     * after that we prepare the request and send it.
     * if the send failed, we stop here, and "continue later".
     * if everything goes-well, we do it recursively until wil drain and
     * delete the table.
     *
     * @param table
     * @throws Exception
     */
    public void flush(Table table) throws Exception {
        int bulkSize = config.getBulkSize();
        Batch batch;
        while (true) {
            batch = storage.getEvents(table, bulkSize);
            if (batch != null && batch.events.size() > 1) {
                int byteSize = batch.events.toString().getBytes("UTF-8").length;
                if (byteSize <= config.getMaximumRequestLimit()) break;
                bulkSize = (int) (bulkSize / ceil(byteSize / config.getMaximumRequestLimit()));
            } else break;
        }
        if (batch != null) {
            JSONObject event = new JSONObject();
            event.put(ReportIntent.TABLE, table.name);
            event.put(ReportIntent.TOKEN, table.token);
            event.put(ReportIntent.DATA, batch.events.toString());
            SendStatus res = send(createMessage(event, true), config.getAtomBulkEndPoint(table.token));
            if (res == SendStatus.RETRY) {
                throw new Exception("Failed flush entries for table: " + table.name);
            }
            if (storage.deleteEvents(table, batch.lastId) < bulkSize || storage.count(table) == 0) {
                storage.deleteTable(table);
            } else {
                flush(table);
            }
        }
    }

    /**
     * Prepare the giving object before sending it to IronSourceAtom(Do auth, etc..)
     *
     * @param obj  - the given event to working on.
     * @param bulk - indicate if it need to add a bulk field.
     * @return
     */
    private String createMessage(JSONObject obj, boolean bulk) {
        String message = "";
        try {
            JSONObject clone = new JSONObject(obj.toString());
            String data = clone.getString(ReportIntent.DATA);
            if (!clone.getString(ReportIntent.TOKEN).isEmpty()) {
                clone.put(ReportIntent.AUTH,
                        Utils.auth(data, (String) clone.remove(ReportIntent.TOKEN)));
            } else {
                clone.remove(ReportIntent.TOKEN);
            }
            if (bulk) {
                clone.put(ReportIntent.BULK, true);
            }
            message = clone.toString();
        } catch (Exception e) {
            Logger.log(TAG, "Failed to create message" + e, Logger.SDK_DEBUG);
        }
        return message;
    }

    /**
     * @param data - Stringified JSON. used as a request body.
     * @param url  - IronSourceAtomFactory url endpoint.
     * @return sendStatus ENUM that indicate what to do later on.
     */
    protected SendStatus send(String data, String url) {
        int nRetry = config.getNumOfRetries();
        Logger.log(TAG, "Tracking data:" + data, Logger.SDK_DEBUG);
        while (nRetry-- > 0) {
            try {
                RemoteService.Response response = client.post(data, url);
                if (response.code == HttpURLConnection.HTTP_OK) {
                    Logger.log(TAG, "Server Response Status: " + response.code, Logger.SDK_DEBUG);
                    return SendStatus.SUCCESS;
                }
                if (response.code >= HttpURLConnection.HTTP_BAD_REQUEST &&
                        response.code < HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    Logger.log(TAG, "Server Response Status: " + response.code, Logger.SDK_DEBUG);
                    return SendStatus.DELETE;
                }
            } catch (SocketTimeoutException | UnknownHostException | SocketException e) {
                Logger.log(TAG, "Connectivity error: " + e, Logger.SDK_DEBUG);
            } catch (IOException e) {
                Logger.log(TAG, "Service IronSourceAtomFactory is unavailable: " + e, Logger.SDK_DEBUG);
            }
        }
        return SendStatus.RETRY;
    }

    /**
     * Test if the handler can use the network.
     *
     * @return
     */
    private boolean canUseNetwork() {
        if ((config.getAllowedNetworkTypes() & networkManager.getNetworkAtomType()) == 0) {
            return false;
        }
        return config.isAllowedOverRoaming() || !networkManager.isDataRoamingEnabled();
    }

    /**
     * For testing purpose. to allow mocking this behavior.
     */
    protected RemoteService getClient() {
        return HttpClient.getInstance();
    }

    protected IsaConfig getConfig(Context context) {
        return IsaConfig.getInstance(context);
    }

    protected StorageService getStorage(Context context) {
        return DbAdapter.getInstance(context);
    }

    protected NetworkManager getNetManager(Context context) {
        return NetworkManager.getInstance(context);
    }

}
