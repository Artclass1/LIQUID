package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LiquidationViewModel(private val repository: LiquidationRepository) : ViewModel() {

    // --- Database Flows ---
    val savedWatchlist: StateFlow<List<LiquidationLot>> = repository.allSavedLots
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val marketplaces: StateFlow<List<MarketplaceSource>> = repository.allSources
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- AI Manifest Analysis States ---
    private val _isAnalyzingManifest = MutableStateFlow(false)
    val isAnalyzingManifest = _isAnalyzingManifest.asStateFlow()

    private val _manifestAnalysisResult = MutableStateFlow<ManifestAnalysisResult?>(null)
    val manifestAnalysisResult = _manifestAnalysisResult.asStateFlow()

    private val _manifestAnalysisError = MutableStateFlow<String?>(null)
    val manifestAnalysisError = _manifestAnalysisError.asStateFlow()

    // --- AI Global Deal Scouting States ---
    private val _isScoutingDeals = MutableStateFlow(false)
    val isScoutingDeals = _isScoutingDeals.asStateFlow()

    private val _scoutedDeals = MutableStateFlow<List<ScoutedLot>>(emptyList())
    val scoutedDeals = _scoutedDeals.asStateFlow()

    private val _scoutingDealsError = MutableStateFlow<String?>(null)
    val scoutingDealsError = _scoutingDealsError.asStateFlow()

    // Selected lot for the in-depth Calculator
    private val _selectedCalculatorLot = MutableStateFlow<LiquidationLot?>(null)
    val selectedCalculatorLot = _selectedCalculatorLot.asStateFlow()

    init {
        // Run an initial scout for the general department items
        scoutGlobalLiquidation("General Merchandise", "Global")
    }

    // --- AI Manifest Analysis Action ---
    fun analyzeManifest(manifestText: String) {
        if (manifestText.trim().isEmpty()) {
            _manifestAnalysisError.value = "Please input a valid manifest list or lot overview."
            return
        }
        viewModelScope.launch {
            _isAnalyzingManifest.value = true
            _manifestAnalysisError.value = null
            _manifestAnalysisResult.value = null
            try {
                val result = repository.analyzeManifest(manifestText)
                _manifestAnalysisResult.value = result
            } catch (e: Exception) {
                _manifestAnalysisError.value = "Failed to evaluate package details: ${e.localizedMessage}"
            } finally {
                _isAnalyzingManifest.value = false
            }
        }
    }

    fun clearManifestAnalysis() {
        _manifestAnalysisResult.value = null
        _manifestAnalysisError.value = null
    }

    // --- AI Global Deal Scouting Action ---
    fun scoutGlobalLiquidation(category: String, region: String) {
        viewModelScope.launch {
            _isScoutingDeals.value = true
            _scoutingDealsError.value = null
            try {
                val deals = repository.scoutGlobalDeals(category, region)
                _scoutedDeals.value = deals
            } catch (e: Exception) {
                _scoutingDealsError.value = "Failed to seek live inventories: ${e.localizedMessage}"
            } finally {
                _isScoutingDeals.value = false
            }
        }
    }

    // --- Database Mutators ---
    fun addToWatchlist(lot: LiquidationLot) {
        viewModelScope.launch {
            repository.saveLot(lot)
        }
    }

    fun deleteFromWatchlist(lot: LiquidationLot) {
        viewModelScope.launch {
            repository.deleteLot(lot)
        }
    }

    fun addCustomSource(source: MarketplaceSource) {
        viewModelScope.launch {
            repository.saveSource(source)
        }
    }

    fun deleteCustomSource(source: MarketplaceSource) {
        viewModelScope.launch {
            repository.deleteSource(source)
        }
    }

    // --- Helpers to auto-convert scouted deals into saved lots ---
    fun saveScoutedDeal(deal: ScoutedLot, customNotes: String = "") {
        viewModelScope.launch {
            val savedLot = LiquidationLot(
                title = deal.title,
                marketplace = deal.marketplace,
                category = deal.category,
                condition = deal.condition,
                retailValue = deal.retailValue,
                costPrice = deal.costPrice,
                itemCount = deal.itemCount,
                shippingCost = deal.shippingCost,
                targetMaxBid = deal.targetMaxBid,
                notes = customNotes,
                manifestSummary = deal.description
            )
            repository.saveLot(savedLot)
        }
    }
}

class LiquidationViewModelFactory(private val repository: LiquidationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiquidationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LiquidationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
