package net.waynepiekarski.displaylockwear;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

public class LockService extends Service
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, GetFirstDataItem.ProcessDataItemAble
{
    PowerManager mPowerManager;
    PowerManager.WakeLock mWakeLock;
    GoogleApiClient mGoogleApiClient;
    boolean mLocked;
    Handler uiThreadHandler;
    boolean mPermanentService = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Const.TAG_SERVICE, "LockService.onStartCommand() with start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly stopped, so return sticky
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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

        // If we are created from BIND_LISTENER, then the LockService is not running permanently. So we need
        // to start it up manually once here. This will also mark it as started, so that when the device
        // reboots, the service will immediately start and set the correct state without a data item change
        if (!mPermanentService) {
            Log.d(Const.TAG_SERVICE, "mPermanentService is not set, so starting LockService to ensure it keeps running");
            Intent startServiceIntent = new Intent(this, LockService.class);
            startService(startServiceIntent);
            mPermanentService = true;
        }
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
                Toast.makeText(LockService.this, message, Toast.LENGTH_SHORT).show();
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
            Log.d(Const.TAG_SERVICE, "Received data item for path " + path + " with state=" + state + ", timestamp=" + timestamp);
            setLockState(state);
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
