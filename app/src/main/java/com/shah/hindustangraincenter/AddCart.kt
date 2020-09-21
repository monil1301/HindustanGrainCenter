package com.shah.hindustangraincenter

import com.google.firebase.database.DatabaseReference

class AddCart {
    var name: String? = null
    var imageUrl: String? = null
    var price: String? = null
    var quantity: String? = null
    var costRef: String? = null
    var id: String? = null
    var cost: String? = null

    constructor(
        //empty constructor
    )

    constructor(name: String?,imageUrl: String?,price: String?,quantity: String,
                costRef: String?,id: String,cost: String?){
        this.name = name
        this.imageUrl = imageUrl
        this.price = price
        this.quantity = quantity
        this.costRef = costRef
        this.id = id
        this.cost = cost
    }
}