package com.shah.hindustangraincenter

class Download {
    var name: String? = null
    var imageUrl: String? = null
    var price: String? = null
    var description: String? = null

    constructor(
        //empty constructor
    )

    constructor(name: String, imageUrl: String?, price: String, description: String){
        this.name = name
        this.imageUrl = imageUrl
        this.price = price
        this.description = description
    }
}