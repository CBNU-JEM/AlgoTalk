package com.jem.algotalk

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.Exception

class User(var name:String="_", var level:String="0") {
    var id:Int = 0

    fun LevalMapping(): String {
        val map = HashMap<String, String>()
        map["1"] = "브론즈"
        map["2"] = "실버"
        map["3"] = "골드"
        map["4"] = "플레티넘"
        map["5"] = "다이아"
        map["6"] = "루비"

        if(map.get(level)!=null){
            val levelChange=map.get(level)
            return "/problem_recommendation{ \"problem_level\": \"$levelChange\" }"
        }
        else{
            return "/problem_recommendation{ \"problem_level\": 0 }"
        }

    }
}