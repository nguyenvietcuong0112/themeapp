package com.app.personalization.presentation.theme

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide

class EmojiPanelContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onItemClickListener: ((String) -> Unit)? = null
    var onStickerClickListener: ((String) -> Unit)? = null

    private val tabContainer: LinearLayout
    private val contentFrame: FrameLayout
    private val gridView: GridView

    private val emojis = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
        "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
        "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
        "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬",
        "👍", "👎", "👊", "✊", "🤛", "🤜", "🤞", "✌️", "🤟", "🤘",
        "👌", "👈", "👉", "👆", "👇", "☝️", "✋", "🤚", "🖐", "🖖",
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔"
    )

    private val kaomojis = listOf(
        "(*^ω^*)", "(^_−)☆", "(o^▽^o)", "(✿◠‿◠)", "(◡‿◡✿)",
        "(っ•́｡•́)っ", "(*¯ ³¯*)♡", "(๑˃̵ᴗ˂̵)", "ヽ(>∀<☆)ノ", "o(≧▽≦)o",
        "(╯°□°)╯︵ ┻━┻", "┬─┬ノ( º _ ºノ)", "(´｡• ᵕ •｡`)", "(^_-)", "(o_O)",
        "(❤ω❤)", "(★ω★)", "(˘³˘)♥", "(´• ω •`) ♡", "( ´ ▽ ` )"
    )

    private var stickersList = ArrayList<String>()

    init {
        orientation = VERTICAL
        setBackgroundColor(0xFF1E1E2E.toInt())
        val density = resources.displayMetrics.density
        val padding = (8 * density).toInt()
        setPadding(padding, padding, padding, padding)

        tabContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, padding)
        }
        addView(tabContainer)

        contentFrame = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (200 * density).toInt())
        }
        addView(contentFrame)

        gridView = GridView(context).apply {
            numColumns = 6
            horizontalSpacing = (8 * density).toInt()
            verticalSpacing = (8 * density).toInt()
            stretchMode = GridView.STRETCH_COLUMN_WIDTH
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        contentFrame.addView(gridView)

        loadStickers()
        setupTabs()
        showTab(0)
    }

    private fun loadStickers() {
        stickersList.clear()
        try {
            val root = "themes/stickers"
            val fileList = context.assets.list(root) ?: emptyArray()
            for (f in fileList) {
                if (f.endsWith(".png") || f.endsWith(".jpg")) {
                    stickersList.add("$root/$f")
                } else {
                    val subFiles = context.assets.list("$root/$f") ?: emptyArray()
                    for (sf in subFiles) {
                        if (sf.endsWith(".png") || sf.endsWith(".jpg")) {
                            stickersList.add("$root/$f/$sf")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (stickersList.isEmpty()) {
            stickersList.add("themes/stickers/sticker7/029-weather.png")
        }
    }

    private fun setupTabs() {
        val tabNames = listOf("Emojis", "Kaomojis", "Stickers")
        for (i in tabNames.indices) {
            val btn = Button(context).apply {
                text = tabNames[i]
                textSize = 12f
                setTextColor(Color.WHITE)
                setBackgroundColor(0xFF2E2E3E.toInt())
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    leftMargin = 8
                    rightMargin = 8
                }
                setOnClickListener {
                    showTab(i)
                }
            }
            tabContainer.addView(btn)
        }
    }

    private fun showTab(tabIndex: Int) {
        for (i in 0 until tabContainer.childCount) {
            val child = tabContainer.getChildAt(i) as? Button
            child?.setBackgroundColor(if (i == tabIndex) 0xFF00E5FF.toInt() else 0xFF2E2E3E.toInt())
            child?.setTextColor(if (i == tabIndex) Color.BLACK else Color.WHITE)
        }

        when (tabIndex) {
            0 -> {
                gridView.numColumns = 6
                gridView.adapter = EmojiAdapter(emojis)
            }
            1 -> {
                gridView.numColumns = 3
                gridView.adapter = KaomojiAdapter(kaomojis)
            }
            2 -> {
                gridView.numColumns = 4
                gridView.adapter = StickerAdapter(stickersList)
            }
        }
    }

    private inner class EmojiAdapter(val list: List<String>) : BaseAdapter() {
        override fun getCount(): Int = list.size
        override fun getItem(position: Int): Any = list[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val tv = (convertView as? TextView) ?: TextView(context).apply {
                textSize = 28f
                gravity = Gravity.CENTER
                layoutParams = AbsListViewLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120)
            }
            val text = list[position]
            tv.text = text
            tv.setOnClickListener {
                onItemClickListener?.invoke(text)
            }
            return tv
        }
    }

    private inner class KaomojiAdapter(val list: List<String>) : BaseAdapter() {
        override fun getCount(): Int = list.size
        override fun getItem(position: Int): Any = list[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val tv = (convertView as? TextView) ?: TextView(context).apply {
                textSize = 14f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setBackgroundColor(0xFF2E2E3E.toInt())
                setPadding(16, 24, 16, 24)
            }
            val text = list[position]
            tv.text = text
            tv.setOnClickListener {
                onItemClickListener?.invoke(text)
            }
            return tv
        }
    }

    private inner class StickerAdapter(val list: List<String>) : BaseAdapter() {
        override fun getCount(): Int = list.size
        override fun getItem(position: Int): Any = list[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val iv = (convertView as? ImageView) ?: ImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                layoutParams = AbsListViewLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
                setPadding(8, 8, 8, 8)
                setBackgroundColor(0xFF1E1E2E.toInt())
            }
            val assetPath = list[position]
            Glide.with(context)
                .load("file:///android_asset/$assetPath")
                .into(iv)

            iv.setOnClickListener {
                onStickerClickListener?.invoke(assetPath)
            }
            return iv
        }
    }

    private class AbsListViewLayoutParams(width: Int, height: Int) :
        android.widget.AbsListView.LayoutParams(width, height)
}
