package ir.demoodite.dakhlokharj.data.room

import android.content.Context
import android.database.sqlite.SQLiteDatabaseCorruptException
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import ir.demoodite.dakhlokharj.data.room.dao.ConsumerDao
import ir.demoodite.dakhlokharj.data.room.dao.PurchaseDao
import ir.demoodite.dakhlokharj.data.room.dao.ResidentDao
import ir.demoodite.dakhlokharj.data.room.models.Consumer
import ir.demoodite.dakhlokharj.data.room.models.Purchase
import ir.demoodite.dakhlokharj.data.room.models.Resident
import kotlinx.coroutines.flow.first
import java.io.File

private const val DATABASE_VERSION = 6

@Database(
    entities = [
        Resident::class,
        Purchase::class,
        Consumer::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = true,
)
abstract class DataRepository : RoomDatabase() {
    // Dao functions and custom getters for better syntax
    protected abstract fun residentDao(): ResidentDao
    val residentDao get() = residentDao()
    protected abstract fun purchaseDao(): PurchaseDao
    val purchaseDao get() = purchaseDao()
    protected abstract fun consumerDao(): ConsumerDao
    val consumerDao get() = consumerDao()

    lateinit var dbFile: File

    companion object {
        // Database info
        private const val databaseName = "dakhlokharj.db"

        // Residents table keys
        const val residentsTableName = "residents"
        const val residentId = "residentId"
        const val residentName = "residentName"
        const val residentActive = "residentActive"
        const val residentDeleted = "residentDeleted"

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
        const val consumedProductId = "purchaseProductId"

        // Declaration of the singleton database object
        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getDatabase(context: Context): DataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = getDatabaseBuilder(context, databaseName).build().also {
                    it.dbFile = context.getDatabasePath(databaseName)
                }
                return INSTANCE!!
            }
        }

        private fun getDatabaseBuilder(
            context: Context,
            databaseName: String,
        ): Builder<DataRepository> {
            return Room.databaseBuilder(
                context.applicationContext, DataRepository::class.java, databaseName
            ).fallbackToDestructiveMigration().setJournalMode(JournalMode.TRUNCATE)
        }

        class DatabaseImporter(context: Context) {
            private val tempDatabaseName = "temp.db"
            private val tempDatabaseBuilder = getDatabaseBuilder(context, tempDatabaseName)
                .openHelperFactory { configuration ->
                    FrameworkSQLiteOpenHelperFactory().create(
                        SupportSQLiteOpenHelper.Configuration(
                            context,
                            configuration.name,
                            object : SupportSQLiteOpenHelper.Callback(DATABASE_VERSION) {

                                override fun onCorruption(db: SupportSQLiteDatabase) {
                                    throw SQLiteDatabaseCorruptException()
                                }

                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    configuration.callback.onCreate(db)
                                }

                                override fun onUpgrade(
                                    db: SupportSQLiteDatabase,
                                    oldVersion: Int,
                                    newVersion: Int,
                                ) {
                                    configuration.callback.onUpgrade(
                                        db,
                                        oldVersion,
                                        newVersion
                                    )
                                }
                            },
                            configuration.useNoBackupDirectory,
                            configuration.allowDataLossOnRecovery
                        )
                    )
                }
            private val tempDatabaseFile = context.getDatabasePath(tempDatabaseName)
            private val database = getDatabase(context)

            suspend fun importDb(file: File): Boolean {
                return try {
                    val tempDatabase = tempDatabaseBuilder.createFromFile(file).build()

                    database.clearAllTables()
                    database.residentDao.insert(tempDatabase.residentDao.getAll().first())
                    database.purchaseDao.insert(tempDatabase.purchaseDao.getAll().first())
                    database.consumerDao.insert(tempDatabase.consumerDao.getAll().first())

                    tempDatabase.close()
                    tempDatabaseFile.delete()

                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            suspend fun isDatabaseValid(dbFile: File): Boolean {
                return try {
                    tempDatabaseFile.writeBytes(dbFile.readBytes())
                    val tempDatabase = tempDatabaseBuilder.build()
                    tempDatabase.openHelper.readableDatabase
                    val residentId = tempDatabase.residentDao.insert(Resident())
                    val purchaseId = tempDatabase.purchaseDao.insert(Purchase())
                    tempDatabase.consumerDao.insert(listOf(Consumer(purchaseId, residentId)))
                    tempDatabase.close()
                    tempDatabaseFile.delete()

                    true
                } catch (e: SQLiteDatabaseCorruptException) {
                    e.printStackTrace()
                    false
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }
    }
}