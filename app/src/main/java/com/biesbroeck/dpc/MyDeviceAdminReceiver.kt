package com.biesbroeck.dpc

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi

class MyDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "MyDeviceAdminReceiver"

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, MyDeviceAdminReceiver::class.java)
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "Device admin enabled")
        // This is called when the admin is enabled, but provisioning might not be fully complete yet
        // for all settings to be applied reliably, especially for Device Owner.
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Log.d(TAG, "Profile provisioning complete. Applying policies.")
        // This is primarily for Profile Owner scenarios.
        // For Device Owner, you might need a different approach or to ensure this is called.
        applyDevicePolicies(context)
    }

    // It's common for the provisioning process to launch an activity in your app
    // once provisioning is successful. You could also apply policies there.

    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyDevicePolicies(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponentName = getComponentName(context)

        // Ensure this app is the active admin and device owner
        if (dpm.isAdminActive(adminComponentName) && dpm.isDeviceOwnerApp(context.packageName)) {
            Log.d(TAG, "App is device owner. Applying policies.")

            // 1. Disable Bluetooth
            try {
                dpm.setBluetoothContactSharingDisabled(adminComponentName, true)
                dpm.addUserRestriction(adminComponentName, android.os.UserManager.DISALLOW_BLUETOOTH)
                dpm.addUserRestriction(adminComponentName, android.os.UserManager.DISALLOW_BLUETOOTH_SHARING)
                // Immediately turn off Bluetooth if enabled
                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                // BluetoothAdapter.disable() is deprecated, but still required for immediate shutdown on all supported Android versions
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                    @Suppress("DEPRECATION")
                    bluetoothAdapter.disable()
                    Log.d(TAG, "Bluetooth adapter disabled immediately.")
                }
                Log.d(TAG, "Bluetooth restrictions applied.")
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to apply Bluetooth policies", e)
            }


            // 2. Set Screen Brightness to 90%
            val brightnessValue = 230
            try {
                // Always grant WRITE_SETTINGS permission first
                dpm.setPermissionGrantState(
                    adminComponentName,
                    context.packageName,
                    android.Manifest.permission.WRITE_SETTINGS,
                    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                )
                Log.d(TAG, "WRITE_SETTINGS permission granted by DPM before setting brightness.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to grant WRITE_SETTINGS permission via DPM", e)
            }
            // Now check and set brightness
            if (Settings.System.canWrite(context)) {
                Log.d(TAG, "App has WRITE_SETTINGS permission, setting brightness to $brightnessValue")
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
                )
                val actual = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, -1)
                Log.d(TAG, "Screen brightness set to $actual (requested $brightnessValue)")
            } else {
                Log.w(TAG, "App still does not have WRITE_SETTINGS permission after DPM grant. Brightness not set.")
            }
        } else {
            Log.w(TAG, "App is not device owner or not active admin. Policies not applied.")
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "Device admin disabled")
    }
}