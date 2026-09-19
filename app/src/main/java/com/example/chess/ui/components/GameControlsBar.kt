package com.example.chess.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.AiDifficulty
import com.example.chess.model.GameMode

@Composable
fun GameControlsBar(
    gameMode: GameMode,
    aiDifficulty: AiDifficulty,
    autoFlip: Boolean,
    canUndo: Boolean,
    onSelectMode: (GameMode) -> Unit,
    onSelectDifficulty: (AiDifficulty) -> Unit,
    onToggleAutoFlip: () -> Unit,
    onManualFlip: () -> Unit,
    onOpenThemePicker: () -> Unit,
    onUndo: () -> Unit,
    onResetGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Mode Selector: VS AI vs PASS TO PLAY
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mode_selector_row")
        ) {
            val modes = listOf(GameMode.VS_AI, GameMode.PASS_TO_PLAY)
            modes.forEachIndexed { index, mode ->
                val selected = gameMode == mode
                SegmentedButton(
                    selected = selected,
                    onClick = { onSelectMode(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                    modifier = Modifier.testTag("mode_btn_${mode.name.lowercase()}")
                ) {
                    Text(
                        text = mode.title,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Secondary row: Difficulty chips for AI or Auto-flip toggle for Pass to Play
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = gameMode == GameMode.VS_AI) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AiDifficulty.values().forEach { diff ->
                        val isSelected = aiDifficulty == diff
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectDifficulty(diff) },
                            label = {
                                Text(
                                    text = diff.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("diff_${diff.name.lowercase()}"),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = gameMode == GameMode.PASS_TO_PLAY) {
                FilterChip(
                    selected = autoFlip,
                    onClick = onToggleAutoFlip,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (autoFlip) "Auto-Flip: ON" else "Auto-Flip: OFF",
                            fontSize = 11.sp,
                            fontWeight = if (autoFlip) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("auto_flip_chip"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Quick actions on the right: Themes, Flip, Undo, Reset
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenThemePicker,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("theme_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Board Themes",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onManualFlip,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("flip_board_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Rotate Board",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo Move",
                        tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onResetGame,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("reset_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "New Game",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
