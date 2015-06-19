package net.waynepiekarski.displaylockwear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BaseActivity extends Activity {

    boolean mPermanentService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure we start up the LockService when the Activity is started. This needs to happen because
        // the app might have just been installed, and the service won't run until we reboot the device, or
        // until a new onDataChanged arrives.
        if (!mPermanentService) {
            Log.d(Const.TAG_ACTIVITY, "mPermanentService is not set, so starting LockService to configure device state with current value");
            Intent startServiceIntent = new Intent(this, LockService.class);
            startService(startServiceIntent);
            mPermanentService = true;
        }
    }
}
