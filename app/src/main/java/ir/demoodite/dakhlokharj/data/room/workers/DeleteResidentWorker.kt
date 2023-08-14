package ir.demoodite.dakhlokharj.data.room.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.demoodite.dakhlokharj.data.room.DataRepository

class DeleteResidentWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = DataRepository.getDatabase(context)
        return try {
            val residentId = inputData.getLong(RESIDENT_ID_KEY, -1)
            database.residentDao.delete(residentId)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val RESIDENT_ID_KEY = "residents_id"
    }
}