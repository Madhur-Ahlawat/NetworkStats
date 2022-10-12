package com.demo.networkstatsmanager.view

import android.Manifest
import com.demo.networkstatsmanager.utils.PackageManagerHelper.isPackage
import com.demo.networkstatsmanager.utils.PackageManagerHelper.getPackageUID_byName
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.os.Bundle
import com.demo.networkstatsmanager.R
import android.annotation.TargetApi
import android.os.Build
import com.bumptech.glide.Glide
import android.content.pm.PackageManager
import android.app.usage.NetworkStatsManager
import com.demo.networkstatsmanager.utils.NetworkStatsHelper
import android.net.TrafficStats
import androidx.core.app.ActivityCompat
import android.app.AppOpsManager
import android.app.AppOpsManager.OnOpChangedListener
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Process
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import com.demo.networkstatsmanager.model.Package

class StatsActivity : AppCompatActivity() {
  var ivIcon: ImageView? = null
  var trafficStatsPackageRx: TextView? = null
  var trafficStatsPackageTx: TextView? = null
  var networkStatsPackageRx: TextView? = null
  var networkStatsPackageTx: TextView? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stats)
    ivIcon = findViewById(R.id.avatar)
  }

  override fun onStart() {
    super.onStart()
    requestPermissions()
  }

  @TargetApi(Build.VERSION_CODES.M)
  override fun onResume() {
    super.onResume()
    if (!hasPermissions()) {
      return
    }
    Glide.with(this).load("https://en.wikipedia.org/wiki/McLaren_F1#/media/File:1996_McLaren_F1_Chassis_No_63_6.1_Front.jpg").into(ivIcon!!)
    initTextViews()
    intiData()
  }

  private fun intiData() {
    val packageName = packageData!!.packageName ?: return
    try {
      ivIcon!!.setImageDrawable(packageManager.getApplicationIcon(packageName))
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    if (!isPackage(this@StatsActivity, packageName)) {
      return
    }
    fillData(packageName)
  }

  private fun requestPermissions() {
//        if (!hasPermissionToReadNetworkHistory()) {
//            return;
//        }
    if (!hasPermissionToReadPhoneStats()) {
      requestPhoneStateStats()
    }
  }

  //    private boolean hasPermissions() {
  //        return hasPermissionToReadNetworkHistory() && hasPermissionToReadPhoneStats();
  //    }
  private fun hasPermissions(): Boolean {
    return hasPermissionToReadPhoneStats()
  }

  private fun initTextViews() {
    trafficStatsPackageRx = findViewById<View>(R.id.traffic_stats_package_rx_value) as TextView
    trafficStatsPackageTx = findViewById<View>(R.id.traffic_stats_package_tx_value) as TextView
    networkStatsPackageRx = findViewById<View>(R.id.network_stats_package_rx_value) as TextView
    networkStatsPackageTx = findViewById<View>(R.id.network_stats_package_tx_value) as TextView
  }

  private fun fillData(packageName: String) {
    val uid = getPackageUID_byName(this, packageName)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val networkStatsManager = applicationContext.getSystemService(NETWORK_STATS_SERVICE) as NetworkStatsManager
      val networkStatsHelper = NetworkStatsHelper(networkStatsManager, uid)
      fillNetworkStatsPackage(uid, networkStatsHelper)
    }
    fillTrafficStatsPackage(uid)
  }

  @TargetApi(Build.VERSION_CODES.M)
  private fun fillNetworkStatsPackage(uid: Int, networkStatsHelper: NetworkStatsHelper) {
    val recievedRxKiloBytes = (networkStatsHelper.getPackageRxBytesMobile(this) + networkStatsHelper.packageRxBytesWifi) / 1024
    val sentRxKiloBytes = (networkStatsHelper.getPackageTxBytesMobile(this) + networkStatsHelper.packageTxBytesWifi) / 1024
    if (recievedRxKiloBytes > 1) {
      networkStatsPackageRx!!.text = "$recievedRxKiloBytes KBs recieved"
    } else {
      networkStatsPackageRx!!.text = "$recievedRxKiloBytes KB recieved"
    }
    if (sentRxKiloBytes > 1) {
      networkStatsPackageTx!!.text = "$sentRxKiloBytes KBs sent"
    } else {
      networkStatsPackageTx!!.text = "$sentRxKiloBytes KB sent"
    }
  }

  private fun fillTrafficStatsPackage(uid: Int) {
    val recievedTxKiloBytes = TrafficStats.getUidRxBytes(uid) / 1024
    val sentTxKiloBytes = TrafficStats.getUidTxBytes(uid) / 1024
    if (recievedTxKiloBytes > 1) {
      trafficStatsPackageRx!!.text = "$recievedTxKiloBytes KBs recieved"
    } else {
      trafficStatsPackageRx!!.text = "$recievedTxKiloBytes KB recieved"
    }
    if (sentTxKiloBytes > 1) {
      trafficStatsPackageTx!!.text = "$sentTxKiloBytes KBs sent"
    } else {
      trafficStatsPackageTx!!.text = "$sentTxKiloBytes KB sent"
    }
  }

  private fun hasPermissionToReadPhoneStats(): Boolean {
    return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_DENIED
  }

  private fun requestPhoneStateStats() {
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), READ_PHONE_STATE_REQUEST)
  }

  private fun hasPermissionToReadNetworkHistory(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return true
    }
    val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
      AppOpsManager.OPSTR_GET_USAGE_STATS,
      Process.myUid(), packageName
    )
    if (mode == AppOpsManager.MODE_ALLOWED) {
      return true
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
        applicationContext.packageName,
        object : OnOpChangedListener {
          @TargetApi(Build.VERSION_CODES.M)
          override fun onOpChanged(op: String, packageName: String) {
            val mode = appOps.checkOpNoThrow(
              AppOpsManager.OPSTR_GET_USAGE_STATS,
              Process.myUid(), getPackageName()
            )
            if (mode != AppOpsManager.MODE_ALLOWED) {
              return
            }
            appOps.stopWatchingMode(this)
            val intent = Intent(this@StatsActivity, StatsActivity::class.java)
            if (getIntent().extras != null) {
              intent.putExtras(getIntent().extras!!)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(intent)
          }
        })
    }
    requestReadNetworkHistoryAccess()
    return false
  }

  private fun requestReadNetworkHistoryAccess() {
    val intent: Intent
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
      startActivity(intent)
    }
  }

  private val packageData: Package?
    private get() {
      val packageManager = packageManager
      return try {
        val mApplicationInfo: ApplicationInfo
        val packageInfo = getPackageManager().getPackageInfo(packageName, 0)
        val packageItem = Package()
        packageItem.version = packageInfo.versionName
        packageItem.packageName = packageInfo.packageName
        mApplicationInfo = packageManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA)
        if (mApplicationInfo == null) {
          return null
        }
        val appName = packageManager.getApplicationLabel(mApplicationInfo)
        if (appName != null) {
          packageItem.name = appName.toString()
        }
        packageItem
      } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
      }
    }

  companion object {
    private const val READ_PHONE_STATE_REQUEST = 37
  }
}