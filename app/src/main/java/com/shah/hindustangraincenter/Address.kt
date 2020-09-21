package com.shah.hindustangraincenter

class Address {
    var name: String? = null
    var addr1: String? = null
    var addr2: String? = null
    var city: String? = null
    var state: String? = null
    var pin: String? = null
    var check: Boolean? = null
    var land: String? = null

    constructor(){
        //empty constructor
    }

    constructor(name: String?,addr1: String?,addr2: String?,city: String?,
                state: String,pin: String,check: Boolean,land: String){
        this.name = name
        this.addr1 = addr1
        this.addr2 = addr2
        this.city = city
        this.state = state
        this.pin = pin
        this.check = check
        this.land = land
    }
}