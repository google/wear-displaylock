package net.waynepiekarski.displaylockwear;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class DataItemListenerService extends WearableListenerService implements DataApi.DataListener {

    PowerManager mPowerManager;
    PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, Const.TAG);
    }

    private void setLockState(boolean state) {
        if (state) {
            Log.d(Const.TAG, "Locking display with power manager SCREEN_DIM_WAKE_LOCK");
            mWakeLock.acquire();
        } else {
            Log.d(Const.TAG, "Unlocking display with power manager SCREEN_DIM_WAKE_LOCK");
            mWakeLock.release();
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(Const.TAG, "onDataChanged()");
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();
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
        }
    }
}
