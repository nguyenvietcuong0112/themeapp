package com.app.personalization.feature_theme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R

class ThemeBannerAdapter(
    private val bannerTypes: List<Int> = listOf(TYPE_BANNER_1, TYPE_BANNER_2, TYPE_BANNER_3),
    private val onBannerClick: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_BANNER_1 = 0
        const val TYPE_BANNER_2 = 1
        const val TYPE_BANNER_3 = 2
    }

    override fun getItemViewType(position: Int): Int {
        val actualPos = position % bannerTypes.size
        return bannerTypes[actualPos]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_BANNER_1 -> {
                val view = inflater.inflate(R.layout.item_banner_theme_1, parent, false)
                Banner1ViewHolder(view)
            }
            TYPE_BANNER_2 -> {
                val view = inflater.inflate(R.layout.item_banner_theme_2, parent, false)
                Banner2ViewHolder(view)
            }
            TYPE_BANNER_3 -> {
                val view = inflater.inflate(R.layout.item_banner_theme_3, parent, false)
                Banner3ViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_banner_theme_1, parent, false)
                Banner1ViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val actualPos = position % bannerTypes.size
        when (holder) {
            is Banner1ViewHolder -> holder.bind(actualPos, onBannerClick)
            is Banner2ViewHolder -> holder.bind(actualPos, onBannerClick)
            is Banner3ViewHolder -> holder.bind(actualPos, onBannerClick)
        }
    }

    override fun getItemCount(): Int {
        return if (bannerTypes.isEmpty()) 0 else Int.MAX_VALUE
    }

    class Banner1ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)

        fun bind(position: Int, onClick: ((Int) -> Unit)?) {
            tvTitle.text = itemView.context.getString(R.string.banner_theme_1_title)
            tvSubtitle.text = itemView.context.getString(R.string.banner_theme_1_subtitle)
            itemView.setOnClickListener {
                onClick?.invoke(position)
            }
        }
    }

    class Banner2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)

        fun bind(position: Int, onClick: ((Int) -> Unit)?) {
            val titleHtml = itemView.context.getString(R.string.banner_theme_2_title)
            tvTitle.text = HtmlCompat.fromHtml(titleHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
            tvSubtitle.text = itemView.context.getString(R.string.banner_theme_2_subtitle)
            itemView.setOnClickListener {
                onClick?.invoke(position)
            }
        }
    }

    class Banner3ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)

        fun bind(position: Int, onClick: ((Int) -> Unit)?) {
            tvTitle.text = itemView.context.getString(R.string.banner_theme_3_title)
            tvSubtitle.text = itemView.context.getString(R.string.banner_theme_3_subtitle)
            itemView.setOnClickListener {
                onClick?.invoke(position)
            }
        }
    }
}
