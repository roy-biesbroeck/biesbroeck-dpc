import qrcode
import json

# Provisioning JSON data
provisioning_data = {
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": "com.biesbroeck.dpc/.MyDeviceAdminReceiver",
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": "https://github.com/roy-biesbroeck/biesbroeck-dpc/releases/download/v1.0.10/app-release.apk",
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM": "Pa63XJKGNLwnXtjJCEBjUWm4kxcySH1PZyzcu7Xf8so=",
    "android.app.extra.PROVISIONING_SKIP_ENCRYPTION": True,
    "android.app.extra.PROVISIONING_WIFI_SSID": "UNITOUCH-ROY",
    "android.app.extra.PROVISIONING_WIFI_PASSWORD": "87654321",
    "android.app.extra.PROVISIONING_WIFI_HIDDEN": False,
    "android.app.extra.PROVISIONING_WIFI_SECURITY_TYPE": "WPA",
    "android.app.extra.PROVISIONING_SKIP_EDUCATION_SCREENS": True,
    "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED": True,
    "android.app.extra.PROVISIONING_MODE": "fully_managed_device"
}

# Convert to a compact JSON string
qr_data = json.dumps(provisioning_data, separators=(',', ':'))

# Generate QR code
qr = qrcode.QRCode(
    version=None,
    error_correction=qrcode.constants.ERROR_CORRECT_Q,
    box_size=10,
    border=4,
)
qr.add_data(qr_data)
qr.make(fit=True)

# Create and save QR code image
img = qr.make_image(fill_color="black", back_color="white")
img.save("provisioning_qr.png")

print("✅ QR code saved as provisioning_qr.png")
