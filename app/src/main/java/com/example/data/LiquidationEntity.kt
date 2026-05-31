package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liquidation_lots")
data class LiquidationLot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val marketplace: String,
    val category: String,
    val condition: String, // Overstock, Customer Returns, Shelf Pulls, Salvage
    val retailValue: Double, // MSRP value of lot
    val costPrice: Double, // Price paid or current bid
    val itemCount: Int,
    val shippingCost: Double,
    val targetMaxBid: Double, // Calculated target bid using liquidation margin formulation
    val notes: String = "",
    val manifestSummary: String = "", // Saved AI manifest analysis summary
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "marketplace_sources")
data class MarketplaceSource(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val websiteUrl: String,
    val region: String, // North America, Europe, Asia, Global
    val description: String,
    val typicalCategories: String,
    val scoring: Float = 4.0f, // user high rating for reliable delivery
    val holdsOverstock: Boolean = true,
    val isUserCreated: Boolean = false
)
