package ir.demoodite.dakhlokharj.data.room.models

data class DetailedPurchase(
    val purchaseId: Long,
    var purchaseProduct: String,
    var purchasePrice: Double,
    var purchaseTime: Long,
    val purchaseBuyerId: Long,
    var buyerName: String,
) {
    val purchase
        get() = Purchase(
            id = purchaseId,
            product = purchaseProduct,
            price = purchasePrice,
            buyerId = purchaseBuyerId,
            time = purchaseTime
        )
}
