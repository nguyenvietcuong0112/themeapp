package com.app.personalization.feature_main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.personalization.feature_theme.ThemeFragment
import com.app.personalization.feature_icon.IconFragment
import com.app.personalization.feature_control_center.ControlCenterFragment
import com.app.personalization.feature_widget.WidgetHomeFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ThemeFragment.newInstance()
            1 -> IconFragment.newInstance()
            2 -> ControlCenterFragment.newInstance()
            3 -> WidgetHomeFragment.newInstance()
            else -> CollectionsFragment.newInstance()
        }
    }
}
