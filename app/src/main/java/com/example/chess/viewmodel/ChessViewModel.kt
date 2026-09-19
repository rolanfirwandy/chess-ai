package com.example.chess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.ai.ChessAi
import com.example.chess.engine.ChessEngine
import com.example.chess.model.AiDifficulty
import com.example.chess.model.BoardTheme
import com.example.chess.model.GameMode
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChessUiState(
    val board: Array<Array<Piece?>> = ChessEngine.createInitialBoard(),
    val currentTurn: PieceColor = PieceColor.WHITE,
    val gameMode: GameMode = GameMode.VS_AI,
    val aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM,
    val playerColor: PieceColor = PieceColor.WHITE,
    val isAiThinking: Boolean = false,
    val selectedSquare: Square? = null,
    val legalMovesForSelected: List<Move> = emptyList(),
    val lastMove: Move? = null,
    val kingInCheckSquare: Square? = null,
    val isCheck: Boolean = false,
    val gameState: GameState = GameState.IN_PROGRESS,
    val winner: PieceColor? = null,
    val boardTheme: BoardTheme = BoardTheme.TournamentGreen,
    val autoFlipInPassToPlay: Boolean = true,
    val manualFlip: Boolean = false,
    val moveHistory: List<Move> = emptyList(),
    val reviewMoveIndex: Int? = null, // null means live game
    val capturedByWhite: List<Piece> = emptyList(),
    val capturedByBlack: List<Piece> = emptyList(),
    val pendingPromotion: Pair<Square, Square>? = null,
    val passNoticeVisible: Boolean = false,
    val passNoticeMessage: String = "",
    val fullMoveNumber: Int = 1
) {
    val isBoardFlipped: Boolean
        get() {
            if (manualFlip) return true
            if (gameMode == GameMode.PASS_TO_PLAY && autoFlipInPassToPlay) {
                return currentTurn == PieceColor.BLACK
            }
            if (gameMode == GameMode.VS_AI && playerColor == PieceColor.BLACK) {
                return true
            }
            return false
        }

    val isInteractive: Boolean
        get() = reviewMoveIndex == null &&
                gameState == GameState.IN_PROGRESS &&
                !isAiThinking &&
                pendingPromotion == null &&
                !passNoticeVisible

    val materialAdvantage: Int
        get() {
            val whiteVal = capturedByWhite.sumOf { it.type.value }
            val blackVal = capturedByBlack.sumOf { it.type.value }
            return (whiteVal - blackVal) / 100
        }
}

class ChessViewModel : ViewModel() {

