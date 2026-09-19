package com.example

import com.example.chess.ai.ChessAi
import com.example.chess.engine.ChessEngine
import com.example.chess.model.AiDifficulty
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessEngineTest {

    @Test
    fun initialBoardHas20LegalMovesForWhite() {
        val engine = ChessEngine()
        val legalMoves = engine.getLegalMoves(PieceColor.WHITE)
        // 16 pawn moves (8 single + 8 double) + 4 knight moves (2 from b1 + 2 from g1)
        assertEquals(20, legalMoves.size)
    }

    @Test
    fun pawnDoublePushAndEnPassantTarget() {
        val engine = ChessEngine()
        // e2 to e4: from (6, 4) to (4, 4)
        val move = Move(Square(6, 4), Square(4, 4), engine.getPiece(6, 4)!!)
        engine.makeMove(move)

        assertEquals(PieceColor.BLACK, engine.currentTurn)
        assertEquals(Square(5, 4), engine.enPassantTarget) // e3 is en passant target
        assertEquals(1, engine.moveHistory.size)
        assertEquals("e4", engine.moveHistory[0].move.san)
    }

    @Test
    fun undoMoveRestoresPosition() {
        val engine = ChessEngine()
        val originalPiece = engine.getPiece(6, 4)
        val move = Move(Square(6, 4), Square(4, 4), originalPiece!!)
        engine.makeMove(move)

        val undone = engine.undoMove()
        assertNotNull(undone)
        assertEquals(PieceColor.WHITE, engine.currentTurn)
        assertEquals(originalPiece, engine.getPiece(6, 4))
        assertEquals(null, engine.getPiece(4, 4))
        assertEquals(0, engine.moveHistory.size)
    }

    @Test
    fun foolsMateCheckmateDetection() {
        val engine = ChessEngine()
        // 1. f3 (f2-f3: 6,5 to 5,5)
        engine.makeMove(Move(Square(6, 5), Square(5, 5), engine.getPiece(6, 5)!!))
        // 1... e5 (e7-e5: 1,4 to 3,4)
        engine.makeMove(Move(Square(1, 4), Square(3, 4), engine.getPiece(1, 4)!!))
        // 2. g4 (g2-g4: 6,6 to 4,6)
        engine.makeMove(Move(Square(6, 6), Square(4, 6), engine.getPiece(6, 6)!!))
        // 2... Qh4# (d8-h4: 0,3 to 4,7)
        val queenMove = Move(Square(0, 3), Square(4, 7), engine.getPiece(0, 3)!!)
        val finishedMove = engine.makeMove(queenMove)

        assertTrue(finishedMove.san.endsWith("#"))
        assertTrue(engine.isInCheck(PieceColor.WHITE))
        assertEquals(GameState.CHECKMATE, engine.getGameState())
    }

    @Test
    fun aiGeneratesValidMove() {
        val engine = ChessEngine()
        val easyMove = ChessAi.selectBestMove(engine, AiDifficulty.EASY)
        assertNotNull(easyMove)
        assertTrue(engine.getLegalMoves(PieceColor.WHITE).contains(easyMove))

        val mediumMove = ChessAi.selectBestMove(engine, AiDifficulty.MEDIUM)
        assertNotNull(mediumMove)
        assertTrue(engine.getLegalMoves(PieceColor.WHITE).contains(mediumMove))

        val hardMove = ChessAi.selectBestMove(engine, AiDifficulty.HARD)
        assertNotNull(hardMove)
        assertTrue(engine.getLegalMoves(PieceColor.WHITE).contains(hardMove))
    }
}
