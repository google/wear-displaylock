package net.waynepiekarski.displaylockwear;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class StateHandler {

    TextView mLockState;
    Button mLockButton;
    Button mUnlockButton;
    Activity mActivity;

    public StateHandler (Activity activity) {

        mActivity = activity;
        mLockState = (TextView)activity.findViewById(R.id.lockState);
        mLockButton = (Button)activity.findViewById(R.id.lock);
        mUnlockButton = (Button)activity.findViewById(R.id.unlock);

        // Always initialize to have the feature turned off by default
        setState(false);

        mLockButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick (View v) {
                setState(true);
            }
        });

        mUnlockButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick (View v) {
                setState(false);
            }
        });
    }

    public void setState(boolean state) {
        if (state) {
            Log.d(Const.LOG_NAME, "Locking display with addFlags FLAG_KEEP_SCREEN_ON");
            mLockState.setText("Display Locked On\nFLAG_KEEP_SCREEN_ON");
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            Log.d(Const.LOG_NAME, "Unlocking display with clearFlags FLAG_KEEP_SCREEN_ON");
            mLockState.setText("Display Not Locked\nAmbient Is Possible");
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
