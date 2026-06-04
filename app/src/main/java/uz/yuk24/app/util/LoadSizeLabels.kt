package uz.yuk24.app.util

import uz.yuk24.app.domain.model.LoadSize

object LoadSizeLabels {

    val DISPLAY: Map<LoadSize, String> = mapOf(
        LoadSize.XSMALL to "100 kg gacha",
        LoadSize.SMALL to "100–250 kg",
        LoadSize.MEDIUM to "250–500 kg",
        LoadSize.LARGE to "500–750 kg",
        LoadSize.XLARGE to "750 kg–1 tonna"
    )

    fun label(loadSize: LoadSize?): String = loadSize?.let { DISPLAY[it] } ?: "—"

    /**
     * Lower-bound price text per size: `BASE_PRICE * multiplier` (i.e. price for a 0 km
     * order without unloading). Yields 10,000 / 12,000 / 15,000 / 20,000 / 25,000 UZS.
     */
    fun minPriceText(loadSize: LoadSize?): String =
        loadSize?.let { PricingUtils.format(PricingUtils.minPrice(it)) } ?: "—"
}
