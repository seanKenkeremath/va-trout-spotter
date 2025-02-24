package com.seank.vatroutbuddy.data.repository

import com.seank.vatroutbuddy.data.html.StockingHtmlParser
import com.seank.vatroutbuddy.data.model.StockingInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockingRepository @Inject constructor() {
    companion object {
        private const val STOCKING_URL = "https://dwr.virginia.gov/fishing/trout-stocking-schedule/"
    }

    suspend fun getRecentStockings(): Result<List<StockingInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val doc = Jsoup.connect(STOCKING_URL).get()
            StockingHtmlParser.parse(doc)
        }
    }
} 