package com.demo.networkstatsmanager.utils;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.M)
public class NetworkStatsHelper {

    NetworkStatsManager networkStatsManager;
    int packageUid;
    NetworkStats networkStats = null;

    public NetworkStatsHelper(NetworkStatsManager networkStatsManager, int packageUid) {
        this.networkStatsManager = networkStatsManager;
        this.packageUid = packageUid;
    }

    public long getPackageRxBytesMobile(int uid, Context context) {

        networkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_MOBILE,
                getAndroidID(context),
                0,
                System.currentTimeMillis(),
                packageUid);
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        networkStats.getNextBucket(bucket);
        return bucket.getRxBytes();
    }

    public long getPackageTxBytesMobile(Context context) {
        NetworkStats networkStats;
        networkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_MOBILE,
                getAndroidID(context),
                0,
                System.currentTimeMillis(),
                packageUid);
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getTxBytes();
    }

    public long getPackageRxBytesWifi() {
        NetworkStats networkStats;
        networkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_WIFI,
                "",
                0,
                System.currentTimeMillis(),
                packageUid);
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getRxBytes();
    }

    public long getPackageTxBytesWifi() {
        NetworkStats networkStats;
        networkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_WIFI,
                "",
                0,
                System.currentTimeMillis(),
                packageUid);
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getTxBytes();
    }

    public static String getAndroidID(Context context) {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("deviceId", deviceId);
        return deviceId;
    }
}
