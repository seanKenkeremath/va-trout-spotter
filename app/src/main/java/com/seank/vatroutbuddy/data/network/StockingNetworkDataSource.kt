package com.seank.vatroutbuddy.data.network

import com.seank.vatroutbuddy.data.html.StockingHtmlParser
import com.seank.vatroutbuddy.domain.model.StockingInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class StockingNetworkDataSource @Inject constructor() {
    companion object {
        private const val STOCKING_URL = "https://dwr.virginia.gov/fishing/trout-stocking-schedule/"
        private val URL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    }

    suspend fun fetchStockings(startDate: LocalDate): Result<List<StockingInfo>> = 
        withContext(Dispatchers.IO) {
            runCatching {
                val formattedDate = startDate.format(URL_DATE_FORMATTER)
                val encodedDate = URLEncoder.encode(formattedDate, "UTF-8")
                val url = "$STOCKING_URL?start_date=$encodedDate&end_date="
                
                val doc = Jsoup.connect(url).get()
                StockingHtmlParser.parse(doc)
            }
        }
} 