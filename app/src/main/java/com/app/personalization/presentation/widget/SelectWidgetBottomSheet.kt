package com.app.personalization.presentation.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.app.personalization.R
import com.app.personalization.data.EventBus
import com.app.personalization.data.Subscribe
import com.app.personalization.data.database.entity.KeyboardTheme
import com.app.personalization.databinding.FragmentSelectWidgetBottomSheetBinding
import com.app.personalization.presentation.widget.Widget2x2Provider
import com.app.personalization.presentation.widget.Widget4x2Provider
import com.app.personalization.presentation.widget.Widget4x4Provider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.app.personalization.presentation.widget.event.WidgetEvent

class SelectWidgetBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentSelectWidgetBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var theme: KeyboardTheme
    private lateinit var widgetType: String // "clock", "weather", "calendar"
    private lateinit var size: String // "2x2", "4x2", "4x4"

    fun setParams(theme: KeyboardTheme, widgetType: String, size: String) {
        this.theme = theme
        this.widgetType = widgetType
        this.size = size
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectWidgetBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        binding.adContainer.visibility = View.GONE
        binding.llAction.visibility = View.GONE
        binding.clInstall.visibility = View.VISIBLE
        binding.tvDownload.text = "Add Widget"

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.clInstall.setOnClickListener {
            downloadAndPinWidget()
        }
    }

    private fun downloadAndPinWidget() {
        val activity = requireActivity()
        val fileName = when (size) {
            "2x2" -> "bg.png"
            "4x2" -> "bg_medium.png"
            "4x4" -> "bg_large.png"
            else -> "bg.png"
        }
        val cdnUrl = com.app.personalization.data.ResourceConfig.getWidgetComponentUrl(
            activity, theme.path, widgetType, fileName
        )

        val downloadDialog = DownloadDialogFragment()
        downloadDialog.setParams(cdnUrl, object : DownloadDialogFragment.DownloadCallback {
            override fun onDownloadComplete(bitmap: Bitmap) {
                saveWidgetBackground(activity, bitmap)
                requestPinWidget(activity)
            }

            override fun onDownloadFailed() {
                Toast.makeText(activity, "Failed to download widget assets", Toast.LENGTH_SHORT).show()
            }
        })
        downloadDialog.show(parentFragmentManager, "download")
    }

    private fun saveWidgetBackground(context: Context, bitmap: Bitmap) {
        try {
            val fileName = "widget_bg_${theme.id}_${widgetType}_$size.png"
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("bg_path_${theme.id}_${widgetType}_$size", fileName)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestPinWidget(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val provider = when (size) {
                "2x2" -> ComponentName(context, Widget2x2Provider::class.java)
                "4x2" -> ComponentName(context, Widget4x2Provider::class.java)
                "4x4" -> ComponentName(context, Widget4x4Provider::class.java)
                else -> ComponentName(context, Widget2x2Provider::class.java)
            }

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val successCallback = WidgetReceiver.getPendingIntent(context)
                appWidgetManager.requestPinAppWidget(provider, null, successCallback)
            } else {
                Toast.makeText(context, "Pinning widgets is not supported by your launcher", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Pinning widgets is supported on Android 8.0+", Toast.LENGTH_SHORT).show()
        }
    }

    @Subscribe
    fun onWidgetEvent(event: WidgetEvent) {
        val activity = activity as? FragmentActivity
        dismissAllowingStateLoss()
        if (activity != null) {
            SetupSucceedDialogFragment().show(activity.supportFragmentManager, "success")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        _binding = null
    }
}
