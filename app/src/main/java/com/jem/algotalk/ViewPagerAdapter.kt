package com.jem.algotalk

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa){
    private val pageCount = 2

    override fun createFragment(position: Int): Fragment {
        Log.i("sangeun", "createFragment")
        return when(position){
            0 -> ChatFragment()
            1 -> BookmarkFragment()
            else -> ErrorFragment()
        }
    }

    override fun getItemCount():Int = pageCount
}