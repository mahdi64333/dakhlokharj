package ir.demoodite.dakhlokharj.models

data class AsyncOperationStatus(
    val isSuccessful: Boolean,
    val messageResourceId: Int = 0,
)