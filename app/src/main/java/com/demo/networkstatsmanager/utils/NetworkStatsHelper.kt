package com.demo.networkstatsmanager.utils

import android.annotation.TargetApi
import android.os.Build
import android.app.usage.NetworkStatsManager
import android.app.usage.NetworkStats
import android.content.Context
import android.net.ConnectivityManager
import android.os.RemoteException
import android.provider.Settings
import android.util.Log
import com.demo.networkstatsmanager.utils.NetworkStatsHelper

@TargetApi(Build.VERSION_CODES.M)
class NetworkStatsHelper(var networkStatsManager: NetworkStatsManager, var packageUid: Int) {
  var networkStats: NetworkStats.Bucket? = null
  fun getPackageRxBytesMobile(context: Context): Long {
    return try {
      networkStats = networkStatsManager.querySummaryForUser(
        ConnectivityManager.TYPE_MOBILE,
        getAndroidID(context),
        0,
        System.currentTimeMillis()
      )
      networkStats!!.getRxBytes()
    } catch (e: RemoteException) {
      e.printStackTrace()
      0
    }
  }

  fun getPackageTxBytesMobile(context: Context): Long {
    val networkStats: NetworkStats.Bucket
    return try {
      networkStats = networkStatsManager.querySummaryForUser(
        ConnectivityManager.TYPE_MOBILE,
        getAndroidID(context),
        0,
        System.currentTimeMillis()
      )
      networkStats.txBytes
    } catch (e: RemoteException) {
      e.printStackTrace()
      0
    }
  }

  val packageRxBytesWifi: Long
    get() {
      val networkStats: NetworkStats.Bucket
      return try {
        networkStats = networkStatsManager.querySummaryForUser(
          ConnectivityManager.TYPE_WIFI,
          "",
          0,
          System.currentTimeMillis()
        )
        networkStats.rxBytes
      } catch (e: RemoteException) {
        e.printStackTrace()
        0
      }
    }
  val packageTxBytesWifi: Long
    get() {
      val networkStats: NetworkStats.Bucket
      return try {
        networkStats = networkStatsManager.querySummaryForUser(
          ConnectivityManager.TYPE_WIFI,
          "",
          0,
          System.currentTimeMillis()
        )
        networkStats.txBytes
      } catch (e: RemoteException) {
        e.printStackTrace()
        0
      }
    }

  companion object {
    fun getAndroidID(context: Context): String? {
      val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
      Log.d("deviceId", deviceId)
      return null
    }
  }
}