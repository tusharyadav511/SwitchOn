package com.switchonkannada.switchon

import com.google.firebase.database.Exclude

class SongHomeModel {

    var poster: String? = null
    var mkey: String? = null

    constructor(){

    }

    constructor(moviePoster: String?){
        this.poster = moviePoster
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