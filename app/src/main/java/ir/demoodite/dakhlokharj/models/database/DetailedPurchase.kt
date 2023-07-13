package ir.demoodite.dakhlokharj.models.database

data class DetailedPurchase(
    val id: Long,
    var product: String,
    var price: Long,
    var buyerId: Long,
    var time: Long,
    var buyerName: String,
)
