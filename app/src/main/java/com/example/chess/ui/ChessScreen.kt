package com.example.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chess.model.GameMode
import com.example.chess.model.GameState
import com.example.chess.model.PieceColor
import com.example.chess.ui.components.ChessBoardView
import com.example.chess.ui.components.GameControlsBar
import com.example.chess.ui.components.GameOverDialog
import com.example.chess.ui.components.MoveHistoryTracker
import com.example.chess.ui.components.PassToPlayBanner
import com.example.chess.ui.components.PlayerHeaderCard
import com.example.chess.ui.components.PromotionDialog
import com.example.chess.ui.components.ResetConfirmDialog
import com.example.chess.ui.components.ThemeSelectorDialog
import com.example.chess.viewmodel.ChessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessScreen(
    viewModel: ChessViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(true) }

    // Re-show game over dialog if game state transitions to game over
    val isGameOver = uiState.gameState != GameState.IN_PROGRESS

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Chess",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (uiState.gameMode == GameMode.VS_AI) "vs AI" else "Pass to Play",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("top_bar_theme_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode & Settings Bar
            GameControlsBar(
                gameMode = uiState.gameMode,
                aiDifficulty = uiState.aiDifficulty,
                autoFlip = uiState.autoFlipInPassToPlay,
                canUndo = uiState.moveHistory.isNotEmpty() && !uiState.isAiThinking,
                onSelectMode = { viewModel.setGameMode(it) },
                onSelectDifficulty = { viewModel.setAiDifficulty(it) },
                onToggleAutoFlip = { viewModel.toggleAutoFlip() },
                onManualFlip = { viewModel.toggleManualFlip() },
                onOpenThemePicker = { showThemeDialog = true },
                onUndo = { viewModel.undo() },
                onResetGame = { showResetDialog = true }
            )

            // Top Player Card (Black or Opponent)
            val topColor = if (uiState.isBoardFlipped) PieceColor.WHITE else PieceColor.BLACK
            val topCaptured = if (topColor == PieceColor.WHITE) uiState.capturedByWhite else uiState.capturedByBlack
            val topAdvantage = if (topColor == PieceColor.WHITE) uiState.materialAdvantage else -uiState.materialAdvantage

            PlayerHeaderCard(
                color = topColor,
                isCurrentTurn = uiState.currentTurn == topColor,
                isAiThinking = uiState.isAiThinking && uiState.currentTurn == topColor,
                isCheck = uiState.isCheck && uiState.currentTurn == topColor,
                gameMode = uiState.gameMode,
                aiDifficulty = uiState.aiDifficulty,
                capturedPieces = topCaptured,
                materialAdvantage = topAdvantage,
                theme = uiState.boardTheme
            )

            // Pass to Play banner
            PassToPlayBanner(
                visible = uiState.passNoticeVisible,
                nextColor = uiState.currentTurn,
                onReady = { viewModel.dismissPassNotice() }
            )

            // Chess Board
            ChessBoardView(
                board = uiState.board,
                theme = uiState.boardTheme,
                isFlipped = uiState.isBoardFlipped,
                selectedSquare = uiState.selectedSquare,
                legalMoves = uiState.legalMovesForSelected,
                lastMove = uiState.lastMove,
                kingInCheckSquare = uiState.kingInCheckSquare,
                onSquareClick = { viewModel.onSquareClicked(it) }
            )

            // Bottom Player Card (White or Player 1)
            val bottomColor = if (uiState.isBoardFlipped) PieceColor.BLACK else PieceColor.WHITE
            val bottomCaptured = if (bottomColor == PieceColor.WHITE) uiState.capturedByWhite else uiState.capturedByBlack
            val bottomAdvantage = if (bottomColor == PieceColor.WHITE) uiState.materialAdvantage else -uiState.materialAdvantage

            PlayerHeaderCard(
                color = bottomColor,
                isCurrentTurn = uiState.currentTurn == bottomColor,
                isAiThinking = uiState.isAiThinking && uiState.currentTurn == bottomColor,
                isCheck = uiState.isCheck && uiState.currentTurn == bottomColor,
                gameMode = uiState.gameMode,
                aiDifficulty = uiState.aiDifficulty,
                capturedPieces = bottomCaptured,
                materialAdvantage = bottomAdvantage,
                theme = uiState.boardTheme
            )

            // Move History Tracker
            MoveHistoryTracker(
                moves = uiState.moveHistory,
                reviewMoveIndex = uiState.reviewMoveIndex,
                onStepToMove = { viewModel.stepToMove(it) },
                theme = uiState.boardTheme
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Dialogs
        if (uiState.pendingPromotion != null) {
            PromotionDialog(
                color = uiState.currentTurn,
                theme = uiState.boardTheme,
                onSelect = { viewModel.onPromotionSelected(it) },
                onDismiss = { viewModel.cancelPromotion() }
            )
        }

        if (showThemeDialog) {
            ThemeSelectorDialog(
                currentTheme = uiState.boardTheme,
                onSelectTheme = {
                    viewModel.setBoardTheme(it)
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        if (showResetDialog) {
            ResetConfirmDialog(
                onConfirm = {
                    viewModel.resetGame()
                    showResetDialog = false
                },
                onDismiss = { showResetDialog = false }
            )
        }

        if (isGameOver && showGameOverDialog) {
            GameOverDialog(
                gameState = uiState.gameState,
                winner = uiState.winner,
                moveCount = uiState.moveHistory.size,
                onPlayAgain = {
                    viewModel.resetGame()
                },
                onReviewBoard = {
                    showGameOverDialog = false
                }
            )
        }
    }
}
