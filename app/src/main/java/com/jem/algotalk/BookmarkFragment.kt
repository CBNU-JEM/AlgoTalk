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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
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
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_layout)
        val chattingScrollView = view.findViewById<NestedScrollView>(R.id.chatScrollView)
        val linearLayout = view.findViewById<LinearLayout>(R.id.chat_layout)
        activity = context as Activity
        dbHelper = FeedReaderDbHelper(requireContext())

        this.inflater = inflater
        if (container != null) {
            this.container = container
        }

        val startTime = System.currentTimeMillis()
        Log.i("tab click start", startTime.toString())
        this.inflater = inflater
        if (container != null) {
            this.container = container
        }

        chattingScrollView.post { chattingScrollView.fullScroll(ScrollView.FOCUS_DOWN) }

        swipeRefreshLayout.setOnRefreshListener {
            linearLayout.removeAllViews()
            val bookmark: MutableList<Bookmark> = dbHelper.readBookmark(view)
            for (i in 0 until bookmark.size) {
                val date = Date(System.currentTimeMillis())
                if (bookmark[i].content == "none")
                    showImageView(bookmark[i].img_uri, date.toString(), view)
                else
                    showTextView(bookmark[i].content, date.toString(), view)
            }
            chattingScrollView.post { chattingScrollView.fullScroll(ScrollView.FOCUS_DOWN) }

            swipeRefreshLayout.isRefreshing = false
        }

        val endTime = System.currentTimeMillis()
        Log.i("tab click end", endTime.toString())
        Log.i("tab click end", (endTime - startTime).toString())

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

        val bookmark = Bookmark()
        bookmark.content = message
        bookmark.img_uri = "none"

        val bookmarkbutton = frameLayout?.findViewById<CheckBox>(R.id.star_button)
        bookmarkbutton?.isChecked=true
        bookmarkbutton?.setOnClickListener { view ->
            if (!bookmarkbutton.isChecked)
                dbHelper.deleteBookmark(bookmark)
            else
                dbHelper.insertBookmark(bookmark)
        }

        val timeTextView = frameLayout?.findViewById<TextView>(R.id.message_time)
        timeTextView?.visibility = View.GONE
    }

    fun showImageView(message: String, date: String, view: View) {
        var frameLayout: FrameLayout? = null
        val linearLayout = view.findViewById<LinearLayout>(R.id.chat_layout)
        frameLayout = getBotLayout("image")

        frameLayout?.isFocusableInTouchMode = true
        linearLayout.addView(frameLayout)
        val messageImageView = frameLayout?.findViewById<ImageView>(R.id.chat_image_message)

        Glide.with(this).load(message).into(messageImageView!!);

        frameLayout?.requestFocus()
        //editText.requestFocus()

        dbHelper = FeedReaderDbHelper(requireContext())

        Log.i("sangeun", "이미지 출력 확인")
        val bookmarkbutton = frameLayout?.findViewById<CheckBox>(R.id.star_button)

        val bookmark = Bookmark()
        bookmark.content = "none"
        bookmark.img_uri = message

        Log.i("sangeun", bookmark.toString())

        val bookmark_flag = dbHelper.isAlready(bookmark)

        if (bookmark_flag == 1)
            bookmarkbutton?.isChecked = true

        bookmarkbutton?.setOnClickListener { view ->
            if (bookmarkbutton.isChecked)
                dbHelper.insertBookmark(bookmark)
            else
                dbHelper.deleteBookmark(bookmark)
        }

        val timeTextView = frameLayout?.findViewById<TextView>(R.id.image_message_time)
        timeTextView?.visibility = View.GONE
    }

    fun getBotLayout(): FrameLayout? {
        val inflater = LayoutInflater.from(activity)
        return inflater.inflate(R.layout.bot_message_area, null) as FrameLayout?
    }

    fun getBotLayout(type: String): FrameLayout? {
        when (type) {
            "image" -> {
                val inflater = LayoutInflater.from(activity)
                return inflater.inflate(R.layout.bot_image_message_area, null) as FrameLayout?
            }
            else -> {
                val inflater = LayoutInflater.from(activity)
                return inflater.inflate(R.layout.bot_message_area, null) as FrameLayout?
            }
        }
    }
}