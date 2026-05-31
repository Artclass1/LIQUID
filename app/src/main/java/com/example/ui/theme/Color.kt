package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- Elegant Dark Design Theme Palette ---
val ElegantDarkBg = Color(0xFF1A1C1E)          // Charcoal screen background
val ElegantSurfaceCard = Color(0xFF333538)     // Card background
val ElegantSurfaceRow = Color(0xFF2E3033)      // Item row background
val ElegantSurfaceChip = Color(0xFF44474E)     // Unselected chip / utility container
val ElegantPurplePrimary = Color(0xFFD0BCFF)   // Material purple highlight
val ElegantPurpleContainer = Color(0xFF4F378B) // Rich deep violet container
val ElegantPurpleDark = Color(0xFF381E72)      // Low-contrast deep grape base
val ElegantLightText = Color(0xFFE2E2E6)       // Principle text color
val ElegantMutedText = Color(0xFFC4C6D0)       // Medium-contrast body
val ElegantDeepMutedText = Color(0xFF938F99)   // Low-contrast subtext

// High Yield / Accent Highlight
val ElegantCoralAccent = Color(0xFFF2B8B5)     // Target ROI / High Yield Accent
val ElegantCoralDark = Color(0xFF601410)       // Low-contrast background for high yield

// Map back to expected imports/names to avoid compilation issues
val GoldAccent = ElegantCoralAccent // Keeps imports working, but uses the modern Elegant Dark palette!

// Map colors for compatibility with general light/dark values
val PrimaryLight = ElegantPurplePrimary
val SecondaryLight = ElegantMutedText
val TertiaryLight = ElegantPurpleContainer
val BackgroundLight = Color(0xFF242629)        // Slightly lighter elegant charcoal for light mode fallback
val SurfaceLight = ElegantSurfaceCard
val DeepCoal = Color(0xFF111315)

// Dark Values (Primary implementation)
val PrimaryDark = ElegantPurplePrimary
val SecondaryDark = ElegantMutedText
val TertiaryDark = ElegantCoralAccent
val BackgroundDark = ElegantDarkBg
val SurfaceDark = ElegantSurfaceCard
val CardBorderDark = ElegantSurfaceChip

// Keep references to older color variables to avoid compilation errors if other components import them.
val DeepJade = Color(0xFF381E72)
val EmeraldGreen = ElegantPurplePrimary
val LightSage = ElegantLightText
val EarthSlate = ElegantSurfaceCard

