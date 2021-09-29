package com.jem.algotalk

class UserMessage {
    private lateinit var sender:String
    private lateinit var message: String
    fun userMessage(id:String,response_message:String){
        sender = id
        message = response_message
    }
}