    private var engine = ChessEngine()
    private val _uiState = MutableStateFlow(ChessUiState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    init {
        syncStateWithEngine()
    }

    private fun syncStateWithEngine() {
        val captured = engine.getCapturedPieces()
        val inCheck = engine.isInCheck(engine.currentTurn)
        val checkSquare = if (inCheck) engine.findKing(engine.currentTurn) else null
        val state = engine.getGameState()
        val winner = if (state == GameState.CHECKMATE) engine.currentTurn.opposite() else null

        val historyMoves = engine.moveHistory.map { it.move }
        val last = historyMoves.lastOrNull()

        _uiState.update { current ->
            current.copy(
                board = copyBoard(engine.board),
                currentTurn = engine.currentTurn,
                lastMove = last,
                kingInCheckSquare = checkSquare,
                isCheck = inCheck,
                gameState = state,
                winner = winner,
                moveHistory = historyMoves,
                capturedByWhite = captured.first,
                capturedByBlack = captured.second,
                fullMoveNumber = engine.fullMoveNumber
            )
        }
    }

    private fun copyBoard(source: Array<Array<Piece?>>): Array<Array<Piece?>> {
        return Array(8) { r -> Array(8) { c -> source[r][c] } }
    }

    fun onSquareClicked(square: Square) {
        val currentState = _uiState.value
        if (!currentState.isInteractive) return

        // If reviewing, clicking square cancels review and returns to live
        if (currentState.reviewMoveIndex != null) {
            stepToMove(null)
            return
        }

        val clickedPiece = engine.getPiece(square)
        val selected = currentState.selectedSquare

        if (selected == null) {
            // Pick piece if it belongs to current player
            if (clickedPiece != null && clickedPiece.color == engine.currentTurn) {
                // In AI mode, prevent moving AI pieces
                if (currentState.gameMode == GameMode.VS_AI && clickedPiece.color != currentState.playerColor) {
                    return
                }
                val legalMoves = engine.getLegalMovesFrom(square)
                _uiState.update { it.copy(selectedSquare = square, legalMovesForSelected = legalMoves) }
            }
        } else {
            // Check if clicking another friendly piece
            if (clickedPiece != null && clickedPiece.color == engine.currentTurn) {
                if (clickedPiece.color == currentState.playerColor || currentState.gameMode == GameMode.PASS_TO_PLAY) {
                    val legalMoves = engine.getLegalMovesFrom(square)
                    _uiState.update { it.copy(selectedSquare = square, legalMovesForSelected = legalMoves) }
                    return
                }
            }

            // Check if move is legal
            val matchingMove = currentState.legalMovesForSelected.find { it.to == square }
            if (matchingMove != null) {
                // Check if promotion
                if (engine.isPromotionMove(matchingMove)) {
                    _uiState.update {
                        it.copy(
                            pendingPromotion = Pair(matchingMove.from, matchingMove.to),
                            selectedSquare = null,
                            legalMovesForSelected = emptyList()
                        )
                    }
                } else {
                    executeMove(matchingMove)
                }
            } else {
                // Deselect
                _uiState.update { it.copy(selectedSquare = null, legalMovesForSelected = emptyList()) }
            }
        }
    }

    fun onPromotionSelected(promotionType: PieceType) {
        val pending = _uiState.value.pendingPromotion ?: return
        val from = pending.first
        val to = pending.second
        val piece = engine.getPiece(from) ?: return

        val move = Move(
            from = from,
            to = to,
            piece = piece,
            promotion = promotionType,
            capturedPiece = engine.getPiece(to)
        )

        _uiState.update { it.copy(pendingPromotion = null) }
        executeMove(move)
    }

    fun cancelPromotion() {
        _uiState.update { it.copy(pendingPromotion = null) }
    }

    private fun executeMove(move: Move) {
        val previousTurn = engine.currentTurn
        engine.makeMove(move)

        _uiState.update {
            it.copy(
                selectedSquare = null,
                legalMovesForSelected = emptyList(),
                reviewMoveIndex = null
            )
        }

        syncStateWithEngine()

        val newState = _uiState.value

        // Check if Pass-to-Play notification should trigger
        if (newState.gameMode == GameMode.PASS_TO_PLAY && newState.gameState == GameState.IN_PROGRESS) {
            val nextPlayer = engine.currentTurn.displayName()
            _uiState.update {
                it.copy(
                    passNoticeVisible = true,
                    passNoticeMessage = "Pass to $nextPlayer's turn"
                )
            }
        }

        // Trigger AI move if VS_AI
        if (newState.gameMode == GameMode.VS_AI &&
            newState.gameState == GameState.IN_PROGRESS &&
            engine.currentTurn != newState.playerColor
        ) {
            triggerAiMove()
        }
    }

    fun dismissPassNotice() {
        _uiState.update { it.copy(passNoticeVisible = false) }
    }

    private fun triggerAiMove() {
        val diff = _uiState.value.aiDifficulty
        _uiState.update { it.copy(isAiThinking = true) }

        viewModelScope.launch {
            // Natural thinking delay
            val delayMs = when (diff) {
                AiDifficulty.EASY -> 400L
                AiDifficulty.MEDIUM -> 650L
                AiDifficulty.HARD -> 850L
            }
            delay(delayMs)

            val bestMove = withContext(Dispatchers.Default) {
                ChessAi.selectBestMove(engine, diff)
            }

            if (bestMove != null && _uiState.value.gameState == GameState.IN_PROGRESS) {
                // If it requires promotion, AI automatically picks Queen
                val actualMove = if (engine.isPromotionMove(bestMove) && bestMove.promotion == null) {
                    bestMove.copy(promotion = PieceType.QUEEN)
                } else {
                    bestMove
                }
                engine.makeMove(actualMove)
            }

            _uiState.update { it.copy(isAiThinking = false) }
            syncStateWithEngine()
        }
    }

    fun setGameMode(mode: GameMode) {
        if (_uiState.value.gameMode == mode) return
        _uiState.update { it.copy(gameMode = mode) }
        resetGame()
    }

    fun setAiDifficulty(difficulty: AiDifficulty) {
        _uiState.update { it.copy(aiDifficulty = difficulty) }
    }

    fun setPlayerColor(color: PieceColor) {
        if (_uiState.value.playerColor == color) return
        _uiState.update { it.copy(playerColor = color) }
        resetGame()
        if (color == PieceColor.BLACK && _uiState.value.gameMode == GameMode.VS_AI) {
            triggerAiMove()
        }
    }

    fun setBoardTheme(theme: BoardTheme) {
        _uiState.update { it.copy(boardTheme = theme) }
    }

    fun toggleAutoFlip() {
        _uiState.update { it.copy(autoFlipInPassToPlay = !it.autoFlipInPassToPlay) }
    }

    fun toggleManualFlip() {
        _uiState.update { it.copy(manualFlip = !it.manualFlip) }
    }

    fun undo() {
        val state = _uiState.value
        if (state.isAiThinking || state.moveHistory.isEmpty()) return

        if (state.gameMode == GameMode.VS_AI) {
            // Undo twice (AI move + human move) if it's currently human's turn
            if (engine.currentTurn == state.playerColor && engine.moveHistory.size >= 2) {
                engine.undoMove()
                engine.undoMove()
            } else {
                engine.undoMove()
            }
        } else {
            engine.undoMove()
        }

        _uiState.update {
            it.copy(
                selectedSquare = null,
                legalMovesForSelected = emptyList(),
                reviewMoveIndex = null,
                passNoticeVisible = false
            )
        }
        syncStateWithEngine()
    }

    fun resetGame() {
        engine = ChessEngine()
        _uiState.update {
            it.copy(
                selectedSquare = null,
                legalMovesForSelected = emptyList(),
                reviewMoveIndex = null,
                pendingPromotion = null,
                passNoticeVisible = false,
                isAiThinking = false
            )
        }
        syncStateWithEngine()

        if (_uiState.value.gameMode == GameMode.VS_AI && _uiState.value.playerColor == PieceColor.BLACK) {
            triggerAiMove()
        }
    }

    fun stepToMove(index: Int?) {
        if (index == null || index >= engine.moveHistory.size || index < 0) {
            // Return to live game
            _uiState.update {
                it.copy(
                    reviewMoveIndex = null,
                    board = copyBoard(engine.board),
                    lastMove = engine.moveHistory.lastOrNull()?.move
                )
            }
            return
        }

        // Replay up to `index` on fresh engine
        val replayEngine = ChessEngine()
        for (i in 0..index) {
            replayEngine.makeMove(engine.moveHistory[i].move)
        }

        val reviewedLastMove = engine.moveHistory[index].move

        _uiState.update {
            it.copy(
                reviewMoveIndex = index,
                board = copyBoard(replayEngine.board),
                lastMove = reviewedLastMove,
                selectedSquare = null,
                legalMovesForSelected = emptyList()
            )
        }
    }
}
