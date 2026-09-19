package com.example.chess.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.BoardTheme
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor

@Composable
fun MoveHistoryTracker(
    moves: List<Move>,
    reviewMoveIndex: Int?,
    onStepToMove: (Int?) -> Unit,
    theme: BoardTheme,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Auto-scroll to current position
    LaunchedEffect(moves.size, reviewMoveIndex) {
        val targetIndex = reviewMoveIndex ?: (moves.size - 1)
        if (targetIndex >= 0 && moves.isNotEmpty()) {
            listState.animateScrollToItem(targetIndex / 2)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("move_history_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row: Title + Status + Copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Move History",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${moves.size} moves",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (moves.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val pgn = formatMovesToPgn(moves)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Chess Moves", pgn)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Move history copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_moves_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy moves",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reviewing past state banner
            AnimatedVisibility(visible = reviewMoveIndex != null) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Inspecting move #${(reviewMoveIndex ?: 0) + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        FilledTonalButton(
                            onClick = { onStepToMove(null) },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "Return to Live",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Move List (Horizontal scrolling pairs: 1. e4 e5 | 2. Nf3 Nc6)
            if (moves.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Game just started. Moves will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Group into turns: 1. White, Black
                val turnPairs = mutableListOf<Pair<Move, Move?>>()
                var i = 0
                while (i < moves.size) {
                    val white = moves[i]
                    val black = if (i + 1 < moves.size) moves[i + 1] else null
                    turnPairs.add(Pair(white, black))
                    i += 2
                }

                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(turnPairs) { turnIndex, (white, black) ->
                        val whiteMoveIndex = turnIndex * 2
                        val blackMoveIndex = whiteMoveIndex + 1

                        val isWhiteActive = reviewMoveIndex == whiteMoveIndex ||
                                (reviewMoveIndex == null && whiteMoveIndex == moves.size - 1)
                        val isBlackActive = reviewMoveIndex == blackMoveIndex ||
                                (reviewMoveIndex == null && blackMoveIndex == moves.size - 1)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${turnIndex + 1}.",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            // White Move
                            MoveChip(
                                text = white.san,
                                isActive = isWhiteActive,
                                onClick = { onStepToMove(whiteMoveIndex) }
                            )

                            // Black Move (if exists)
                            if (black != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                MoveChip(
                                    text = black.san,
                                    isActive = isBlackActive,
                                    onClick = { onStepToMove(blackMoveIndex) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (moves.isNotEmpty()) onStepToMove(0) },
                    enabled = moves.isNotEmpty() && reviewMoveIndex != 0,
                    modifier = Modifier.testTag("history_first_button")
                ) {
                    Icon(Icons.Default.FirstPage, contentDescription = "First move")
                }

                IconButton(
                    onClick = {
                        val current = reviewMoveIndex ?: (moves.size - 1)
                        if (current > 0) onStepToMove(current - 1)
                    },
                    enabled = moves.isNotEmpty() && (reviewMoveIndex ?: moves.size - 1) > 0,
                    modifier = Modifier.testTag("history_prev_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous move")
                }

                IconButton(
                    onClick = {
                        val current = reviewMoveIndex ?: (moves.size - 1)
                        if (current < moves.size - 1) {
                            onStepToMove(current + 1)
                        } else {
                            onStepToMove(null)
                        }
                    },
                    enabled = moves.isNotEmpty() && reviewMoveIndex != null && reviewMoveIndex < moves.size - 1,
                    modifier = Modifier.testTag("history_next_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next move")
                }

                IconButton(
                    onClick = { onStepToMove(null) },
                    enabled = reviewMoveIndex != null,
                    modifier = Modifier.testTag("history_latest_button")
                ) {
                    Icon(Icons.Default.LastPage, contentDescription = "Live position")
                }
            }
        }
    }
}

@Composable
private fun MoveChip(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatMovesToPgn(moves: List<Move>): String {
    val sb = StringBuilder()
    var moveNum = 1
    for (i in moves.indices step 2) {
        sb.append("$moveNum. ")
        sb.append(moves[i].san)
        if (i + 1 < moves.size) {
            sb.append(" ")
            sb.append(moves[i + 1].san)
        }
        sb.append(" ")
        moveNum++
    }
    return sb.toString().trim()
}
