package com.app.personalization.presentation.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ThemeFragment.newInstance()
            1 -> WallpaperFragment.newInstance()
            2 -> IconFragment.newInstance()
            3 -> ShopFragment.newInstance()
            else -> SettingFragment.newInstance()
        }
    }
}
