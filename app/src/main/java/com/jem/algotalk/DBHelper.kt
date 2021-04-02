package com.jem.algotalk

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import java.util.ArrayList

object FeedReaderContract {
    // Table contents are grouped together in an anonymous object.
    object FeedEntry : BaseColumns {
        const val TABLE_NAME = "Bookmarks"
        const val COLUMN_CONTENT = "content"
    }
}

private const val SQL_CREATE_ENTRIES =
    "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${FeedReaderContract.FeedEntry.COLUMN_CONTENT} TEXT)"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME}"

class FeedReaderDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Algotalk.db"
    }

    fun insertBookmark(content: String){
        // Gets the data repository in write mode
        val database = this.writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(FeedReaderContract.FeedEntry.COLUMN_CONTENT, content)
        }

        // Insert the new row, returning the primary key value of the new row
        val newRowId = database?.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values)

//        if(newRowId == (-1).toLong())
//            Toast.makeText(context,"Failed", Toast.LENGTH_LONG).show()
//        else
//            Toast.makeText(context,"Success", Toast.LENGTH_SHORT).show()
    }

    fun readBookmark(view: View): MutableList<Bookmark>{
        // Gets the data repository in write mode
        val database = this.readableDatabase

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

        val bookmarklist :MutableList<Bookmark> = ArrayList()

        if(cursor.moveToFirst()){
            do {
                Log.i("sangeun", cursor.getString(cursor.getColumnIndex("content")))
                val bookmark = Bookmark()
                bookmark.content = cursor.getString(cursor.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_CONTENT))
                bookmarklist.add(bookmark)
            }while (cursor.moveToNext())
        }
            //Toast.makeText(context,"There is no data.", Toast.LENGTH_LONG).show()

        return bookmarklist
    }

    fun deleteBookmark(content: String){
        // Gets the data repository in write mode
        val database = this.writableDatabase

        // Define 'where' part of query.
        val selection = "${FeedReaderContract.FeedEntry.COLUMN_CONTENT} LIKE ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(content)
        // Issue SQL statement.
        val deletedRows = database.delete(FeedReaderContract.FeedEntry.TABLE_NAME, selection, selectionArgs)

//        if(deletedRows == (-1))
//            Toast.makeText(context,"Failed", Toast.LENGTH_LONG).show()
//        else
//            Toast.makeText(context,"Success", Toast.LENGTH_LONG).show()
    }
}