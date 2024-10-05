package com.caurix.distributorauto;

import com.caurix.distributor.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {
    private TextView tvVersionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_caurix);
        tvVersionName = (TextView) findViewById(R.id.tvVersionName);

        Context context = getApplicationContext();
        PackageManager manager = context.getPackageManager();
        PackageInfo packageInfo;
        String version;
        try {
            packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "3.7";
        }
        tvVersionName.setText("Version: " + version);
    }

    public void onWindowFocusChanged(boolean isTrue) {
        super.onWindowFocusChanged(isTrue);

        if (!isTrue) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }
}
