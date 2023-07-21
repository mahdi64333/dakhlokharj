package ir.demoodite.dakhlokharj.data.room.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.demoodite.dakhlokharj.data.room.DataRepository
import ir.demoodite.dakhlokharj.data.room.models.Purchase

class DeletePurchaseWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = DataRepository.getDatabase(context)
        return try {
            val purchaseId = inputData.getLong(PURCHASE_ID_KEY, -1)
            database.purchaseDao.delete(Purchase(purchaseId))
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val PURCHASE_ID_KEY = "purchase_id"
    }
}