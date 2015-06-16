package net.waynepiekarski.displaylockwear;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class DataItemListenerService extends WearableListenerService implements DataApi.DataListener {

    void setLockState(boolean state) {
        if (state) {
            Log.d(Const.TAG, "Locking display with addFlags FLAG_KEEP_SCREEN_ON");
        } else {
            Log.d(Const.TAG, "Unlocking display with clearFlags FLAG_KEEP_SCREEN_ON");
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
