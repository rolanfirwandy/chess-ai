package com.example.chess.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.GameState
import com.example.chess.model.PieceColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameOverDialog(
    gameState: GameState,
    winner: PieceColor?,
    moveCount: Int,
    onPlayAgain: () -> Unit,
    onReviewBoard: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onReviewBoard) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.testTag("game_over_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy or Handshake Icon
                val isDraw = gameState != GameState.CHECKMATE
                val icon = if (isDraw) Icons.Default.Handshake else Icons.Default.EmojiEvents
                val tint = if (isDraw) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

                Surface(
                    color = tint.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val titleText = when (gameState) {
                    GameState.CHECKMATE -> "Checkmate!"
                    GameState.STALEMATE -> "Stalemate"
                    GameState.DRAW_INSUFFICIENT_MATERIAL -> "Draw"
                    GameState.DRAW_50_MOVES -> "Draw (50 Moves)"
                    GameState.DRAW_REPETITION -> "Draw (Repetition)"
                    else -> "Game Concluded"
                }

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                val subtitleText = when (gameState) {
                    GameState.CHECKMATE -> "${winner?.displayName()} wins the game!"
                    GameState.STALEMATE -> "No legal moves remaining and not in check."
                    GameState.DRAW_INSUFFICIENT_MATERIAL -> "Insufficient material to deliver checkmate."
                    GameState.DRAW_50_MOVES -> "50 consecutive moves without a pawn push or capture."
                    else -> "Game ended in a draw."
                }

                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$moveCount moves played",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReviewBoard,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("review_board_button")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Review")
                    }

                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("play_again_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Game")
                    }
                }
            }
        }
    }
}
