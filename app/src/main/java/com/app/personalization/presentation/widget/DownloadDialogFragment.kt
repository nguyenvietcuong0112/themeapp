package com.app.personalization.presentation.widget

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.app.personalization.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadDialogFragment : DialogFragment() {

    interface DownloadCallback {
        fun onDownloadComplete(bitmap: Bitmap)
        fun onDownloadFailed()
    }

    private var downloadUrl: String = ""
    private var callback: DownloadCallback? = null

    fun setParams(url: String, callback: DownloadCallback) {
        this.downloadUrl = url
        this.callback = callback
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.fragment_download_dialog, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.pbLoading)
        val tvProgress = view.findViewById<TextView>(R.id.tvProgress)

        progressBar?.isIndeterminate = false
        progressBar?.max = 100

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(downloadUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Server HTTP ${connection.responseCode}")
                }

                val fileLength = connection.contentLength
                val input = connection.inputStream
                val output = ByteArrayOutputStream()

                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count
                    if (fileLength > 0) {
                        val progress = (total * 100 / fileLength).toInt()
                        withContext(Dispatchers.Main) {
                            progressBar?.progress = progress
                            tvProgress?.text = "$progress%"
                        }
                    }
                    output.write(data, 0, count)
                }

                val bytes = output.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                
                withContext(Dispatchers.Main) {
                    dismissAllowingStateLoss()
                    if (bitmap != null) {
                        callback?.onDownloadComplete(bitmap)
                    } else {
                        callback?.onDownloadFailed()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    dismissAllowingStateLoss()
                    callback?.onDownloadFailed()
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()
    }
}
