package com.app.personalization.feature_control_center

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.app.personalization.R
import com.app.personalization.feature_collections.data.CollectionItem
import com.app.personalization.feature_collections.data.CollectionRepository
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.coroutines.launch

class ControlCenterPreviewBottomSheet : BottomSheetDialogFragment() {

    private var item: CollectionItem? = null

    fun setItem(item: CollectionItem) {
        this.item = item
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_control_center_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentItem = item ?: return dismiss()

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val ivPreview = view.findViewById<RoundedImageView>(R.id.ivPreview)
        val btnApply = view.findViewById<View>(R.id.btnApply)

        tvTitle.text = currentItem.name

        val previewUrl = if (currentItem.targetPath.startsWith("assets_control_center")) {
            "file:///android_asset/${currentItem.targetPath}/thumb.webp"
        } else {
            currentItem.previewPath
        }

        Glide.with(this)
            .load(previewUrl)
            .placeholder(R.drawable.bg_collection_thumb_card)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(ivPreview)

        btnApply.setOnClickListener {
            ControlCenterPreviewActivity.start(requireContext(), currentItem.targetPath, currentItem.name)
            dismiss()
        }
    }

    companion object {
        fun newInstance(item: CollectionItem): ControlCenterPreviewBottomSheet {
            return ControlCenterPreviewBottomSheet().apply {
                setItem(item)
            }
        }
    }
}
