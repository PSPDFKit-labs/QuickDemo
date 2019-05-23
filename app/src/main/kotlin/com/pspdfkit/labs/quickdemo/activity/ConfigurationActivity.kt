/*
 *   Copyright (c) 2016-2019 PSPDFKit GmbH. All rights reserved.
 *
 *   THIS SOURCE CODE AND ANY ACCOMPANYING DOCUMENTATION ARE PROTECTED BY INTERNATIONAL COPYRIGHT LAW
 *   AND MAY NOT BE RESOLD OR REDISTRIBUTED. USAGE IS BOUND TO THE PSPDFKIT LICENSE AGREEMENT.
 *   UNAUTHORIZED REPRODUCTION OR DISTRIBUTION IS SUBJECT TO CIVIL AND CRIMINAL PENALTIES.
 *   This notice may not be removed from this file
 */

package com.pspdfkit.labs.quickdemo.activity


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import androidx.annotation.ArrayRes
import androidx.appcompat.app.AppCompatActivity
import com.pspdfkit.labs.quickdemo.DemoMode
import com.pspdfkit.labs.quickdemo.R

class ConfigurationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setIcon(R.drawable.ic_configuration_activity)
        fragmentManager.beginTransaction().replace(android.R.id.content, DemoModePreferences()).commit()
    }

    override fun onStart() {
        super.onStart()

        // Whenever this activity comes to the foreground, we check if the required permissions have been granted.
        // If the permissions are missing, launch the setup guide.
        val demoMode = DemoMode.get(this)
        if (!demoMode.requiredPermissionsGranted || !demoMode.demoModeAllowed) {
            SetupGuideActivity.launch(this)
            // We have to finish this activity, otherwise users that do not complete the setup guide would end up in a endless loop.
            finish()
        }
    }

    class DemoModePreferences : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
        private lateinit var demoMode: DemoMode
        private var updateFromReceiver: Boolean = false

        private val demoModeChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                updateFromReceiver = true
                updateDemoModeSwitch()
                updateFromReceiver = false
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            demoMode = DemoMode.get(context)
            addPreferencesFromResource(R.xml.demo_mode_preferences)
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
            updateSummaries()
        }

        fun updateSummaries() {
            setSummaryFromArrayValue("batteryLevel", R.array.batteryLevelEscaped, R.array.batteryLevelValues, DemoMode.DEFAULT_BATTERY_LEVEL.toString())
            setSummaryFromArrayValue("statusBarMode", R.array.statusBarModes, R.array.statusBarModeValues, DemoMode.DEFAULT_STATUS_BAR_MODE)
            setSummaryFromArrayValue("networkMobileDatatype", R.array.networkMobileDatatypes, R.array.networkMobileDatatypeValues, DemoMode.DEFAULT_MOBILE_DATATYPE)
            setSummaryFromArrayValue("networkMobileLevel", R.array.networkReceptionLevels, R.array.networkReceptionLevelValues, DemoMode.DEFAULT_MOBILE_LEVEL.toString())
            setSummaryFromArrayValue("networkNumberOfSims", R.array.networkNumOfSims, R.array.networkNumOfSims, DemoMode.DEFAULT_NETWORK_NUM_OF_SIMS.toString())
            setSummaryFromArrayValue("networkWifiLevel", R.array.networkReceptionLevels, R.array.networkReceptionLevelValues, DemoMode.DEFAULT_WIFI_LEVEL.toString())
            setSummaryFromArrayValue("statusVolume", R.array.statusVolume, R.array.statusVolumeValues, DemoMode.DEFAULT_STATUS_HIDDEN)
            setSummaryFromArrayValue("statusBluetooth", R.array.statusBluetooth, R.array.statusBluetoothValues, DemoMode.DEFAULT_STATUS_HIDDEN)
        }

        fun setSummaryFromArrayValue(key: String, @ArrayRes labelArrayRes: Int, @ArrayRes valueArrayRes: Int, default: String) {
            val preference = findPreference(key)
            val value = preferenceManager.sharedPreferences.getString(key, default)
            val i = resources.getStringArray(valueArrayRes).indexOf(value)
            if (i >= 0) preference.summary = resources.getStringArray(labelArrayRes)[i]
        }

        override fun onStart() {
            super.onStart()
            context.registerReceiver(demoModeChangeReceiver, IntentFilter("com.android.systemui.demo"))
            updateDemoModeSwitch()
        }

        override fun onStop() {
            super.onStop()
            context.unregisterReceiver(demoModeChangeReceiver)
        }

        private fun updateDemoModeSwitch() {
            val enabled = findPreference("enable_demo") as SwitchPreference
            enabled.isChecked = demoMode.enabled
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            when (key) {
                "enable_demo" -> {
                    if (!updateFromReceiver) demoMode.enabled = sharedPreferences.getBoolean(key, false)
                }
                "batteryLevel", "statusBarMode", "networkMobileDatatype", "networkMobileLevel",
                "networkNumberOfSims", "networkWifiLevel", "statusVolume", "statusBluetooth" -> {
                    updateSummaries()
                }
            }
        }
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, ConfigurationActivity::class.java)
            context.startActivity(intent)
        }
    }
}
