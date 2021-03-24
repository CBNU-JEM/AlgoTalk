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
    private lateinit var database : SQLiteDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        val chattingScrollView = view.findViewById<NestedScrollView>(R.id.chatScrollView)
        activity = context as Activity

        this.inflater=inflater
        if (container != null) {
            this.container=container
        }

        chattingScrollView.post { chattingScrollView.fullScroll(ScrollView.FOCUS_DOWN) }

        readBookmark(view)

        return view
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

        Log.i("sangeun", "메세지 출력 확인")
        val bookmarkbutton = frameLayout?.findViewById<CheckBox>(R.id.star_button)
        bookmarkbutton?.isChecked=true
        bookmarkbutton?.setOnClickListener { view ->
            if (!bookmarkbutton.isChecked)
                deleteBookmark(message)
            else
                insertBookmark(message)
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

    fun insertBookmark(content: String){
        //디비헬퍼, 디비 선언
        dbHelper = FeedReaderDbHelper(this.requireContext())
        // Gets the data repository in write mode
        database = dbHelper.writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(FeedReaderContract.FeedEntry.COLUMN_CONTENT, content)
        }

        // Insert the new row, returning the primary key value of the new row
        val newRowId = database?.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values)

        if(newRowId == (-1).toLong())
            Toast.makeText(context,"Failed",Toast.LENGTH_LONG).show()
        else
            Toast.makeText(context,"Success",Toast.LENGTH_LONG).show()
    }

    fun readBookmark(view: View){
        //디비헬퍼, 디비 선언
        dbHelper = FeedReaderDbHelper(this.requireContext())
        // Gets the data repository in write mode
        database = dbHelper.readableDatabase

//        if(deletedRows == (-1))
//            Toast.makeText(context,"Failed",Toast.LENGTH_LONG).show()
//        else
//            Toast.makeText(context,"Success",Toast.LENGTH_LONG).show()

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(BaseColumns._ID, FeedReaderContract.FeedEntry.COLUMN_CONTENT)

//        // Filter results WHERE "title" = 'My Title'
//        val selection = "${FeedReaderContract.FeedEntry.COLUMN_CONTENT} = ?"
//        val selectionArgs = arrayOf("My Title")
//
//        val cursor = database.query(
//            FeedEntry.TABLE_NAME,   // The table to query
//            projection,             // The array of columns to return (pass null to get all)
//            selection,              // The columns for the WHERE clause
//            selectionArgs,          // The values for the WHERE clause
//            null,                   // don't group the rows
//            null,                   // don't filter by row groups
//            sortOrder               // The sort order
//        )

        val cursor = database.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

        while(cursor.moveToNext()){
            Log.i("sangeun", cursor.getString(cursor.getColumnIndex("content")))
            val date = Date(System.currentTimeMillis())
            showTextView(cursor.getString(cursor.getColumnIndex("content")), date.toString(), view)
            //System.out.println("content : "+cursor.getString(cursor.getColumnIndex("content")));
        }
    }

    fun deleteBookmark(content: String){
        //디비헬퍼, 디비 선언
        dbHelper = FeedReaderDbHelper(this.requireContext())
        // Gets the data repository in write mode
        database = dbHelper.writableDatabase

        // Define 'where' part of query.
        val selection = "${FeedReaderContract.FeedEntry.COLUMN_CONTENT} LIKE ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(content)
        // Issue SQL statement.
        val deletedRows = database.delete(FeedReaderContract.FeedEntry.TABLE_NAME, selection, selectionArgs)

        if(deletedRows == (-1))
            Toast.makeText(context,"Failed",Toast.LENGTH_LONG).show()
        else
            Toast.makeText(context,"Success",Toast.LENGTH_LONG).show()
    }
}