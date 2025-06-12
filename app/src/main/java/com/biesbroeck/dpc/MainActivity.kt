package com.biesbroeck.dpc

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences // For persistence
import android.os.Build
import android.os.Bundle
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import com.biesbroeck.dpc.MyDeviceAdminReceiver
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponentName: ComponentName
    private lateinit var prefs: SharedPreferences

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate called")
        super.onCreate(savedInstanceState)
        dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponentName = MyDeviceAdminReceiver.getComponentName(this)
        prefs = getSharedPreferences("DpcPrefs", MODE_PRIVATE)

        // Check if app is device owner and policies need to be applied
        if (dpm.isDeviceOwnerApp(packageName) && !policiesApplied()) {
            Log.d("MainActivity", "Device owner detected, applying policies from MainActivity.")
            applyDeviceOwnerPolicies()
            markPoliciesApplied()
        } else if (dpm.isDeviceOwnerApp(packageName) && policiesApplied()) {
            Log.d("MainActivity", "Device owner detected, policies already applied.")
        } else if (!dpm.isDeviceOwnerApp(packageName)) {
            Log.d("MainActivity", "Not a device owner.")
        }
        // No UI needed for manual activation or policy application
    }

    private fun policiesApplied(): Boolean {
        return prefs.getBoolean("policies_applied_v1", false)
    }

    private fun markPoliciesApplied() {
        prefs.edit { putBoolean("policies_applied_v1", true) }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyDeviceOwnerPolicies() {
        // 1. Disable Bluetooth (just turn it off once, allow user to toggle later)
        try {
            dpm.setBluetoothContactSharingDisabled(adminComponentName, true)
            // Remove user restrictions so user can toggle Bluetooth after provisioning
            dpm.clearUserRestriction(adminComponentName, UserManager.DISALLOW_BLUETOOTH)
            dpm.clearUserRestriction(adminComponentName, UserManager.DISALLOW_BLUETOOTH_SHARING)
            // Immediately turn off Bluetooth if enabled
            val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager
            val bluetoothAdapter = bluetoothManager?.adapter
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                @Suppress("DEPRECATION")
                bluetoothAdapter.disable()
                Log.d("MainActivity", "Bluetooth adapter disabled immediately.")
            }
            Log.d("MainActivity", "Bluetooth turned off, but user can toggle it after provisioning.")
        } catch (e: SecurityException) {
            Log.e("MainActivity", "Failed to apply Bluetooth policies", e)
        }

        // 2. Set Screen Brightness to 90%
        val brightnessValue = 230
        try {
            dpm.setPermissionGrantState(
                adminComponentName,
                packageName,
                Manifest.permission.WRITE_SETTINGS,
                DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
            )
            Log.d("MainActivity", "WRITE_SETTINGS permission granted by DPM before setting brightness.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to grant WRITE_SETTINGS permission via DPM", e)
        }
        if (Settings.System.canWrite(this)) {
            Log.d("MainActivity", "App has WRITE_SETTINGS permission, setting brightness to $brightnessValue")
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessValue
            )
            val actual = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, -1)
            Log.d("MainActivity", "Screen brightness set to $actual (requested $brightnessValue)")
        } else {
            Log.w("MainActivity", "App still does not have WRITE_SETTINGS permission after DPM grant. Brightness not set.")
        }
    }
}