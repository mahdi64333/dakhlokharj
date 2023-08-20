package ir.demoodite.dakhlokharj.ui.components.filterPurchases.filters

/**
 * All the ways that purchases can get filtered.
 */
enum class PurchaseFilters {
    PRODUCT_NAME,
    PRICE,
    BUYER,
    TIME,
    CONSUMER;

    companion object {
        operator fun get(ordinal: Int): PurchaseFilters {
            return values()[ordinal]
        }

        val size get() = values().size
    }
}