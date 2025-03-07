package com.seank.vatroutbuddy.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.seank.vatroutbuddy.domain.model.StockingInfo
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "stockings",

    indices = [
        // De-dupe scraped data
        Index(
            value = ["date", "waterbody"],
            unique = true
        ),
        // Index for pagination
        Index(value = ["date", "waterbody", "id"])
    ],
)
data class StockingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val county: String,
    val waterbody: String,
    val category: String,
    val species: List<String>,
    val isNationalForest: Boolean,
    val isNsf: Boolean,
    val isHeritageDayWater: Boolean,
    val isDelayedHarvest: Boolean,
    val lastUpdated: LocalDateTime
) {
    fun toStockingInfo() = StockingInfo(
        id = id,
        date = date,
        county = county,
        waterbody = waterbody,
        category = category,
        species = species,
        isNationalForest = isNationalForest,
        isNsf = isNsf,
        isHeritageDayWater = isHeritageDayWater,
        isDelayedHarvest = isDelayedHarvest
    )

    companion object {
        fun fromStockingInfo(
            info: StockingInfo,
            lastUpdated: LocalDateTime = LocalDateTime.now()
        ) = StockingEntity(
            date = info.date,
            county = info.county,
            waterbody = info.waterbody,
            category = info.category,
            species = info.species,
            isNationalForest = info.isNationalForest,
            isNsf = info.isNsf,
            isHeritageDayWater = info.isHeritageDayWater,
            isDelayedHarvest = info.isDelayedHarvest,
            lastUpdated = lastUpdated
        )
    }
} 