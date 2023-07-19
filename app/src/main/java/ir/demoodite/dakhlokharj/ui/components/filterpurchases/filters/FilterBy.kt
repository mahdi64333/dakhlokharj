package ir.demoodite.dakhlokharj.ui.components.filterpurchases.filters

enum class FilterBy {
    PRODUCT_NAME,
    PRICE,
    BUYER,
    TIME,
    CONSUMER;

    companion object {
        operator fun get(ordinal: Int): FilterBy {
            return values()[ordinal]
        }

        val size get() = values().size
    }
}