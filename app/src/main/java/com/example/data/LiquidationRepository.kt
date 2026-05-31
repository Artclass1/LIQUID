package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

// Types to represent AI results cleanly in the ViewModel
data class ManifestAnalysisResult(
    val totalMSRP: Double,
    val itemsCount: Int,
    val suggestedMaxBid: Double,
    val marginPercent: Int,
    val riskLevel: String, // Low, Medium, High
    val riskScore: Int, // 0 to 100
    val summary: String,
    val items: List<AnalyzedItem>,
    val ebayResellPotential: String,
    val shippingAdvice: String
)

data class AnalyzedItem(
    val name: String,
    val qty: Int,
    val estimatedMSRP: Double,
    val conditionScore: String // A to F
)

data class ScoutedLot(
    val title: String,
    val marketplace: String,
    val region: String,
    val category: String,
    val condition: String,
    val retailValue: Double,
    val costPrice: Double,
    val itemCount: Int,
    val shippingCost: Double,
    val targetMaxBid: Double,
    val confidenceScore: Int,
    val description: String
)

class LiquidationRepository(private val dao: LiquidationDao) {

    val allSavedLots: Flow<List<LiquidationLot>> = dao.getAllLotsFlow()
    val allSources: Flow<List<MarketplaceSource>> = dao.getAllSourcesFlow()

    suspend fun saveLot(lot: LiquidationLot): Long = withContext(Dispatchers.IO) {
        dao.insertLot(lot)
    }

    suspend fun deleteLot(lot: LiquidationLot) = withContext(Dispatchers.IO) {
        dao.deleteLot(lot)
    }

    suspend fun saveSource(source: MarketplaceSource): Long = withContext(Dispatchers.IO) {
        dao.insertSource(source)
    }

    suspend fun deleteSource(source: MarketplaceSource) = withContext(Dispatchers.IO) {
        dao.deleteSource(source)
    }

