package com.switchonkannada.switchon

import com.google.firebase.database.Exclude

class SongHomeModel {

    var poster: String? = null
    var songUrl:String ?= null
    var mkey: String? = null

    constructor(){

    }

    constructor(moviePoster: String? , songUrl: String?){
        this.poster = moviePoster
        this.songUrl = songUrl
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