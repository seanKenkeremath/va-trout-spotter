package com.kenkeremath.vatroutbuddy

import java.time.LocalDate
import java.time.Month

object AppConfig {
    // Initial stocking history fetch
    const val DEFAULT_MONTHS_PAST = 12L

    // Historical data fetch
    val HISTORICAL_DATA_START_DATE: LocalDate = LocalDate.of(2018, Month.OCTOBER, 1)

    // Pagination
    const val DEFAULT_PAGE_SIZE = 30
    const val PRELOAD_PAGE_OFFSET = 6

    // Periodic background fetch config
    const val BACKGROUND_FETCH_FREQUENCY_HOURS = 12L
    const val BACKGROUND_FETCH_DELAY_HOURS = 2L

    // Animations
    // This should be longest or tied for longest among modal animation lengths
    const val MODAL_CONTENT_ANIMATION_DURATION_IN = 500
    const val MODAL_CONTENT_ANIMATION_DURATION_OUT = 300
    const val MODAL_LOADING_ANIMATION_DURATION_IN = 300
    // This should be longer than modal exit length or it will not be visible
    const val MODAL_LOADING_ANIMATION_DURATION_OUT = 500
    const val MODAL_ANIMATION_DELAY = 100
    // The relative height of the screen the bottom waves should take up when refreshing
    const val REFRESH_WAVE_ANIMATION_HEIGHT_FRACTION = .2f
    const val REFRESH_WAVE_ANIMATION_ALPHA = .4f

    // Nav
    const val ALLOW_COLLAPSIBLE_NAV = false

    // Pull to refresh throttling
    const val REFRESH_THROTTLE_MILLIS = 120000L
    // Default refresh animation duration when throttled
    const val REFRESH_THROTTLE_DELAY_MILLIS = 1500L
}