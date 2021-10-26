package com.jem.algotalk

import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.regex.Matcher
import java.util.regex.Pattern

class UrlData {
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
            val imageUrl = doc.select("meta[property=og:image]")[0].attr("content")
            var title =doc.select("title").first().html()
            var description = ""
            try{
                title = doc.select("meta[property=og:title]").first().attr("content")
                description=doc.select("meta[property=og:description]")[0].attr("content")
            }finally {
                metadata.metadata(url, imageUrl, title, description)
            }
            return@withContext metadata
        }catch(ex: Exception){
            Log.e("ogerror0", ex.toString())
            return@withContext metadata
        }



    }
}
