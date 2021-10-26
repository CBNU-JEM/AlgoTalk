package com.jem.algotalk

class BotResponse(
    //var recipient_id: String,
    var text: String,
    var image: String,
    var buttons: List<Button>,
    var custom: Custom

) {
    inner class Button(var payload: String, var title: String)
    inner class Element(var text: String, var buttons: List<Button>)
    inner class Custom(var list: List<Element>)

}