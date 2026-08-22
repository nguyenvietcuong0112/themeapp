package com.themes.diy.widgets.keyboard.controlcenter.feature_main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.themes.diy.widgets.keyboard.controlcenter.feature_theme.ThemeFragment
import com.themes.diy.widgets.keyboard.controlcenter.feature_icon.IconFragment
import com.themes.diy.widgets.keyboard.controlcenter.feature_control_center.ControlCenterFragment
import com.themes.diy.widgets.keyboard.controlcenter.feature_widget.WidgetConfigFragment
import com.themes.diy.widgets.keyboard.controlcenter.feature_collections.CollectionsFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ThemeFragment.newInstance()
            1 -> IconFragment.newInstance()
            2 -> ControlCenterFragment.newInstance()
            3 -> WidgetConfigFragment.newInstance()
            else -> CollectionsFragment.newInstance()
        }
    }
}
