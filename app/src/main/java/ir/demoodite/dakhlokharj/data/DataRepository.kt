package ir.demoodite.dakhlokharj.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ir.demoodite.dakhlokharj.models.database.Consumer
import ir.demoodite.dakhlokharj.models.database.Purchase
import ir.demoodite.dakhlokharj.models.database.Resident

@Database(
    entities = [
        Resident::class,
        Purchase::class,
        Consumer::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class DataRepository : RoomDatabase() {
    // Dao functions and custom getters for better syntax
    protected abstract fun residentDao(): ResidentDao
    val residentDao get() = residentDao()
    protected abstract fun purchaseDao(): PurchaseDao
    val purchaseDao get() = purchaseDao()
    protected abstract fun consumerDao(): ConsumerDao
    val consumerDao get() = consumerDao()

    companion object {
        // Database info
        private const val databaseName = "dakhlokharj.db"

        // Residents table keys
        const val residentsTableName = "residents"
        const val residentId = "residentId"
        const val residentName = "residentName"
        const val residentActive = "residentActive"

        // Receipts table keys
        const val purchasesTableName = "purchases"
        const val purchaseId = "purchaseId"
        const val purchaseProduct = "purchaseProduct"
        const val purchasePrice = "purchasePrice"
        const val purchaseBuyerId = "purchaseBuyerId"
        const val purchaseTime = "purchaseTime"

        // Consumers table keys
        const val consumersTableName = "consumers"
        const val consumerResidentId = "consumerId"
        const val purchaseProductId = "purchaseProductId"

        // Declaration of the singleton database object
        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getDatabase(context: Context): DataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    DataRepository::class.java,
                    databaseName
                )
                    .fallbackToDestructiveMigration()
                    .build()
                return INSTANCE!!
            }
        }
    }
}