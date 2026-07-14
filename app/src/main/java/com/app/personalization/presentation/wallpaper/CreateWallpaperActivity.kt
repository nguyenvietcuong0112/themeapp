package com.app.personalization.presentation.wallpaper

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.CdnPathResolver
import com.app.personalization.data.database.DiyDatabase
import com.app.personalization.data.database.entity.Design
import com.app.personalization.data.database.entity.DesignPage
import com.app.personalization.data.database.entity.DiySticker
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Trình biên tập chỉnh sửa hình nền (CreateWallpaperActivity) hỗ trợ vẽ các PageComponent con
 * và kết nối SQLite lưu trữ trạng thái chỉnh sửa.
 */
class CreateWallpaperActivity : AppCompatActivity() {

    private lateinit var pageWrapperView: PageWrapperView
    private var currentDesign: Design? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wallpaper_draft)

        pageWrapperView = findViewById(R.id.pageWrapperView)

        findViewById<View>(R.id.ivBack).setOnClickListener { finish() }

        // Đọc đối tượng Design từ Intent gửi tới
        currentDesign = intent.getSerializableExtra("INTENT_DESIGN_DATA") as? Design

        loadDesignData()

        setupButtons()
    }

    private fun loadDesignData() {
        val design = currentDesign ?: return
        
        // Dựng lại ảnh nền và các PageComponent của trang đầu tiên
        val pages = design.getPages()
        val firstPage = pages.firstOrNull() ?: return
        
        pageWrapperView.loadPageComponents(firstPage.pageComponents)
    }

    private fun setupButtons() {
        findViewById<View>(R.id.btnBackground).setOnClickListener {
            showBackgroundOptions()
        }

        findViewById<View>(R.id.btnSticker).setOnClickListener {
            showStickerBottomSheet()
        }

        findViewById<View>(R.id.btnText).setOnClickListener {
            showTextEditorDialog()
        }

        findViewById<View>(R.id.btnSave).setOnClickListener {
            saveDesignDraft()
        }
    }

    private fun showBackgroundOptions() {
        val colors = listOf("Aesthetic Blue", "Rose Pink", "Sunset Orange", "Mint Green", "Default Theme")
        val hexColors = listOf("#BBDEFB", "#F8BBD0", "#FFE0B2", "#C8E6C9", "https://csc-themeapp-widget.pages.dev/theme_1/wallpapers/bg_wallpaper.png")

        AlertDialog.Builder(this)
            .setTitle("Chọn màu nền")
            .setItems(colors.toTypedArray()) { _, which ->
                val selection = hexColors[which]
                if (selection.startsWith("http")) {
                    pageWrapperView.setWallpaper(selection)
                } else {
                    pageWrapperView.setBackgroundColor(Color.parseColor(selection))
                }
            }
            .show()
    }

    private fun showStickerBottomSheet() {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.fragment_change_create_theme_icon, null)
        
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val pbCreate: View = view.findViewById(R.id.pbCreate)
        
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        pbCreate.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val list = DiyDatabase.getDatabase(this@CreateWallpaperActivity).diyDao().getAllStickers()
            withContext(Dispatchers.Main) {
                pbCreate.visibility = View.GONE
                recyclerView.adapter = StickerAdapter(list) { sticker ->
                    val url = CdnPathResolver.getDiyStickerUrl(sticker.folderName, sticker.imageName)
                    pageWrapperView.addSticker(url)
                    bottomSheet.dismiss()
                }
            }
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun showTextEditorDialog() {
        val editText = EditText(this).apply {
            hint = "Nhập chữ nghệ thuật..."
            setPadding(32, 32, 32, 32)
        }
        
        AlertDialog.Builder(this)
            .setTitle("Chèn chữ nghệ thuật")
            .setView(editText)
            .setPositiveButton("Chèn") { dialog, _ ->
                val text = editText.text.toString()
                if (text.isNotEmpty()) {
                    pageWrapperView.addText(text, Color.WHITE)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveDesignDraft() {
        pageWrapperView.clearSelections()

        val width = pageWrapperView.width
        val height = pageWrapperView.height
        if (width <= 0 || height <= 0) {
            Toast.makeText(this, "Bản vẽ chưa dựng hình hoàn tất!", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        pageWrapperView.draw(canvas)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val designId = currentDesign?.id ?: UUID.randomUUID()
                val filename = "diy_snap_${designId}.png"
                val file = File(filesDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }

                val components = pageWrapperView.getPageComponents()
                val page = DesignPage(
                    id = UUID.randomUUID().toString(),
                    pageComponents = components
                )
                val pagesList = arrayListOf(page)

                val updatedDesign = Design.create(
                    id = designId,
                    name = "Bản nháp ${designId.toString().take(4)}",
                    snapshot = file.absolutePath,
                    isLiveWallpaper = false,
                    pages = pagesList,
                    templateId = currentDesign?.templateId
                )

                DiyDatabase.getDatabase(this@CreateWallpaperActivity).diyDao().insertDesign(updatedDesign)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateWallpaperActivity, "Đã lưu bản nháp thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateWallpaperActivity, "Lỗi khi lưu bản nháp!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private class StickerAdapter(
        private val list: List<DiySticker>,
        private val onSelected: (DiySticker) -> Unit
    ) : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val imageView = ImageView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (100 * parent.context.resources.displayMetrics.density).toInt()
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                setPadding(12, 12, 12, 12)
            }
            return ViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.bind(item, onSelected)
        }

        override fun getItemCount(): Int = list.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun bind(item: DiySticker, onSelected: (DiySticker) -> Unit) {
                val imageView = itemView as ImageView
                val url = CdnPathResolver.getDiyStickerUrl(item.folderName, item.imageName)
                
                Glide.with(itemView.context)
                    .load(url)
                    .into(imageView)

                itemView.setOnClickListener {
                    onSelected(item)
                }
            }
        }
    }
}
