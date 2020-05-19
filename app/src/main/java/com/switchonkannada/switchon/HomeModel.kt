package com.switchonkannada.switchon

import com.google.firebase.database.Exclude

class HomeModel {
    var postUrl: String? = null
    var mkey: String? = null

    constructor(){

    }

    constructor(postUrl: String?){
        this.postUrl = postUrl
    }

    @Exclude
    fun getKey(): String? {
        return mkey
    }

    @Exclude
    fun setKey(key: String) {
        mkey = key
    }
}