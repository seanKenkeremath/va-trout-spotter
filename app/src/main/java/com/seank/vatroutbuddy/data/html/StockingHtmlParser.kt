package com.seank.vatroutbuddy.data.html

import com.seank.vatroutbuddy.domain.model.StockingInfo
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StockingHtmlParser {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    private val bracketPattern = Regex("\\s*\\[.*?\\]\\s*")

    fun parse(document: Document): List<StockingInfo> {
        return document.select("table tr")
            .drop(1) // Drop header row
            .mapNotNull { row ->
                val cells = row.select("td")
                if (cells.size >= 5) {
                    val dateText = cells[0].text()
                    val county = cells[1].text()
                    val rawWaterbody = cells[2].text()
                    val category = cells[3].select("a").text()
                    val speciesList = cells[4].select("li").map { it.text() }
                    
                    if (rawWaterbody == "No Stockings Today") {
                        null
                    } else {
                        // Parse waterbody tags
                        val isNationalForest = rawWaterbody.contains("[National Forest Water]", ignoreCase = true)
                        val isNsf = rawWaterbody.contains("[NSF]", ignoreCase = true)
                        val isHeritageWater = rawWaterbody.contains("[Heritage Day Water]", ignoreCase = true)
                        val isDelayedHarvest = rawWaterbody.contains("[Delayed Harvest Water]", ignoreCase = true)

                        // Clean waterbody name by removing all bracketed text
                        val cleanWaterbody = bracketPattern.replace(rawWaterbody, " ").trim()
                        
                        StockingInfo(
                            id = -1, // Placeholder ID until we generate one locally
                            date = LocalDate.parse(dateText, dateFormatter),
                            county = county,
                            waterbody = cleanWaterbody,
                            category = category,
                            species = speciesList,
                            isNationalForest = isNationalForest,
                            isNsf = isNsf,
                            isHeritageDayWater = isHeritageWater,
                            isDelayedHarvest = isDelayedHarvest
                        )
                    }
                } else null
            }
    }
} 