package com.jem.algotalk

import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.regex.Matcher
import java.util.regex.Pattern

object UrlData {
    private lateinit var url: String
    private var flag: Boolean = false
    val metadata = Metadata()
    fun extractUrlFromText(text: String): Boolean {

        val regex =
            "[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)"
        val p: Pattern = Pattern.compile(regex)
        val m: Matcher = p.matcher(text)
        if (m.find()) {
            url=m.group()
            flag = true
        } else {
            flag = false
        }
        return flag

    }

    suspend fun getMetadataFromUrl(): Metadata? = withContext(Dispatchers.IO){
        try {
            val doc = Jsoup.connect(url).get()
            Log.e("metadate true", "doc")
            val imageUrl = doc.select("meta[property=og:image]")[0].attr("content")
            val title = doc.select("meta[property=og:title]").first().attr("content")
            val description = doc.select("meta[property=og:description]")[0].attr("content")
            metadata.metadata(url, imageUrl, title, description)
            Log.e("metadate", "${imageUrl}")
            return@withContext metadata
        }catch(ex: Exception){
            Log.e("ogerror", "${url}")
            return@withContext metadata
        }



    }
}
