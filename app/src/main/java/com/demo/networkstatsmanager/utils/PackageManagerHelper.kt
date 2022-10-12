package com.demo.networkstatsmanager.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.util.Log
import com.demo.networkstatsmanager.view.StatsActivity

object PackageManagerHelper {
  @JvmStatic
  fun isPackage(context: Context, s: CharSequence): Boolean {
    val packageManager = context.packageManager
    try {
      packageManager.getPackageInfo(s.toString(), PackageManager.GET_META_DATA)
    } catch (e: PackageManager.NameNotFoundException) {
      return false
    }
    return true
  }

  @JvmStatic
  fun getPackageUID_byName(context: Context, packageName: String?): Int {
    val packageManager = context.packageManager
    var uid = -1
    try {
      val packageInfo = packageManager.getPackageInfo(packageName!!, PackageManager.GET_META_DATA)
      Log.d(StatsActivity::class.java.simpleName, packageInfo.packageName)
      uid = packageInfo.applicationInfo.uid
    } catch (e: PackageManager.NameNotFoundException) {
    }
    return uid
  }
}