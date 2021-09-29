package com.jem.algotalk

class Metadata {
    lateinit var url: String
    lateinit var imageUrl: String
    lateinit var title: String
    lateinit var description: String

    fun metadata(url: String, imageUrl: String, title: String, description: String) {
        this.url = url
        this.imageUrl = imageUrl
        this.title = title
        this.description = description
    }
}