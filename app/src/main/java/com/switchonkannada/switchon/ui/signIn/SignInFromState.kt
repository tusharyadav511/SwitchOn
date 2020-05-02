package com.switchonkannada.switchon.ui.signIn

data class SignInFromState (
    val nameError:Int? = null,
    val emailError:Int? = null,
    val passwordError:Int? = null,
    val verifyPasswordError:Int? = null,
    val isDataValid: Boolean = false
)