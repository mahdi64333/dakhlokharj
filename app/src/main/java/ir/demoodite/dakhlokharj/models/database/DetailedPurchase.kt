package ir.demoodite.dakhlokharj.models.database

data class DetailedPurchase(
    val purchaseId: Long,
    var purchaseProduct: String,
    var purchasePrice: Long,
    var purchaseTime: Long,
    var buyerName: String,
)
