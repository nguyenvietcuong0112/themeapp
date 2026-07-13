package com.app.personalization.presentation.theme

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.domain.trie.Trie
import java.io.InputStream
import kotlin.concurrent.thread

class SuggestionBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val trie = Trie()
    private val suggestions = ArrayList<String>()
    var onSuggestionClickListener: ((String) -> Unit)? = null

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        adapter = SuggestionAdapter()
        setBackgroundColor(0xFF1E1E2E.toInt())
        isHorizontalScrollBarEnabled = false
    }

    fun loadDictionaryAsync(locale: String) {
        thread {
            try {
                val dictName = "dicts/main_$locale.dict"
                val assetsList = context.assets.list("dicts") ?: emptyArray()
                val targetFile = if (assetsList.contains("main_$locale.dict")) dictName else "dicts/main_en-US.dict"
                
                val inputStream: InputStream = context.assets.open(targetFile)
                val buffer = ByteArray(4096)
                var bytesRead: Int
                val sb = java.lang.StringBuilder()

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    for (i in 0 until bytesRead) {
                        val c = buffer[i].toInt().toChar()
                        if (c.isLetter()) {
                            sb.append(c)
                        } else {
                            if (sb.length in 2..15) {
                                trie.insert(sb.toString().lowercase())
                            }
                            sb.setLength(0)
                        }
                    }
                }
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                val fallbacks = listOf("the", "and", "hello", "keyboard", "personalization", "widget", "wallpaper", "theme")
                for (w in fallbacks) trie.insert(w)
            }
        }
    }

    fun updateSuggestions(prefix: String) {
        if (prefix.isEmpty()) {
            suggestions.clear()
            adapter?.notifyDataSetChanged()
            return
        }

        thread {
            val results = trie.searchPrefix(prefix.lowercase(), 5)
            post {
                suggestions.clear()
                suggestions.addAll(results)
                if (suggestions.isEmpty()) {
                    suggestions.add(prefix)
                }
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private inner class SuggestionAdapter : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvText: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val word = suggestions[position]
            holder.tvText.apply {
                text = word
                setTextColor(Color.WHITE)
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(32, 12, 32, 12)
                gravity = android.view.Gravity.CENTER
            }
            holder.itemView.setOnClickListener {
                onSuggestionClickListener?.invoke(word)
            }
        }

        override fun getItemCount(): Int = suggestions.size
    }
}
