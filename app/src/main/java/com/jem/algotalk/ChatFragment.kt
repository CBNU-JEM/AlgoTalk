package com.jem.algotalk

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.flexbox.AlignContent
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    companion object {
        private const val USER = 0
        const val BOT = 1
        const val PHONEWIDTH = 0.75
    }

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
    private lateinit var userLevel: String

    var endTime1 = System.currentTimeMillis()
    var startTime1 = System.currentTimeMillis()

    //디비헬퍼, 디비 선언
    private lateinit var dbHelper: FeedReaderDbHelper

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
                "ex) 정렬 알고리즘 문제 추천해줘, 골드 난이도 문제 추천해줘, 랜덤으로 문제 추천해줘\n" +
                "\uD83D\uDD38 대회정보\n" +
                "너를 위해 대회 정보도 준비해놨지~ 히힛 \n" +
                "ex) 카카오 대회 알려줘, 진행중인(or 지난 or 열릴) 대회 알려줘, 2021년 11월에 예정된 대회 알려줘\n" +
                "\n" +
                "\uD83D\uDD39왼쪽 아래에 더보기버튼을 누르면 너의 알고리즘 문제풀이 수준을 설정할 수 있어! 알고리즘 문제풀이 수준을 모르겠다면 https://solved.ac/ 사이트를 참고해줘\uD83D\uDE09"

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

                //바꿀때마다 유저레벨 슬롯 설정
                userLevel = dbHelper.readUser(view).level
                val userLevelMessage = "user_level = $userLevel"
                sendMessage(view, userLevelMessage, "")
            }

            val noButton = mDialogView.findViewById<Button>(R.id.close_popup_Button)
            noButton.setOnClickListener {
                mAlertDialog.dismiss()
            }

        }

        //처음켰을때 유저레벨 슬롯 설정
        userLevel = dbHelper.readUser(view).level
        val userLevelMessage = "user_level = $userLevel"
        sendMessage(view, userLevelMessage, "")

        return view
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    fun sendMessage(view: View, message: String, printMessage: String) {
        dbHelper = FeedReaderDbHelper(requireContext())
        var user_info = User()
        var sender_id = Settings.Secure.getString(
            context!!.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        sender_id
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
            Log.e("Msg", sender_id + " msssage: $message " + dbHelper.readUser(view).level)
            editText.setText("")
            userMessage.UserMessage(sender_id, message, user_info.level.toInt())
            if (printMessage == "") {

            } else if (printMessage.isNotEmpty())
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
            @RequiresApi(Build.VERSION_CODES.M)
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
                        if (botResponse.text != null && botResponse.text != "set user_level : success") {
                            val textFrame =
                                showTextView(botResponse.text, BOT, date.toString(), view)
                            if (botResponse.buttons != null && textFrame != null) {
                                showButtonView(botResponse.buttons, BOT, view, textFrame)
                            }
                        }
                        if (botResponse.image != null) {
                            showImageView(botResponse.image, BOT, date.toString(), view)
                        }

                        //json 파일일시
                        if (botResponse.custom != null) {
                            showSlideAreaView(botResponse.custom.list, BOT, date.toString(), view)
                        }

                        editText.requestFocus()
                    }
                }

                endTime1 = System.currentTimeMillis()
