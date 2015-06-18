package net.waynepiekarski.displaylockwear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Const.TAG_SERVICE, "BootReceiver will now start LockService");
        Intent startServiceIntent = new Intent(context, LockService.class);
        context.startService(startServiceIntent);
    }
}
