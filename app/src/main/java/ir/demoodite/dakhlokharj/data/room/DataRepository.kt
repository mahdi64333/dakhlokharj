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
        private set

    companion object {
        // Database info
        private const val DATABASE_NAME = "dakhlokharj.db"

        // Temp database info
        private const val TEMP_DATABASE_NAME = "temp.db"

        // Residents table keys
        const val RESIDENTS_TABLE_NAME = "residents"
        const val RESIDENT_ID = "residentId"
        const val RESIDENT_NAME = "residentName"
        const val RESIDENT_ACTIVE = "residentActive"
        const val RESIDENT_DELETED = "residentDeleted"

        // Receipts table keys
        const val PURCHASES_TABLE_NAME = "purchases"
        const val PURCHASE_ID = "purchaseId"
        const val PURCHASE_PRODUCT = "purchaseProduct"
        const val PURCHASE_PRICE = "purchasePrice"
        const val PURCHASE_BUYER_ID = "purchaseBuyerId"
        const val PURCHASE_TIME = "purchaseTime"

        // Consumers table keys
        const val CONSUMERS_TABLE_NAME = "consumers"
        const val CONSUMERS_RESIDENT_ID = "consumerId"
        const val CONSUMERS_PRODUCT_ID = "purchaseProductId"

        // Declaration of the singleton database object
        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getDatabase(context: Context): DataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = getDatabaseBuilder(context, DATABASE_NAME).build().also {
                    it.dbFile = context.getDatabasePath(DATABASE_NAME)
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
            )
                .fallbackToDestructiveMigration()
                .setJournalMode(JournalMode.TRUNCATE)
        }

        class DatabaseImporter(context: Context) {
            /*
            * All of this boilerplate code below it just for overriding the onCorruption method.
            * Unfortunately the default implementation of openHelperFactory by Room library
            * doesn't throw any error when a database is corrupted and just creates a blank
            * database silently.
            *  */
            private val tempDatabaseBuilder = getDatabaseBuilder(context, TEMP_DATABASE_NAME)
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
                                    // The important part
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
            private val tempDatabaseFile = context.getDatabasePath(TEMP_DATABASE_NAME)

            /**
             * An instance of the main database for importing data.
             * */
            private val mainDatabase = getDatabase(context)


            /**
             * Imports a database from file.
             *
             * @param dbFile File of the database to import
             * @return Whether importing was successful or not
             * */
            suspend fun importDb(dbFile: File): Boolean {
                return try {
                    val tempDatabase = tempDatabaseBuilder.createFromFile(dbFile).build()

                    /*
                    * Simply coping a file is not possible to import a database
                    * so data must be inserted manually.
                    * */
                    mainDatabase.clearAllTables()
                    mainDatabase.residentDao.insert(tempDatabase.residentDao.getAll().first())
                    mainDatabase.purchaseDao.insert(tempDatabase.purchaseDao.getAll().first())
                    mainDatabase.consumerDao.insert(tempDatabase.consumerDao.getAll().first())

                    tempDatabase.close()
                    tempDatabaseFile.delete()

                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            /**
             * Validates a database file scheme and format.
             *
             * @param dbFile File of the database to validate
             * @return Whether database was valid or not
             * */
            suspend fun isDatabaseValid(dbFile: File): Boolean {
                return try {
                    /*
                    * Coping dbFile to the temp database location
                    * to open it temporarily.
                    * */
                    tempDatabaseFile.writeBytes(dbFile.readBytes())

                    val tempDatabase = tempDatabaseBuilder.build()

                    /*
                    * Trying to access readableDatabase will result
                    * in a exception if database file is not a valid sqlite3 file.
                    * */
                    tempDatabase.openHelper.readableDatabase

                    /*
                    * Trying to write a record to tables will result
                    * in a exception if database scheme is not valid.
                    * */
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