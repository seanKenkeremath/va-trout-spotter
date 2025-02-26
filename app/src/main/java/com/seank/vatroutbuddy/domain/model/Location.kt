package com.seank.vatroutbuddy.domain.model

data class Location(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
) 