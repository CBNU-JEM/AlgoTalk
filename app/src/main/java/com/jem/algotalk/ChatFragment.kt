package com.jem.algotalk

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import kotlinx.coroutines.*
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

    //private lateinit var viewPagerAdapter: ViewPagerAdapter
    //private lateinit var viewPager: ViewPager2
    private lateinit var editText: EditText
    private lateinit var sendButton: FloatingActionButton
    private lateinit var popupButton: ImageButton
    private lateinit var container: ViewGroup
    private lateinit var inflater: LayoutInflater
    private lateinit var activity: Activity

    private lateinit var editUserName: EditText
    private lateinit var spinner_level: Spinner

    var endTime1 = System.currentTimeMillis()
    var startTime1 = System.currentTimeMillis()

    //디비헬퍼, 디비 선언
    private lateinit var dbHelper: FeedReaderDbHelper
    //private lateinit var btnRecyclerView: RecyclerView

    //kakao
    private lateinit var client: SpeechRecognizerClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        val chattingScrollView = view.findViewById<NestedScrollView>(R.id.chatScrollView)
        activity = context as Activity

        this.inflater = inflater
        if (container != null) {
            this.container = container
        }


        editText = view.findViewById(R.id.editText_chattingArea)
        editText.setOnKeyListener { vieww, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                val msg: String = editText.text.toString().trim()
                if (msg != "")
                    sendMessage(view, msg, msg)
                return@setOnKeyListener true
            }
            false
        }

        val opening = "\uD83E\uDD16나와 대화하는 방법에 대해서 알려줄게\uD83E\uDD16\n" +
                "나에게 궁금한 것을 물어봐줘~ 내가 알려줄수 있는 정보는 아래를 확인해줘ㅎㅎ\n" +
                "\n" +
                "\uD83D\uDD38 알고리즘\n" +
                "나는 알고리즘에 대한 설명과 난이도를 알려줄 수 있어.\n" +
                "ex) 정렬 알고리즘 (자세하게) 알려줘, 정렬 알고리즘 난이도 알려줘\n" +
                "\uD83D\uDD38 문제추천\n" +
                "나는 알고리즘에 관련된 문제를 추천해줄 수도 있어ㅎㅎ\n" +
                "ex) A+B 문제 알려줘 -> 난이도 선택버튼, 정렬 알고리즘 문제 추천해줘 -> 난이도 선택버튼, 랜덤으로 문제 추천해줘\n" +
                "\uD83D\uDD38 대회정보\n" +
                "너를 위해 대회 정보도 준비해놨지~ 히힛 \n" +
                "ex) 준파고를 잡아라 대회 알려줘, 진행중인(or 지난 or 최근) 대회 알려줘\n" +
                "\n" +
                "\uD83D\uDD39왼쪽 아래에 더보기버튼을 누르면 너의 알고리즘 수준을 설정할 수 있어! 알고리즘 수준을 모르겠다면 solved.ac 사이트를 참고해줘\uD83D\uDE09"

        val open_date = Date(System.currentTimeMillis())
        showOpeningView(opening, BOT, open_date.toString(), view)

        chattingScrollView.post { chattingScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        sendButton = view.findViewById(R.id.send_button)
        sendButton.setOnClickListener {
            val msg: String = editText.text.toString().trim()
            if (msg != "")
                sendMessage(view, msg, msg)
        }

        popupButton = view.findViewById(R.id.show_user_level_popup)
        popupButton.setOnClickListener {
            val mDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.set_user, null)
            val mBuilder = AlertDialog.Builder(getActivity(), R.style.MyDialogTheme)
                .setView(mDialogView)
                .setTitle("너의 코딩실력을 알려줘 😀")

            val mAlertDialog = mBuilder.show()

            dbHelper = FeedReaderDbHelper(requireContext())

            var old_user = User()
            var new_user = User()

            old_user = dbHelper.readUser(view)

            editUserName = mDialogView.findViewById<EditText>(R.id.edit_user_name)
            spinner_level = mDialogView.findViewById<Spinner>(R.id.spinner_level)

            editUserName.setText(old_user.name)

            val level = resources.getStringArray(R.array.level)
            val adapter = getActivity()?.let { it1 ->
                ArrayAdapter(
                    it1,
                    android.R.layout.simple_spinner_item,
                    level
                )
            }
            spinner_level.adapter = adapter
            //            spinner_level.setAdapter(adapter)
            spinner_level.setSelection(old_user.level.toInt(), true)

            val okButton = mDialogView.findViewById<Button>(R.id.edit_user_level_confirm_Button)
            okButton.setOnClickListener {
                new_user.name = editUserName.getText().toString()
                new_user.level = spinner_level.getSelectedItemId().toString()
                dbHelper.updateUserName(old_user, new_user)
                dbHelper.updateUserLevel(old_user, new_user)
                Toast.makeText(getActivity(), "변경 완료되었습니다.", Toast.LENGTH_SHORT).show()
                mAlertDialog.dismiss()
            }

            val noButton = mDialogView.findViewById<Button>(R.id.close_popup_Button)
            noButton.setOnClickListener {
                mAlertDialog.dismiss()
            }

        }


        return view
    }

    override fun onDestroy() {
        dbHelper.close()
        SpeechRecognizerManager.getInstance().finalizeLibrary()
        super.onDestroy()
    }

    fun sendMessage(view: View, message: String, printMessage: String) {
        dbHelper = FeedReaderDbHelper(requireContext())
        var user_info = User()
        val sender_id = Settings.Secure.getString(
            context!!.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        Log.i("sender_id", sender_id.toString())
        startTime1 = System.currentTimeMillis()
//        val startTime= System.currentTimeMillis()
//        Log.i("server response start",  startTime.toString())
        val date = Date(System.currentTimeMillis())
        //rasa run -m models --enable-api --endpoints endpoints.yml 서버 실행 코드
        val okHttpClient = HttpsClient().unSafeOkHttpClient()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://algotalk.kro.kr/webhooks/rest/")
//            .baseUrl("http://192.168.0.7:5005/webhooks/rest/")
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val userMessage = UserMessage()
        if (message.trim().isEmpty())
            Toast.makeText(getActivity(), "쿼리를 확인해줘", Toast.LENGTH_SHORT).show()
        else {
            Log.e("Msg", sender_id + " msssage: $message " + user_info.level)
            editText.setText("")
            userMessage.UserMessage(sender_id, message, user_info.level.toInt())
            if (printMessage.isNotEmpty())
                showTextView(printMessage, USER, date.toString(), view)
            else
                showTextView(message, USER, date.toString(), view)
        }
        //유저 난이도 정보 전달
        if (printMessage.isNotEmpty()) {
            if (message.compareTo("/problem_recommendation{ \"problem_level\": -1 }") == 0) {
                //val messageUserLevel= "/problem_recommendation{ \"problem_level\": ${dbHelper.readUser(view).level} }"
                user_info.level = dbHelper.readUser(view).level
                userMessage.UserMessage(
                    sender_id,
                    user_info.LevalMapping(),
                    dbHelper.readUser(view).level.toInt()
                )


                Log.e("userMessage change", "msssage: ${userMessage.message} ")
            }
        }
        val messageSender = retrofit.create(MessageSender::class.java)
        val response =
            messageSender.sendMessage(userMessage)

        response.enqueue(object : Callback<List<BotResponse>> {
            override fun onResponse(
                call: Call<List<BotResponse>>,
                response: Response<List<BotResponse>>
            ) {

                val endTime = System.currentTimeMillis()
//                Log.i("server response end", endTime.toString())
//                Log.i("response time(ms)", (endTime - startTime).toString())
                if (response.body() == null || response.body()!!.isEmpty()) {
                    val botMessage = "미안.. 무슨말인지 이해하지 못 하겠어 \uD83D\uDE05"
                    showTextView(botMessage, BOT, date.toString(), view)
                } else {
                    response.body()!!.forEach { botResponse ->
                        if (botResponse.text != null) {
                            showTextView(botResponse.text, BOT, date.toString(), view)
                        }
                        if (botResponse.image != null) {
                            showImageView(botResponse.image, BOT, date.toString(), view)
                        }
                        if (botResponse.buttons != null) {
                            showButtonView(botResponse.buttons, BOT, view)
                        }
                        //json 파일일시
                        if (botResponse.custom != null) {
                            showSlideAreaView(botResponse.custom.list, BOT, date.toString(), view)
                        }
                    }
                }

                endTime1 = System.currentTimeMillis()
//                Log.i("message print end", endTime1.toString())
//                Log.i("print time(ms)", (endTime1 - startTime1).toString())
            }

            override fun onFailure(call: Call<List<BotResponse>>, t: Throwable) {
                val botMessage = "네트워크 연결을 확인해봐 \uD83E\uDD7A"
                showTextView(botMessage, BOT, date.toString(), view)
                t.printStackTrace()
                Toast.makeText(getActivity(), "" + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showOpeningView(message: String, type: Int, date: String, view: View) {
        val linearLayout = view.findViewById<LinearLayout>(R.id.chat_layout)
        val frameLayout: FrameLayout? = when (type) {
            USER -> {
                getUserLayout()
            }
            BOT -> {
                getBotLayout()
            }
            else -> {
                getBotLayout()
            }
        }
        frameLayout?.isFocusableInTouchMode = true
        linearLayout.addView(frameLayout)
        val messageTextView = frameLayout?.findViewById<TextView>(R.id.chat_message)
        messageTextView?.text = message

        val metrics = resources.displayMetrics
        val screenHeight = metrics.heightPixels
        val screenWidth = metrics.widthPixels

        messageTextView?.maxWidth = (screenWidth * 0.8).toInt()

        frameLayout?.requestFocus()
        editText.requestFocus()
        dbHelper = FeedReaderDbHelper(requireContext())
        //book mark
        val bookmarkbutton = frameLayout?.findViewById<CheckBox>(R.id.star_button)
        bookmarkbutton?.visibility = View.GONE
    }


    fun showTextView(message: String, type: Int, date: String, view: View) {
        val linearLayout = view.findViewById<LinearLayout>(R.id.chat_layout)
        val frameLayout: FrameLayout? = when (type) {
            USER -> {
                getUserLayout()
            }
            BOT -> {
                getBotLayout()
            }
            else -> {
                getBotLayout()
            }
        }
        frameLayout?.isFocusableInTouchMode = true
        linearLayout.addView(frameLayout)
        val messageTextView = frameLayout?.findViewById<TextView>(R.id.chat_message)
        messageTextView?.text = message

        val metrics = resources.displayMetrics
        val screenHeight = metrics.heightPixels
        val screenWidth = metrics.widthPixels

        messageTextView?.maxWidth = (screenWidth * 0.8).toInt()

        frameLayout?.requestFocus()
        editText.requestFocus()
        dbHelper = FeedReaderDbHelper(requireContext())
        //book mark
        val bookmarkbutton = frameLayout?.findViewById<CheckBox>(R.id.star_button)
        val bookmark = Bookmark()
        bookmark.content = message
        bookmark.img_uri = "none"
        val bookmark_flag = dbHelper.isAlready(bookmark)

        if (bookmark_flag == 1)
            bookmarkbutton?.isChecked = true
        bookmarkbutton?.setOnClickListener {
            if (bookmarkbutton.isChecked)
                dbHelper.insertBookmark(bookmark)
            else
                dbHelper.deleteBookmark(bookmark)
        }
        //open graph
        val url = UrlData()
        if (url.extractUrlFromText(message)) {
            CoroutineScope(Dispatchers.Main).launch {
                //url 확인 후 크롤링을 통해 메타데이터 구분
                url.getMetadataFromUrl()
                val messageLayout =
                    frameLayout?.findViewById<LinearLayout>(R.id.chat_message_layout)
                if (messageLayout != null) {
                    showOpenGraphView(url.metadata, messageLayout, BOT)
                }
            }
        }
        //time
        val currentDateTime = Date(System.currentTimeMillis())
        val dateNew = Date(date)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val currentDate = dateFormat.format(currentDateTime)
        val providedDate = dateFormat.format(dateNew)
        var time: String
        time = if (currentDate == providedDate) {
            val timeFormat = SimpleDateFormat(
                "hh:mm aa",
                Locale.ENGLISH
            )
            timeFormat.format(dateNew)
        } else {
            val dateTimeFormat = SimpleDateFormat(
                "dd-MM-yy hh:mm aa",
                Locale.ENGLISH
            )
            dateTimeFormat.format(dateNew)
        }
        val timeTextView = frameLayout?.findViewById<TextView>(R.id.message_time)

        timeTextView?.text = time
    }


    fun showImageView(message: String, type: Int, date: String, view: View) {
        var frameLayout: FrameLayout? = null
        val linearLayout = view.findViewById<LinearLayout>(R.id.chat_layout)
        when (type) {
            USER -> {
                frameLayout = getUserLayout()
            }
            BOT -> {
                frameLayout = getBotLayout("image")
            }
            else -> {
                frameLayout = getBotLayout("image")
            }
        }
        frameLayout?.isFocusableInTouchMode = true
        linearLayout.addView(frameLayout)
        val messageImageView = frameLayout?.findViewById<ImageView>(R.id.chat_image_message)
        //GlideApp.with(this).load(message).into(messageImageView!!)

        Glide.with(this).load(message).into(messageImageView!!)

        frameLayout?.requestFocus()
        editText.requestFocus()

        dbHelper = FeedReaderDbHelper(requireContext())

        //Log.i("sangeun", "이미지 출력 확인")
        val bookmarkbutton = frameLayout?.findViewById<CheckBox>(R.id.star_button)

        val bookmark = Bookmark()
        bookmark.content = "none"
        bookmark.img_uri = message

        //Log.i("sangeun", bookmark.toString())

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
        var time: String
        time = if (currentDate == providedDate) {
            val timeFormat = SimpleDateFormat(
                "hh:mm aa",
                Locale.ENGLISH
            )
            timeFormat.format(dateNew)
        } else {
            val dateTimeFormat = SimpleDateFormat(
                "dd-MM-yy hh:mm aa",
                Locale.ENGLISH
            )
            dateTimeFormat.format(dateNew)
        }
        val timeTextView = frameLayout?.findViewById<TextView>(R.id.image_message_time)
        timeTextView?.text = time.toString()
    }

    fun showButtonView(buttons: List<BotResponse.Button>, type: Int, view: View) {
        var frameLayout: FrameLayout? = null
        val linearLayout = view.findViewById<LinearLayout>(R.id.chat_layout)
        frameLayout = when (type) {
            USER -> {
                getUserLayout()
            }
            BOT -> {
                getBotLayout("button")
            }
            else -> {
                getBotLayout("button")
            }
        }
        frameLayout?.isFocusableInTouchMode = true
        linearLayout.addView(frameLayout)
        frameLayout?.requestFocus()
        editText.requestFocus()
        val buttonRecyclerView = ButtonRecyclerView(buttons)
        val layoutManager: FlexboxLayoutManager?
        val recyclerView = frameLayout?.findViewById<RecyclerView>(R.id.button_list)
        layoutManager = FlexboxLayoutManager(activity)
        //layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView?.layoutManager = layoutManager
        recyclerView?.adapter = buttonRecyclerView
    }

    private fun showOpenGraphView(
        message: Metadata,
        messageLayout: LinearLayout,
        type: Int
    ) {
        var frameLayout: FrameLayout? = null
        frameLayout = when (type) {
            USER -> {
                getUserLayout()
            }
            BOT -> {
                getBotLayout("openGraph")
            }
            else -> {
                getBotLayout()
            }
        }
        messageLayout.addView(frameLayout, 1)
        //이미지 출력

        val messageOpenGraphView =
            frameLayout?.findViewById<ImageView>(R.id.chat_open_graph_image_message)
        if (messageOpenGraphView != null) {
            Glide.with(this).load(message.imageUrl)
                .override(800, 400).centerCrop().into(messageOpenGraphView!!)
        }

        //타이틀+설명 출력
        val messageTextView = frameLayout?.findViewById<TextView>(R.id.chat_open_graph_message)
        messageTextView?.text = message.title

        val metrics = resources.displayMetrics
        val screenHeight = metrics.heightPixels
        val screenWidth = metrics.widthPixels

        messageTextView?.maxWidth = (screenWidth * 0.8).toInt()
        val linkExplainView = frameLayout?.findViewById<TextView>(R.id.chat_open_graph_link)
        linkExplainView?.text = "\n여기를 눌러 링크를 확인하세요."


        //레이아웃 클릭시 앱브라우저로 url 실행
        frameLayout?.findViewById<LinearLayout>(R.id.chat_open_graph_layout)?.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(message.url)
            startActivity(i)
        }
    }

    fun showSlideAreaView(
        elements: List<BotResponse.Element>,
        type: Int,
        date: String,
        view: View
    ) {
        val linearLayout = view.findViewById<LinearLayout>(R.id.chat_layout)
        val frameLayout: FrameLayout? = when (type) {
            USER -> {
                getUserLayout()
            }
            BOT -> {
                getBotLayout("slideArea")
            }
            else -> {
                getBotLayout()
            }
        }
        frameLayout?.isFocusableInTouchMode = true
        linearLayout.addView(frameLayout)
        val chattingScrollView = frameLayout?.findViewById<HorizontalScrollView>(R.id.chatScrollView)
        chattingScrollView?.post { chattingScrollView.fullScroll(ScrollView.FOCUS_LEFT) }


        val slideLayout = frameLayout?.findViewById<LinearLayout>(R.id.slide_chat_layout)
        elements.forEach {
            //버튼 위치문제
            if (slideLayout != null) {
                val slideFrame = showSlideView(it.text, BOT, date, slideLayout)
                if (slideFrame != null) {
                    showSlideButtonView(it.buttons, BOT, slideFrame)
                }
                it.buttons.forEach {
                    Log.i("slide buttons", it.payload)
                }
            }
        }

    }


    private fun showSlideView(message: String, type: Int, date: String, slideLayout: LinearLayout): FrameLayout? {
        val frameLayout: FrameLayout? = when (type) {
            USER -> {
                getUserLayout()
            }
            BOT -> {
                getBotLayout("slide")
            }
            else -> {
                getBotLayout()
            }
        }
        frameLayout?.isFocusableInTouchMode = true
        slideLayout.addView(frameLayout)

        val messageTextView = frameLayout?.findViewById<TextView>(R.id.chat_message)
        messageTextView?.text = message

        val metrics = resources.displayMetrics
        val screenHeight = metrics.heightPixels
        val screenWidth = metrics.widthPixels

        messageTextView?.maxWidth = (screenWidth * 0.8).toInt()

        frameLayout?.requestFocus()
        editText.requestFocus()
        dbHelper = FeedReaderDbHelper(requireContext())
        //book mark
        val bookmarkbutton = frameLayout?.findViewById<CheckBox>(R.id.star_button)
        val bookmark = Bookmark()
        bookmark.content = message
        bookmark.img_uri = "none"
        val bookmark_flag = dbHelper.isAlready(bookmark)

        if (bookmark_flag == 1)
            bookmarkbutton?.isChecked = true
        bookmarkbutton?.setOnClickListener {
            if (bookmarkbutton.isChecked)
                dbHelper.insertBookmark(bookmark)
            else
                dbHelper.deleteBookmark(bookmark)
        }



        CoroutineScope(Dispatchers.Main).launch {
            //open graph
            val url = UrlData()
            if (url.extractUrlFromText(message)) {
                url.getMetadataFromUrl()
                val slideOGLayout =
                    frameLayout?.findViewById<LinearLayout>(R.id.slide_chat_message_layout)
                if (slideOGLayout != null) {
                    showOpenGraphView(url.metadata, slideOGLayout, BOT)
                }
            }
        }


        //time
        val currentDateTime = Date(System.currentTimeMillis())
        val dateNew = Date(date)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val currentDate = dateFormat.format(currentDateTime)
        val providedDate = dateFormat.format(dateNew)
        var time: String
        time = if (currentDate == providedDate) {
            val timeFormat = SimpleDateFormat(
                "hh:mm aa",
                Locale.ENGLISH
            )
            timeFormat.format(dateNew)
        } else {
            val dateTimeFormat = SimpleDateFormat(
                "dd-MM-yy hh:mm aa",
                Locale.ENGLISH
            )
            dateTimeFormat.format(dateNew)
        }

        val timeTextView = frameLayout?.findViewById<TextView>(R.id.message_time)

        timeTextView?.text = time
        return frameLayout
    }


    private fun showSlideButtonView(
        buttons: List<BotResponse.Button>,
        type: Int,
        slideLayout: FrameLayout
    ) {
        var frameLayout: FrameLayout? = null
        frameLayout = when (type) {
            USER -> {
                getUserLayout()
            }
            BOT -> {
                getBotLayout("button")
            }
            else -> {
                getBotLayout("button")
            }
        }
        slideLayout.findViewById<LinearLayout>(R.id.slide_chat_linear_layout).addView(frameLayout,1)
//        var slideMessageFrameLayout: FrameLayout? = null
//        slideMessageFrameLayout = when (type) {
//            USER -> {
//                getUserLayout()
//            }
//            BOT -> {
//                getBotLayout("slideArea")
//            }
//            else -> {
//                getBotLayout("slideArea")
//            }
//        }
//        slideMessageFrameLayout?.findViewById<LinearLayout>(R.id.slide_chat_layout)?.addView(frameLayout)
        frameLayout?.isFocusableInTouchMode = true
//        slideLayout.addView(frameLayout)
        //frameLayout?.requestFocus()
        editText.requestFocus()
        val buttonRecyclerView = ButtonRecyclerView(buttons)
        val layoutManager: FlexboxLayoutManager?
        val recyclerView = frameLayout?.findViewById<RecyclerView>(R.id.button_list)
        layoutManager = FlexboxLayoutManager(activity)
        //layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView?.layoutManager = layoutManager
        recyclerView?.adapter = buttonRecyclerView
    }

    inner class ButtonRecyclerView(private var buttons: List<BotResponse.Button>) :
        RecyclerView.Adapter<ButtonRecyclerView.ButtonViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
            val view =
                LayoutInflater.from(activity).inflate(R.layout.button_list_item, parent, false)
            return ButtonViewHolder(view)

        }


        override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
            val payloadButton = buttons[position]
            holder.button.text = payloadButton.title
            holder.button.setOnClickListener {

                startTime1 = System.currentTimeMillis()
                Log.i("message print start", startTime1.toString())
                view?.let { it1 -> sendMessage(it1, payloadButton.payload, payloadButton.title) }
            }
        }

        override fun getItemCount(): Int {
            buttons.isEmpty()
            return buttons.size
        }

        inner class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val button: MaterialButton = view.findViewById<MaterialButton>(R.id.payload_button)
        }
    }

    private fun getUserLayout(): FrameLayout? {
        val inflater = LayoutInflater.from(activity)
        return inflater.inflate(R.layout.user_message_area, null) as FrameLayout?
    }

    private fun getBotLayout(): FrameLayout? {
        val inflater = LayoutInflater.from(activity)
        return inflater.inflate(R.layout.bot_message_area, null) as FrameLayout?
    }

    private fun getBotLayout(type: String): FrameLayout? {
        when (type) {
            "image" -> {
                val inflater = LayoutInflater.from(activity)
                return inflater.inflate(R.layout.bot_image_message_area, null) as FrameLayout?
            }
            "button" -> {
                val inflater = LayoutInflater.from(activity)
                return inflater.inflate(R.layout.bot_button_area, null) as FrameLayout?
            }
            "openGraph" -> {
                val inflater = LayoutInflater.from(activity)
                return inflater.inflate(R.layout.bot_opengraph_area, null) as FrameLayout?
            }
            "slide" -> {
                val inflater = LayoutInflater.from(activity)
                return inflater.inflate(R.layout.bot_slide_message, null) as FrameLayout?
            }
            "slideArea" -> {
                val inflater = LayoutInflater.from(activity)
                return inflater.inflate(R.layout.bot_slide_message_area, null) as FrameLayout?
            }
            else -> {
                val inflater = LayoutInflater.from(activity)
                return inflater.inflate(R.layout.bot_message_area, null) as FrameLayout?
            }
        }
    }
}
