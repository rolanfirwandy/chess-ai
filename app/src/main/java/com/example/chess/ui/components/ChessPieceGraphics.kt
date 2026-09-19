package com.example.chess.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.chess.model.BoardTheme
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType

@Composable
fun ChessPieceView(
    piece: Piece,
    theme: BoardTheme,
    modifier: Modifier = Modifier
) {
    val isWhite = piece.color == PieceColor.WHITE
    val fillColor = if (isWhite) theme.pieceWhiteFill else theme.pieceBlackFill
    val strokeColor = if (isWhite) theme.pieceWhiteStroke else theme.pieceBlackStroke

    Box(
        modifier = modifier.padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (piece.type) {
                PieceType.PAWN -> drawPawn(w, h, fillColor, strokeColor)
                PieceType.KNIGHT -> drawKnight(w, h, fillColor, strokeColor)
                PieceType.BISHOP -> drawBishop(w, h, fillColor, strokeColor)
                PieceType.ROOK -> drawRook(w, h, fillColor, strokeColor)
                PieceType.QUEEN -> drawQueen(w, h, fillColor, strokeColor)
                PieceType.KING -> drawKing(w, h, fillColor, strokeColor)
            }
        }
    }
}

private fun DrawScope.drawPawn(w: Float, h: Float, fill: Color, stroke: Color) {
    val strokeWidth = w * 0.045f

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.22f, h * 0.88f)
        lineTo(w * 0.78f, h * 0.88f)
        lineTo(w * 0.72f, h * 0.78f)
        lineTo(w * 0.28f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = strokeWidth))

    // Body
    val bodyPath = Path().apply {
        moveTo(w * 0.32f, h * 0.78f)
        cubicTo(w * 0.36f, h * 0.62f, w * 0.40f, h * 0.50f, w * 0.42f, h * 0.42f)
        lineTo(w * 0.58f, h * 0.42f)
        cubicTo(w * 0.60f, h * 0.50f, w * 0.64f, h * 0.62f, w * 0.68f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, fill, style = Fill)
    drawPath(bodyPath, stroke, style = Stroke(width = strokeWidth))

    // Collar
    drawRoundRect(
        color = fill,
        topLeft = Offset(w * 0.38f, h * 0.40f),
        size = Size(w * 0.24f, h * 0.06f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
    )
    drawRoundRect(
        color = stroke,
        topLeft = Offset(w * 0.38f, h * 0.40f),
        size = Size(w * 0.24f, h * 0.06f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
        style = Stroke(width = strokeWidth)
    )

    // Head
    drawCircle(
        color = fill,
        radius = w * 0.16f,
        center = Offset(w * 0.50f, h * 0.26f)
    )
    drawCircle(
        color = stroke,
        radius = w * 0.16f,
        center = Offset(w * 0.50f, h * 0.26f),
        style = Stroke(width = strokeWidth)
    )
}

private fun DrawScope.drawKnight(w: Float, h: Float, fill: Color, stroke: Color) {
    val strokeWidth = w * 0.045f

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        lineTo(w * 0.80f, h * 0.88f)
        lineTo(w * 0.74f, h * 0.78f)
        lineTo(w * 0.26f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = strokeWidth))

    // Knight Profile
    val horsePath = Path().apply {
        moveTo(w * 0.28f, h * 0.78f)
        // Back mane
        cubicTo(w * 0.30f, h * 0.60f, w * 0.32f, h * 0.40f, w * 0.38f, h * 0.24f)
        // Ears
        lineTo(w * 0.44f, h * 0.14f)
        lineTo(w * 0.50f, h * 0.22f)
        // Forehead
        cubicTo(w * 0.58f, h * 0.24f, w * 0.68f, h * 0.30f, w * 0.76f, h * 0.38f)
        // Muzzle
        lineTo(w * 0.74f, h * 0.46f)
        lineTo(w * 0.62f, h * 0.48f)
        // Jaw & Chest
        cubicTo(w * 0.56f, h * 0.54f, w * 0.64f, h * 0.66f, w * 0.72f, h * 0.78f)
        close()
    }
    drawPath(horsePath, fill, style = Fill)
    drawPath(horsePath, stroke, style = Stroke(width = strokeWidth))

    // Eye
    drawCircle(
        color = stroke,
        radius = w * 0.035f,
        center = Offset(w * 0.56f, h * 0.32f)
    )

    // Mane accent line
    val maneLine = Path().apply {
        moveTo(w * 0.42f, h * 0.32f)
        lineTo(w * 0.36f, h * 0.46f)
    }
    drawPath(maneLine, stroke, style = Stroke(width = strokeWidth * 0.8f))
}

private fun DrawScope.drawBishop(w: Float, h: Float, fill: Color, stroke: Color) {
    val strokeWidth = w * 0.045f

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        lineTo(w * 0.80f, h * 0.88f)
        lineTo(w * 0.72f, h * 0.78f)
        lineTo(w * 0.28f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = strokeWidth))

    // Body
    val bodyPath = Path().apply {
        moveTo(w * 0.32f, h * 0.78f)
        cubicTo(w * 0.36f, h * 0.60f, w * 0.40f, h * 0.50f, w * 0.42f, h * 0.44f)
        lineTo(w * 0.58f, h * 0.44f)
        cubicTo(w * 0.60f, h * 0.50f, w * 0.64f, h * 0.60f, w * 0.68f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, fill, style = Fill)
    drawPath(bodyPath, stroke, style = Stroke(width = strokeWidth))

    // Mitre (head)
    val mitrePath = Path().apply {
        moveTo(w * 0.38f, h * 0.44f)
        cubicTo(w * 0.30f, h * 0.32f, w * 0.42f, h * 0.18f, w * 0.50f, h * 0.16f)
        cubicTo(w * 0.58f, h * 0.18f, w * 0.70f, h * 0.32f, w * 0.62f, h * 0.44f)
        close()
    }
    drawPath(mitrePath, fill, style = Fill)
    drawPath(mitrePath, stroke, style = Stroke(width = strokeWidth))

    // Mitre cut / slit
    val slitPath = Path().apply {
        moveTo(w * 0.54f, h * 0.22f)
        lineTo(w * 0.44f, h * 0.36f)
    }
    drawPath(slitPath, stroke, style = Stroke(width = strokeWidth))

    // Top Cross / ball
    drawCircle(
        color = fill,
        radius = w * 0.05f,
        center = Offset(w * 0.50f, h * 0.13f)
    )
    drawCircle(
        color = stroke,
        radius = w * 0.05f,
        center = Offset(w * 0.50f, h * 0.13f),
        style = Stroke(width = strokeWidth * 0.8f)
    )
}

private fun DrawScope.drawRook(w: Float, h: Float, fill: Color, stroke: Color) {
    val strokeWidth = w * 0.045f

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.18f, h * 0.88f)
        lineTo(w * 0.82f, h * 0.88f)
        lineTo(w * 0.74f, h * 0.78f)
        lineTo(w * 0.26f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = strokeWidth))

    // Body
    val bodyPath = Path().apply {
        moveTo(w * 0.30f, h * 0.78f)
        lineTo(w * 0.34f, h * 0.38f)
        lineTo(w * 0.66f, h * 0.38f)
        lineTo(w * 0.70f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, fill, style = Fill)
    drawPath(bodyPath, stroke, style = Stroke(width = strokeWidth))

    // Turret / Battlements
    val turretPath = Path().apply {
        moveTo(w * 0.24f, h * 0.38f)
        lineTo(w * 0.24f, h * 0.20f)
        lineTo(w * 0.35f, h * 0.20f)
        lineTo(w * 0.35f, h * 0.28f)
        lineTo(w * 0.44f, h * 0.28f)
        lineTo(w * 0.44f, h * 0.20f)
        lineTo(w * 0.56f, h * 0.20f)
        lineTo(w * 0.56f, h * 0.28f)
        lineTo(w * 0.65f, h * 0.28f)
        lineTo(w * 0.65f, h * 0.20f)
        lineTo(w * 0.76f, h * 0.20f)
        lineTo(w * 0.76f, h * 0.38f)
        close()
    }
    drawPath(turretPath, fill, style = Fill)
    drawPath(turretPath, stroke, style = Stroke(width = strokeWidth))
}

