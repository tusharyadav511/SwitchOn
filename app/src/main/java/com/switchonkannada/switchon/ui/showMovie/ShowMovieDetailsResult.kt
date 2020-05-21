package com.switchonkannada.switchon.ui.showMovie

data class ShowMovieDetailsResult (
    val name :String ?= null,
    val about : String ? = null,
    val actionType : String ?= null,
    val age : String ?= null,
    val language : String ?= null,
    val movieUrl : String ?= null,
    val poster : String ?= null,
    val error : String ?= null
)