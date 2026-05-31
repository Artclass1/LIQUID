package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LiquidationLot
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.DealSpotterScreen
import com.example.ui.screens.ManifestAnalyzerScreen
import com.example.ui.screens.WatchlistAndSourcesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    viewModel: LiquidationViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) }
    var prepopulatedCalcLot by remember { mutableStateOf<LiquidationLot?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "GLOBAL LIQUI-TRADER",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Deal Spotter") },
                    label = { Text("Seek Lots", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_seek_lots")
                )

                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Analyzer") },
                    label = { Text("AI Auditor", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_ai_auditor")
                )

                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") },
                    label = { Text("ROI Calc", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_roi_calc")
                )

                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved Watchlist") },
                    label = { Text("Watchlist", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_watchlist")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> {
                    DealSpotterScreen(
                        viewModel = viewModel,
                        onNavigateToCalculator = { scoutedDeal ->
                            // Convert scouted deal into a lot representation for the Calculator
                            prepopulatedCalcLot = LiquidationLot(
                                title = scoutedDeal.title,
                                marketplace = scoutedDeal.marketplace,
                                category = scoutedDeal.category,
                                condition = scoutedDeal.condition,
                                retailValue = scoutedDeal.retailValue,
                                costPrice = scoutedDeal.costPrice,
                                itemCount = scoutedDeal.itemCount,
                                shippingCost = scoutedDeal.shippingCost,
                                targetMaxBid = scoutedDeal.targetMaxBid
                            )
                            // Switch tab to Calculator
                            activeTab = 2
                        }
                    )
                }
                1 -> {
                    ManifestAnalyzerScreen(viewModel = viewModel)
                }
                2 -> {
                    CalculatorScreen(
                        viewModel = viewModel,
                        prepopulatedLot = prepopulatedCalcLot
                    )
                    // Clear the buffer after loading to allow other local calculations
                    DisposableEffect(Unit) {
                        onDispose {
                            prepopulatedCalcLot = null
                        }
                    }
                }
                3 -> {
                    WatchlistAndSourcesScreen(viewModel = viewModel)
                }
            }
        }
    }
}
