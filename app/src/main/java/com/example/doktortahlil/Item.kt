package com.example.doktortahlil

data class Item(
    val Tahlil: String,
    val ref_range: String,
    val low_message: String,
    val high_message: String,
    var isUpArrowSelected: Boolean = false,
    var isDownArrowSelected: Boolean = false
)