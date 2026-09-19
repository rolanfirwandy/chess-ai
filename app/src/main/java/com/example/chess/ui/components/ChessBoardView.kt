package com.example.chess.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.BoardTheme
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.Square

@Composable
fun ChessBoardView(
    board: Array<Array<Piece?>>,
    theme: BoardTheme,
    isFlipped: Boolean,
    selectedSquare: Square?,
    legalMoves: List<Move>,
    lastMove: Move?,
    kingInCheckSquare: Square?,
    onSquareClick: (Square) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(theme.boardBorder)
            .padding(6.dp)
            .testTag("chess_board_container"),
        contentAlignment = Alignment.Center
    ) {
        val boardSize = maxWidth

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        ) {
            for (displayRow in 0..7) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (displayCol in 0..7) {
                        // Map display coordinates to logical board coordinates based on flip
                        val actualRow = if (isFlipped) 7 - displayRow else displayRow
                        val actualCol = if (isFlipped) 7 - displayCol else displayCol
                        val square = Square(actualRow, actualCol)

                        val isDarkSquare = (actualRow + actualCol) % 2 == 1
                        val piece = board[actualRow][actualCol]

                        val isSelected = selectedSquare == square
                        val isLastMove = lastMove?.from == square || lastMove?.to == square
                        val isInCheck = kingInCheckSquare == square
                        val legalMove = legalMoves.find { it.to == square }

                        ChessSquareView(
                            square = square,
                            piece = piece,
                            theme = theme,
                            isDark = isDarkSquare,
                            isSelected = isSelected,
                            isLastMove = isLastMove,
                            isInCheck = isInCheck,
                            legalMove = legalMove,
                            showRankLabel = displayCol == 0,
                            showFileLabel = displayRow == 7,
                            rankText = (8 - actualRow).toString(),
                            fileText = ('a' + actualCol).toString(),
                            onClick = { onSquareClick(square) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChessSquareView(
    square: Square,
    piece: Piece?,
    theme: BoardTheme,
    isDark: Boolean,
    isSelected: Boolean,
    isLastMove: Boolean,
    isInCheck: Boolean,
    legalMove: Move?,
    showRankLabel: Boolean,
    showFileLabel: Boolean,
    rankText: String,
    fileText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseColor = if (isDark) theme.darkSquare else theme.lightSquare

    val overlayColor by animateColorAsState(
        targetValue = when {
            isInCheck -> theme.inCheckGlow
            isSelected -> theme.selectedSquare
            isLastMove -> theme.lastMoveHighlight
            else -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "square_overlay"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(baseColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("square_${square.algebraic}"),
        contentAlignment = Alignment.Center
    ) {
        // Highlight overlay (selection, last move, check)
        if (overlayColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayColor)
            )
        }

        // Rank label (top-left of first column)
        if (showRankLabel) {
            Text(
                text = rankText,
                color = theme.rankFileTextColor.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 3.dp, top = 1.dp)
            )
        }

        // File label (bottom-right of bottom row)
        if (showFileLabel) {
            Text(
                text = fileText,
                color = theme.rankFileTextColor.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 3.dp, bottom = 1.dp)
            )
        }

        // Legal move indicators (dot for empty square, ring for capture)
        if (legalMove != null) {
            val isCapture = piece != null || legalMove.isEnPassant
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                if (isCapture) {
                    // Capture target ring
                    val ringRadius = size.width * 0.42f
                    drawCircle(
                        color = theme.legalMoveCapture,
                        radius = ringRadius,
                        center = center,
                        style = Stroke(width = size.width * 0.08f)
                    )
                } else {
                    // Destination dot
                    val dotRadius = size.width * 0.16f
                    drawCircle(
                        color = theme.legalMoveDot,
                        radius = dotRadius,
                        center = center
                    )
                }
            }
        }

        // Piece view
        if (piece != null) {
            ChessPieceView(
                piece = piece,
                theme = theme,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
