// ---------------------------------------------------------------------
// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ---------------------------------------------------------------------

package net.waynepiekarski.displaylockwear;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

public abstract class BaseActivity extends WearableActivity {

    boolean mPermanentService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAmbientEnabled();

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

    abstract public void setUiVisibility(boolean b);

    @Override
    public void onEnterAmbient(Bundle b) {
        super.onEnterAmbient(b);
        setUiVisibility(false);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        setUiVisibility(true);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        // Nothing to refresh here
    }
}
