package com.jem.algotalk

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import java.util.*
import android.app.Activity

class SettingsFragment : Fragment() {
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var container: ViewGroup
    private lateinit var inflater: LayoutInflater
    private lateinit var activity: Activity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        activity = context as Activity
        this.inflater=inflater
        if (container != null) {
            this.container=container
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}