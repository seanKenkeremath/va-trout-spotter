package com.seank.vatroutbuddy.data.html

import com.seank.vatroutbuddy.domain.model.StockingInfo
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StockingHtmlParser {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    fun parse(document: Document): List<StockingInfo> {
        return document.select("table tr")
            .drop(1) // Drop header row
            .mapNotNull { row ->
                val cells = row.select("td")
                if (cells.size >= 5) {
                    val dateText = cells[0].text()
                    val county = cells[1].text()
                    val waterbody = cells[2].text()
                    val category = cells[3].select("a").text()
                    val speciesList = cells[4].select("li").map { it.text() }
                    
                    val isNationalForest = waterbody.contains("[National Forest Water]")
                    val cleanWaterbody = waterbody.replace("[National Forest Water]", "").trim()

                    if (waterbody == "No Stockings Today") {
                        null
                    } else {
                        // TODO: Separate DTO fro scraping?
                        StockingInfo(
                            id = -1, // Placeholder ID until we generate one locally
                            date = LocalDate.parse(dateText, dateFormatter),
                            county = county,
                            waterbody = cleanWaterbody,
                            category = category,
                            species = speciesList,
                            isNationalForest = isNationalForest
                        )
                    }
                } else null
            }
    }
} 