package com.example.chess.model

import androidx.compose.ui.graphics.Color

data class BoardTheme(
    val id: String,
    val name: String,
    val lightSquare: Color,
    val darkSquare: Color,
    val boardBorder: Color,
    val selectedSquare: Color,
    val legalMoveDot: Color,
    val legalMoveCapture: Color,
    val lastMoveHighlight: Color,
    val inCheckGlow: Color,
    val rankFileTextColor: Color,
    val pieceWhiteFill: Color = Color(0xFFFFFFFF),
    val pieceWhiteStroke: Color = Color(0xFF1E293B),
    val pieceBlackFill: Color = Color(0xFF1E293B),
    val pieceBlackStroke: Color = Color(0xFFCBD5E1)
) {
    companion object {
        val ClassicWood = BoardTheme(
            id = "classic_wood",
            name = "Classic Wood",
            lightSquare = Color(0xFFF0D9B5),
            darkSquare = Color(0xFFB58863),
            boardBorder = Color(0xFF6B4226),
            selectedSquare = Color(0x99F6E05E),
            legalMoveDot = Color(0x664A3525),
            legalMoveCapture = Color(0x99E53E3E),
            lastMoveHighlight = Color(0x66CDD26A),
            inCheckGlow = Color(0xCCE53E3E),
            rankFileTextColor = Color(0xFF4A3525)
        )

        val TournamentGreen = BoardTheme(
            id = "tournament_green",
            name = "Tournament Green",
            lightSquare = Color(0xFFEEEED2),
            darkSquare = Color(0xFF769656),
            boardBorder = Color(0xFF385127),
            selectedSquare = Color(0x99BACA44),
            legalMoveDot = Color(0x662C401B),
            legalMoveCapture = Color(0x99D32F2F),
            lastMoveHighlight = Color(0x77F7F769),
            inCheckGlow = Color(0xCCE53E3E),
            rankFileTextColor = Color(0xFF2C401B)
        )

        val MidnightSlate = BoardTheme(
            id = "midnight_slate",
            name = "Midnight Slate",
            lightSquare = Color(0xFF94A3B8),
            darkSquare = Color(0xFF334155),
            boardBorder = Color(0xFF0F172A),
            selectedSquare = Color(0x9938BDF8),
            legalMoveDot = Color(0x8838BDF8),
            legalMoveCapture = Color(0x99F43F5E),
            lastMoveHighlight = Color(0x660284C7),
            inCheckGlow = Color(0xCCEF4444),
            rankFileTextColor = Color(0xFFE2E8F0),
            pieceWhiteFill = Color(0xFFF8FAFC),
            pieceWhiteStroke = Color(0xFF0F172A),
            pieceBlackFill = Color(0xFF0F172A),
            pieceBlackStroke = Color(0xFF94A3B8)
        )

        val CyberNeon = BoardTheme(
            id = "cyber_neon",
            name = "Cyber Neon",
            lightSquare = Color(0xFF222B45),
            darkSquare = Color(0xFF131722),
            boardBorder = Color(0xFF0A0D14),
            selectedSquare = Color(0x9900E5FF),
            legalMoveDot = Color(0xAA00E5FF),
            legalMoveCapture = Color(0xAAFF007F),
            lastMoveHighlight = Color(0x5500E5FF),
            inCheckGlow = Color(0xCCFF0055),
            rankFileTextColor = Color(0xFF00E5FF),
            pieceWhiteFill = Color(0xFF00E5FF),
            pieceWhiteStroke = Color(0xFF0A0D14),
            pieceBlackFill = Color(0xFFFF007F),
            pieceBlackStroke = Color(0xFF0A0D14)
        )

        val WarmWalnut = BoardTheme(
            id = "warm_walnut",
            name = "Warm Walnut",
            lightSquare = Color(0xFFEAD8C0),
            darkSquare = Color(0xFFA0714F),
            boardBorder = Color(0xFF5D3A20),
            selectedSquare = Color(0x99E2A85C),
            legalMoveDot = Color(0x774A2E19),
            legalMoveCapture = Color(0x99D9534F),
            lastMoveHighlight = Color(0x66E2A85C),
            inCheckGlow = Color(0xCCD9534F),
            rankFileTextColor = Color(0xFF4A2E19)
        )

        val MinimalistCharcoal = BoardTheme(
            id = "minimalist_charcoal",
            name = "Charcoal Minimal",
            lightSquare = Color(0xFFE2E8F0),
            darkSquare = Color(0xFF64748B),
            boardBorder = Color(0xFF1E293B),
            selectedSquare = Color(0x99818CF8),
            legalMoveDot = Color(0x771E293B),
            legalMoveCapture = Color(0x99EF4444),
            lastMoveHighlight = Color(0x66818CF8),
            inCheckGlow = Color(0xCCEF4444),
            rankFileTextColor = Color(0xFF1E293B)
        )

        val AllThemes = listOf(
            TournamentGreen,
            ClassicWood,
            MidnightSlate,
            CyberNeon,
            WarmWalnut,
            MinimalistCharcoal
        )
    }
}
