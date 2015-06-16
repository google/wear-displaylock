package net.waynepiekarski.displaylockwear;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class DataItemListenerService
        extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, GetFirstDataItem.ProcessDataItemAble
{
    PowerManager mPowerManager;
    PowerManager.WakeLock mWakeLock;
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, Const.TAG);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    private void setLockState(boolean state) {
        if (state) {
            Log.d(Const.TAG, "Locking display with power manager SCREEN_BRIGHT_WAKE_LOCK");
            mWakeLock.acquire();
        } else {
            Log.d(Const.TAG, "Unlocking display with power manager SCREEN_BRIGHT_WAKE_LOCK");
            mWakeLock.release();
        }
    }

    public void processDataItem(DataItem dataItem) {
        DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
        String path = dataItem.getUri().getPath();
        if (path.equals(Const.LOCK_PATH)) {
            long timestamp = dataMap.getLong("timestamp");
            boolean state = dataMap.getBoolean("state");
            if (Const.DEVICE == Const.PHONE) {
                Log.d(Const.TAG, "Ignoring data item on phone for path " + path + " with state=" + state + ", timestamp=" + timestamp);
            } else {
                Log.d(Const.TAG, "Received data item for path " + path + " with state=" + state + ", timestamp=" + timestamp);
                setLockState(state);
            }
        } else {
            Log.d(Const.TAG, "Ignoring data item update for path " + path);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(Const.TAG, "onDataChanged()");
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                processDataItem(dataItem);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int status) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(Const.TAG, "Failed to connect to Google Play Services " + result);
        throw new RuntimeException("Play Services failed");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(Const.TAG, "Successful connection to Google Play Services");
        Wearable.DataApi.addListener(mGoogleApiClient, this);

        // Retrieve the latest data item from any source, calls processDataItem when done
        GetFirstDataItem.callProcessDataItem(mGoogleApiClient, Const.LOCK_PATH, this);
    }
}
