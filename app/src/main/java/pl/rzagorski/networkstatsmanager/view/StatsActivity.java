package pl.rzagorski.networkstatsmanager.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import pl.rzagorski.networkstatsmanager.R;
import pl.rzagorski.networkstatsmanager.utils.NetworkStatsHelper;
import pl.rzagorski.networkstatsmanager.utils.PackageManagerHelper;
import pl.rzagorski.networkstatsmanager.utils.TrafficStatsHelper;

public class StatsActivity extends AppCompatActivity {
    private static final int READ_PHONE_STATE_REQUEST = 37;
    public static final String EXTRA_PACKAGE = "ExtraPackage";

    AppCompatImageView ivIcon;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;

    EditText packageNameEd;
    TextView TrafficStatsAllUid;
    TextView TrafficStatsAllRx;
    TextView TrafficStatsAllTx;
    TextView TrafficStatsPackageUid;
    TextView TrafficStatsPackageRx;
    TextView TrafficStatsPackageTx;

    TextView NetworkStatsManagerAllUid;
    TextView NetworkStatsManagerAllRx;
    TextView NetworkStatsManagerAllTx;
    TextView NetworkStatsManagerPackageUid;
    TextView NetworkStatsManagerPackageRx;
    TextView NetworkStatsManagerPackageTx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ivIcon = (AppCompatImageView) findViewById(R.id.avatar);
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
        initTextViews();
        checkIntent();
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        String packageName = extras.getString(EXTRA_PACKAGE);
        if (packageName == null) {
            return;
        }
        try {
            ivIcon.setImageDrawable(getPackageManager().getApplicationIcon(packageName));
            toolbar.setTitle(getPackageManager().getApplicationLabel(
                    getPackageManager().getApplicationInfo(
                            packageName, PackageManager.GET_META_DATA)));
            toolbar.setSubtitle(packageName + ":" + PackageManagerHelper.getPackageUid(this, packageName));
            setSupportActionBar(toolbar);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        packageNameEd.setText(packageName);
    }

    private void requestPermissions() {
        if (!hasPermissionToReadNetworkHistory()) {
            return;
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
            return;
        }
    }

    private boolean hasPermissions() {
        return hasPermissionToReadNetworkHistory() && hasPermissionToReadPhoneStats();
    }

    private void initTextViews() {
        packageNameEd = (EditText) findViewById(R.id.package_name_edit_text);
        packageNameEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (PackageManagerHelper.isPackage(StatsActivity.this, s)) {
                    fillData(s.toString());
                }
            }
        });
        TrafficStatsAllUid = (TextView) findViewById(R.id.traffic_stats_all_uid);
        TrafficStatsAllRx = (TextView) findViewById(R.id.traffic_stats_all_rx);
        TrafficStatsAllTx = (TextView) findViewById(R.id.textView3);
        TrafficStatsPackageUid = (TextView) findViewById(R.id.textView14);
        TrafficStatsPackageRx = (TextView) findViewById(R.id.textView16);
        TrafficStatsPackageTx = (TextView) findViewById(R.id.textView18);
        NetworkStatsManagerAllUid = (TextView) findViewById(R.id.network_stats_manager_all_uid);
        NetworkStatsManagerAllRx = (TextView) findViewById(R.id.network_stats_manager_all_rx);
        NetworkStatsManagerAllTx = (TextView) findViewById(R.id.textView6);
        NetworkStatsManagerPackageUid = (TextView) findViewById(R.id.textView7);
        NetworkStatsManagerPackageRx = (TextView) findViewById(R.id.textView10);
        NetworkStatsManagerPackageTx = (TextView) findViewById(R.id.textView19);
    }

    private void fillData(String packageName) {
        int uid = PackageManagerHelper.getPackageUid(this, packageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkStatsManager networkStatsManager = (NetworkStatsManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
            NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid);
            fillNetworkStatsAll(networkStatsHelper);
            fillNetworkStatsPackage(uid, networkStatsHelper);
        }
        fillTrafficStatsAll();
        fillTrafficStatsPackage(uid);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsAll(NetworkStatsHelper networkStatsHelper) {
        NetworkStatsManagerAllUid.setText("-1");
        long mobileWifiRx = networkStatsHelper.getAllRxBytesMobile(this) + networkStatsHelper.getAllRxBytesWifi();
        NetworkStatsManagerAllRx.setText(mobileWifiRx + " B");
        long mobileWifiTx = networkStatsHelper.getAllRxBytesMobile(this) + networkStatsHelper.getAllRxBytesWifi();
        NetworkStatsManagerAllTx.setText(mobileWifiTx + " B");
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsPackage(int uid, NetworkStatsHelper networkStatsHelper) {
        NetworkStatsManagerPackageUid.setText(packageNameEd.getText().toString() + " :" + uid);
        long mobileWifiRx = networkStatsHelper.getPackageRxBytesMobile(this) + networkStatsHelper.getPackageRxBytesWifi();
        NetworkStatsManagerPackageRx.setText(mobileWifiRx + " B");
        long mobileWifiTx = networkStatsHelper.getPackageRxBytesMobile(this) + networkStatsHelper.getPackageRxBytesWifi();
        NetworkStatsManagerPackageTx.setText(mobileWifiTx + " B");
    }

    private void fillTrafficStatsAll() {
        TrafficStatsAllUid.setText("-1");
        TrafficStatsAllRx.setText(TrafficStatsHelper.getAllRxBytes() + " B");
        TrafficStatsAllTx.setText(TrafficStatsHelper.getAllTxBytes() + " B");
    }

    private void fillTrafficStatsPackage(int uid) {
        TrafficStatsPackageUid.setText(packageNameEd.getText().toString() + " :" + uid);
        TrafficStatsPackageRx.setText(TrafficStatsHelper.getPackageRxBytes(uid) + " B");
        TrafficStatsPackageTx.setText(TrafficStatsHelper.getPackageTxBytes(uid) + " B");
    }

    private boolean hasPermissionToReadPhoneStats() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return true;
        }
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
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //fillData(packageNameEd.getText().toString());
        }
    }
}