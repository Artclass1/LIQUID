package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LiquidationDao {

    // --- Liquidation Watchlist Operations ---
    @Query("SELECT * FROM liquidation_lots ORDER BY dateAdded DESC")
    fun getAllLotsFlow(): Flow<List<LiquidationLot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: LiquidationLot): Long

    @Delete
    suspend fun deleteLot(lot: LiquidationLot)

    @Query("SELECT * FROM liquidation_lots WHERE id = :id")
    suspend fun getLotById(id: Long): LiquidationLot?

    // --- Marketplace Sourcing Platform Operations ---
    @Query("SELECT * FROM marketplace_sources ORDER BY region ASC, name ASC")
    fun getAllSourcesFlow(): Flow<List<MarketplaceSource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: MarketplaceSource): Long

    @Delete
    suspend fun deleteSource(source: MarketplaceSource)

    @Query("SELECT COUNT(*) FROM marketplace_sources")
    suspend fun getSourcesCount(): Int
}