private fun DrawScope.drawQueen(w: Float, h: Float, fill: Color, stroke: Color) {
    val strokeWidth = w * 0.045f

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.18f, h * 0.88f)
        lineTo(w * 0.82f, h * 0.88f)
        lineTo(w * 0.74f, h * 0.78f)
        lineTo(w * 0.26f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = strokeWidth))

    // Body
    val bodyPath = Path().apply {
        moveTo(w * 0.30f, h * 0.78f)
        cubicTo(w * 0.36f, h * 0.60f, w * 0.40f, h * 0.48f, w * 0.42f, h * 0.42f)
        lineTo(w * 0.58f, h * 0.42f)
        cubicTo(w * 0.60f, h * 0.48f, w * 0.64f, h * 0.60f, w * 0.70f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, fill, style = Fill)
    drawPath(bodyPath, stroke, style = Stroke(width = strokeWidth))

    // Crown spikes (5 points)
    val crownPath = Path().apply {
        moveTo(w * 0.34f, h * 0.42f)
        lineTo(w * 0.20f, h * 0.24f)
        lineTo(w * 0.36f, h * 0.32f)
        lineTo(w * 0.40f, h * 0.18f)
        lineTo(w * 0.50f, h * 0.28f)
        lineTo(w * 0.60f, h * 0.18f)
        lineTo(w * 0.64f, h * 0.32f)
        lineTo(w * 0.80f, h * 0.24f)
        lineTo(w * 0.66f, h * 0.42f)
        close()
    }
    drawPath(crownPath, fill, style = Fill)
    drawPath(crownPath, stroke, style = Stroke(width = strokeWidth))

    // Crown jewels / pearls
    val pearlCoords = arrayOf(
        Offset(w * 0.20f, h * 0.22f),
        Offset(w * 0.40f, h * 0.16f),
        Offset(w * 0.60f, h * 0.16f),
        Offset(w * 0.80f, h * 0.22f)
    )
    for (coord in pearlCoords) {
        drawCircle(color = fill, radius = w * 0.035f, center = coord)
        drawCircle(color = stroke, radius = w * 0.035f, center = coord, style = Stroke(width = strokeWidth * 0.7f))
    }
}

