package com.biesbroeck.dpc

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi

class ComplianceActivity : Activity() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val action = intent.action
        Log.i("ComplianceActivity", "Received intent with action: $action")

        if (action != null) {
            when (action) {
                DevicePolicyManager.ACTION_GET_PROVISIONING_MODE -> {
                    val allowedModes = intent.getIntegerArrayListExtra(
                        DevicePolicyManager.EXTRA_PROVISIONING_ALLOWED_PROVISIONING_MODES
                    )
                    Log.i("ComplianceActivity", "Allowed provisioning modes: $allowedModes")

                    val resultData = Intent().apply {
                        putExtra(
                            DevicePolicyManager.EXTRA_PROVISIONING_MODE,
                            DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE
                        )
                    }
                    Log.i("ComplianceActivity", "Returning provisioning mode: FULLY_MANAGED_DEVICE")
                    setResult(RESULT_OK, resultData)
                }

                DevicePolicyManager.ACTION_ADMIN_POLICY_COMPLIANCE -> {
                    Log.i("ComplianceActivity", "Handling ADMIN_POLICY_COMPLIANCE: returning OK")
                    setResult(RESULT_OK)
                }

                else -> {
                    Log.w("ComplianceActivity", "Unhandled action: $action")
                    setResult(Activity.RESULT_CANCELED)
                }
            }
        } else {
            Log.w("ComplianceActivity", "Intent action was null")
            setResult(Activity.RESULT_CANCELED)
        }

        finish()
    }
}
