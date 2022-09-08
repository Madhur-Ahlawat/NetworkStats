package com.demo.networkstatsmanager.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.demo.networkstatsmanager.R;
import com.demo.networkstatsmanager.model.Package;
import com.demo.networkstatsmanager.utils.NetworkStatsHelper;
import com.demo.networkstatsmanager.utils.PackageManagerHelper;

public class StatsActivity extends AppCompatActivity {
    private static final int READ_PHONE_STATE_REQUEST = 37;

    ImageView ivIcon;

    TextView trafficStatsPackageRx;
    TextView trafficStatsPackageTx;

    TextView networkStatsPackageRx;
    TextView networkStatsPackageTx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        ivIcon = findViewById(R.id.avatar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onResume() {
        super.onResume();
        if (!hasPermissions()) {
            return;
        }
        Glide.with(this).load("https://en.wikipedia.org/wiki/McLaren_F1#/media/File:1996_McLaren_F1_Chassis_No_63_6.1_Front.jpg").into(ivIcon);

        initTextViews();
        intiData();
    }

    private void intiData() {
        String packageName = getPackageData().getPackageName();
        if (packageName == null) {
            return;
        }
        try {
            ivIcon.setImageDrawable(getPackageManager().getApplicationIcon(packageName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (!PackageManagerHelper.isPackage(StatsActivity.this, packageName)) {
            return;
        }
        fillData(packageName);
    }

    private void requestPermissions() {
        if (!hasPermissionToReadNetworkHistory()) {
            return;
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
        }
    }

    private boolean hasPermissions() {
        return hasPermissionToReadNetworkHistory() && hasPermissionToReadPhoneStats();
    }

    private void initTextViews() {
        trafficStatsPackageRx = (TextView) findViewById(R.id.traffic_stats_package_rx_value);
        trafficStatsPackageTx = (TextView) findViewById(R.id.traffic_stats_package_tx_value);
        networkStatsPackageRx = (TextView) findViewById(R.id.network_stats_package_rx_value);
        networkStatsPackageTx = (TextView) findViewById(R.id.network_stats_package_tx_value);
    }

    private void fillData(String packageName) {
        int uid = PackageManagerHelper.getPackageUID_byName(this, packageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkStatsManager networkStatsManager = (NetworkStatsManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
            NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid);
            fillNetworkStatsPackage(uid, networkStatsHelper);
        }
        fillTrafficStatsPackage(uid);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsPackage(int uid, NetworkStatsHelper networkStatsHelper) {

        long recievedRxKiloBytes = (networkStatsHelper.getPackageRxBytesMobile(uid, this) + networkStatsHelper.getPackageRxBytesWifi()) / 1024;
        long sentRxKiloBytes = (networkStatsHelper.getPackageTxBytesMobile(this) + networkStatsHelper.getPackageTxBytesWifi()) / 1024;

        if (recievedRxKiloBytes > 1) {
            networkStatsPackageRx.setText(recievedRxKiloBytes + " KBs recieved");
        } else {
            networkStatsPackageRx.setText(recievedRxKiloBytes + " KB recieved");
        }

        if (sentRxKiloBytes > 1) {
            networkStatsPackageTx.setText(sentRxKiloBytes + " KBs sent");
        } else {
            networkStatsPackageTx.setText(sentRxKiloBytes + " KB sent");
        }
    }

    private void fillTrafficStatsPackage(int uid) {
        long recievedTxKiloBytes = TrafficStats.getUidRxBytes(uid) / 1024;
        long sentTxKiloBytes = TrafficStats.getUidTxBytes(uid) / 1024;

        if (recievedTxKiloBytes > 1) {
            trafficStatsPackageRx.setText(recievedTxKiloBytes + " KBs recieved");
        } else {
            trafficStatsPackageRx.setText(recievedTxKiloBytes + " KB recieved");
        }

        if (sentTxKiloBytes > 1) {
            trafficStatsPackageTx.setText(sentTxKiloBytes + " KBs sent");
        } else {
            trafficStatsPackageTx.setText(sentTxKiloBytes + " KB sent");
        }
    }

    private boolean hasPermissionToReadPhoneStats() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_DENIED;
    }

    private void requestPhoneStateStats() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST);
    }

    private boolean hasPermissionToReadNetworkHistory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(), getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        }
                        appOps.stopWatchingMode(this);
                        Intent intent = new Intent(StatsActivity.this, StatsActivity.class);
                        if (getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                });
        requestReadNetworkHistoryAccess();
        return false;
    }

    private void requestReadNetworkHistoryAccess() {
        Intent intent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    private Package getPackageData() {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo mApplicationInfo;
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            Package packageItem = new Package();
            packageItem.setVersion(packageInfo.versionName);
            packageItem.setPackageName(packageInfo.packageName);
            mApplicationInfo = packageManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
            if (mApplicationInfo == null) {
                return null;
            }
            CharSequence appName = packageManager.getApplicationLabel(mApplicationInfo);
            if (appName != null) {
                packageItem.setName(appName.toString());
            }
            return packageItem;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }
}
