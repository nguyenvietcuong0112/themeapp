package com.app.personalization.presentation.wallpaper

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.data.database.DiyDatabase
import com.app.personalization.data.database.entity.Design
import com.app.personalization.databinding.FragmentMyDiyWallpaperBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Màn hình danh sách bản nháp hình nền tự thiết kế (MyDiyWallpaperFragment).
 */
class MyDiyWallpaperFragment : Fragment() {

    private var _binding: FragmentMyDiyWallpaperBinding? = null
    private val binding get() = _binding!!

    private val designsList = mutableListOf<Design>()
    private lateinit var adapter: MyDiyWallpaperAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyDiyWallpaperBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Phát hiện Máy tính bảng để tự động tăng số cột Grid
        val isTablet = resources.configuration.smallestScreenWidthDp >= 600
        val spanCount = if (isTablet) 3 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)

        adapter = MyDiyWallpaperAdapter(designsList, 
            onEdit = { design ->
                val intent = Intent(context, CreateWallpaperActivity::class.java).apply {
                    putExtra("INTENT_DESIGN_DATA", design)
                }
                startActivity(intent)
            },
            onDelete = { design ->
                deleteDesign(design)
            },
            onSetWallpaper = { design ->
                applyWallpaper(design)
            }
        )
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            // Khởi chạy tạo mới thiết kế
            val intent = Intent(context, CreateWallpaperActivity::class.java)
            startActivity(intent)
        }

        loadDrafts()
    }

    override fun onResume() {
        super.onResume()
        loadDrafts()
    }

    private fun loadDrafts() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = DiyDatabase.getDatabase(requireContext()).diyDao().getAllDesigns()
            withContext(Dispatchers.Main) {
                designsList.clear()
                designsList.addAll(list)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun deleteDesign(design: Design) {
        lifecycleScope.launch(Dispatchers.IO) {
            DiyDatabase.getDatabase(requireContext()).diyDao().deleteDesign(design)
            // Xóa file ảnh snapshot tương ứng để giải phóng dung lượng
            try {
                if (design.snapshot.isNotEmpty()) {
                    val file = File(design.snapshot)
                    if (file.exists()) file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            loadDrafts()
        }
    }

    private fun applyWallpaper(design: Design) {
        if (design.snapshot.isEmpty()) {
            Toast.makeText(context, "Không tìm thấy tệp ảnh preview!", Toast.LENGTH_SHORT).show()
            return
        }
        val file = File(design.snapshot)
        if (!file.exists()) {
            Toast.makeText(context, "Tệp ảnh preview không tồn tại!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                val bottomSheet = SetWallpaperBottomSheet(bitmap)
                bottomSheet.show(parentFragmentManager, "SetWallpaperBottomSheet")
            } else {
                Toast.makeText(context, "Lỗi khi giải mã hình ảnh!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Lỗi khi áp dụng hình nền!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter nội bộ
    private class MyDiyWallpaperAdapter(
        private val items: List<Design>,
        private val onEdit: (Design) -> Unit,
        private val onDelete: (Design) -> Unit,
        private val onSetWallpaper: (Design) -> Unit
    ) : RecyclerView.Adapter<MyDiyWallpaperAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_my_diy_wallpaper,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item, onEdit, onDelete, onSetWallpaper)
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            private val ivEdit: ImageView = view.findViewById(R.id.ivEdit)
            private val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
            private val btnSetWallpaper: Button = view.findViewById(R.id.btnSetWallpaper)

            fun bind(
                item: Design,
                onEdit: (Design) -> Unit,
                onDelete: (Design) -> Unit,
                onSetWallpaper: (Design) -> Unit
            ) {
                // Hiển thị ảnh chụp màn hình bản thiết kế đã lưu
                if (item.snapshot.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(File(item.snapshot))
                        .centerCrop()
                        .into(ivPreview)
                } else {
                    ivPreview.setImageResource(android.R.color.darker_gray)
                }

                ivEdit.setOnClickListener { onEdit(item) }
                ivDelete.setOnClickListener { onDelete(item) }
                btnSetWallpaper.setOnClickListener { onSetWallpaper(item) }
            }
        }
    }
}
