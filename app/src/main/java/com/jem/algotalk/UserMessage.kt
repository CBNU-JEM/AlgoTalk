package com.jem.algotalk

class UserMessage {
    public lateinit var sender:String
    public lateinit var message: String
    public var level: Int = 1
    fun UserMessage(id:String,response_message:String, user_level:Int){
        sender = id
        message = response_message
        level = user_level
    }
}