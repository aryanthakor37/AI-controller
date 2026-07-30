package com.aimobile.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Primary Palette ───────────────────────────────────────────────────────────
val Primary         = Color(0xFF4F46E5) // Indigo
val Secondary       = Color(0xFF7C3AED) // Violet
val Accent          = Color(0xFF06B6D4) // Cyan
val Success         = Color(0xFF10B981) // Emerald
val Danger          = Color(0xFFEF4444) // Red

// ─── Backgrounds ───────────────────────────────────────────────────────────────
val Background      = Color(0xFF09090B) // Near-black
val CardBg          = Color(0x9911121C) // Translucent 3D Dark Glass (60% opacity)
val BorderColor     = Color(0x38555B8B) // Glowing 3D Glass Border

// ─── Text ──────────────────────────────────────────────────────────────────────
val TextPrimary     = Color(0xFFFFFFFF)
val TextSub         = Color(0xFFA1A1AA) // Zinc-400

// ─── Surfaces / Glass ─────────────────────────────────────────────────────────
val GlassWhite       = Color(0x1F1A1C2C) // Translucent glass fill
val GlassWhiteBorder = Color(0x405B6295) // Glowing 3D border highlight

// ─── Legacy aliases (backward compat — files that still import these will compile) ─
val DarkBackground  = Background
val SurfaceDark     = CardBg
val PrimaryBlue     = Primary
val AccentPurple    = Secondary
val AccentCyan      = Accent
val TextSecondary   = TextSub
