data class StockingFilters(
    val counties: Set<String> = emptySet(),
    val isNationalForest: Boolean? = null,
    val isHeritageDayWater: Boolean? = null,
    val isNsf: Boolean? = null,
    val isDelayedHarvest: Boolean? = null,
    val searchTerm: String? = null,
) {
    val activeFilterCount: Int
        get() = listOf(
            counties.isNotEmpty(),
            isNationalForest != null,
            isHeritageDayWater != null,
            isNsf != null,
            isDelayedHarvest != null,
            !searchTerm.isNullOrBlank()
        ).count { it }
} 