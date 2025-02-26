package com.seank.vatroutbuddy.data.repository

import com.seank.vatroutbuddy.data.html.StockingHtmlParser
import com.seank.vatroutbuddy.domain.model.StockingInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockingRepository @Inject constructor() {
    companion object {
        private const val BASE_URL = "https://dwr.virginia.gov/fishing/trout-stocking-schedule/"
        private val URL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    }

    suspend fun getRecentStockings(startDate: LocalDate): Result<List<StockingInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val formattedDate = startDate.format(URL_DATE_FORMATTER)
            val encodedDate = URLEncoder.encode(formattedDate, "UTF-8")
            val url = "$BASE_URL?start_date=$encodedDate&end_date="
            
            val doc = Jsoup.connect(url).get()
            StockingHtmlParser.parse(doc)
        }
    }
} 