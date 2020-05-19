package com.switchonkannada.switchon

import com.google.firebase.database.Exclude

class HomeModel {
    var moviePoster: String? = null
    var mkey: String? = null

    constructor(){

    }

    constructor(moviePoster: String?){
        this.moviePoster = moviePoster
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