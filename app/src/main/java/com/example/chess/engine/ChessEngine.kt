package com.example.chess.engine

import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import kotlin.math.abs

class ChessEngine(
    initialBoard: Array<Array<Piece?>>? = null,
    initialTurn: PieceColor = PieceColor.WHITE,
    initialEnPassant: Square? = null,
    initialCastling: CastlingRights = CastlingRights()
) {
    data class CastlingRights(
        var whiteKingside: Boolean = true,
        var whiteQueenside: Boolean = true,
        var blackKingside: Boolean = true,
        var blackQueenside: Boolean = true
    ) {
        fun copy(): CastlingRights = CastlingRights(
            whiteKingside, whiteQueenside, blackKingside, blackQueenside
        )
    }

    data class HistoryEntry(
        val move: Move,
        val capturedPiece: Piece?,
        val enPassantTarget: Square?,
        val castlingRights: CastlingRights,
        val halfMoveClock: Int
    )

    val board: Array<Array<Piece?>> = initialBoard ?: createInitialBoard()
    var currentTurn: PieceColor = initialTurn
        private set
    var enPassantTarget: Square? = initialEnPassant
        private set
    var castlingRights: CastlingRights = initialCastling.copy()
        private set
    var halfMoveClock: Int = 0
        private set
    var fullMoveNumber: Int = 1
        private set

    val moveHistory = mutableListOf<HistoryEntry>()

    companion object {
        fun createInitialBoard(): Array<Array<Piece?>> {
            val b = Array(8) { arrayOfNulls<Piece>(8) }
            // Black major pieces (row 0, rank 8)
            b[0][0] = Piece(PieceType.ROOK, PieceColor.BLACK)
            b[0][1] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
            b[0][2] = Piece(PieceType.BISHOP, PieceColor.BLACK)
            b[0][3] = Piece(PieceType.QUEEN, PieceColor.BLACK)
            b[0][4] = Piece(PieceType.KING, PieceColor.BLACK)
            b[0][5] = Piece(PieceType.BISHOP, PieceColor.BLACK)
            b[0][6] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
            b[0][7] = Piece(PieceType.ROOK, PieceColor.BLACK)

            // Black pawns (row 1, rank 7)
            for (c in 0..7) {
                b[1][c] = Piece(PieceType.PAWN, PieceColor.BLACK)
            }

            // White pawns (row 6, rank 2)
            for (c in 0..7) {
                b[6][c] = Piece(PieceType.PAWN, PieceColor.WHITE)
            }

            // White major pieces (row 7, rank 1)
            b[7][0] = Piece(PieceType.ROOK, PieceColor.WHITE)
            b[7][1] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
            b[7][2] = Piece(PieceType.BISHOP, PieceColor.WHITE)
            b[7][3] = Piece(PieceType.QUEEN, PieceColor.WHITE)
            b[7][4] = Piece(PieceType.KING, PieceColor.WHITE)
            b[7][5] = Piece(PieceType.BISHOP, PieceColor.WHITE)
            b[7][6] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
            b[7][7] = Piece(PieceType.ROOK, PieceColor.WHITE)

            return b
        }
    }

    fun clone(): ChessEngine {
        val newBoard = Array(8) { r ->
            Array(8) { c -> board[r][c] }
        }
        val copy = ChessEngine(newBoard, currentTurn, enPassantTarget, castlingRights.copy())
        copy.halfMoveClock = this.halfMoveClock
        copy.fullMoveNumber = this.fullMoveNumber
        return copy
    }

    fun getPiece(square: Square): Piece? = board[square.row][square.col]
    fun getPiece(row: Int, col: Int): Piece? = board[row][col]

    fun findKing(color: PieceColor): Square? {
        for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c]
                if (p?.type == PieceType.KING && p.color == color) {
                    return Square(r, c)
                }
            }
        }
        return null
    }

    fun isSquareAttacked(square: Square, byColor: PieceColor): Boolean {
        // Check Knight attacks
        val knightOffsets = arrayOf(
            Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
        )
        for ((dr, dc) in knightOffsets) {
            val r = square.row + dr
            val c = square.col + dc
            if (r in 0..7 && c in 0..7) {
                val p = board[r][c]
                if (p?.color == byColor && p.type == PieceType.KNIGHT) return true
            }
        }

        // Check Pawn attacks
        val pawnDir = if (byColor == PieceColor.WHITE) 1 else -1 // white pawns attack upwards (from higher row to lower row)
        val pawnRow = square.row + pawnDir
        for (dc in arrayOf(-1, 1)) {
            val c = square.col + dc
            if (pawnRow in 0..7 && c in 0..7) {
                val p = board[pawnRow][c]
                if (p?.color == byColor && p.type == PieceType.PAWN) return true
            }
        }

        // Check King attacks (adjacent 1 square)
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = square.row + dr
                val c = square.col + dc
                if (r in 0..7 && c in 0..7) {
                    val p = board[r][c]
                    if (p?.color == byColor && p.type == PieceType.KING) return true
                }
            }
        }

        // Straight ray attacks (Rook & Queen)
        val straightDirs = arrayOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
        for ((dr, dc) in straightDirs) {
            var r = square.row + dr
            var c = square.col + dc
            while (r in 0..7 && c in 0..7) {
                val p = board[r][c]
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                r += dr
                c += dc
            }
        }

        // Diagonal ray attacks (Bishop & Queen)
        val diagonalDirs = arrayOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))
        for ((dr, dc) in diagonalDirs) {
            var r = square.row + dr
            var c = square.col + dc
            while (r in 0..7 && c in 0..7) {
                val p = board[r][c]
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                r += dr
                c += dc
            }
        }

        return false
    }

    fun isInCheck(color: PieceColor): Boolean {
        val kingSquare = findKing(color) ?: return false
        return isSquareAttacked(kingSquare, color.opposite())
    }

    fun getLegalMoves(color: PieceColor = currentTurn): List<Move> {
        val moves = mutableListOf<Move>()
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board[r][c]
                if (piece != null && piece.color == color) {
                    moves.addAll(getLegalMovesFrom(Square(r, c)))
                }
            }
        }
        return moves
    }

    fun getLegalMovesFrom(from: Square): List<Move> {
        val piece = getPiece(from) ?: return emptyList()
        if (piece.color != currentTurn) return emptyList()

        val pseudoMoves = generatePseudoLegalMoves(from, piece)
        val legalMoves = mutableListOf<Move>()

        for (move in pseudoMoves) {
            if (isMoveLegal(move)) {
                // If it's a promotion move with null promotion type, generate queen promotion (and others if needed)
                if (isPromotionMove(move)) {
                    legalMoves.add(move.copy(promotion = PieceType.QUEEN))
                } else {
                    legalMoves.add(move)
                }
            }
        }

        return legalMoves
    }

    fun getPromotionChoices(from: Square, to: Square): List<Move> {
        val piece = getPiece(from) ?: return emptyList()
        val captured = getPiece(to)
        return listOf(
            Move(from, to, piece, captured, promotion = PieceType.QUEEN),
            Move(from, to, piece, captured, promotion = PieceType.ROOK),
            Move(from, to, piece, captured, promotion = PieceType.BISHOP),
            Move(from, to, piece, captured, promotion = PieceType.KNIGHT)
        )
    }

    fun isPromotionMove(move: Move): Boolean {
        if (move.piece.type != PieceType.PAWN) return false
        return (move.piece.color == PieceColor.WHITE && move.to.row == 0) ||
               (move.piece.color == PieceColor.BLACK && move.to.row == 7)
    }

    private fun isMoveLegal(move: Move): Boolean {
        // Clone and execute move
        val clone = this.clone()
        clone.executeRawMove(move)
        return !clone.isInCheck(move.piece.color)
    }

    private fun generatePseudoLegalMoves(from: Square, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        when (piece.type) {
            PieceType.PAWN -> generatePawnMoves(from, piece, moves)
            PieceType.KNIGHT -> generateKnightMoves(from, piece, moves)
            PieceType.BISHOP -> generateSlidingMoves(from, piece, moves, arrayOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)))
            PieceType.ROOK -> generateSlidingMoves(from, piece, moves, arrayOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)))
            PieceType.QUEEN -> {
                generateSlidingMoves(from, piece, moves, arrayOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)))
                generateSlidingMoves(from, piece, moves, arrayOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)))
            }
            PieceType.KING -> generateKingMoves(from, piece, moves)
        }
        return moves
    }

    private fun generatePawnMoves(from: Square, piece: Piece, moves: MutableList<Move>) {
        val dir = if (piece.color == PieceColor.WHITE) -1 else 1
        val startRow = if (piece.color == PieceColor.WHITE) 6 else 1

        // 1 step forward
        val forwardRow = from.row + dir
        if (forwardRow in 0..7 && board[forwardRow][from.col] == null) {
            val to = Square(forwardRow, from.col)
            moves.add(Move(from, to, piece))

            // 2 steps forward from initial rank
            val doubleRow = from.row + 2 * dir
            if (from.row == startRow && board[doubleRow][from.col] == null) {
                moves.add(Move(from, Square(doubleRow, from.col), piece))
            }
        }

        // Diagonal captures
        for (dc in arrayOf(-1, 1)) {
            val capCol = from.col + dc
            if (forwardRow in 0..7 && capCol in 0..7) {
                val targetPiece = board[forwardRow][capCol]
                if (targetPiece != null && targetPiece.color != piece.color) {
                    moves.add(Move(from, Square(forwardRow, capCol), piece, capturedPiece = targetPiece))
                } else if (targetPiece == null && enPassantTarget == Square(forwardRow, capCol)) {
                    // En Passant capture
                    val capturedPawn = board[from.row][capCol]
                    moves.add(
                        Move(
                            from,
                            Square(forwardRow, capCol),
                            piece,
                            capturedPiece = capturedPawn,
                            isEnPassant = true
                        )
                    )
                }
            }
        }
    }

    private fun generateKnightMoves(from: Square, piece: Piece, moves: MutableList<Move>) {
        val offsets = arrayOf(
            Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
        )
        for ((dr, dc) in offsets) {
            val r = from.row + dr
            val c = from.col + dc
            if (r in 0..7 && c in 0..7) {
                val targetPiece = board[r][c]
                if (targetPiece == null || targetPiece.color != piece.color) {
                    moves.add(Move(from, Square(r, c), piece, capturedPiece = targetPiece))
                }
            }
        }
    }

    private fun generateSlidingMoves(
        from: Square,
        piece: Piece,
        moves: MutableList<Move>,
        directions: Array<Pair<Int, Int>>
    ) {
        for ((dr, dc) in directions) {
            var r = from.row + dr
            var c = from.col + dc
            while (r in 0..7 && c in 0..7) {
                val targetPiece = board[r][c]
                if (targetPiece == null) {
                    moves.add(Move(from, Square(r, c), piece))
                } else {
                    if (targetPiece.color != piece.color) {
                        moves.add(Move(from, Square(r, c), piece, capturedPiece = targetPiece))
                    }
                    break
                }
                r += dr
                c += dc
            }
        }
    }

    private fun generateKingMoves(from: Square, piece: Piece, moves: MutableList<Move>) {
        // Standard moves
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = from.row + dr
                val c = from.col + dc
                if (r in 0..7 && c in 0..7) {
                    val targetPiece = board[r][c]
                    if (targetPiece == null || targetPiece.color != piece.color) {
                        moves.add(Move(from, Square(r, c), piece, capturedPiece = targetPiece))
                    }
                }
            }
        }

        // Castling
        val enemyColor = piece.color.opposite()
        if (!isSquareAttacked(from, enemyColor)) {
            val row = from.row
            if (piece.color == PieceColor.WHITE && row == 7 && from.col == 4) {
                // White Kingside: e1(7,4) -> g1(7,6)
                if (castlingRights.whiteKingside &&
                    board[7][5] == null && board[7][6] == null &&
                    board[7][7]?.type == PieceType.ROOK && board[7][7]?.color == PieceColor.WHITE &&
                    !isSquareAttacked(Square(7, 5), enemyColor) &&
                    !isSquareAttacked(Square(7, 6), enemyColor)
                ) {
                    moves.add(Move(from, Square(7, 6), piece, isCastling = true))
                }
                // White Queenside: e1(7,4) -> c1(7,2)
                if (castlingRights.whiteQueenside &&
                    board[7][1] == null && board[7][2] == null && board[7][3] == null &&
                    board[7][0]?.type == PieceType.ROOK && board[7][0]?.color == PieceColor.WHITE &&
                    !isSquareAttacked(Square(7, 3), enemyColor) &&
                    !isSquareAttacked(Square(7, 2), enemyColor)
                ) {
                    moves.add(Move(from, Square(7, 2), piece, isCastling = true))
                }
            } else if (piece.color == PieceColor.BLACK && row == 0 && from.col == 4) {
                // Black Kingside: e8(0,4) -> g8(0,6)
                if (castlingRights.blackKingside &&
                    board[0][5] == null && board[0][6] == null &&
                    board[0][7]?.type == PieceType.ROOK && board[0][7]?.color == PieceColor.BLACK &&
                    !isSquareAttacked(Square(0, 5), enemyColor) &&
                    !isSquareAttacked(Square(0, 6), enemyColor)
                ) {
                    moves.add(Move(from, Square(0, 6), piece, isCastling = true))
                }
                // Black Queenside: e8(0,4) -> c8(0,2)
                if (castlingRights.blackQueenside &&
                    board[0][1] == null && board[0][2] == null && board[0][3] == null &&
                    board[0][0]?.type == PieceType.ROOK && board[0][0]?.color == PieceColor.BLACK &&
                    !isSquareAttacked(Square(0, 3), enemyColor) &&
                    !isSquareAttacked(Square(0, 2), enemyColor)
                ) {
                    moves.add(Move(from, Square(0, 2), piece, isCastling = true))
                }
            }
        }
    }

    private fun executeRawMove(move: Move) {
        val movingPiece = move.promotion?.let { Piece(it, move.piece.color) } ?: move.piece
        board[move.to.row][move.to.col] = movingPiece
        board[move.from.row][move.from.col] = null

        // Handle Castling Rook move
        if (move.isCastling) {
            val row = move.from.row
            if (move.to.col == 6) { // Kingside
                board[row][5] = board[row][7]
                board[row][7] = null
            } else if (move.to.col == 2) { // Queenside
                board[row][3] = board[row][0]
                board[row][0] = null
            }
        }

        // Handle En Passant captured pawn removal
        if (move.isEnPassant) {
            board[move.from.row][move.to.col] = null
        }
    }

    fun makeMove(move: Move): Move {
        val oldEnPassant = enPassantTarget
        val oldCastling = castlingRights.copy()
        val oldHalfMove = halfMoveClock

        val captured = if (move.isEnPassant) {
            board[move.from.row][move.to.col]
        } else {
            board[move.to.row][move.to.col]
        }

        // Calculate SAN before state updates
        val san = computeSan(move)
        val finalMove = move.copy(capturedPiece = captured, san = san)

        // Execute board updates
        executeRawMove(finalMove)

        // Update Castling rights
        when (move.piece.type) {
            PieceType.KING -> {
                if (move.piece.color == PieceColor.WHITE) {
                    castlingRights.whiteKingside = false
                    castlingRights.whiteQueenside = false
                } else {
                    castlingRights.blackKingside = false
                    castlingRights.blackQueenside = false
                }
            }
            PieceType.ROOK -> {
                if (move.from == Square(7, 0)) castlingRights.whiteQueenside = false
                if (move.from == Square(7, 7)) castlingRights.whiteKingside = false
                if (move.from == Square(0, 0)) castlingRights.blackQueenside = false
                if (move.from == Square(0, 7)) castlingRights.blackKingside = false
            }
            else -> {}
        }

        // Also if a rook is captured on its home square
        if (move.to == Square(7, 0)) castlingRights.whiteQueenside = false
        if (move.to == Square(7, 7)) castlingRights.whiteKingside = false
        if (move.to == Square(0, 0)) castlingRights.blackQueenside = false
        if (move.to == Square(0, 7)) castlingRights.blackKingside = false

        // Update En Passant target
        if (move.piece.type == PieceType.PAWN && abs(move.to.row - move.from.row) == 2) {
            val middleRow = (move.to.row + move.from.row) / 2
            enPassantTarget = Square(middleRow, move.from.col)
        } else {
            enPassantTarget = null
        }

        // Half-move clock (50-move rule)
        if (move.piece.type == PieceType.PAWN || captured != null) {
            halfMoveClock = 0
        } else {
            halfMoveClock++
        }

        if (currentTurn == PieceColor.BLACK) {
            fullMoveNumber++
        }

        currentTurn = currentTurn.opposite()

        moveHistory.add(
            HistoryEntry(
                move = finalMove,
                capturedPiece = captured,
                enPassantTarget = oldEnPassant,
                castlingRights = oldCastling,
                halfMoveClock = oldHalfMove
            )
        )

        return finalMove
    }

    fun undoMove(): Move? {
        if (moveHistory.isEmpty()) return null
        val lastEntry = moveHistory.removeAt(moveHistory.lastIndex)
        val move = lastEntry.move

        // Restore turn
        currentTurn = move.piece.color
        if (currentTurn == PieceColor.BLACK) {
            fullMoveNumber--
        }

        // Restore piece at origin
        board[move.from.row][move.from.col] = move.piece
        board[move.to.row][move.to.col] = null

        // Restore captured piece
        if (move.isEnPassant) {
            board[move.from.row][move.to.col] = lastEntry.capturedPiece
        } else if (lastEntry.capturedPiece != null) {
            board[move.to.row][move.to.col] = lastEntry.capturedPiece
        }

        // Undo castling rook
        if (move.isCastling) {
            val row = move.from.row
            if (move.to.col == 6) { // Kingside
                board[row][7] = board[row][5]
                board[row][5] = null
            } else if (move.to.col == 2) { // Queenside
                board[row][0] = board[row][3]
                board[row][3] = null
            }
        }

        enPassantTarget = lastEntry.enPassantTarget
        castlingRights = lastEntry.castlingRights
        halfMoveClock = lastEntry.halfMoveClock

        return move
    }

    private fun computeSan(move: Move): String {
        if (move.isCastling) {
            return if (move.to.col == 6) "O-O" else "O-O-O"
        }

        val sb = StringBuilder()
        val isCapture = move.isEnPassant || board[move.to.row][move.to.col] != null

        if (move.piece.type == PieceType.PAWN) {
            if (isCapture) {
                sb.append(move.from.file)
                sb.append('x')
            }
            sb.append(move.to.algebraic)
            if (move.promotion != null) {
                sb.append("=").append(move.promotion.symbol)
            }
        } else {
            sb.append(move.piece.type.symbol)

            // Disambiguation
            val similarCandidates = getLegalMoves(move.piece.color).filter {
                it.piece.type == move.piece.type && it.to == move.to && it.from != move.from
            }
            if (similarCandidates.isNotEmpty()) {
                val sameFile = similarCandidates.any { it.from.col == move.from.col }
                val sameRank = similarCandidates.any { it.from.row == move.from.row }
                if (!sameFile) {
                    sb.append(move.from.file)
                } else if (!sameRank) {
                    sb.append(move.from.rank)
                } else {
                    sb.append(move.from.algebraic)
                }
            }

            if (isCapture) sb.append('x')
            sb.append(move.to.algebraic)
        }

        // Check if this move delivers check or checkmate
        val clone = this.clone()
        clone.executeRawMove(move)
        val enemyColor = move.piece.color.opposite()
        val opponentInCheck = clone.isInCheck(enemyColor)
        if (opponentInCheck) {
            val opponentLegalMoves = clone.getLegalMoves(enemyColor)
            if (opponentLegalMoves.isEmpty()) {
                sb.append("#")
            } else {
                sb.append("+")
            }
        }

        return sb.toString()
    }

    fun getGameState(): GameState {
        val legalMoves = getLegalMoves(currentTurn)
        val inCheck = isInCheck(currentTurn)

        if (legalMoves.isEmpty()) {
            return if (inCheck) GameState.CHECKMATE else GameState.STALEMATE
        }

        if (halfMoveClock >= 100) {
            return GameState.DRAW_50_MOVES
        }

        if (isInsufficientMaterial()) {
            return GameState.DRAW_INSUFFICIENT_MATERIAL
        }

        return GameState.IN_PROGRESS
    }

    fun isInsufficientMaterial(): Boolean {
        val pieces = mutableListOf<Piece>()
        for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c]
                if (p != null) pieces.add(p)
            }
        }

        // King vs King
        if (pieces.size == 2) return true

        // King + Minor Piece vs King
        if (pieces.size == 3) {
            val minor = pieces.find { it.type != PieceType.KING }
            if (minor?.type == PieceType.BISHOP || minor?.type == PieceType.KNIGHT) {
                return true
            }
        }

        return false
    }

    fun getCapturedPieces(): Pair<List<Piece>, List<Piece>> {
        // Pieces captured by White (i.e. missing Black pieces)
        // Pieces captured by Black (i.e. missing White pieces)
        val initialWhitePieces = mutableListOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        ).apply { repeat(8) { add(PieceType.PAWN) } }

        val initialBlackPieces = mutableListOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        ).apply { repeat(8) { add(PieceType.PAWN) } }

        for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c] ?: continue
                if (p.type == PieceType.KING) continue
                if (p.color == PieceColor.WHITE) {
                    initialWhitePieces.remove(p.type)
                } else {
                    initialBlackPieces.remove(p.type)
                }
            }
        }

        val capturedBlack = initialBlackPieces.map { Piece(it, PieceColor.BLACK) }
        val capturedWhite = initialWhitePieces.map { Piece(it, PieceColor.WHITE) }

        return Pair(capturedBlack, capturedWhite)
    }
}
