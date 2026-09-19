package com.example.chess.model

enum class PieceType(val symbol: String, val value: Int) {
    PAWN("P", 100),
    KNIGHT("N", 320),
    BISHOP("B", 330),
    ROOK("R", 500),
    QUEEN("Q", 900),
    KING("K", 20000)
}

enum class PieceColor {
    WHITE,
    BLACK;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
    fun displayName(): String = if (this == WHITE) "White" else "Black"
}

data class Piece(
    val type: PieceType,
    val color: PieceColor
)

data class Square(
    val row: Int, // 0 is rank 8 (black back rank), 7 is rank 1 (white back rank)
    val col: Int  // 0 is file a, 7 is file h
) {
    init {
        require(row in 0..7 && col in 0..7) { "Square coordinates must be 0..7" }
    }

    val file: Char get() = ('a' + col)
    val rank: Int get() = (8 - row)
    val algebraic: String get() = "$file$rank"

    companion object {
        fun fromAlgebraic(str: String): Square? {
            if (str.length != 2) return null
            val col = str[0] - 'a'
            val rank = str[1].digitToIntOrNull() ?: return null
            val row = 8 - rank
            if (col !in 0..7 || row !in 0..7) return null
            return Square(row, col)
        }
    }
}

data class Move(
    val from: Square,
    val to: Square,
    val piece: Piece,
    val capturedPiece: Piece? = null,
    val promotion: PieceType? = null,
    val isCastling: Boolean = false,
    val isEnPassant: Boolean = false,
    val san: String = ""
)

data class MoveRecord(
    val moveNumber: Int,
    val whiteMove: Move? = null,
    val blackMove: Move? = null
)

enum class GameState {
    IN_PROGRESS,
    CHECKMATE,
    STALEMATE,
    DRAW_INSUFFICIENT_MATERIAL,
    DRAW_50_MOVES,
    DRAW_REPETITION
}

enum class GameMode(val title: String, val description: String) {
    VS_AI("Play vs AI", "Challenge smart chess engine with adjustable difficulty"),
    PASS_TO_PLAY("Pass to Play", "Play with a friend on one device in real-time")
}

enum class AiDifficulty(val title: String, val depth: Int, val description: String) {
    EASY("Casual", 1, "Relaxed play with occasional mistakes"),
    MEDIUM("Challenger", 2, "Tactical play with piece coordination"),
    HARD("Master", 3, "Sharp calculation with alpha-beta search")
}
