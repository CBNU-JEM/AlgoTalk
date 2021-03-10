package com.jem.algotalk

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tabLayoutTextArray = arrayOf("즐겨찾기", "채팅")
        val tabLayoutColorArray = arrayOf(R.color.light_green, R.color.light_green)
        //val tabLayoutIconArray = arrayOf(R.drawable.ic_view_list_48px,R.drawable.ic_info_48px)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager2 = findViewById<ViewPager2>(R.id.viewPager2)

        val fgAdapter = ViewPagerAdapter(this)

        viewPager2.adapter = fgAdapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = tabLayoutTextArray[position]
            //tab.view.setBackgroundColor(tabLayoutColorArray[position])
            //tab.setIcon(tabLayoutIconArray[position])
        }.attach()
    }
}
