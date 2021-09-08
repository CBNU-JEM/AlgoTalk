package com.jem.algotalk

class UserMessage {
    public lateinit var sender:String
    public lateinit var message: String
    public var sender_level: Int = 1
    fun UserMessage(id:String,response_message:String, user_level:Int){
        sender = id
        message = response_message
        sender_level = user_level
    }
}