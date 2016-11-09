/*
 * DemoMode.kt
 *
 *   PSPDFKit
 *
 *   Copyright (c) 2016 PSPDFKit GmbH. All rights reserved.
 *
 *   THIS SOURCE CODE AND ANY ACCOMPANYING DOCUMENTATION ARE PROTECTED BY INTERNATIONAL COPYRIGHT LAW
 *   AND MAY NOT BE RESOLD OR REDISTRIBUTED. USAGE IS BOUND TO THE PSPDFKIT LICENSE AGREEMENT.
 *   UNAUTHORIZED REPRODUCTION OR DISTRIBUTION IS SUBJECT TO CIVIL AND CRIMINAL PENALTIES.
 *   This notice may not be removed from this file
 */

package com.pspdfkit.labs.quickdemo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.content.ContextCompat
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class DemoMode private constructor(context: Context) {
    private val applicationContext: Context = context.applicationContext
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val demoModeProperties = mutableSetOf<DemoModeProperty<out Any>>()

    companion object {
        private var singleton: DemoMode? = null

        /** Returns the demo mode singleton */
        fun get(context: Context): DemoMode {
            var demoMode = singleton
            if (demoMode == null) {
                demoMode = DemoMode(context)
                singleton = demoMode
            }

            return demoMode
        }

        const val DEFAULT_MOBILE_DATATYPE = "hidden"
        const val DEFAULT_MOBILE_LEVEL = 4
        const val DEFAULT_WIFI_LEVEL = 4
        const val DEFAULT_NETWORK_NUM_OF_SIMS = 1
        const val DEFAULT_STATUS_BAR_MODE = "transparent"
        const val DEFAULT_BATTERY_LEVEL = 100
        const val DEFAULT_STATUS_HIDDEN = "hidden"
    }

    /** Returns `true` if the required permissions to send out demo mode broadcasts are available, or `false` if they need to be requested first. */
    val requiredPermissionsGranted: Boolean
        get() = ContextCompat.checkSelfPermission(applicationContext, "android.permission.DUMP") == PackageManager.PERMISSION_GRANTED

    /** Returns `true` if the demo mode has been activated in system UI. **/
    val demoModeAllowed: Boolean
        get() = Settings.Global.getInt(applicationContext.contentResolver, "sysui_demo_allowed", 0) == 1

    /**
     * Enables or disables the demo mode. If this is set to `false`, no other demo mode settings will have effect.
     */
    var enabled: Boolean
        get() = sharedPreferences.getBoolean("demoEnabled", false)
        set(value) {
            sharedPreferences.edit().putBoolean("demoEnabled", value).commit()

            if (value) {
                sendDemoCommand("enter")
                for (property in demoModeProperties) {
                    property.issueDemoModeCommand()
                }
            } else {
                sendDemoCommand("exit")
            }

        }

    /**
     * If set to `true`, all notifications get hidden.
     */
    var hideNotifications by booleanPreference("hideNotifications", true) { value ->
        if (enabled) sendDemoCommand("notifications", "visible" to !value)
    }

    /**
     * Sets the time in the format of `hhmm`.
     */
    var time by stringPreference("time", "0700") { value ->
        if (enabled) sendDemoCommand("clock", "hhmm" to value)
    }

    /**
     * Can be set to any of `opaque`, `translucent`, `semi-transparent`, `transparent`, or `warning`.
     */
    var statusBarMode by stringPreference("statusBarMode", DEFAULT_STATUS_BAR_MODE) { value ->
        if (enabled) sendDemoCommand("bars", "mode" to value)
    }

    /**
     * Configure the network display (Wi-Fi, mobile, airplane, etc.).
     */
    val network = Network()

    inner class Network {
        /**
         * Enables or disables airplane mode. Disabled by default.
         */
        var showAirplane by booleanPreference("networkShowAirplane", false) { value ->
            if (enabled) sendDemoCommand("network", "airplane" to if (value) "show" else "hide")
        }

        /**
         * Enables or disables full network connectivity mode. Enabled by default.
         */
        var fullConnectivity by booleanPreference("networkFullConnectivity", true) { value ->
            if (enabled) sendDemoCommand("network", "fully" to value)
        }

        /**
         * Shows or hides the Wi-Fi icon. Hidden by default.
         */
        var showWifi by booleanPreference("networkShowWifi", false) { value ->
            if (enabled) sendDemoCommand("network", "wifi" to if (value) "show" else "hide")
        }

        /**
         * Sets the Wi-Fi connection level. Can be an integer from `0` (no reception) to `4` (full reception). Defaults to `4`.
         */
        var wifiLevel by intPreference("networkWifiLevel", DEFAULT_WIFI_LEVEL) { value ->
            if (enabled && showWifi) sendDemoCommand("network", "wifi" to "show", "level" to value)
        }

        /**
         * Shows or hides the mobile network icon. Shown by default.
         */
        var mobileShown by booleanPreference("networkMobileShown", true) { value ->
            if (enabled) sendDemoCommand("network", "mobile" to if (value) "show" else "hide")
        }

        /**
         * Sets the mobile network level. Can be an integer from `0` (no reception) to `4` (full reception). Defaults to `4`.
         */
        var mobileLevel by intPreference("networkMobileLevel", DEFAULT_MOBILE_LEVEL) { value ->
            if (enabled && mobileShown) sendDemoCommand("network", "mobile" to "show", "level" to value)
        }

        /**
         * Sets the mobile network connection type. Can be any of `1x`, `3g`, `4g`, `e`, `g`, `h`, `lte`, `roam`, or any other value to hide. Defaults to `lte`.
         */
        var mobileDatatype by stringPreference("networkMobileDatatype", DEFAULT_MOBILE_DATATYPE) { value ->
            if (enabled && mobileShown) sendDemoCommand("network", "mobile" to "show", "datatype" to value)
        }

        /**
         * Sets mobile signal icon to carrier network change UX when disconnected. Disabled by default.
         */
        var showCarrierNetworkChange by booleanPreference("networkCarrierNetworkChange", false) { value ->
            if (enabled) sendDemoCommand("network", "carriernetworkchange" to if (value) "show" else "hide")
        }

        /**
         * Sets the number of used SIMs. Can be an integer from `1` to `8`. Defaults to `1`.
         */
        var numberOfSims by intPreference("networkNumberOfSims", DEFAULT_NETWORK_NUM_OF_SIMS) { value ->
            if (enabled) sendDemoCommand("network", "sims" to value)
        }

        /**
         * Show or hide the "no SIM" icon. Hidden by default.
         */
        var showNoSim by booleanPreference("networkShowNoSim", false) { value ->
            if (enabled) sendDemoCommand("network", "nosim" to if (value) "show" else "hide")
        }
    }

    /**
     * Configure the status icons (alarm, volume, bluetooth, etc.)
     */
    val status = Status()

    inner class Status {
        /**
         * Sets the volume icon. Can be any of `silent`, `vibrate`, or any other value to hide the icon. Hidden by default.
         */
        var volume by stringPreference("statusVolume", DEFAULT_STATUS_HIDDEN) { value ->
            if (enabled) sendDemoCommand("status", "volume" to value)
        }

        /**
         * Sets the bluetooth icon. Can be any of `connected`, `disconnected`, `hidden`. Hidden by default.
         */
        var bluetooth by stringPreference("statusBluetooth", DEFAULT_STATUS_HIDDEN, { value ->
            if (enabled) sendDemoCommand("status", "bluetooth" to value)
        })

        /**
         * Shows or hides the icon in the location slot. Hidden by default.
         */
        var showLocation by booleanPreference("statusLocation", false) { value ->
            if (enabled) sendDemoCommand("status", "location" to if (value) "show" else "hide")
        }

        /**
         * Shows or hides the alarm clock icon. Hidden by default.
         */
        var showAlarm by booleanPreference("statusAlarm", false) { value ->
            if (enabled) sendDemoCommand("status", "alarm" to if (value) "show" else "hide")
        }

        /**
         * Shows or hides the sync icon. Hidden by default.
         */
        var showSync by booleanPreference("statusSync", false) { value ->
            if (enabled) sendDemoCommand("status", "sync" to if (value) "show" else "hide")
        }

        /**
         * Shows or hides the TTY icon. Hidden by default.
         */
        var showTty by booleanPreference("statusTty", false) { value ->
            if (enabled) sendDemoCommand("status", "tty" to if (value) "show" else "hide")
        }

        /**
         * Shows or hides the CDMA ERI icon. Hidden by default.
         */
        var showEri by booleanPreference("statusEri", false) { value ->
            if (enabled) sendDemoCommand("status", "eri" to if (value) "show" else "hide")
        }

        /**
         * Shows or hides the mute icon. Hidden by default.
         */
        var showMute by booleanPreference("statusMute", false) { value ->
            if (enabled) sendDemoCommand("status", "mute" to if (value) "show" else "hide")
        }

        /**
         * Shows or hides the speakerphone icon. Hidden by default.
         */
        var showSpeakerphone by booleanPreference("statusSpeakerphone", false) { value ->
            if (enabled) sendDemoCommand("status", "speakerphone" to if (value) "show" else "hide")
        }
    }

    /**
     * Configure the battery display.
     */
    val battery = Battery()

    inner class Battery() {
        /**
         * Sets the shown battery level. Can be an integer from `0` (depleted) to `100` (fully charged). Defaults to `100`.
         */
        var level by intPreference("batteryLevel", DEFAULT_BATTERY_LEVEL) { value ->
            if (enabled) sendDemoCommand("battery", "level" to value)
        }

        /**
         * Enables or disables the battery being shown as being plugged in and charging. Defaults to `false`.
         */
        var plugged by booleanPreference("batteryPlugged", false) { value ->
            if (enabled) sendDemoCommand("battery", "plugged" to value)
        }
    }

    private fun sendDemoCommand(command: String, vararg extras: Pair<String, Any>) {
        val intent = Intent("com.android.systemui.demo").apply {
            putExtra("command", command)
            for ((key, value) in extras) {
                putExtra(key, value.toString())
            }
        }

        applicationContext.sendBroadcast(intent)
    }

    abstract class DemoModeProperty<T> : ReadWriteProperty<Any, T>, SharedPreferences.OnSharedPreferenceChangeListener {
        abstract val key: String
        abstract fun issueDemoModeCommand()
        abstract fun getStoredValue(): T
        override fun getValue(thisRef: Any, property: KProperty<*>) = getStoredValue()
        override fun onSharedPreferenceChanged(p0: SharedPreferences?, changedKey: String?) {
            if (key == changedKey) issueDemoModeCommand()
        }
    }

    fun booleanPreference(key: String, default: Boolean, onSendCommand: (Boolean) -> Unit) = object : DemoModeProperty<Boolean>() {
        override val key = key
        override fun issueDemoModeCommand() = onSendCommand(getStoredValue())
        override fun getStoredValue() = sharedPreferences.getBoolean(key, default)
        override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
            sharedPreferences.edit().putBoolean(key, value).apply()
        }
    }.apply {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        demoModeProperties.add(this)
    }

    fun intPreference(key: String, default: Int, onSendCommand: (Int) -> Unit) = object : DemoModeProperty<Int>() {
        override val key = key
        override fun issueDemoModeCommand() = onSendCommand(getStoredValue())
        override fun getStoredValue() = sharedPreferences.getString(key, default.toString()).toInt()
        override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
            sharedPreferences.edit().putString(key, value.toString()).apply()
        }
    }.apply {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        demoModeProperties.add(this)
    }

    fun stringPreference(key: String, default: String, onSendCommand: (String) -> Unit) = object : DemoModeProperty<String>() {
        override val key = key
        override fun issueDemoModeCommand() = onSendCommand(getStoredValue())
        override fun getStoredValue() = sharedPreferences.getString(key, default)
        override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
            sharedPreferences.edit().putString(key, value).apply()
        }
    }.apply {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        demoModeProperties.add(this)
    }
}