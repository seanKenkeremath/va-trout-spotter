package com.kenkeremath.vatroutspotter.util

interface Clock {
    fun currentTimeMillis(): Long
}

class DefaultClock : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
} 