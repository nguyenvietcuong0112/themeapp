package com.themes.diy.widgets.keyboard.controlcenter.feature_control_center

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.themes.diy.widgets.keyboard.controlcenter.R
import com.themes.diy.widgets.keyboard.controlcenter.core.utils.PermissionDetector
import com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.service.ControlCenterAccessibilityService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial

class ControlCenterPermissionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var btnCloseDialog: ImageView
    private lateinit var switchAccessibility: SwitchMaterial
    private lateinit var switchWriteSettings: SwitchMaterial
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var switchOverlay: SwitchMaterial

    var onPermissionUpdated: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_control_center_permission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnCloseDialog = view.findViewById(R.id.btnCloseDialog)
        switchAccessibility = view.findViewById(R.id.switchAccessibility)
        switchWriteSettings = view.findViewById(R.id.switchWriteSettings)
        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchOverlay = view.findViewById(R.id.switchOverlay)

        btnCloseDialog.setOnClickListener {
            dismiss()
        }

        setupSwitchListeners(view)
        updateSwitchStates()
    }

    override fun onResume() {
        super.onResume()
        updateSwitchStates()
    }

    private fun setupSwitchListeners(view: View) {
        // 1. Accessibility Service
        val cardAcc = view.findViewById<View>(R.id.layoutCardAccessibility)
        val onAccClick = View.OnClickListener {
            if (!isAccessibilityGranted()) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)

                val act = activity ?: return@OnClickListener
                PermissionDetector.startDetectingPermission(
                    activity = act,
                    checkPermission = { isAccessibilityGranted() },
                    onGranted = {
                        updateSwitchStates()
                        onPermissionUpdated?.invoke()
                        Toast.makeText(context, "Dịch vụ trợ năng đã được bật!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        cardAcc.setOnClickListener(onAccClick)
        switchAccessibility.setOnClickListener(onAccClick)

        // 2. Write Settings
        val cardSettings = view.findViewById<View>(R.id.layoutCardWriteSettings)
        val onSettingsClick = View.OnClickListener {
            if (!isWriteSettingsGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:${requireContext().packageName}")
                    )
                    startActivity(intent)

                    val act = activity ?: return@OnClickListener
                    PermissionDetector.startDetectingPermission(
                        activity = act,
                        checkPermission = { isWriteSettingsGranted() },
                        onGranted = {
                            updateSwitchStates()
                            onPermissionUpdated?.invoke()
                            Toast.makeText(context, "Cài đặt hệ thống đã được cấp quyền!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
        cardSettings.setOnClickListener(onSettingsClick)
        switchWriteSettings.setOnClickListener(onSettingsClick)

        // 3. Notifications
        val cardNotif = view.findViewById<View>(R.id.layoutCardNotifications)
        val onNotifClick = View.OnClickListener {
            if (!isNotificationsGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 102)
                } else {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    }
                    startActivity(intent)
                }
            }
        }
        cardNotif.setOnClickListener(onNotifClick)
        switchNotifications.setOnClickListener(onNotifClick)

        // 4. Display Over Other Apps (Overlay)
        val cardOverlay = view.findViewById<View>(R.id.layoutCardOverlay)
        val onOverlayClick = View.OnClickListener {
            if (!isOverlayGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${requireContext().packageName}")
                    )
                    startActivity(intent)

                    val act = activity ?: return@OnClickListener
                    PermissionDetector.startDetectingPermission(
                        activity = act,
                        checkPermission = { isOverlayGranted() },
                        onGranted = {
                            updateSwitchStates()
                            onPermissionUpdated?.invoke()
                            Toast.makeText(context, "Quyền hiển thị trên ứng dụng khác đã được cấp!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
        cardOverlay.setOnClickListener(onOverlayClick)
        switchOverlay.setOnClickListener(onOverlayClick)
    }

    private fun updateSwitchStates() {
        val context = context ?: return

        switchAccessibility.isChecked = isAccessibilityGranted()
        switchWriteSettings.isChecked = isWriteSettingsGranted()
        switchNotifications.isChecked = isNotificationsGranted()
        switchOverlay.isChecked = isOverlayGranted()
    }

    private fun isAccessibilityGranted(): Boolean {
        val context = context ?: return false
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val myService = "${context.packageName}/${ControlCenterAccessibilityService::class.java.name}"
        return enabledServices.any { 
            val id = it.id
            id.contains(context.packageName) && id.contains("ControlCenterAccessibilityService")
        }
    }

    private fun isWriteSettingsGranted(): Boolean {
        val context = context ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    private fun isNotificationsGranted(): Boolean {
        val context = context ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun isOverlayGranted(): Boolean {
        val context = context ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    override fun onDestroy() {
        PermissionDetector.stopDetecting()
        super.onDestroy()
    }

    companion object {
        fun newInstance(): ControlCenterPermissionBottomSheet {
            return ControlCenterPermissionBottomSheet()
        }
    }
}
