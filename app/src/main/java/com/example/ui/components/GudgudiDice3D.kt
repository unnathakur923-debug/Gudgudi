package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.GameRoundState
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun GudgudiDice3D(
    state: GameRoundState,
    winningNumber: Int?,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 100.dp
) {
    val isRolling = (state == GameRoundState.ROLLING)

    // Infinite rotation when rolling
    val infiniteTransition = rememberInfiniteTransition(label = "dice_roll")
    val rollRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Rapidly changing face during roll
    var transientFace by remember { mutableIntStateOf(1) }
    LaunchedEffect(isRolling) {
        if (isRolling) {
            while (true) {
                transientFace = Random.nextInt(1, 7)
                kotlinx.coroutines.delay(70L)
            }
        }
    }

    val displayFace = when {
        isRolling -> transientFace
        winningNumber != null -> winningNumber
        else -> 6 // Default majestic 6
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .scale(if (isRolling) bounceScale else 1f)
            .rotate(if (isRolling) rollRotation else 0f),
        contentAlignment = Alignment.Center
    ) {
        // Shadow underneath
        Canvas(
            modifier = Modifier
                .size(sizeDp)
                .offset(y = (sizeDp.value * 0.12f).dp)
        ) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                ),
                topLeft = Offset(size.width * 0.1f, size.height * 0.75f),
                size = Size(size.width * 0.8f, size.height * 0.25f)
            )
        }

        // Dice Body (Golden Ivory Cube)
        Box(
            modifier = Modifier
                .size(sizeDp * 0.85f)
                .shadow(
                    elevation = if (isRolling) 16.dp else 8.dp,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GudgudiGoldLight,
                            GudgudiGold,
                            GudgudiGoldDark
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(300f, 300f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFF9DB), GudgudiGoldAmber, GudgudiCrimson)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Dice pips (1 to 6)
            DicePipsCanvas(face = displayFace, modifier = Modifier.fillMaxSize())
        }

        // Winner aura when at result
        if (state == GameRoundState.RESULT && winningNumber != null) {
            Box(
                modifier = Modifier
                    .size(sizeDp * 1.15f)
                    .border(
                        width = 3.dp,
                        brush = Brush.sweepGradient(
                            listOf(GudgudiEmerald, GudgudiGold, GudgudiEmeraldLight, GudgudiGold)
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
            )
        }
    }
}

@Composable
private fun DicePipsCanvas(face: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(10.dp)) {
        val w = size.width
        val h = size.height
        val pipRadius = w * 0.10f

        val left = w * 0.22f
        val center = w * 0.50f
        val right = w * 0.78f

        val top = h * 0.22f
        val middle = h * 0.50f
        val bottom = h * 0.78f

        fun drawPip(cx: Float, cy: Float) {
            // Pip shadow
            drawCircle(
                color = Color(0x66000000),
                radius = pipRadius * 1.1f,
                center = Offset(cx + 2f, cy + 2f)
            )
            // Pip Crimson ruby dot with gold highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GudgudiCrimsonLight, GudgudiCrimson, GudgudiMaroonDark),
                    center = Offset(cx - pipRadius * 0.2f, cy - pipRadius * 0.2f),
                    radius = pipRadius
                ),
                radius = pipRadius,
                center = Offset(cx, cy)
            )
            // Tiny gleam
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = pipRadius * 0.25f,
                center = Offset(cx - pipRadius * 0.35f, cy - pipRadius * 0.35f)
            )
        }

        when (face) {
            1 -> {
                // Large center pip
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GudgudiCrimsonLight, GudgudiCrimson, GudgudiMaroonDark),
                        center = Offset(center - pipRadius * 0.3f, middle - pipRadius * 0.3f),
                        radius = pipRadius * 1.5f
                    ),
                    radius = pipRadius * 1.4f,
                    center = Offset(center, middle)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = pipRadius * 0.35f,
                    center = Offset(center - pipRadius * 0.4f, middle - pipRadius * 0.4f)
                )
            }
            2 -> {
                drawPip(left, top)
                drawPip(right, bottom)
            }
            3 -> {
                drawPip(left, top)
                drawPip(center, middle)
                drawPip(right, bottom)
            }
            4 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
            5 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(center, middle)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
            6 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(left, middle)
                drawPip(right, middle)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
        }
    }
}
