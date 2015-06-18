package net.waynepiekarski.displaylockwear;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

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
    boolean mLocked;
    Handler uiThreadHandler;

    @Override
    public void onCreate() {
        Log.d(Const.TAG_SERVICE, "onCreate()");
        super.onCreate();
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mLocked = false;
        uiThreadHandler = new Handler();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        Log.d(Const.TAG_SERVICE, "onDestroy()");
    }

    private void setLockState(boolean state) {
        Log.d(Const.TAG_SERVICE, "setLockState(state=" + state + ") and mLocked=" + mLocked + " mWakeLock=" + mWakeLock);
        if (state == mLocked) {
            Log.d(Const.TAG_SERVICE, "Skipping setLockState since state=" + state + " and mLocked=" + mLocked + " are the same");
        } else if (state) {
            Log.d(Const.TAG_SERVICE, "Locking display with power manager SCREEN_BRIGHT_WAKE_LOCK");
            mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, Const.TAG_SERVICE);
            mWakeLock.acquire();
            mLocked = true;
            showToastUiThread(Const.TAG_MISC + ": Locking display on");
        } else {
            Log.d(Const.TAG_SERVICE, "Unlocking display with power manager SCREEN_BRIGHT_WAKE_LOCK");
            mWakeLock.release();
            mWakeLock = null;
            mLocked = false;
            showToastUiThread(Const.TAG_MISC + ": Release display to ambient");
        }
    }

    // Toast messages must be done from the UI thread or you get a RuntimeException
    private void showToastUiThread(final String message) {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DataItemListenerService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void missingDataItem() {
        Log.d(Const.TAG_SERVICE, "missingDataItem() doing nothing inside service, there is no need to do anything");
    }

    public void processDataItem(DataItem dataItem) {
        DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
        String path = dataItem.getUri().getPath();
        if (path.equals(Const.LOCK_PATH)) {
            long timestamp = dataMap.getLong("timestamp");
            boolean state = dataMap.getBoolean("state");
            if (Const.DEVICE == Const.PHONE) {
                Log.d(Const.TAG_SERVICE, "Ignoring data item on phone for path " + path + " with state=" + state + ", timestamp=" + timestamp);
            } else {
                Log.d(Const.TAG_SERVICE, "Received data item for path " + path + " with state=" + state + ", timestamp=" + timestamp);
                setLockState(state);
            }
        } else {
            Log.d(Const.TAG_SERVICE, "Ignoring data item update for path " + path);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(Const.TAG_SERVICE, "onDataChanged() with " + dataEvents.getCount() + " events");
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                processDataItem(dataItem);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int status) {
        Log.d(Const.TAG_SERVICE, "onConnectionSuspended(): " + status);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(Const.TAG_SERVICE, "onConnectionFailed(): Failed to connect to Google Play Services " + result);
        throw new RuntimeException("Play Services failed");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(Const.TAG_SERVICE, "onConnected(): Successful connection to Google Play Services");
        Wearable.DataApi.addListener(mGoogleApiClient, this);

        // Retrieve the latest data item from any source, calls processDataItem when done
        Log.d(Const.TAG_SERVICE, "Requesting list of existing data items to sync up to the current state");
        GetFirstDataItem.callProcessDataItem(mGoogleApiClient, Const.LOCK_PATH, this);
    }
}
