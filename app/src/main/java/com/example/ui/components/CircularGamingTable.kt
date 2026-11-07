package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GameRoundState
import com.example.model.GameTable
import com.example.model.SeatPlayer
import com.example.ui.theme.*

@Composable
fun CircularGamingTable(
    table: GameTable,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    onVoiceResultClick: (() -> Unit)? = null
) {
    // Total table betting coins (pot): 6 players * tier coins (or 600 chips for practice)
    val totalBettingCoins = if (table.tierCoins > 0) {
        table.tierCoins * 6
    } else {
        600L
    }

    val perPlayerBet = if (table.tierCoins > 0) {
        "${table.tierCoins} Coins"
    } else {
        "Free Play"
    }

    // Top seats: Seats 3, 4, 5
    val topSeats = listOf(
        table.seats.getOrNull(2), // Seat 3
        table.seats.getOrNull(3), // Seat 4
        table.seats.getOrNull(4)  // Seat 5
    )

    // Bottom seats: Seat 2, Seat 1 (YOU - center bottom), Seat 6
    val bottomSeats = listOf(
        table.seats.getOrNull(1), // Seat 2
        table.seats.getOrNull(0), // Seat 1 (YOU)
        table.seats.getOrNull(5)  // Seat 6
    )

    val tableHeight = if (isLandscape) 140.dp else 190.dp
    val diceSize = if (isLandscape) 42.dp else 54.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stadium_gaming_table"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // TOP PLAYERS ROW (3 Seats)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            topSeats.forEachIndexed { idx, seat ->
                PlayerPod(
                    seat = seat,
                    seatNumber = idx + 3,
                    roundState = table.state,
                    tierCoins = table.tierCoins,
                    isLandscape = isLandscape
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 1.dp else 2.dp))

        // THE CASINO STADIUM FELT TABLE (Adaptive responsive aspect, matches uploaded image)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tableHeight)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            // High-fidelity Casino Stadium Table Felt Image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E15)),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF39FF14), // Neon green glow line
                            GudgudiGold,
                            Color(0xFF10B981),
                            Color(0xFF8B5A2B)
                        )
                    )
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.casino_table_felt),
                        contentDescription = "Casino Game Table",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Subtle luxury gradient sheen over table felt
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x22000000),
                                        Color(0x66000000)
                                    )
                                )
                            )
                    )
                }
            }

            // CENTER STAGE: TOTAL BETTING COINS (POT) + 3D ANIMATED DICE
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Total Betting Coins Plaque in the middle of the table
                Box(
                    modifier = Modifier
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0D281E).copy(alpha = 0.95f),
                                    Color(0xFF1B4332).copy(alpha = 0.95f),
                                    Color(0xFF0D281E).copy(alpha = 0.95f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(GudgudiGold, Color(0xFF39FF14), GudgudiGoldAmber)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = if (isLandscape) 8.dp else 12.dp, vertical = if (isLandscape) 2.dp else 3.dp)
                        .testTag("table_center_pot"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TOTAL BETTING COINS",
                            color = Color(0xFF86EFAC),
                            fontSize = if (isLandscape) 7.5.sp else 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = GudgudiGold,
                                modifier = Modifier.size(if (isLandscape) 14.dp else 16.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "%,d".format(totalBettingCoins),
                                color = GudgudiGoldLight,
                                fontSize = if (isLandscape) 14.sp else 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (table.tierCoins > 0) "COINS" else "CHIPS",
                                color = GudgudiGold,
                                fontSize = if (isLandscape) 9.sp else 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!isLandscape) {
                            Text(
                                text = "Bazi: $perPlayerBet",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isLandscape) 2.dp else 3.dp))

                // 2. Animated 3D Gudgudi Dice
                GudgudiDice3D(
                    state = table.state,
                    winningNumber = table.winningNumber,
                    sizeDp = diceSize
                )

                Spacer(modifier = Modifier.height(if (isLandscape) 2.dp else 3.dp))

                // 3. Round State / Timer Plaque
                when (table.state) {
                    GameRoundState.BETTING -> {
                        Surface(
                            color = Color(0xCC000000),
                            shape = RoundedCornerShape(7.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF39FF14))
                        ) {
                            Text(
                                text = "⏱️ ${table.countdownSeconds}s Baki",
                                color = Color(0xFF39FF14),
                                fontSize = if (isLandscape) 8.sp else 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                    GameRoundState.ROLLING -> {
                        Surface(
                            color = GudgudiCrimson,
                            shape = RoundedCornerShape(7.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGold)
                        ) {
                            Text(
                                text = "🎲 GHOOM RAHA HAI...",
                                color = GudgudiGoldLight,
                                fontSize = if (isLandscape) 8.sp else 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                    GameRoundState.RESULT -> {
                        val winnerNum = table.winningNumber ?: 1
                        Surface(
                            onClick = { onVoiceResultClick?.invoke() },
                            color = GudgudiEmeraldDark,
                            shape = RoundedCornerShape(7.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiEmeraldLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Boliye Pansa Result",
                                    tint = GudgudiGold,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "PANSA: #$winnerNum 🔊",
                                    color = Color.White,
                                    fontSize = if (isLandscape) 8.5.sp else 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    GameRoundState.WAITING -> {
                        Text(
                            text = "Waiting...",
                            color = GudgudiTextMuted,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 1.dp else 2.dp))

        // BOTTOM PLAYERS ROW (3 Seats: Seat 2, Seat 1 (YOU - center bottom), Seat 6)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            bottomSeats.forEachIndexed { idx, seat ->
                val seatNumber = when (idx) {
                    0 -> 2
                    1 -> 1 // YOU
                    else -> 6
                }
                PlayerPod(
                    seat = seat,
                    seatNumber = seatNumber,
                    roundState = table.state,
                    tierCoins = table.tierCoins,
                    isLandscape = isLandscape
                )
            }
        }
    }
}

@Composable
private fun PlayerPod(
    seat: SeatPlayer?,
    seatNumber: Int,
    roundState: GameRoundState,
    tierCoins: Long,
    isLandscape: Boolean = false
) {
    val isOccupied = seat != null
    val isUser = seat?.isUser == true
    val isWinner = seat?.isWinner == true

    val avatarSize = if (isLandscape) 30.dp else 38.dp
    val podWidth = if (isLandscape) 82.dp else 96.dp

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val winnerPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "winnerPulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(podWidth)
            .scale(if (isWinner && roundState == GameRoundState.RESULT) winnerPulse else 1f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Player Avatar Circle
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .shadow(
                        elevation = if (isWinner) 8.dp else 2.dp,
                        shape = CircleShape
                    )
                    .background(
                        brush = when {
                            isWinner -> Brush.linearGradient(listOf(GudgudiEmerald, GudgudiGold, Color(0xFF39FF14)))
                            isUser -> Brush.linearGradient(listOf(GudgudiCrimson, GudgudiGoldAmber))
                            isOccupied -> Brush.linearGradient(listOf(GudgudiMaroonCard, Color(0xFF3D162B)))
                            else -> Brush.linearGradient(listOf(Color(0xFF222222), Color(0xFF111111)))
                        },
                        shape = CircleShape
                    )
                    .border(
                        width = if (isUser || isWinner) 2.dp else 1.dp,
                        color = when {
                            isWinner -> Color(0xFF39FF14)
                            isUser -> GudgudiGoldLight
                            isOccupied -> GudgudiGoldDark.copy(alpha = 0.7f)
                            else -> Color(0xFF444444)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isOccupied) {
                    val avatarLetter = seat.playerName.take(1).uppercase()
                    Text(
                        text = avatarLetter,
                        color = if (isUser) GudgudiGoldLight else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isLandscape) 12.sp else 15.sp
                    )
                } else {
                    Text(
                        text = "$seatNumber",
                        color = Color.Gray,
                        fontSize = if (isLandscape) 9.sp else 11.sp
                    )
                }
            }

            // Winner Trophy/Crown on top
            if (isWinner && roundState == GameRoundState.RESULT) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Winner",
                    tint = GudgudiGold,
                    modifier = Modifier
                        .size(if (isLandscape) 13.dp else 17.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-5).dp)
                )
            }

            // Chosen Number Chip (1 - 6) at bottom of avatar
            if (isOccupied && seat.selectedNumber != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = if (isLandscape) 6.dp else 8.dp)
                        .background(
                            brush = if (isWinner) {
                                Brush.horizontalGradient(listOf(Color(0xFF39FF14), GudgudiEmerald))
                            } else {
                                Brush.horizontalGradient(listOf(GudgudiGold, GudgudiGoldAmber))
                            },
                            shape = RoundedCornerShape(5.dp)
                        )
                        .border(
                            1.dp,
                            if (isWinner) Color.White else GudgudiMaroonDark,
                            RoundedCornerShape(5.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = "#${seat.selectedNumber}",
                        color = if (isWinner) Color.Black else GudgudiMaroonDark,
                        fontSize = if (isLandscape) 8.sp else 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 7.dp else 9.dp))

        // Player Name & You Label
        Text(
            text = if (isOccupied) (if (isUser) "AAP (${seat.playerName.split(" ").first()})" else seat.playerName.split(" ").first()) else "Seat $seatNumber",
            color = when {
                isWinner -> Color(0xFF86EFAC)
                isUser -> GudgudiGold
                isOccupied -> GudgudiTextPrimary
                else -> Color.Gray
            },
            fontSize = if (isLandscape) 8.5.sp else 10.sp,
            fontWeight = if (isUser || isWinner) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Player Betting Coins Display (e.g. 500 Coins)
        if (isOccupied) {
            val betLabel = if (tierCoins > 0) "🪙 $tierCoins" else "Free"
            Surface(
                color = Color(0x66000000),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 1.dp)
            ) {
                Text(
                    text = betLabel,
                    color = if (isUser) GudgudiGoldLight else GudgudiTextSecondary,
                    fontSize = if (isLandscape) 7.5.sp else 8.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                )
            }
        }
    }
}
