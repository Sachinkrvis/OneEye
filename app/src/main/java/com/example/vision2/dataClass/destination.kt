package com.example.vision2.dataClass

sealed class Destination(val route :String){
    data object HOME:Destination("Home")
    data object NAVIGATION:Destination("Navigation")
    data object EMAIL:Destination("Email")
    data object PHONE_CALL:Destination("Phone Call")
    data object TRANSLATE:Destination("Translate")
}