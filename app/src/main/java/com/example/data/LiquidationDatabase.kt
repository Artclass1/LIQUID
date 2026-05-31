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
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDefaultMarketplaces(database.liquidationDao())
                }
            }
        }

        private suspend fun populateDefaultMarketplaces(dao: LiquidationDao) {
            // Seed standard top liquidation marketplaces around the globe
            val defaultSources = listOf(
                MarketplaceSource(
                    name = "B-Stock Network",
                    websiteUrl = "https://bstock.com",
                    region = "Global (US & Europe)",
                    description = "Official liquidation network for Amazon, Walmart, Costco, Target, and Best Buy. Direct auctions for pallets and truckloads.",
                    typicalCategories = "Electronics, Home Goods, Apparel, Appliances",
                    scoring = 4.8f,
                    holdsOverstock = true,
                    isUserCreated = false
                ),
                MarketplaceSource(
                    name = "Direct Liquidation",
                    websiteUrl = "https://www.directliquidation.com",
                    region = "North America",
                    description = "Major liquidation broker featuring returns, overstocks, and refurbished merchandise from tier-1 US retail brands.",
                    typicalCategories = "Computers, Consoles, Tablets, General Merchandise",
                    scoring = 4.5f,
                    holdsOverstock = true,
                    isUserCreated = false
                ),
                MarketplaceSource(
                    name = "Merkandi Wholesale",
                    websiteUrl = "https://merkandi.com",
                    region = "Europe & UK",
                    description = "Leading European destination for overstock, bankruptcy stocks, and liquidation lots. Connects wholesale buyers with sellers.",
                    typicalCategories = "Apparel, Stocklots, Food & Beverage, Home Decor",
                    scoring = 4.6f,
                    holdsOverstock = true,
                    isUserCreated = false
                ),
                MarketplaceSource(
                    name = "Liquidation.com",
                    websiteUrl = "https://www.liquidation.com",
                    region = "North America",
                    description = "One of the oldest bulk liquidation brokers with distribution centers worldwide. Sells by box, pallet, or truckload.",
                    typicalCategories = "Electronics, Industrial, Jewelry, Clothing",
                    scoring = 4.2f,
                    holdsOverstock = true,
                    isUserCreated = false
                ),
                MarketplaceSource(
                    name = "BULQ Lot Brokerage",
                    websiteUrl = "https://www.bulq.com",
                    region = "North America",
                    description = "Known for transparent manifests and fixed pricing/auctions. Friendly platform for beginner retail arbitrage.",
                    typicalCategories = "Toys, Apparel, Living Goods, Office Supplies",
                    scoring = 4.4f,
                    holdsOverstock = true,
                    isUserCreated = false
                ),
                MarketplaceSource(
                    name = "Clearance Joblots",
                    websiteUrl = "https://www.clearancejoblots.co.uk",
                    region = "Europe & UK",
                    description = "UK-centered surplus stocking network for retail clearance, shop closeouts, and salvaged stocklots.",
                    typicalCategories = "Household Essentials, Tools, Giftware, Cosmetics",
                    scoring = 4.3f,
                    holdsOverstock = true,
                    isUserCreated = false
                ),
                MarketplaceSource(
                    name = "Gemini Wholesale UK",
                    websiteUrl = "https://www.geminiwholesale.co.uk",
                    region = "Europe & UK",
                    description = "Clearance stock wholesaling, importing store liquidations, closeouts and surplus inventory in bulk.",
                    typicalCategories = "Pound Store Stock, Toys, Sourcing Pallets",
                    scoring = 4.5f,
                    holdsOverstock = true,
                    isUserCreated = false
                ),
                MarketplaceSource(
                    name = "Globalsources Overstock",
                    websiteUrl = "https://gxsourcing.com",
                    region = "Asia & Global",
                    description = "B2B catalog platform connecting global buyers directly with Asian factories unloading unsold factory-overrun lots.",
                    typicalCategories = "Consumer Electronics, Accessories, OEM components",
                    scoring = 4.7f,
                    holdsOverstock = true,
                    isUserCreated = false
                )
            )
            for (source in defaultSources) {
                dao.insertSource(source)
            }
        }
    }
}