//                Log.i("message print end", endTime1.toString())
//                Log.i("print time(ms)", (endTime1 - startTime1).toString())
            }

            override fun onFailure(call: Call<List<BotResponse>>, t: Throwable) {
                val botMessage = "네트워크 연결을 확인해봐 \uD83E\uDD7A"
                showTextView(botMessage, BOT, date.toString(), view)
//                t.printStackTrace()
//                Toast.makeText(getActivity(), "" + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showOpeningView(message: String, type: Int, date: String, view: View) {
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

        messageTextView?.maxWidth = (screenWidth * PHONEWIDTH).toInt()

        frameLayout?.requestFocus()
        editText.requestFocus()
        dbHelper = FeedReaderDbHelper(requireContext())
        //book mark
        val bookmarkbutton = frameLayout?.findViewById<CheckBox>(R.id.star_button)
        bookmarkbutton?.visibility = View.GONE
    }


    fun showTextView(message: String, type: Int, date: String, view: View): LinearLayout? {
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

        messageTextView?.maxWidth = (screenWidth * PHONEWIDTH).toInt()
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
        CoroutineScope(Dispatchers.Main).launch {
            val url = UrlData()
            if (url.extractUrlFromText(message)) {
                //url 확인 후 크롤링을 통해 메타데이터 구분
                url.getMetadataFromUrl()
                val messageLayout =
                    frameLayout?.findViewById<LinearLayout>(R.id.chat_message_layout)
                if (messageLayout != null && url.metadata.imageUrl != "") {
                    showOpenGraphView(url.metadata, messageLayout, BOT)
                }
            }
            editText.requestFocus()
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

        frameLayout?.requestFocus()
        editText.requestFocus()
        return frameLayout?.findViewById<LinearLayout>(R.id.chat_message_layout)
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

    fun showButtonView(
        buttons: List<BotResponse.Button>,
        type: Int,
        view: View,
        textFrame: LinearLayout?
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
        frameLayout?.isFocusableInTouchMode = true
        textFrame?.addView(frameLayout, 1)
        frameLayout?.requestFocus()
        editText.requestFocus()
        val buttonRecyclerView = ButtonRecyclerView(buttons)

        val metrics = resources.displayMetrics
        val maxWidth = (metrics.widthPixels * PHONEWIDTH).toInt()

        val buttonWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            95f,
            resources.displayMetrics
        ).toInt()

        if (buttonRecyclerView.itemCount >= maxWidth / buttonWidth) {
            frameLayout?.layoutParams!!.width = maxWidth
        }

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
            val metrics = resources.displayMetrics
            val screenHeight = metrics.heightPixels
            Glide.with(this).load(message.imageUrl)
                .override(screenHeight / 2, screenHeight / 4).centerCrop()
                .into(messageOpenGraphView!!)

        }
        frameLayout?.isFocusableInTouchMode = true
        frameLayout?.requestFocus()
        editText.requestFocus()
        //타이틀+설명 출력
        val messageTextView = frameLayout?.findViewById<TextView>(R.id.chat_open_graph_message)
        messageTextView?.text = message.title

        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels

        messageTextView?.maxWidth = (screenWidth * PHONEWIDTH).toInt()
        val linkExplainView = frameLayout?.findViewById<TextView>(R.id.chat_open_graph_link)
        linkExplainView?.text = "\n여기를 눌러 링크를 확인하세요."


        //레이아웃 클릭시 앱브라우저로 url 실행
        frameLayout?.findViewById<LinearLayout>(R.id.chat_open_graph_layout)?.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(message.url)
            startActivity(i)
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
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
        frameLayout?.requestFocus()
        editText.requestFocus()

        linearLayout.addView(frameLayout)
        val horizontalViewPager2 = frameLayout?.findViewById<ViewPager2>(R.id.viewPager)
        val adapter = HorizontalViewPagerAdapter(elements, date)
        horizontalViewPager2?.adapter = adapter
        horizontalViewPager2?.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        if (horizontalViewPager2 != null && elements.size < 2) {
            val wormDotsIndicator =
                frameLayout?.findViewById<WormDotsIndicator>(R.id.worm_dots_indicator)
            wormDotsIndicator?.visibility = INVISIBLE
        } else if (horizontalViewPager2 != null && elements.size > 1) {
            val wormDotsIndicator =
                frameLayout?.findViewById<WormDotsIndicator>(R.id.worm_dots_indicator)
            wormDotsIndicator.setViewPager2(horizontalViewPager2)
        }
        val chattingScrollView = view.findViewById<NestedScrollView>(R.id.chatScrollView)
        chattingScrollView.post { chattingScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        //양옆의 페이지 노출
//        val metrics = resources.displayMetrics
//        val px: Float =
//            44f * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
//        val px2: Float =
//            94f * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
//        val currentVisibleItemPx = px.toInt()
//        val nextVisibleItemPx = px2.toInt()
//        val pageTranslationX =
//            nextVisibleItemPx + currentVisibleItemPx
//        horizontalViewPager2?.offscreenPageLimit = 2
//        horizontalViewPager2?.setPageTransformer { page, position ->
//            page.translationX = -pageTranslationX * (position)
//        }
//        horizontalViewPager2?.addItemDecoration(object : RecyclerView.ItemDecoration() {
//            override fun getItemOffsets(
//                outRect: Rect,
//                view: View,
//                parent: RecyclerView,
//                state: RecyclerView.State
//            ) {
//                outRect.right = currentVisibleItemPx
//                outRect.left = currentVisibleItemPx
//            }
//        })

//        fun Int.dpToPx(displayMetrics: DisplayMetrics): Int = (this * displayMetrics.density).toInt()
//
//        // 좌/우 노출되는 크기를 크게하려면 offsetPx 증가
//        val offsetPx = 15.dpToPx(resources.displayMetrics)
//        horizontalViewPager2?.setPadding(offsetPx, offsetPx, offsetPx, offsetPx)
//
//        // 페이지간 마진 크게하려면 pageMarginPx 증가
//        val pageMarginPx = 7.dpToPx(resources.displayMetrics)
//        val marginTransformer = MarginPageTransformer(pageMarginPx)
//        horizontalViewPager2?.setPageTransformer(marginTransformer)
//
//        val slideLayout: LinearLayout? = when (type) {
//            USER -> {
//                getUserLayout()
//            }
//            BOT -> {
//                getBotLayout("slide")
//            }
//            else -> {
//                getBotLayout()
//            }
//        }?.findViewById(R.id.slide_chat_linear_layout)
//        val horizontalScrollView =
//            frameLayout?.findViewById<HorizontalScrollView>(R.id.chatScrollView)
//        horizontalScrollView?.post { horizontalScrollView.fullScroll(ScrollView.FOCUS_LEFT) }
//        val slideLayout = frameLayout?.findViewById<LinearLayout>(R.id.slide_chat_layout)
//        elements.forEach {
//            if (slideLayout != null) {
//                val slideFrame = showSlideView(it.text, BOT, date, slideLayout)
//                if (slideFrame != null && it.buttons != null) {
//                    showSlideButtonView(it.buttons, BOT, slideFrame)
//                }
//            it.text.forEach {
//                Log.i("slide buttons", it.title)
//            }
//        }
    }

    fun showSlideView(
        message: String,
        type: Int,
        date: String,
        slideLayout: LinearLayout
    ): FrameLayout? {
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
        slideLayout.addView(frameLayout)

        val messageTextView = frameLayout?.findViewById<TextView>(R.id.chat_message)
        messageTextView?.text = message

        val metrics = resources.displayMetrics
        val screenHeight = metrics.heightPixels
        val screenWidth = metrics.widthPixels

        messageTextView?.maxWidth = (screenWidth * PHONEWIDTH).toInt()

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

        frameLayout?.isFocusableInTouchMode = true
        frameLayout?.requestFocus()
        editText.requestFocus()


        CoroutineScope(Dispatchers.Main).launch {
            //open graph
            val url = UrlData()
            if (url.extractUrlFromText(message)) {
                url.getMetadataFromUrl()
                val slideOGLayout =
                    frameLayout?.findViewById<LinearLayout>(R.id.slide_chat_message_layout)
                if (slideOGLayout != null && url.metadata.imageUrl != "") {
                    showOpenGraphView(url.metadata, slideOGLayout, BOT)
                }
            }
            editText.requestFocus()
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


    fun showSlideButtonView(
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
        slideLayout.findViewById<LinearLayout>(R.id.slide_chat_message_layout)
            .addView(frameLayout, 1)
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
        frameLayout?.requestFocus()
        editText.requestFocus()

        val buttonRecyclerView = ButtonRecyclerView(buttons)
        val metrics = resources.displayMetrics
        val maxWidth = (metrics.widthPixels * PHONEWIDTH).toInt()

        val buttonWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            95f,
            resources.displayMetrics
        ).toInt()

        if (buttonRecyclerView.itemCount >= maxWidth / buttonWidth) {
            frameLayout?.layoutParams!!.width = maxWidth
        }
        val layoutManager: FlexboxLayoutManager? = FlexboxLayoutManager(activity)
        val recyclerView = frameLayout?.findViewById<RecyclerView>(R.id.button_list)

//        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
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
                view?.let { it1 ->
                    sendMessage(
                        it1,
                        payloadButton.payload,
                        payloadButton.title
                    )
                }
            }
        }

        override fun getItemCount(): Int {
            return buttons.size
        }

        inner class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val button: MaterialButton = view.findViewById<MaterialButton>(R.id.payload_button)
        }
    }

    inner class HorizontalViewPagerAdapter(
        private val layoutList: List<BotResponse.Element>,
        private val date: String
    ) :
        RecyclerView.Adapter<HorizontalViewPagerAdapter.PagerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
            val view =
                LayoutInflater.from(activity).inflate(R.layout.bot_slide_message, parent, false)
            return PagerViewHolder(view)
        }

        override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
            val it = layoutList[position]
            Log.e("onBindViewHolder"," ")
            changeSlideView(it, holder.linearLayout)
        }

        inner class PagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val linearLayout: LinearLayout =
                view.findViewById<LinearLayout>(R.id.slide_chat_linear_layout)
        }

        override fun getItemCount(): Int = layoutList.size

        fun createLayout(linearLayout: LinearLayout) {
            layoutList.forEach {
                val slideFrame =
                    showSlideView(it.text, BOT, date, linearLayout)
                if (slideFrame != null && it.buttons != null) {
                    showSlideButtonView(it.buttons, BOT, slideFrame)
                }
            }
        }

        private fun changeSlideView(
            Element: BotResponse.Element,
            linearLayout: LinearLayout
        ) {
            //버튼 변경, 오픈그래프
            val messageTextView = linearLayout.findViewById<TextView>(R.id.chat_message)
            messageTextView?.text = Element.text

            val metrics = resources.displayMetrics
            val screenWidth = metrics.widthPixels
            messageTextView?.maxWidth = (screenWidth * PHONEWIDTH).toInt()

            dbHelper = FeedReaderDbHelper(requireContext())
            //book mark
            val bookmarkbutton = linearLayout.findViewById<CheckBox>(R.id.star_button)
            val bookmark = Bookmark()
            bookmark.content = Element.text
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

//            editText.requestFocus()
            CoroutineScope(Dispatchers.Main).launch {
                //open graph
                val url = UrlData()
                if (url.extractUrlFromText(Element.text)) {
                    url.getMetadataFromUrl()
                    if (url.metadata.imageUrl != "") {
                        changeOpenGraphView(url.metadata, linearLayout, BOT)
                    }
                }
                editText.requestFocus()
            }
            changeButtonView(Element.buttons, linearLayout)
        }

        private fun changeButtonView(
            buttons: List<BotResponse.Button>,
            textFrame: LinearLayout?
        ) {
            var frameLayout: FrameLayout? = null
            frameLayout = when (BOT) {
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
            if (textFrame?.findViewById<RecyclerView>(R.id.button_list) == null) {
                val linearLayout =
                    textFrame?.findViewById<LinearLayout>(R.id.slide_chat_message_layout)
                linearLayout?.addView(frameLayout, 1)
                val buttonRecyclerView = ButtonRecyclerView(buttons)

                val metrics = resources.displayMetrics
                val maxWidth = (metrics.widthPixels * PHONEWIDTH).toInt()

                val buttonWidth = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    95f,
                    resources.displayMetrics
                ).toInt()

                if (buttonRecyclerView.itemCount >= maxWidth / buttonWidth) {
                    frameLayout?.layoutParams!!.width = maxWidth
                }

                val layoutManager: FlexboxLayoutManager?
                val recyclerView = frameLayout?.findViewById<RecyclerView>(R.id.button_list)
                layoutManager = FlexboxLayoutManager(activity)
                //layoutManager.orientation = LinearLayoutManager.HORIZONTAL
                recyclerView?.layoutManager = layoutManager
                recyclerView?.adapter = buttonRecyclerView
                frameLayout?.requestFocus()
                editText.requestFocus()
            }
        }

        private fun changeOpenGraphView(metadata: Metadata, linearLayout: LinearLayout, type: Int) {
            val metrics = resources.displayMetrics
            val screenHeight = metrics.heightPixels
            val screenWidth = metrics.widthPixels
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
            //이미지변환
            var imageArea =
                linearLayout.findViewById<ImageView>(R.id.chat_open_graph_image_message)
            //없을시 생성 있을시 변경만
            if (imageArea == null) {
                val messageLayout =
                    linearLayout.findViewById<LinearLayout>(R.id.slide_chat_message_layout)
                messageLayout.addView(frameLayout, 1)
                imageArea = frameLayout?.findViewById(R.id.chat_open_graph_image_message)
            }

            if (imageArea != null) {
                Glide.with(activity).load(metadata.imageUrl)
                    .override(screenHeight / 2, screenHeight / 4).centerCrop()
                    .into(imageArea!!)
            }
            //타이틀+설명 변환
            val textArea = linearLayout.findViewById<TextView>(R.id.chat_open_graph_message)
            textArea?.text = metadata.title
            textArea?.maxWidth = (screenWidth * PHONEWIDTH).toInt()
            val linkArea = linearLayout.findViewById<TextView>(R.id.chat_open_graph_link)
            linkArea?.text = "\n여기를 눌러 링크를 확인하세요."
            //레이아웃 클릭시 앱브라우저로 url 실행
            linearLayout.findViewById<LinearLayout>(R.id.chat_open_graph_layout)
                .setOnClickListener {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(metadata.url)
                    startActivity(i)
                }

//            frameLayout?.isFocusableInTouchMode = true
            frameLayout?.requestFocus()
            editText.requestFocus()
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
