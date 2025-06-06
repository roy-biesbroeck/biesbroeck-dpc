import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences // For persistence
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import com.biesbroeck.dpc.MyDeviceAdminReceiver
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponentName: ComponentName
    private lateinit var prefs: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponentName = MyDeviceAdminReceiver.getComponentName(this)
        prefs = getSharedPreferences("DpcPrefs", Context.MODE_PRIVATE)

        // Check if app is device owner and policies need to be applied
        if (dpm.isDeviceOwnerApp(packageName) && !policiesApplied()) {
            Log.d("MainActivity", "Device owner detected, applying policies from MainActivity.")
            // No need to call applyDevicePoliciesFromActivity, handled by DeviceAdminReceiver
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

    // Removed: applyDevicePoliciesFromActivity and activateDeviceAdmin, as well as all Compose UI code for manual actions
}