private fun DrawScope.drawKing(w: Float, h: Float, fill: Color, stroke: Color) {
    val strokeWidth = w * 0.045f

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.18f, h * 0.88f)
        lineTo(w * 0.82f, h * 0.88f)
        lineTo(w * 0.74f, h * 0.78f)
        lineTo(w * 0.26f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill, style = Fill)
    drawPath(basePath, stroke, style = Stroke(width = strokeWidth))

    // Body
    val bodyPath = Path().apply {
        moveTo(w * 0.30f, h * 0.78f)
        cubicTo(w * 0.36f, h * 0.60f, w * 0.38f, h * 0.48f, w * 0.40f, h * 0.40f)
        lineTo(w * 0.60f, h * 0.40f)
        cubicTo(w * 0.62f, h * 0.48f, w * 0.64f, h * 0.60f, w * 0.70f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, fill, style = Fill)
    drawPath(bodyPath, stroke, style = Stroke(width = strokeWidth))

    // Crown robe
    val crownPath = Path().apply {
        moveTo(w * 0.34f, h * 0.40f)
        cubicTo(w * 0.24f, h * 0.30f, w * 0.34f, h * 0.22f, w * 0.50f, h * 0.22f)
        cubicTo(w * 0.66f, h * 0.22f, w * 0.76f, h * 0.30f, w * 0.66f, h * 0.40f)
        close()
    }
    drawPath(crownPath, fill, style = Fill)
    drawPath(crownPath, stroke, style = Stroke(width = strokeWidth))

    // Royal Cross
    val crossV = Path().apply {
        moveTo(w * 0.50f, h * 0.10f)
        lineTo(w * 0.50f, h * 0.22f)
    }
    drawPath(crossV, stroke, style = Stroke(width = strokeWidth * 1.3f))

    val crossH = Path().apply {
        moveTo(w * 0.42f, h * 0.15f)
        lineTo(w * 0.58f, h * 0.15f)
    }
    drawPath(crossH, stroke, style = Stroke(width = strokeWidth * 1.3f))
}
