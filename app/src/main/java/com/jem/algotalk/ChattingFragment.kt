package com.jem.algotalk

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [ChattingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class ChattingFragment : Fragment() {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.

    private val USER = 0
    private val BOT = 1
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var editText: EditText
    private lateinit var sendButton: FloatingActionButton
    private lateinit var container: ViewGroup
    private lateinit var inflater: LayoutInflater
    private lateinit var activity: Activity


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chatting, container, false);
        val chattingScrollView = view.findViewById<NestedScrollView>(R.id.chattingScrollView)
        activity = context as Activity
        this.inflater=inflater
        if (container != null) {
            this.container=container
        }
        editText = view.findViewById(R.id.editText_chattingArea)
        chattingScrollView.post { chattingScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        sendButton = view.findViewById(R.id.send_button)
        sendButton.setOnClickListener {
            sendMessage(view)
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    fun sendMessage(view:View) {
        val message = editText.text.toString()
        val date = Date(System.currentTimeMillis())
        if (message.trim().isEmpty())
            Toast.makeText(activity, "enter message", Toast.LENGTH_SHORT).show()
        showTextView(message, USER,view)
        Log.e("MSg", "massage: $message")
        editText.setText("")
    }

    fun showTextView(message: String, position: Int,view:View) {
        //val inflater = LayoutInflater.from(activity)
        var frameLayout: FrameLayout? = null
        val linearLayoutMain = view.findViewById<LinearLayout>(R.id.chatting_layout)
        when (position) {
            USER -> {
                frameLayout = getUserLayout()
            }
            BOT -> {
                frameLayout = getBotLayout()
            }
        }

        frameLayout?.isFocusableInTouchMode = true
        linearLayoutMain.addView(frameLayout)
        val messageTextView = frameLayout?.findViewById<TextView>(R.id.chatting_message)
        messageTextView?.setText(message)
        frameLayout?.requestFocus()
        editText.requestFocus()
        val date = Date(System.currentTimeMillis());
        val dateFormat = SimpleDateFormat(
            "hh:mm aa",
            Locale.ENGLISH
        )
        val time = dateFormat.format(date)
        val timeTextView = frameLayout?.findViewById<TextView>(R.id.message_time)
        timeTextView?.setText(time.toString())
    }

    fun getUserLayout(): FrameLayout? {
        //val inflater = LayoutInflater.from(activity)
        return inflater.inflate(R.layout.user_message_area, null) as FrameLayout?
    }

    fun getBotLayout(): FrameLayout? {
        //val inflater = LayoutInflater.from(activity)
        return inflater.inflate(R.layout.bot_message_area, null) as FrameLayout?
    }
}
