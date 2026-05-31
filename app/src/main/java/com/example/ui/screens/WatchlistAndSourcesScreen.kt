package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LiquidationLot
import com.example.data.MarketplaceSource
import com.example.ui.LiquidationViewModel
import com.example.ui.components.StarRatingBar
import com.example.ui.components.launchBrowserUrl
import com.example.ui.theme.GoldAccent

@Composable
fun WatchlistAndSourcesScreen(
    viewModel: LiquidationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val savedWatchlist by viewModel.savedWatchlist.collectAsState()
    val marketplaces by viewModel.marketplaces.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Watchlist, 1: Global Sources
    var showAddSourceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Header Selection row
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().testTag("watchlist_sub_tabs")
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("My Watchlist (${savedWatchlist.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Liquidation Platforms", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (activeSubTab == 0) {
                // --- WATCHLIST PORTFOLIO LAYOUT ---
                if (savedWatchlist.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Empty Watchlist",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Portfolio Sourcing Watchlist is empty",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Activate Global Seek & Find tab to scout wholesale lot auctions, or paste any description into the AI Manifest Analyzer to estimate gross profitability.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(savedWatchlist) { lot ->
                            WatchlistItemCard(
                                lot = lot,
                                onDelete = {
                                    viewModel.deleteFromWatchlist(lot)
                                    Toast.makeText(context, "Removed lot from Watchlist.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            } else {
                // --- GLOBAL DEALS PLATFORMS LISTING ---
                Column(modifier = Modifier.fillMaxSize()) {
                    // Floating register button wrapper
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Authenticated Stock Brokers",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Button(
                            onClick = { showAddSourceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp).testTag("add_custom_source")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Register Source", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(marketplaces) { source ->
                            MarketplaceSourceCard(
                                source = source,
                                onDeleteCustom = {
                                    if (source.isUserCreated) {
                                        viewModel.deleteCustomSource(source)
                                        Toast.makeText(context, "Custom source deleted.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet dialog to add custom wholesale brokers
    if (showAddSourceDialog) {
        var newName by remember { mutableStateOf("") }
        var newUrl by remember { mutableStateOf("") }
        var newRegion by remember { mutableStateOf("Global") }
        var newDesc by remember { mutableStateOf("") }
        var newCats by remember { mutableStateOf("") }

        val regions = listOf("Global", "North America", "Europe", "Asia")
        var regionExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddSourceDialog = false },
            title = {
                Text(
                    "Local Liquidation Broker Registry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Marketplace Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("src_input_name"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("Website Domain / URL *") },
                        modifier = Modifier.fillMaxWidth().testTag("src_input_url"),
                        singleLine = true,
                        placeholder = { Text("e.g. wholesaleliquidators.co") }
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newRegion,
                            onValueChange = { },
                            label = { Text("Wholesale Sourcing Region") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().testTag("src_input_region"),
                            trailingIcon = {
                                IconButton(onClick = { regionExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = regionExpanded,
                            onDismissRequest = { regionExpanded = false }
                        ) {
                            regions.forEach { reg ->
                                DropdownMenuItem(
                                    text = { Text(reg) },
                                    onClick = {
                                        newRegion = reg
                                        regionExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newCats,
                        onValueChange = { newCats = it },
                        label = { Text("Primary Categories") },
                        modifier = Modifier.fillMaxWidth().testTag("src_input_cats"),
                        singleLine = true,
                        placeholder = { Text("e.g. Toys, Consumer Tech, Overstock") }
                    )

                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Strategic Sourcing Notes") },
                        modifier = Modifier.fillMaxWidth().height(80.dp).testTag("src_input_desc"),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isBlank() || newUrl.isBlank()) {
                            Toast.makeText(context, "Name and Website URL are core required fields.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.addCustomSource(
                            MarketplaceSource(
                                name = newName,
                                websiteUrl = newUrl,
                                region = newRegion,
                                description = if (newDesc.isBlank()) "Custom registered wholesale warehouse source." else newDesc,
                                typicalCategories = if (newCats.isBlank()) "General Merchandise" else newCats,
                                scoring = 5.0f,
                                isUserCreated = true
                            )
                        )
                        showAddSourceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Register Source")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSourceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WatchlistItemCard(
    lot: LiquidationLot,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("watchlist_card_${lot.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lot.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Warehouse Hub: ${lot.marketplace} • ${lot.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Delete Icon
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_watchlist_${lot.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Trash delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Margin assessment layout
            val projectedProfit = lot.retailValue - lot.costPrice - lot.shippingCost
            val profitColor = if (projectedProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total MSRP valuation", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(String.format("$%,.2f", lot.retailValue), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("Winning Bid / Costs", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(String.format("$%,.2f", lot.costPrice), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("Est Net Returns", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(
                        String.format("$%,.2f", projectedProfit),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = profitColor
                    )
                }
            }

            // Associated manifest brief descriptions or notes
            if (lot.manifestSummary.isNotEmpty() || lot.notes.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    val label = if (lot.notes.isNotEmpty()) lot.notes else lot.manifestSummary
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun MarketplaceSourceCard(
    source: MarketplaceSource,
    onDeleteCustom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("source_card_${source.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(2.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) {
                            Text(
                                text = source.region.uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        StarRatingBar(rating = source.scoring)
                    }
                }

                // Delete custom registered sources
                if (source.isUserCreated) {
                    IconButton(
                        onClick = onDeleteCustom,
                        modifier = Modifier.size(24.dp).testTag("delete_source_${source.id}")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete custom",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = source.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Core focus: ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = source.typicalCategories,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Launch Browser url to inspect deals live!
            Button(
                onClick = { launchBrowserUrl(context, source.websiteUrl) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .testTag("visit_button_${source.id}")
            ) {
                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Surf Sourcing Platform", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
