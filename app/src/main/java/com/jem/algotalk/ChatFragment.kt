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
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class ChatFragment : Fragment() {
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

    //디비헬퍼, 디비 선언
    private lateinit var dbHelper : FeedReaderDbHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false);
        val chattingScrollView = view.findViewById<NestedScrollView>(R.id.chatScrollView)
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

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    fun sendMessage(view: View) {
        val msg:String = editText.text.toString().trim()
        val date = Date(System.currentTimeMillis())

        //rasa run -m models --enable-api --endpoints endpoints.yml 서버 실행 코드
        val okHttpClient = OkHttpClient()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://algotalk.kro.kr/rasa/webhooks/rest/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val userMessage = UserMessage()
        if(msg.trim().isEmpty())
            Toast.makeText(getActivity(), "Please enter your query", Toast.LENGTH_SHORT).show()
        else {
            Log.e("Msg", "msssage: $msg")
            editText.setText("")
            userMessage.UserMessage("User", msg)
            showTextView(msg, USER, date.toString(), view)

        }
        val messageSender = retrofit.create(MessageSender::class.java)
        val response =
            messageSender.sendMessage(userMessage)

        response.enqueue(object : Callback<List<BotResponse>> {
            override fun onResponse(
                call: Call<List<BotResponse>>,
                response: Response<List<BotResponse>>
            ) {
                if (response.body() == null || response.body()!!.size == 0) {
                    val botMessage = "Sorry didn't understand"
                    showTextView(botMessage, BOT, date.toString(), view)
                } else {
                    response.body()!!.forEach { botResponse ->

                        Log.e("text c", "${botResponse.text}")
                        if (botResponse.text != null) {
                            showTextView(botResponse.text, BOT, date.toString(), view)
                        }

                        Log.e("image c", "${botResponse.image}")
                        if (botResponse.image != null) {
                            showImageView(botResponse.image, BOT, date.toString(), view)
                        }

                        if (botResponse.buttons != null) {
                            Log.e("Button c", "${botResponse.buttons.size}")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<BotResponse>>, t: Throwable) {
                val botMessage = "Check your network connection"
                showTextView(botMessage, BOT, date.toString(), view)
                t.printStackTrace()
                Toast.makeText(getActivity(), "" + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showTextView(message: String, type: Int, date: String, view: View) {
        var frameLayout: FrameLayout? = null
        val linearLayout = view.findViewById<LinearLayout>(R.id.chat_layout)
        when(type){
            USER -> {
                frameLayout = getUserLayout()
            }
            BOT -> {
                frameLayout = getBotLayout()
            }
            else->{
                frameLayout = getBotLayout()
            }
        }
        frameLayout?.isFocusableInTouchMode = true
        linearLayout.addView(frameLayout)
        val messageTextView = frameLayout?.findViewById<TextView>(R.id.chat_message)
        messageTextView?.setText(message)
        frameLayout?.requestFocus()
        editText.requestFocus()
        dbHelper = FeedReaderDbHelper(requireContext())

        Log.i("sangeun", "메세지 출력 확인")
        val bookmarkbutton = frameLayout?.findViewById<CheckBox>(R.id.star_button)

        val bookmark = Bookmark()
        bookmark.content = message
        bookmark.img_uri = "none"

        Log.i("sangeun_con", bookmark.content.toString())
        Log.i("sangeun_img", bookmark.img_uri.toString())

        val bookmark_flag = dbHelper.isAlready(bookmark)

        if (bookmark_flag == 1)
            bookmarkbutton?.isChecked = true

        bookmarkbutton?.setOnClickListener { view ->
            if (bookmarkbutton.isChecked)
                dbHelper.insertBookmark(bookmark)
            else
                dbHelper.deleteBookmark(bookmark)
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



    fun showImageView(message: String, type: Int, date: String, view: View) {
        var frameLayout: FrameLayout? = null
        val linearLayout = view.findViewById<LinearLayout>(R.id.chat_layout)
        when(type){
            USER -> {
                frameLayout = getUserLayout()
            }
            BOT -> {
                frameLayout = getBotLayout("image")
            }
            else->{
                frameLayout = getBotLayout("image")
            }
        }
        frameLayout?.isFocusableInTouchMode = true
        linearLayout.addView(frameLayout)
        val messageImageView = frameLayout?.findViewById<ImageView>(R.id.chat_image_message)

        Glide.with(this).load(message).into(messageImageView!!);

        frameLayout?.requestFocus()
        editText.requestFocus()

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
        val timeTextView = frameLayout?.findViewById<TextView>(R.id.image_message_time)
        timeTextView?.setText(time.toString())
    }

    fun getUserLayout(): FrameLayout? {
        val inflater = LayoutInflater.from(activity)
        return inflater.inflate(R.layout.user_message_area, null) as FrameLayout?
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