package com.sportydev.appclimaroque

data class Day(
    val dayNumber: Int,
    val dayName: String,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val isSelectable: Boolean = true

)
