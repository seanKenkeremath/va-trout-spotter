package com.kenkeremath.vatroutspotter.util

import com.kenkeremath.vatroutspotter.data.db.StockingEntity
import com.kenkeremath.vatroutspotter.domain.model.StockingInfo
import java.time.LocalDate
import java.time.LocalDateTime

object TestFactory {
    fun createStockingEntity(
        id: Long,
        date: LocalDate,
        waterbody: String,
        county: String = "County",
        category: String = "Category",
        species: List<String> = listOf("Rainbow Trout"),
        isNationalForest: Boolean = false,
        isNsf: Boolean = false,
        isHeritageDayWater: Boolean = false,
        isDelayedHarvest: Boolean = false,
        lastUpdated: LocalDateTime = LocalDateTime.now()
    ) = StockingEntity(
        id = id,
        date = date,
        county = county,
        waterbody = waterbody,
        category = category,
        species = species,
        isNationalForest = isNationalForest,
        isNsf = isNsf,
        isHeritageDayWater = isHeritageDayWater,
        isDelayedHarvest = isDelayedHarvest,
        lastUpdated = lastUpdated
    )

    fun createStockingInfo(
        id: Long,
        date: LocalDate,
        waterbody: String,
        county: String = "County",
        category: String = "Category",
        species: List<String> = listOf("Rainbow Trout"),
        isNationalForest: Boolean = false,
        isNsf: Boolean = false,
        isHeritageDayWater: Boolean = false,
        isDelayedHarvest: Boolean = false
    ) = StockingInfo(
        id = id,
        date = date,
        county = county,
        waterbody = waterbody,
        category = category,
        species = species,
        isNationalForest = isNationalForest,
        isNsf = isNsf,
        isHeritageDayWater = isHeritageDayWater,
        isDelayedHarvest = isDelayedHarvest,
    )
}