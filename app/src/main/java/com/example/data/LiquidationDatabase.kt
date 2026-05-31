package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [LiquidationLot::class, MarketplaceSource::class], version = 1, exportSchema = false)
abstract class LiquidationDatabase : RoomDatabase() {

    abstract fun liquidationDao(): LiquidationDao

    companion object {
        @Volatile
        private var INSTANCE: LiquidationDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): LiquidationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LiquidationDatabase::class.java,
                    "liquidation_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.beginTransaction()
            try {
                // Seed standard top liquidation marketplaces around the globe directly in the transaction
                val defaultSources = listOf(
                    arrayOf("B-Stock Network", "https://bstock.com", "Global (US & Europe)", "Official liquidation network for Amazon, Walmart, Costco, Target, and Best Buy. Direct auctions for pallets and truckloads.", "Electronics, Home Goods, Apparel, Appliances", 4.8f, 1, 0),
                    arrayOf("Direct Liquidation", "https://www.directliquidation.com", "North America", "Major liquidation broker featuring returns, overstocks, and refurbished merchandise from tier-1 US retail brands.", "Computers, Consoles, Tablets, General Merchandise", 4.5f, 1, 0),
                    arrayOf("Merkandi Wholesale", "https://merkandi.com", "Europe & UK", "Leading European destination for overstock, bankruptcy stocks, and liquidation lots. Connects wholesale buyers with sellers.", "Apparel, Stocklots, Food & Beverage, Home Decor", 4.6f, 1, 0),
                    arrayOf("Liquidation.com", "https://www.liquidation.com", "North America", "One of the oldest bulk liquidation brokers with distribution centers worldwide. Sells by box, pallet, or truckload.", "Electronics, Industrial, Jewelry, Clothing", 4.2f, 1, 0),
                    arrayOf("BULQ Lot Brokerage", "https://www.bulq.com", "North America", "Known for transparent manifests and fixed pricing/auctions. Friendly platform for beginner retail arbitrage.", "Toys, Apparel, Living Goods, Office Supplies", 4.4f, 1, 0),
                    arrayOf("Clearance Joblots", "https://www.clearancejoblots.co.uk", "Europe & UK", "UK-centered surplus stocking network for retail clearance, shop closeouts, and salvaged stocklots.", "Household Essentials, Tools, Giftware, Cosmetics", 4.3f, 1, 0),
                    arrayOf("Gemini Wholesale UK", "https://www.geminiwholesale.co.uk", "Europe & UK", "Clearance stock wholesaling, importing store liquidations, closeouts and surplus inventory in bulk.", "Pound Store Stock, Toys, Sourcing Pallets", 4.5f, 1, 0),
                    arrayOf("Globalsources Overstock", "https://gxsourcing.com", "Asia & Global", "B2B catalog platform connecting global buyers directly with Asian factories unloading unsold factory-overrun lots.", "Consumer Electronics, Accessories, OEM components", 4.7f, 1, 0)
                )
                for (source in defaultSources) {
                    db.execSQL(
                        "INSERT INTO marketplace_sources (name, websiteUrl, region, description, typicalCategories, scoring, holdsOverstock, isUserCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        source
                    )
                }
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
        }
    }
}
