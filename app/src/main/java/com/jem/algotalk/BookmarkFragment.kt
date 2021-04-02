package com.jem.algotalk

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [BookmarkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class BookmarkFragment : Fragment() {
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var container: ViewGroup
    private lateinit var inflater: LayoutInflater
    private lateinit var activity: Activity


    //디비헬퍼, 디비 선언
    private lateinit var dbHelper : FeedReaderDbHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        val chattingScrollView = view.findViewById<NestedScrollView>(R.id.chatScrollView)
        activity = context as Activity
        dbHelper = FeedReaderDbHelper(requireContext())

        this.inflater=inflater
        if (container != null) {
            this.container=container
        }

        chattingScrollView.post { chattingScrollView.fullScroll(ScrollView.FOCUS_DOWN) }

        val bookmark: MutableList<Bookmark> = dbHelper.readBookmark(view)
        for (i in 0 until bookmark.size){
            val date = Date(System.currentTimeMillis())
            showTextView(bookmark[i].content, date.toString(), view)
        }
        Log.i("sangeun", "북마m 뷰 create")
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("sangeun", "onDestroyView")
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
        Log.i("sangeun", "onDestroy")
    }

    fun showTextView(message: String, date: String, view:View) {
        var frameLayout: FrameLayout? = null
        frameLayout = getBotLayout()
        val linearLayout = view.findViewById<LinearLayout>(R.id.chat_layout)
        frameLayout?.isFocusableInTouchMode = true
        linearLayout.addView(frameLayout)
        val messageTextView = frameLayout?.findViewById<TextView>(R.id.chat_message)
        messageTextView?.setText(message)
        frameLayout?.requestFocus()
        //editText.requestFocus()
        dbHelper = FeedReaderDbHelper(requireContext())

        //Log.i("sangeun", "메세지 출력 확인")
        val bookmarkbutton = frameLayout?.findViewById<CheckBox>(R.id.star_button)
        bookmarkbutton?.isChecked=true
        bookmarkbutton?.setOnClickListener { view ->
            if (!bookmarkbutton.isChecked)
                dbHelper.deleteBookmark(message)
            else
                dbHelper.insertBookmark(message)
        }

        val currentDateTime = Date(System.currentTimeMillis())
        val dateNew = Date(date)
        val dateFormat = SimpleDateFormat("dd-MM-YYYY", Locale.ENGLISH)
        val currentDate = dateFormat.format(currentDateTime)
        val providedDate = dateFormat.format(dateNew)
        var time = ""
        if(currentDate.equals(providedDate)) {
            val timeFormat = SimpleDateFormat(
                "hh:mm aa",
                Locale.ENGLISH
            )
            time = timeFormat.format(dateNew)
        }else{
            val dateTimeFormat = SimpleDateFormat(
                "dd-MM-yy hh:mm aa",
                Locale.ENGLISH
            )
            time = dateTimeFormat.format(dateNew)
        }
        val timeTextView = frameLayout?.findViewById<TextView>(R.id.message_time)
        timeTextView?.setText(time.toString())
    }

    fun getBotLayout(): FrameLayout? {
        val inflater = LayoutInflater.from(activity)
        return inflater.inflate(R.layout.bot_message_area, null) as FrameLayout?
    }
}