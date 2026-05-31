package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LiquidationLot
import com.example.ui.LiquidationViewModel
import com.example.ui.components.CircularRiskGauge
import com.example.ui.theme.GoldAccent

@Composable
fun ManifestAnalyzerScreen(
    viewModel: LiquidationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var manifestText by remember { mutableStateOf("") }

    val analysisResult by viewModel.manifestAnalysisResult.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingManifest.collectAsState()
    val analysisError by viewModel.manifestAnalysisError.collectAsState()

    // Preloaded helper presets for instant testing
    val presets = listOf(
        Pair("📱 Phone Lot Returns", "45x iPhone 12 (mix of 64GB & 128GB, customer returns), 15x Samsung Galaxy S21, 50x Charging adaptors, 20x wireless earbuds (untested, open-box)"),
        Pair("💻 Office Closeout", "12x ThinkPad T490 Core i5 (used, powers on), 8x Apple iPad 7th Gen (cracked screens), 15x mechanical computer keyboards, 6x curved monitors (A-Grade)"),
        Pair("👟 Apparel Packout", "150x Premium athletic sneakers (unopened box, overstock, Nike and Adidas assortments), 80x vintage hoodies, 50x unisex cargo trousers")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top branding intro
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI manifest",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "AI Manifest Deconstructor & Appraiser",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Wholesale shipping lots and container sales often ship with dense lists. Paste any manifest, invoice text, or inventory description and click Evaluate Cargo Lot. Our AI deconstructs each line, estimates items individual MSRP values, evaluates risks, and formulates maximum bid thresholds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        // Input card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Inventory Manifest / Descriptions:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // The input text box
                OutlinedTextField(
                    value = manifestText,
                    onValueChange = { manifestText = it },
                    placeholder = {
                        Text(
                            text = "Example input:\n30x Samsung SmartTVs (assorted returns)\n15x soundbars\n7x gaming laptops (repair needed)\nOr paste standard Excel/Wholesaler manifest tables here.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("manifest_input_text"),
                    textStyle = TextStyle(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Presets chip cluster
                Text(
                    text = "Select a quick Preset cargo to test:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        AssistChip(
                            onClick = { manifestText = preset.second },
                            label = { Text(preset.first, fontSize = 11.sp) },
                            modifier = Modifier.testTag("preset_${preset.first.replace(" ", "_")}")
                        )
                    }
                }

                // Call to actions trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            manifestText = ""
                            viewModel.clearManifestAnalysis()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(42.dp).testTag("clear_button")
                    ) {
                        Text("Clear")
                    }

                    Button(
                        onClick = { viewModel.analyzeManifest(manifestText) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(2f).height(42.dp).testTag("evaluate_button")
                    ) {
                        Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Evaluate Cargo Lot", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Processing & Loading
        if (isAnalyzing) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Gemini is auditing manifest lines, matching global manufacturer retail profiles, and compiling financial projections...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Parsing analysis errors
        if (analysisError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Text(
                        text = analysisError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Analysis Result View display
        analysisResult?.let { result ->
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Primary report card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "AI Inventory Audit Report",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    text = "SUCCESS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Divider()

                        // Multi-Metrics Grid Layout spacing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                val stats = listOf(
                                    Pair("Total Est. MSRP", formatPrice(result.totalMSRP)),
                                    Pair("Wholesale Buying Limit", formatPrice(result.suggestedMaxBid)),
                                    Pair("Estimated Inventory Vol.", "${result.itemsCount} assets")
                                )
                                stats.forEach { item ->
                                    Column {
                                        Text(item.first, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        Text(
                                            item.second,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (item.first.contains("MSRP")) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Circular risk gauge right aligned
                            CircularRiskGauge(
                                score = result.riskScore,
                                riskLevel = result.riskLevel
                            )
                        }

                        // Project margin bar widget
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Suggested Gross ROI Target", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text("${result.marginPercent}%", fontSize = 12.sp, fontWeight = FontWeight.Black, color = GoldAccent)
                            }
                            LinearProgressIndicator(
                                progress = result.marginPercent.toFloat() / 100f,
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = GoldAccent,
                                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                // AI Expert qualitative commentary reports
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Cargo Strategic Assessment:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = result.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Sell, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                            Column {
                                Text("Online Sales Channel Potential:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text(result.ebayResellPotential, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Column {
                                Text("Logistical Shipping Recommendation:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text(result.shippingAdvice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                    }
                }

                // Items list breakdown table
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Itemized Inventory Breakdown",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TableTitleRow()

                        result.items.forEach { item ->
                            ItemTableRow(item = item)
                        }
                    }
                }

                // Single-tap quick save watchlist action
                Button(
                    onClick = {
                        val firstThreeDesc = manifestText.take(50) + "..."
                        val mockLot = LiquidationLot(
                            title = "APRAISED LOT: ${result.items.firstOrNull()?.name ?: "Bulk Assets"} + ${result.items.size - 1} items",
                            marketplace = "AI Audited Invoice",
                            category = "Multi-Category Analysis",
                            condition = "Verify Manifest Audit Report",
                            retailValue = result.totalMSRP,
                            costPrice = result.suggestedMaxBid * 0.8, // assume buy discount
                            itemCount = result.itemsCount,
                            shippingCost = 250.0,
                            targetMaxBid = result.suggestedMaxBid,
                            notes = "AI audit report. Strategic advice: ${result.shippingAdvice}",
                            manifestSummary = result.summary
                        )
                        viewModel.addToWatchlist(mockLot)
                        Toast.makeText(context, "Analyzed Lot Saved to Watchlist!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_audit_report"),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Appraisal Lot to Watchlist", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TableTitleRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Description", modifier = Modifier.weight(3f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Text("Qty", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
        Text("Unit MSRP", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
        Text("Grade", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
    }
}

@Composable
fun ItemTableRow(item: com.example.data.AnalyzedItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(item.name, modifier = Modifier.weight(3f), fontSize = 11.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
        Text("${item.qty}", modifier = Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
        Text(formatPrice(item.estimatedMSRP), modifier = Modifier.weight(1.5f), fontSize = 11.sp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.primary)
        Surface(
            modifier = Modifier.weight(1f).padding(start = 6.dp),
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Text(item.conditionScore, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        }
    }
    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}
