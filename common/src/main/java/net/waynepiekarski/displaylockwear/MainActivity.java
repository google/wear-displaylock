package net.waynepiekarski.displaylockwear;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

public class MainActivity
        extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, GetFirstDataItem.ProcessDataItemAble
{
    private GoogleApiClient mGoogleApiClient;
    private Handler mHandler;
    TextView mLockState;
    Button mLockButton;
    Button mUnlockButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        mLockState = (TextView)findViewById(R.id.lockState);
        mLockButton = (Button)findViewById(R.id.lock);
        mUnlockButton = (Button)findViewById(R.id.unlock);

        mLockButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick (View v) {
                sendState(true);
            }
        });

        mUnlockButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick (View v) {
                sendState(false);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(Const.TAG_ACTIVITY, "Successful connection to Google Play Services");
        Wearable.DataApi.addListener(mGoogleApiClient, this);

        // Retrieve the latest data item from any source, calls processDataItem when done
        GetFirstDataItem.callProcessDataItem(mGoogleApiClient, Const.LOCK_PATH, this);
    }

    @Override
    public void onConnectionSuspended(int status) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(Const.TAG_ACTIVITY, "Failed to connect to Google Play Services " + result);
        throw new RuntimeException("Play Services failed");
    }

    public void sendState(boolean state) {
        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Const.LOCK_PATH);
        Date d = new Date();
        putDataMapRequest.getDataMap().putLong("timestamp", d.getTime());
        putDataMapRequest.getDataMap().putBoolean("state", state);

        if (!mGoogleApiClient.isConnected())
            throw new RuntimeException("Cannot send data item when connection is not ready");
        Log.d(Const.TAG_ACTIVITY, "Preparing data item for send, isConnected=" + mGoogleApiClient.isConnected());
        final PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {
                            Log.e(Const.TAG_ACTIVITY, "Failed to send data map item for " + Const.LOCK_PATH + " status: " + dataItemResult.getStatus().getStatusCode());
                        } else {
                            Log.d(Const.TAG_ACTIVITY, "Sent data map item for " + Const.LOCK_PATH + " " + putDataMapRequest);
                        }
                    }
                });
    }

    public void setLockStateString(final String in) {
        Log.d(Const.TAG_ACTIVITY, "Scheduling set of lock state UI string to " + in);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(Const.TAG_ACTIVITY, "Actually setting lock state UI string to " + in);
                mLockState.setText(in);
            }
        });
    }

    public void setLockState(boolean state) {
        String deviceType;
        if (Const.DEVICE == Const.PHONE) {
            deviceType = "Remote Wear";
        } else {
            deviceType = "Local";
        }
        if (state) {
            setLockStateString(deviceType + " Display Locked On\nAmbient Will Never Happen");
        } else {
            setLockStateString(deviceType + " Display Not Locked\nAmbient Is Possible");
        }
    }

    public void missingDataItem() {
        Log.d(Const.TAG_ACTIVITY, "missingDataItem() setting state=false");
        setLockState(false);
    }

    public void processDataItem(DataItem dataItem) {
        DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
        String path = dataItem.getUri().getPath();
        if (path.equals(Const.LOCK_PATH)) {
            long timestamp = dataMap.getLong("timestamp");
            boolean state = dataMap.getBoolean("state");
            Log.d(Const.TAG_ACTIVITY, "Updating UI based on data item for path " + path + " with state=" + state + ", timestamp=" + timestamp);
            setLockState(state);
        } else {
            Log.d(Const.TAG_ACTIVITY, "Ignoring data item update for unknown path " + path);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(Const.TAG_ACTIVITY, "MainActivity onDataChanged() to update the UI state");
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                processDataItem(dataItem);
            }
        }
    }
}
