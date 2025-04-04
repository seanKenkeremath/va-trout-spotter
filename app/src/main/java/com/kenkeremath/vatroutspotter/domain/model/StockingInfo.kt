package com.kenkeremath.vatroutspotter.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class StockingInfo(
    val id: Long,
    val date: LocalDate,
    val county: String,
    val waterbody: String,
    val category: String,
    val species: List<String>,
    val isNationalForest: Boolean,
    val isNsf: Boolean,
    val isHeritageDayWater: Boolean,
    val isDelayedHarvest: Boolean
) : Parcelable