    /**
     * Uses Gemini to analyze a pasted manifest or list of inventory items.
     * Calculates MSRP, margins, target bids, and risks.
     */
    suspend fun analyzeManifest(manifestText: String): ManifestAnalysisResult = withContext(Dispatchers.IO) {
        if (!RetrofitClient.isApiKeyConfigured()) {
            return@withContext getFallbackManifestAnalysis(manifestText)
        }

        val prompt = """
            You are a expert professional Liquidation stock lot assessor and retail arbitrage consultant.
            Deconstruct and analyze the following liquidation inventory manifest list or cargo description. 
            Estimate standard pricing, quantities, risks, auction resale valuation, and return a clean JSON object.

            In the JSON response, estimate the items, total retail value, suggested maximum purchase target price (bid target), margins, and risk dynamics.

            Input Manifest text:
            "$manifestText"

            Provide ONLY a valid JSON object matching this structure (do not wrap in markdown ```json or other formatting):
            {
              "totalMSRP": 12500.0,
              "itemsCount": 42,
              "suggestedMaxBid": 3125.0,
              "marginPercent": 45,
              "riskLevel": "Medium",
              "riskScore": 55,
              "summary": "Detailed strategic overview of this specific inventory selection...",
              "ebayResellPotential": "Excellent online traction due to high demand...",
              "shippingAdvice": "LTL Freight recommended. Expected crate weight is roughly...",
              "items": [
                {
                  "name": "Item Name Match",
                  "qty": 10,
                  "estimatedMSRP": 120.0,
                  "conditionScore": "B"
                }
              ]
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.2f)
        )

        try {
            val response = RetrofitClient.service.generateContent(RetrofitClient.getApiKey(), request)
            val jsonString = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseManifestResult(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackManifestAnalysis(manifestText, error = e.localizedMessage)
        }
    }

    /**
     * Uses Gemini to scout simulated active global liquidation stock deals matching a search term or category.
     * Generates extremely realistic international deals with bid/logistical details.
     */
    suspend fun scoutGlobalDeals(category: String, region: String): List<ScoutedLot> = withContext(Dispatchers.IO) {
        if (!RetrofitClient.isApiKeyConfigured()) {
            return@withContext getFallbackScoutedDeals(category, region)
        }

        val prompt = """
            You are an international wholesale trader. Scout and generate 4 high-potential current global liquidation stocks or lot clearout auctions matching:
            Category: "$category"
            Region Focus: "$region"

            Each liquidation lot deal must represent a real, live-style scenario from top sources (e.g., B-Stock, Merkandi, Direct Liquidation, BULQ). Include cargo details, wholesale items count, real MSRP retail market price, current wholesale acquisition target price, shipping weight logistics costs, target bidding maximums, and dynamic risk values.

            Provide ONLY a valid JSON array of objects (do not wrap in markdown or code blocks):
            [
              {
                "title": "Clean Lot Name",
                "marketplace": "Platform Sourced From (e.g. Merkandi Wholesale)",
                "region": "Continent Target",
                "category": "Department Group",
                "condition": "Shelf Pulls / Customer Returns / Overstock / Salvage",
                "retailValue": 18200.0,
                "costPrice": 4200.0,
                "itemCount": 120,
                "shippingCost": 480.0,
                "targetMaxBid": 5100.0,
                "confidenceScore": 88,
                "description": "Short, realistic commercial summary of the deal..."
              }
            ]
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.7f)
        )

        try {
            val response = RetrofitClient.service.generateContent(RetrofitClient.getApiKey(), request)
            val jsonString = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseScoutedLots(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackScoutedDeals(category, region)
        }
    }

    // --- JSON Parsers using Android SDK's robust org.json ---

    private fun parseManifestResult(jsonString: String): ManifestAnalysisResult {
        val raw = jsonString.trim().removeSurrounding("```json", "```").trim()
        val obj = JSONObject(raw)
        val itemsArray = obj.getJSONArray("items")
        val itemList = mutableListOf<AnalyzedItem>()
        for (i in 0 until itemsArray.length()) {
            val it = itemsArray.getJSONObject(i)
            itemList.add(
                AnalyzedItem(
                    name = it.getString("name"),
                    qty = it.optInt("qty", 1),
                    estimatedMSRP = it.optDouble("estimatedMSRP", 0.0),
                    conditionScore = it.optString("conditionScore", "B")
                )
            )
        }
        return ManifestAnalysisResult(
            totalMSRP = obj.optDouble("totalMSRP", 0.0),
            itemsCount = obj.optInt("itemsCount", itemList.sumOf { it.qty }),
            suggestedMaxBid = obj.optDouble("suggestedMaxBid", 0.0),
            marginPercent = obj.optInt("marginPercent", 35),
            riskLevel = obj.optString("riskLevel", "Medium"),
            riskScore = obj.optInt("riskScore", 50),
            summary = obj.optString("summary", "Complete manifest analyzed successfully."),
            items = itemList,
            ebayResellPotential = obj.optString("ebayResellPotential", "High demand online."),
            shippingAdvice = obj.optString("shippingAdvice", "Standard carrier shipment recommended.")
        )
    }

    private fun parseScoutedLots(jsonString: String): List<ScoutedLot> {
        val raw = jsonString.trim().removeSurrounding("```json", "```").trim()
        val list = mutableListOf<ScoutedLot>()
        val arr = if (raw.startsWith("[")) JSONArray(raw) else JSONObject(raw).getJSONArray("deals")
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                ScoutedLot(
                    title = obj.getString("title"),
                    marketplace = obj.optString("marketplace", "Global Liquidation"),
                    region = obj.optString("region", "Global"),
                    category = obj.optString("category", "General"),
                    condition = obj.optString("condition", "Customer Returns"),
                    retailValue = obj.optDouble("retailValue", 1000.0),
                    costPrice = obj.optDouble("costPrice", 300.0),
                    itemCount = obj.optInt("itemCount", 10),
                    shippingCost = obj.optDouble("shippingCost", 50.0),
                    targetMaxBid = obj.optDouble("targetMaxBid", 400.0),
                    confidenceScore = obj.optInt("confidenceScore", 80),
                    description = obj.optString("description", "A wholesale bulk shipment bundle.")
                )
            )
        }
        return list
    }

    // --- Intelligent Fallback Seed Data (Used when Gemini API Key is not set or fails) ---

    private fun getFallbackManifestAnalysis(manifestText: String, error: String? = null): ManifestAnalysisResult {
        // Attempt smart parsing of text locally
        val cleaned = manifestText.lowercase()
        var estimatedCount = 15
        var msrpPrice = 1200.0

        // Parse custom quantities loosely if user enters standard formats e.g. "10x laptop" or "30 phones"
        val regexMatch = Regex("""(\d+)\s*(?:x|\s)\s*([a-zA-Z\s]+)""").findAll(cleaned)
        val extractedItems = mutableListOf<AnalyzedItem>()

        if (regexMatch.any()) {
            estimatedCount = 0
            msrpPrice = 0.0
            regexMatch.forEach { match ->
                val qty = match.groupValues[1].toInt()
                val name = match.groupValues[2].trim().capitalize()
                val costEst = when {
                    name.lowercase().contains("phone") || name.lowercase().contains("iphone") -> 800.0
                    name.lowercase().contains("laptop") || name.lowercase().contains("macbook") -> 1200.0
                    name.lowercase().contains("charger") || name.lowercase().contains("cable") -> 25.0
                    name.lowercase().contains("shoe") || name.lowercase().contains("apparel") -> 90.0
                    else -> 150.0
                }
                estimatedCount += qty
                msrpPrice += (costEst * qty)
                extractedItems.add(AnalyzedItem(name, qty, costEst, if (qty > 8) "B+" else "A-"))
            }
        }

        if (extractedItems.isEmpty()) {
            extractedItems.add(AnalyzedItem("Bulk Assorted Electronics", 12, 450.0, "B"))
            extractedItems.add(AnalyzedItem("Open-Box Accessories", 25, 30.0, "C"))
            msrpPrice = 6150.0
            estimatedCount = 37
        }

        val suggestedBid = msrpPrice * 0.22 // standard 22% liquidation purchase boundary
        return ManifestAnalysisResult(
            totalMSRP = msrpPrice,
            itemsCount = estimatedCount,
            suggestedMaxBid = suggestedBid,
            marginPercent = 48,
            riskLevel = if (error != null) "Unverified" else "Low",
            riskScore = if (error != null) 70 else 35,
            summary = "Analysis calculated locally in Offline Mode. ${error?.let { "Reason: Gemini Key not configured or offline ($it)." } ?: "To unlock deep strategic AI forecasts, verify your API Key in Settings."}",
            items = extractedItems,
            ebayResellPotential = "Stable local resale profile. Recommended individually for high turnover.",
            shippingAdvice = "Local courier or standard LTL Freight shipping is average for this cargo volume."
        )
    }

    private fun getFallbackScoutedDeals(category: String, region: String): List<ScoutedLot> {
        val isEurope = region.contains("Europe") || region.contains("UK")
        val isAsia = region.contains("Asia")
        val currencySymbol = if (isEurope) "€" else "$"

        return listOf(
            ScoutedLot(
                title = "High-End Overstock Electronics Pallet",
                marketplace = if (isEurope) "Merkandi Germany" else "B-Stock Amazon Liquidation US",
                region = region,
                category = if (category.isEmpty()) "Consumer Electronics" else category,
                condition = "Overstock (Shelf Pulls)",
                retailValue = 18600.0,
                costPrice = 4100.0,
                itemCount = 85,
                shippingCost = 350.0,
                targetMaxBid = 4600.0,
                confidenceScore = 92,
                description = "Unsold store inventory returns. Brand new in box gadgets, earbuds, powerbanks and camera accessories. 100% manifest verification."
            ),
            ScoutedLot(
                title = "Bulk Mixed Brand Apparel & Athleisure Lot",
                marketplace = if (isEurope) "UK Clearance Hub" else "DirectLiquidation Target Lots",
                region = region,
                category = if (category.isEmpty()) "Apparel & Fashion" else category,
                condition = "Shelf Pulls (New with tags)",
                retailValue = 9400.0,
                costPrice = 1800.0,
                itemCount = 210,
                shippingCost = 180.0,
                targetMaxBid = 2200.0,
                confidenceScore = 85,
                description = "Assorted shirts, sportswear, designer jackets, and accessories. Clean high-margin stocklot perfect for eBay, Vinted, or local retail."
            ),
            ScoutedLot(
                title = "Customer Returned Smart Home & Vacuum Lot",
                marketplace = "Liquidation.com Logistics",
                region = region,
                category = if (category.isEmpty()) "Home Goods" else category,
                condition = "Customer Returns",
                retailValue = 14200.0,
                costPrice = 2200.0,
                itemCount = 54,
                shippingCost = 290.0,
                targetMaxBid = 2800.0,
                confidenceScore = 78,
                description = "Smart heating hubs, vacuums, robotic mappers, and humidifiers. Minor packaging damage, high repairability rate."
            ),
            ScoutedLot(
                title = "Salvage Repair Grade Smartphones & Tablets Container",
                marketplace = "B-Stock Wholesale US",
                region = region,
                category = if (category.isEmpty()) "Electronics" else category,
                condition = "Salvage (Repair/Parts needed)",
                retailValue = 24000.0,
                costPrice = 3800.0,
                itemCount = 110,
                shippingCost = 140.0,
                targetMaxBid = 4200.0,
                confidenceScore = 65,
                description = "Ideal for electronics repair technicians. Cracked screens or battery cycles needing replacement. Significant parts harvesting feasibility."
            )
        )
    }
}
