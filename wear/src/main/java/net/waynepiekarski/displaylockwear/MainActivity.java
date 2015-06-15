package net.waynepiekarski.displaylockwear;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        mTextView = (TextView) layout.findViewById(R.id.lockState);
    }
}
