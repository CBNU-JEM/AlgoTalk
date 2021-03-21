package com.jem.algotalk

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa){
    val pageCount = 2
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> BookmarkFragment()
            1 -> ChatFragment()
            else -> ErrorFragment()
        }
    }
    override fun getItemCount():Int = pageCount
}