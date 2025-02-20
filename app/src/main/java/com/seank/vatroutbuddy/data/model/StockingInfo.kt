package com.seank.vatroutbuddy.data.model

import java.time.LocalDate

data class StockingInfo(
    val id: String,
    val locationId: String,
    val date: LocalDate,
    val species: String,
    val amount: Int
) 