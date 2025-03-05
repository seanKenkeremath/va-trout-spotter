package com.seank.vatroutbuddy

import java.time.LocalDate
import java.time.Month

object AppConfig {
    // Initial stocking history fetch
    const val DEFAULT_MONTHS_PAST = 12L

    // Historical data fetch
    val HISTORICAL_DATA_START_DATE: LocalDate = LocalDate.of(2018, Month.OCTOBER, 1)

    // Pagination
    const val DEFAULT_PAGE_SIZE = 30

    // Periodic background fetch config
    const val BACKGROUND_FETCH_FREQUENCY_HOURS = 12L

    // Animations
    const val MODAL_ANIMATION_DURATION = 300
} 