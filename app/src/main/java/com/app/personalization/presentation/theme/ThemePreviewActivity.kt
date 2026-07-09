package com.app.personalization.presentation.theme

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.app.personalization.R

class ThemePreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_preview)

        val themeId = intent.getStringExtra("theme_id") ?: ""

        val ivClose = findViewById<View>(R.id.ivClose)
        ivClose?.setOnClickListener {
            finish()
        }

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager?.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 1
            override fun createFragment(position: Int): Fragment {
                return ThemePreviewFragment.newInstance(themeId)
            }
        }
    }
}
