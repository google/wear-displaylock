package net.waynepiekarski.displaylockwear;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


class ExtractedDataItem {
    ExtractedDataItem (DataItem d) {
        uri = d.getUri();
        path = uri.getPath();
        DataMap m = DataMapItem.fromDataItem(d).getDataMap();
        timestamp = m.getLong("timestamp");
        state = m.getBoolean("state");
        dataitem = d;
    }
    public String path;
    public Uri uri;
    public long timestamp;
    public boolean state;
    public DataItem dataitem;
}


public class GetFirstDataItem {

    public interface ProcessDataItemAble {
        void processDataItem (DataItem in);
        void missingDataItem ();
    }

    public static void callProcessDataItem(GoogleApiClient mGoogleApiClient, final String path, final ProcessDataItemAble callback) {
        Wearable.DataApi.getDataItems(mGoogleApiClient).setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer buffer) {
                if (buffer.getStatus().isSuccess()) {

                    ArrayList<ExtractedDataItem> array = new ArrayList();
                    for (int i = 0; i < buffer.getCount(); i++) {
                        DataItem temp = buffer.get(i);
                        ExtractedDataItem item = new ExtractedDataItem(temp);
                        if (item.path.contains(path)) {
                            Log.d(Const.TAG_MISC, "Sorting candidate item " + item.uri);
                            array.add(item);
                        } else {
                            Log.d(Const.TAG_MISC, "Rejecting candidate item " + item.uri);
                        }
                    }
                    Collections.sort(array, Comparators.compareTimestamp);
                    if (array.size() == 0) {
                        Log.d(Const.TAG_MISC, "No data item found, so calling missingDataItem()");
                        callback.missingDataItem();
                    }
                    for (int i = 0; i < array.size(); i++) {
                        ExtractedDataItem item = array.get(i);
                        if (i == 0) {
                            Log.d(Const.TAG_MISC, "Will use newest data item " + item + " with timestamp=" + item.timestamp + " state=" + item.state);
                            callback.processDataItem(item.dataitem);
                        } else {
                            Log.d(Const.TAG_MISC, "Skipping older data item " + item + " with timestamp=" + item.timestamp + " state=" + item.state);
                        }
                    }
                } else {
                    Log.d(Const.TAG_MISC, "getDataItems() returned an error status");
                }

                buffer.release();
            }
        });
    }

    public static class Comparators {
        public static Comparator<ExtractedDataItem> compareTimestamp = new Comparator<ExtractedDataItem>() {
            @Override
            public int compare(ExtractedDataItem a, ExtractedDataItem b) {

                long diff = b.timestamp - a.timestamp;
                if (diff < 0)
                    return -1;
                else if (diff > 0)
                    return +1;
                else
                    return 0;
            }
        };
    }
}
