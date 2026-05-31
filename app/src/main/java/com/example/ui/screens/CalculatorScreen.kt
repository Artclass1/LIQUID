package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LiquidationLot
import com.example.ui.LiquidationViewModel
import com.example.ui.theme.GoldAccent

@Composable
fun CalculatorScreen(
    viewModel: LiquidationViewModel,
    prepopulatedLot: LiquidationLot? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Calculator state inputs
    var inputMSRP by remember { mutableStateOf("10000") }
    var inputBidPrice by remember { mutableStateOf("2500") }
    var inputShipping by remember { mutableStateOf("300") }
    var inputSalesTax by remember { mutableStateOf("8.5") }
    var inputChannelFees by remember { mutableStateOf("13") }
    var inputRefurbishCost by remember { mutableStateOf("0") }
    var inputResaleDiscount by remember { mutableStateOf("30") } // sell items at 30% off MSRP (i.e. 70% value)

    // Prepopulate inputs if launched from a deal or watchlist lot context
    LaunchedEffect(prepopulatedLot) {
        prepopulatedLot?.let {
            inputMSRP = it.retailValue.toInt().toString()
            inputBidPrice = it.costPrice.toInt().toString()
            inputShipping = it.shippingCost.toInt().toString()
            // Assume defaults for other values, or load if notes have indicators
            inputRefurbishCost = "0"
        }
    }

    // Mathematical calculations
    val msrp = inputMSRP.toDoubleOrNull() ?: 0.0
    val bidPrice = inputBidPrice.toDoubleOrNull() ?: 0.0
    val shipping = inputShipping.toDoubleOrNull() ?: 0.0
    val taxRate = (inputSalesTax.toDoubleOrNull() ?: 0.0) / 100.0
    val referralFeeRate = (inputChannelFees.toDoubleOrNull() ?: 0.0) / 100.0
    val refurbish = inputRefurbishCost.toDoubleOrNull() ?: 0.0
    val discountPercent = (inputResaleDiscount.toDoubleOrNull() ?: 0.0) / 100.0

    // Formulations
    val taxPaid = bidPrice * taxRate
    val totalInvestment = bidPrice + shipping + taxPaid + refurbish

    // Expected retail sales revenue = MSRP * (1 - resaleDiscountPercent)
    val expectedRevenue = msrp * (1.0 - discountPercent)
    val channelFeesPaid = expectedRevenue * referralFeeRate

    // Financial outcomes
    val netPayout = expectedRevenue - channelFeesPaid
    val netProfit = netPayout - totalInvestment
    val roi = if (totalInvestment > 0) (netProfit / totalInvestment) * 100.0 else 0.0
    val grossMargin = if (expectedRevenue > 0) (netProfit / expectedRevenue) * 100.0 else 0.0

    // Breakeven sales price
    val breakevenPrice = totalInvestment / (1.0 - referralFeeRate)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App branding
        Text(
            text = "Arbitrage Profit Margin Calculator",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Input Controls Column
            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalculatorInputField(
                    label = "Lot MSRP ($)",
                    value = inputMSRP,
                    onValueChange = { inputMSRP = it },
                    tag = "calc_msrp"
                )
                CalculatorInputField(
                    label = "Target Bid / Cost ($)",
                    value = inputBidPrice,
                    onValueChange = { inputBidPrice = it },
                    tag = "calc_bid"
                )
                CalculatorInputField(
                    label = "Freight Shipping ($)",
                    value = inputShipping,
                    onValueChange = { inputShipping = it },
                    tag = "calc_shipping"
                )
                CalculatorInputField(
                    label = "Expected Resale Discount (%)",
                    value = inputResaleDiscount,
                    onValueChange = { inputResaleDiscount = it },
                    tag = "calc_discount",
                    tooltip = "Discounts off MSRP when selling items. E.g. 30% discount = items sell at 70% of retail MSRP."
                )
                CalculatorInputField(
                    label = "Channel Fees (%)",
                    value = inputChannelFees,
                    onValueChange = { inputChannelFees = it },
                    tag = "calc_fees",
                    tooltip = "Selling platforms take. E.g., eBay takes roughly 12-14%."
                )
                CalculatorInputField(
                    label = "Refurbish/Taxes/Parts ($)",
                    value = inputRefurbishCost,
                    onValueChange = { inputRefurbishCost = it },
                    tag = "calc_refurbish"
                )
            }

            // Right Financial Ledger Dashboard Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Capital Needed Card
                LedgerMiniCard(
                    title = "Total Capital",
                    value = formatPrice(totalInvestment),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Projected Sales Revenue Card
                LedgerMiniCard(
                    title = "Projected Sales",
                    value = formatPrice(expectedRevenue),
                    color = MaterialTheme.colorScheme.primary
                )

                // Sells Platform Fees Card
                LedgerMiniCard(
                    title = "Broker/Channel Fees",
                    value = formatPrice(channelFeesPaid),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Sells Breakeven Target Card
                LedgerMiniCard(
                    title = "Breakeven Sales Target",
                    value = formatPrice(breakevenPrice),
                    color = GoldAccent,
                    tooltip = "You must generate this sales volume overall to cover procurement, freight, and referral costs."
                )
            }
        }

        // Expanded financial assessment summary card
        val summaryColor = if (netProfit >= 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        val outlineColor = if (netProfit >= 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
        val signText = if (netProfit >= 0) "Expected Profit" else "Projected Deficit"

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, outlineColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(signText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(
                            text = formatPrice(netProfit),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = if (netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Expected Net ROI", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(
                            text = String.format("%.1f%%", roi),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = if (roi >= 25 && netProfit >= 0) GoldAccent else if (netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Acquisition Bid Percentage of MSRP:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    val bidPercent = if (msrp > 0) (bidPrice / msrp) * 100.0 else 0.0
                    Text(
                        text = String.format("%.1f%%", bidPercent),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gross Merchant Margin:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(
                        text = String.format("%.1f%%", grossMargin),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (grossMargin > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                // AI Expert Advice prompt
                val advice = when {
                    netProfit < 0 -> "🔴 OUT OF BOUNDS: This procurement is in standard deficit. To make this deal work, you must negotiate the procurement winning bid down below ${formatPrice(bidPrice + netProfit)} or secure better logistics."
                    roi in 0.0..15.0 -> "🟡 CAUTION: Low margin lot (ROI under 15%). The physical overhead (storing, grading, shipping cargo) might absorb this profit. Acceptable only if items have very high turnover speed."
                    roi in 15.0..35.0 -> "🟢 STABLE DEAL: Healthy retail arbitrage parameters. Safe to proceed if items descriptions confirm at least 80% working condition."
                    else -> "🏆 HIGH MARGIN WIN: Unusually high profit potential (ROI exceeds 35%). Highly recommended. Double check shipping constraints, local sales taxes, and ensure items are not counterfeit."
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = advice,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Fast Action to save this custom calculation lot scenario directly to the Watchlist Room DB!
        Button(
            onClick = {
                val calculatedLot = LiquidationLot(
                    title = "CALCULATED LOT: Lot MSRP " + formatPrice(msrp),
                    marketplace = "Custom Sourcing Calc",
                    category = "Wholesale Arbitrage Profile",
                    condition = "Estimated ${inputResaleDiscount}% Off Retail",
                    retailValue = msrp,
                    costPrice = bidPrice,
                    itemCount = 100, // placeholder
                    shippingCost = shipping,
                    targetMaxBid = breakevenPrice,
                    notes = "Custom calculation metrics context. ROI ${String.format("%.1f%%", roi)}. Profit ${formatPrice(netProfit)}. Shipping cost ${formatPrice(shipping)}.",
                    manifestSummary = "Expected revenue ${formatPrice(expectedRevenue)}, custom input parts costs: ${formatPrice(refurbish)}. Break-even price target: ${formatPrice(breakevenPrice)}."
                )
                viewModel.addToWatchlist(calculatedLot)
                Toast.makeText(context, "Calculated Lot Saved to Watchlist!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .height(48.dp)
                .testTag("save_calculator_scenario")
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Scenario to Watchlist", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CalculatorInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    tag: String,
    tooltip: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag(tag),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(fontSize = 12.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            singleLine = true
        )

        tooltip?.let {
            Text(
                text = it,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
fun LedgerMiniCard(
    title: String,
    value: String,
    color: Color,
    tooltip: String? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            tooltip?.let {
                Text(
                    text = it,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    lineHeight = 10.sp
                )
            }
        }
    }
}
