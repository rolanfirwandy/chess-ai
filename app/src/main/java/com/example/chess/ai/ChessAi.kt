package com.example.chess.ai

import com.example.chess.engine.ChessEngine
import com.example.chess.model.AiDifficulty
import com.example.chess.model.Move
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import kotlin.random.Random

object ChessAi {

    // Piece values
    private val pieceValues = mapOf(
        PieceType.PAWN to 100,
        PieceType.KNIGHT to 320,
        PieceType.BISHOP to 330,
        PieceType.ROOK to 500,
        PieceType.QUEEN to 900,
        PieceType.KING to 20000
    )

    // Piece-square tables for White (mirrored for Black)
    private val pawnPst = intArrayOf(
         0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
         5,  5, 10, 25, 25, 10,  5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5, -5,-10,  0,  0,-10, -5,  5,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    )

    private val knightPst = intArrayOf(
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    )

    private val bishopPst = intArrayOf(
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  5,  0,  0,  5,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    )

    private val rookPst = intArrayOf(
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10, 10, 10, 10, 10,  5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
         0,  0,  0,  5,  5,  0,  0,  0
    )

    private val queenPst = intArrayOf(
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
          0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    )

    private val kingMiddlePst = intArrayOf(
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20,
         20, 30, 10,  0,  0, 10, 30, 20
    )

    fun selectBestMove(engine: ChessEngine, difficulty: AiDifficulty): Move? {
        val legalMoves = engine.getLegalMoves(engine.currentTurn)
        if (legalMoves.isEmpty()) return null

        return when (difficulty) {
            AiDifficulty.EASY -> selectEasyMove(engine, legalMoves)
            AiDifficulty.MEDIUM -> selectMinimaxMove(engine, depth = 2)
            AiDifficulty.HARD -> selectMinimaxMove(engine, depth = 3)
        }
    }

    private fun selectEasyMove(engine: ChessEngine, legalMoves: List<Move>): Move {
        // 70% random or tactical capture
        val captures = legalMoves.filter { it.capturedPiece != null }
        if (captures.isNotEmpty() && Random.nextFloat() < 0.65f) {
            return captures.random()
        }
        if (Random.nextFloat() < 0.5f) {
            // Pick a safe move if possible
            return legalMoves.random()
        }
        // Otherwise do a depth 1 greedy search with slight noise
        return selectMinimaxMove(engine, depth = 1) ?: legalMoves.random()
    }

    private fun selectMinimaxMove(engine: ChessEngine, depth: Int): Move? {
        val color = engine.currentTurn
        val isMaximizing = (color == PieceColor.WHITE)
        var bestScore = if (isMaximizing) Int.MIN_VALUE else Int.MAX_VALUE
        var bestMoves = mutableListOf<Move>()

        val moves = orderMoves(engine.getLegalMoves(color), engine)
        for (move in moves) {
            val clone = engine.clone()
            clone.makeMove(move)

            val score = minimax(
                engine = clone,
                depth = depth - 1,
                alpha = Int.MIN_VALUE + 1000,
                beta = Int.MAX_VALUE - 1000,
                isMaximizing = !isMaximizing
            )

            if (isMaximizing) {
                if (score > bestScore) {
                    bestScore = score
                    bestMoves = mutableListOf(move)
                } else if (score == bestScore) {
                    bestMoves.add(move)
                }
            } else {
                if (score < bestScore) {
                    bestScore = score
                    bestMoves = mutableListOf(move)
                } else if (score == bestScore) {
                    bestMoves.add(move)
                }
            }
        }

        return bestMoves.randomOrNull()
    }

    private fun minimax(
        engine: ChessEngine,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean
    ): Int {
        var currentAlpha = alpha
        var currentBeta = beta

        val moves = engine.getLegalMoves(engine.currentTurn)
        if (moves.isEmpty()) {
            // Checkmate or stalemate
            return if (engine.isInCheck(engine.currentTurn)) {
                if (isMaximizing) -100000 - depth else 100000 + depth
            } else {
                0 // Stalemate / draw
            }
        }

        if (depth <= 0) {
            return evaluateBoard(engine)
        }

        val orderedMoves = orderMoves(moves, engine)

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in orderedMoves) {
                val clone = engine.clone()
                clone.makeMove(move)
                val eval = minimax(clone, depth - 1, currentAlpha, currentBeta, false)
                maxEval = maxOf(maxEval, eval)
                currentAlpha = maxOf(currentAlpha, eval)
                if (currentBeta <= currentAlpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in orderedMoves) {
                val clone = engine.clone()
                clone.makeMove(move)
                val eval = minimax(clone, depth - 1, currentAlpha, currentBeta, true)
                minEval = minOf(minEval, eval)
                currentBeta = minOf(currentBeta, eval)
                if (currentBeta <= currentAlpha) break
            }
            return minEval
        }
    }

    private fun orderMoves(moves: List<Move>, engine: ChessEngine): List<Move> {
        return moves.sortedByDescending { move ->
            var score = 0
            if (move.capturedPiece != null) {
                val victimVal = pieceValues[move.capturedPiece.type] ?: 0
                val attackerVal = pieceValues[move.piece.type] ?: 0
                score += 1000 + (victimVal - attackerVal / 10)
            }
            if (move.promotion != null) {
                score += 800
            }
            if (move.isCastling) {
                score += 50
            }
            score
        }
    }

    fun evaluateBoard(engine: ChessEngine): Int {
        var score = 0

        for (r in 0..7) {
            for (c in 0..7) {
                val piece = engine.getPiece(r, c) ?: continue
                val baseVal = pieceValues[piece.type] ?: 0
                val pstVal = getPstValue(piece.type, piece.color, r, c)

                val totalPieceVal = baseVal + pstVal
                if (piece.color == PieceColor.WHITE) {
                    score += totalPieceVal
                } else {
                    score -= totalPieceVal
                }
            }
        }

        return score
    }

    private fun getPstValue(type: PieceType, color: PieceColor, r: Int, c: Int): Int {
        val index = if (color == PieceColor.WHITE) {
            r * 8 + c
        } else {
            // Mirror row for Black: row 0 becomes row 7
            (7 - r) * 8 + c
        }

        return when (type) {
            PieceType.PAWN -> pawnPst.getOrElse(index) { 0 }
            PieceType.KNIGHT -> knightPst.getOrElse(index) { 0 }
            PieceType.BISHOP -> bishopPst.getOrElse(index) { 0 }
            PieceType.ROOK -> rookPst.getOrElse(index) { 0 }
            PieceType.QUEEN -> queenPst.getOrElse(index) { 0 }
            PieceType.KING -> kingMiddlePst.getOrElse(index) { 0 }
        }
    }
}
