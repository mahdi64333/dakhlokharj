package ir.demoodite.dakhlokharj.data.room.models

data class DetailedPurchase(
    val purchaseId: Long,
    var purchaseProduct: String,
    var purchasePrice: Long,
    var purchaseTime: Long,
    val purchaseBuyerId: Long,
    var buyerName: String,
)
