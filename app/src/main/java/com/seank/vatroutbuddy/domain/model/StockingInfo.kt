package com.seank.vatroutbuddy.domain.model

import java.time.LocalDate

data class StockingInfo(
    val date: LocalDate,
    val county: String,
    val waterbody: String,
    val category: String,
    val species: List<String>,
    val isNationalForest: Boolean
)