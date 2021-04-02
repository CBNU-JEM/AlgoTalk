package com.jem.algotalk

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa){
    val pageCount = 2

    override fun createFragment(position: Int): Fragment {
        Log.i("sangeun", "createFragment")
        return when(position){
            0 -> BookmarkFragment()
            1 -> ChatFragment()
            else -> ErrorFragment()
        }
    }

    override fun getItemCount():Int = pageCount
}