package com.app.personalization.presentation.icon

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R

class IconChangerActivity : AppCompatActivity() {

    private lateinit var layoutCustomizationPanel: LinearLayout
    private lateinit var ivSelectedAppIcon: ImageView
    private lateinit var tvSelectedAppName: TextView
    private lateinit var tvSelectedAppPackage: TextView
    private lateinit var etCustomName: EditText
    private lateinit var ivCustomIconPreview: ImageView
    private lateinit var btnSelectCustomIcon: Button
    private lateinit var btnCreateShortcut: Button
    private lateinit var rvApps: RecyclerView

    private var selectedAppPackage: String? = null
    private var selectedAppOriginalIcon: Drawable? = null
    private var customIconBitmap: Bitmap? = null

    companion object {
        private const val REQUEST_PICK_CUSTOM_ICON = 6001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_icon_changer)

        layoutCustomizationPanel = findViewById(R.id.layoutCustomizationPanel)
        ivSelectedAppIcon = findViewById(R.id.ivSelectedAppIcon)
        tvSelectedAppName = findViewById(R.id.tvSelectedAppName)
        tvSelectedAppPackage = findViewById(R.id.tvSelectedAppPackage)
        etCustomName = findViewById(R.id.etCustomName)
        ivCustomIconPreview = findViewById(R.id.ivCustomIconPreview)
        btnSelectCustomIcon = findViewById(R.id.btnSelectCustomIcon)
        btnCreateShortcut = findViewById(R.id.btnCreateShortcut)
        rvApps = findViewById(R.id.rvApps)

        setupAppList()
        setupButtons()
    }

    private fun setupAppList() {
        rvApps.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        val sortedApps = resolveInfos.sortedWith(Comparator { a, b ->
            a.loadLabel(pm).toString().compareTo(b.loadLabel(pm).toString(), ignoreCase = true)
        })

        rvApps.adapter = AppListAdapter(sortedApps) { app ->
            selectApp(app)
        }
    }

    private fun selectApp(app: ResolveInfo) {
        val pm = packageManager
        selectedAppPackage = app.activityInfo.packageName
        val appName = app.loadLabel(pm).toString()
        selectedAppOriginalIcon = app.loadIcon(pm)

        layoutCustomizationPanel.visibility = View.VISIBLE
        ivSelectedAppIcon.setImageDrawable(selectedAppOriginalIcon)
        tvSelectedAppName.text = appName
        tvSelectedAppPackage.text = selectedAppPackage

        etCustomName.setText(appName)
        ivCustomIconPreview.setImageDrawable(selectedAppOriginalIcon)
        customIconBitmap = drawableToBitmap(selectedAppOriginalIcon)
    }

    private fun setupButtons() {
        btnSelectCustomIcon.setOnClickListener {
            showIconSourceDialog()
        }

        btnCreateShortcut.setOnClickListener {
            createPinShortcut()
        }
    }

    private fun showIconSourceDialog() {
        val options = arrayOf("Preset Weather Icon", "Preset Clock Dial", "Preset Neon Star", "Choose from Gallery...")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Custom Icon")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    val bmp = BitmapFactory.decodeResource(resources, R.drawable.ic_style_weather)
                    setCustomIcon(bmp)
                }
                1 -> {
                    val bmp = BitmapFactory.decodeResource(resources, R.drawable.widget_clock_1_dial)
                    setCustomIcon(bmp)
                }
                2 -> {
                    val bmp = BitmapFactory.decodeResource(resources, android.R.drawable.btn_star_big_on)
                    setCustomIcon(bmp)
                }
                3 -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"
                    }
                    startActivityForResult(intent, REQUEST_PICK_CUSTOM_ICON)
                }
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun setCustomIcon(bmp: Bitmap) {
        customIconBitmap = bmp
        ivCustomIconPreview.setImageBitmap(bmp)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_CUSTOM_ICON && resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let { uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                try {
                    val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                    val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                    val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                    parcelFileDescriptor?.close()

                    if (bitmap != null) {
                        setCustomIcon(bitmap)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createPinShortcut() {
        val pkg = selectedAppPackage ?: return
        val label = etCustomName.text.toString().trim()
        val bmp = customIconBitmap ?: return

        if (label.isEmpty()) {
            Toast.makeText(this, "Please enter a name for the shortcut", Toast.LENGTH_SHORT).show()
            return
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
        if (launchIntent != null) {
            val shortcutId = "changer_$pkg"
            val builder = ShortcutInfoCompat.Builder(this, shortcutId)
                .setShortLabel(label)
                .setIcon(IconCompat.createWithBitmap(bmp))
                .setIntent(launchIntent)

            ShortcutManagerCompat.requestPinShortcut(this, builder.build(), null)
            Toast.makeText(this, "Shortcut pinned successfully! Check your home screen.", Toast.LENGTH_LONG).show()
            layoutCustomizationPanel.visibility = View.GONE
        } else {
            Toast.makeText(this, "Launch intent not found for this package", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        if (drawable is BitmapDrawable) return drawable.bitmap
        
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    private inner class AppListAdapter(
        private val list: List<ResolveInfo>,
        private val onClick: (ResolveInfo) -> Unit
    ) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivIcon: ImageView = view.findViewById(R.id.ivAppIcon)
            val tvName: TextView = view.findViewById(R.id.tvAppName)
            val tvPackage: TextView = view.findViewById(R.id.tvAppPackage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_custom_app_changer, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = list[position]
            val pm = packageManager
            holder.tvName.text = app.loadLabel(pm).toString()
            holder.tvPackage.text = app.activityInfo.packageName
            holder.ivIcon.setImageDrawable(app.loadIcon(pm))

            holder.itemView.setOnClickListener {
                onClick(app)
            }
        }

        override fun getItemCount(): Int = list.size
    }
}
