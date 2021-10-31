package com.jem.algotalk

class Metadata {
    var url: String= ""
    var imageUrl: String = ""
    var title: String= ""
    var description: String= ""

    fun metadata(url: String, imageUrl: String, title: String, description: String) {
        this.url = url
        this.imageUrl = imageUrl
        this.title = title
        this.description = description
    }
}