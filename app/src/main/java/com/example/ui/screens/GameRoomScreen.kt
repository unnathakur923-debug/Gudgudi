package com.example.ui.screens

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.GameRepository
import com.example.model.GameRoundState
import com.example.model.RoundSettlement
import com.example.ui.components.CircularGamingTable
import com.example.ui.theme.*
import com.example.util.VoiceManager

@Composable
fun GameRoomScreen(
    repository: GameRepository,
    onLeaveTable: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val activeTable by repository.activeTable.collectAsState()
    val wallet by repository.walletState.collectAsState()

    // Initialize Hindi Voice commentary
    val voiceManager = remember { VoiceManager(context) }
    var isVoiceMuted by remember { mutableStateOf(false) }

    // Dialog control for reviewing settlement hisab
    var showSettlementDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.shutdown()
        }
    }

    BackHandler {
        voiceManager.shutdown()
        repository.leaveTable()
        onLeaveTable()
    }

    if (activeTable == null) {
        Box(
            modifier = modifier.fillMaxSize().background(GudgudiMaroonDark),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GudgudiGold)
        }
        return
    }

    val table = activeTable!!
    val userSeat = table.seats.firstOrNull { it.isUser }
    val userSelectedNumber = userSeat?.selectedNumber
    val isWin = (userSeat?.isWinner == true)

    // Automatically open settlement dialog and trigger applause sound when round finishes
    LaunchedEffect(table.state) {
        when (table.state) {
            GameRoundState.BETTING -> {
                showSettlementDialog = false
                voiceManager.speakBettingOpen(30)
            }
            GameRoundState.ROLLING -> {
                showSettlementDialog = false
                // Tactile vibration
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(180)
                    }
                }
                voiceManager.speakDiceRolling()
            }
            GameRoundState.RESULT -> {
                showSettlementDialog = true
                val winNum = table.winningNumber ?: 6
                val rewardCoins = table.lastSettlement?.playerResults?.firstOrNull { it.isUser }?.winAmount ?: 0L
                // Play crowd applause clapping sound and Hindi voice announcement with 15% hisab!
                voiceManager.playApplauseSound()
                voiceManager.speakDiceResultWithAccounting(winNum, isWin, rewardCoins)
            }
            GameRoundState.WAITING -> {
                showSettlementDialog = false
            }
        }
    }

    // Voice countdown warnings during 30s betting period (at 15s and 5s)
    LaunchedEffect(table.countdownSeconds, table.state) {
        if (table.state == GameRoundState.BETTING) {
            if (table.countdownSeconds == 15 || table.countdownSeconds == 5) {
                voiceManager.speakTimeWarning(table.countdownSeconds)
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GudgudiMaroonSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = if (isLandscape) 2.dp else 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            voiceManager.shutdown()
                            repository.leaveTable()
                            onLeaveTable()
                        },
                        modifier = Modifier.size(if (isLandscape) 30.dp else 34.dp).testTag("leave_table_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Leave Table",
                            tint = GudgudiGold,
                            modifier = Modifier.size(if (isLandscape) 18.dp else 20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    Column {
                        Text(
                            text = table.tableName,
                            color = GudgudiGold,
                            fontSize = if (isLandscape) 12.5.sp else 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (table.tierCoins == 0L) "Free Practice (30s Gap)" else "${table.tierCoins} Coins Bazi • 15% Admin Fee",
                            color = Color(0xFF86EFAC),
                            fontSize = if (isLandscape) 8.5.sp else 9.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // In Landscape: Display the status ribbon right in the top bar to maximize table space
                if (isLandscape) {
                    StatusRibbonContent(
                        state = table.state,
                        countdownSeconds = table.countdownSeconds,
                        winningNumber = table.winningNumber,
                        isWin = isWin,
                        tierCoins = table.tierCoins,
                        onRepeatVoice = {
                            val winNum = table.winningNumber ?: 6
                            val rewardCoins = table.lastSettlement?.playerResults?.firstOrNull { it.isUser }?.winAmount ?: 0L
                            voiceManager.playApplauseSound()
                            voiceManager.speakDiceResultWithAccounting(winNum, isWin, rewardCoins)
                        },
                        isLandscape = true
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Hindi Voice Mute / Unmute Button
                    IconButton(
                        onClick = {
                            isVoiceMuted = !isVoiceMuted
                            voiceManager.isMuted = isVoiceMuted
                            Toast.makeText(
                                context,
                                if (isVoiceMuted) "Hindi Voice Muted 🔇" else "Hindi Voice Active 🔊",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.size(if (isLandscape) 30.dp else 34.dp).testTag("toggle_voice_btn")
                    ) {
                        Icon(
                            imageVector = if (isVoiceMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Hindi Voice Toggle",
                            tint = if (isVoiceMuted) Color.Gray else Color(0xFF39FF14),
                            modifier = Modifier.size(if (isLandscape) 18.dp else 20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    Surface(
                        color = GudgudiMaroonCard,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGoldAmber)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = GudgudiGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "%,d".format(wallet.availableCoins),
                                color = GudgudiGoldLight,
                                fontSize = if (isLandscape) 9.5.sp else 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    Surface(
                        color = GudgudiMaroonCard,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = table.roundId,
                            color = GudgudiTextMuted,
                            fontSize = if (isLandscape) 8.sp else 8.5.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.5.dp)
                        )
                    }
                }
            }
        },
        containerColor = GudgudiMaroonDark
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLandscape) {
                // ==========================================
                // HORIZONTAL (LANDSCAPE) VIEWPORT ADAPTATION
                // Whole game fits the horizontal screen with zero scrolling!
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left & Center Table Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularGamingTable(
                            table = table,
                            modifier = Modifier.fillMaxWidth(),
                            isLandscape = true,
                            onVoiceResultClick = {
                                val winNum = table.winningNumber ?: 6
                                val rewardCoins = table.lastSettlement?.playerResults?.firstOrNull { it.isUser }?.winAmount ?: 0L
                                voiceManager.playApplauseSound()
                                voiceManager.speakDiceResultWithAccounting(winNum, isWin, rewardCoins)
                            }
                        )

                        // FLASH MODE Overlay when rolling
                        if (table.state == GameRoundState.ROLLING) {
                            FlashModeDiceBanner(isLandscape = true)
                        }
                    }

                    // Right-Side Fixed Keypad & Controls Dock
                    Card(
                        modifier = Modifier
                            .width(190.dp)
                            .fillMaxHeight()
                            .padding(end = 6.dp, top = 2.dp, bottom = 4.dp)
                            .testTag("number_selection_panel"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, GudgudiGoldDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "LUCKY NUMBER",
                                    color = GudgudiGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = if (table.tierCoins > 0) "85% POOL (15% ADMIN FEE)" else "PRACTICE WIN",
                                    color = Color(0xFF39FF14),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 2x3 Grid of 6 Number Buttons [1 2 3] and [4 5 6]
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (num in 1..3) {
                                        NumberSelectButton(
                                            number = num,
                                            userSelectedNumber = userSelectedNumber,
                                            roundState = table.state,
                                            winningNumber = table.winningNumber,
                                            isLandscape = true,
                                            onSelect = {
                                                repository.selectUserNumber(num)
                                                voiceManager.speakDiceNumberSelected(num)
                                            }
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (num in 4..6) {
                                        NumberSelectButton(
                                            number = num,
                                            userSelectedNumber = userSelectedNumber,
                                            roundState = table.state,
                                            winningNumber = table.winningNumber,
                                            isLandscape = true,
                                            onSelect = {
                                                repository.selectUserNumber(num)
                                                voiceManager.speakDiceNumberSelected(num)
                                            }
                                        )
                                    }
                                }
                            }

                            // Selected status text
                            Text(
                                text = if (userSelectedNumber != null) "🎯 Chuna: #$userSelectedNumber" else "Tap 1-6 above",
                                color = if (userSelectedNumber != null) GudgudiGold else GudgudiTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            // Quick actions: Replay result & Hisab sheet
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        val winNum = table.winningNumber ?: 6
                                        val rewardCoins = table.lastSettlement?.playerResults?.firstOrNull { it.isUser }?.winAmount ?: 0L
                                        voiceManager.playApplauseSound()
                                        voiceManager.speakDiceResultWithAccounting(winNum, isWin, rewardCoins)
                                    },
                                    color = GudgudiMaroonSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGoldAmber),
                                    modifier = Modifier.weight(1f).height(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "🔊 Pansa",
                                            color = GudgudiGold,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (table.lastSettlement != null) {
                                    Surface(
                                        onClick = { showSettlementDialog = true },
                                        color = GudgudiEmeraldDark,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF39FF14)),
                                        modifier = Modifier.weight(1f).height(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "📊 Hisab",
                                                color = Color.White,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // VERTICAL (PORTRAIT) VIEWPORT ADAPTATION
                // Locked screen dimensions with zero scrolling
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. TOP STATUS RIBBON
                    StatusRibbonContent(
                        state = table.state,
                        countdownSeconds = table.countdownSeconds,
                        winningNumber = table.winningNumber,
                        isWin = isWin,
                        tierCoins = table.tierCoins,
                        onRepeatVoice = {
                            val winNum = table.winningNumber ?: 6
                            val rewardCoins = table.lastSettlement?.playerResults?.firstOrNull { it.isUser }?.winAmount ?: 0L
                            voiceManager.playApplauseSound()
                            voiceManager.speakDiceResultWithAccounting(winNum, isWin, rewardCoins)
                        },
                        isLandscape = false
                    )

                    // 2. MIDDLE TABLE SECTION (Weighted 1f, Fixed Centered)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularGamingTable(
                            table = table,
                            modifier = Modifier.fillMaxWidth(),
                            isLandscape = false,
                            onVoiceResultClick = {
                                val winNum = table.winningNumber ?: 6
                                val rewardCoins = table.lastSettlement?.playerResults?.firstOrNull { it.isUser }?.winAmount ?: 0L
                                voiceManager.playApplauseSound()
                                voiceManager.speakDiceResultWithAccounting(winNum, isWin, rewardCoins)
                            }
                        )

                        // FLASH MODE Overlay when rolling
                        if (table.state == GameRoundState.ROLLING) {
                            FlashModeDiceBanner(isLandscape = false)
                        }
                    }

                    // 3. BOTTOM KEYPAD & CONTROLS PANEL (Fixed at Screen Bottom)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("number_selection_panel"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, GudgudiGoldDark)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "LUCKY NUMBER",
                                            color = GudgudiGold,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (table.tierCoins > 0) "(${table.tierCoins} Coins)" else "(Free)",
                                            color = Color(0xFF86EFAC),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        color = GudgudiMaroonSurface,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF39FF14))
                                    ) {
                                        Text(
                                            text = if (table.tierCoins == 0L) "Practice Win" else "85% POOL • 15% ADMIN FEE",
                                            color = Color(0xFF39FF14),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // 6 Large Number Selection Buttons [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ] [ 6 ]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (num in 1..6) {
                                        NumberSelectButton(
                                            number = num,
                                            userSelectedNumber = userSelectedNumber,
                                            roundState = table.state,
                                            winningNumber = table.winningNumber,
                                            isLandscape = false,
                                            onSelect = {
                                                repository.selectUserNumber(num)
                                                voiceManager.speakDiceNumberSelected(num)
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Selected number confirmation text
                                if (userSelectedNumber != null) {
                                    Text(
                                        text = "🎯 Aapne chuna hai: Number #$userSelectedNumber",
                                        color = GudgudiGold,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Bazi lagane ke liye 1 se 6 number dabayein (30s Gap)",
                                        color = GudgudiTextSecondary,
                                        fontSize = 9.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Hindi Quick Voice Action Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val cheers = listOf(
                                "🔊 Pansa Result" to {
                                    val winNum = table.winningNumber ?: 6
                                    val rewardCoins = table.lastSettlement?.playerResults?.firstOrNull { it.isUser }?.winAmount ?: 0L
                                    voiceManager.playApplauseSound()
                                    voiceManager.speakDiceResultWithAccounting(winNum, isWin, rewardCoins)
                                },
                                "👏 Taaliyan" to {
                                    voiceManager.speakCheer("applause")
                                },
                                "🎲 Gudgudi" to {
                                    voiceManager.speakCheer("gudgudi")
                                },
                                "📊 Hisab Screen" to {
                                    showSettlementDialog = true
                                }
                            )

                            cheers.forEach { (label, action) ->
                                Surface(
                                    onClick = action,
                                    color = GudgudiMaroonCard,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiMaroonBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        color = GudgudiGoldLight,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }

            // ==========================================
            // END OF GAME ROUND SETTLEMENT & 15% HISAB SCREEN
            // ==========================================
            if (showSettlementDialog && table.lastSettlement != null) {
                RoundSettlementDialog(
                    settlement = table.lastSettlement!!,
                    countdownSeconds = table.countdownSeconds,
                    onPlayApplause = {
                        voiceManager.playApplauseSound()
                    },
                    onDismiss = {
                        showSettlementDialog = false
                    }
                )
            }
        }
    }
}

/**
 * Flash Mode banner with neon glowing dice logo when round starts/rolls.
 */
@Composable
private fun FlashModeDiceBanner(isLandscape: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "flash_mode")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val neonGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonGlow"
    )

    val diceRotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "diceRot"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isLandscape) 40.dp else 24.dp)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xEE090306)),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF39FF14).copy(alpha = neonGlowAlpha),
                        GudgudiGold,
                        Color(0xFF00E5FF).copy(alpha = neonGlowAlpha)
                    )
                )
            ),
            modifier = Modifier.shadow(16.dp, RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = if (isLandscape) 4.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Flash Mode Dice Logo Icon
                Box(
                    modifier = Modifier
                        .size(if (isLandscape) 36.dp else 46.dp)
                        .rotate(diceRotation)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(GudgudiGold, GudgudiCrimson, GudgudiMaroonDark)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(1.5.dp, Color(0xFF39FF14), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎲",
                        fontSize = if (isLandscape) 20.sp else 26.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡ FLASH MODE ⚡",
                            color = Color(0xFF39FF14),
                            fontSize = if (isLandscape) 11.sp else 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "PANSA CHALU • GUDGUDI DICE ROLLING LIVE!",
                        color = GudgudiGoldLight,
                        fontSize = if (isLandscape) 9.sp else 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * End-of-Game Screen: All players' results (Won/Lost) + 15% Admin Commission Accounting.
 */
@Composable
private fun RoundSettlementDialog(
    settlement: RoundSettlement,
    countdownSeconds: Int,
    onPlayApplause: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .shadow(24.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
            border = androidx.compose.foundation.BorderStroke(2.dp, GudgudiGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // 1. Header Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🏆 KHEL KHATAM • ROUND RESULT",
                            color = GudgudiGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${settlement.tableName} (${settlement.roundId})",
                            color = GudgudiTextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    // Winning Number Badge
                    Surface(
                        color = Color(0xFF0F3D24),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF39FF14))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🎲", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PANSA #${settlement.winningNumber}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. 15% Admin Commission & Pot Accounting Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GudgudiMaroonSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGoldDark)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "💰 ROUND KA PURA HISAB (15% ADMIN FEE)",
                            color = GudgudiGoldLight,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Kul Table Bazi (Pot):", color = GudgudiTextSecondary, fontSize = 10.sp)
                                Text("%,d Coins".format(settlement.totalPot), color = GudgudiGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("15% Admin Fee:", color = Color(0xFFFCA5A5), fontSize = 10.sp)
                                Text("-%,d Coins".format(settlement.adminCommission15Percent), color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("85% Net Winner Pool:", color = Color(0xFF86EFAC), fontSize = 10.sp)
                                Text("+%,d Coins".format(settlement.netPrizePool85Percent), color = Color(0xFF39FF14), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Scoreboard List: All 6 Players (Who Won, Who Lost, Net Coins)
                Text(
                    text = "SABHI 6 KHILARIYON KA RESULT:",
                    color = GudgudiGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(settlement.playerResults) { player ->
                        val isWinPlayer = player.isWinner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isWinPlayer) Color(0xFF133824) else Color(0xFF220A17)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isWinPlayer) Color(0xFF39FF14) else GudgudiMaroonBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Avatar Initial
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isWinPlayer) Color(0xFF15803D) else GudgudiMaroonCard
                                            )
                                            .border(1.dp, if (isWinPlayer) Color(0xFF39FF14) else GudgudiGoldDark, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = player.playerName.take(1).uppercase(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = player.playerName,
                                                color = if (player.isUser) GudgudiGold else Color.White,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (player.isUser) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Surface(
                                                    color = GudgudiCrimson,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "YOU",
                                                        color = GudgudiGoldLight,
                                                        fontSize = 7.5.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "Chuna Number: #${player.chosenNumber ?: "-"} • Bazi: ${player.betAmount}",
                                            color = GudgudiTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    if (isWinPlayer) {
                                        Text(
                                            text = "JEET GAYE 🎉",
                                            color = Color(0xFF39FF14),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = "+%,d Coins".format(player.winAmount),
                                            color = Color(0xFF86EFAC),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = "HAAR GAYE ❌",
                                            color = Color(0xFFEF4444),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "-%,d Coins".format(player.betAmount),
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Action Row: Play Clapping Sound & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPlayApplause,
                        colors = ButtonDefaults.buttonColors(containerColor = GudgudiMaroonSurface),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, GudgudiGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("👏 Taaliyan", color = GudgudiGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = GudgudiGoldAmber),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Agla Round (${countdownSeconds}s)", color = GudgudiMaroonDark, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberSelectButton(
    number: Int,
    userSelectedNumber: Int?,
    roundState: GameRoundState,
    winningNumber: Int?,
    isLandscape: Boolean,
    onSelect: () -> Unit
) {
    val isSelected = (userSelectedNumber == number)
    val isWinningPip = (roundState == GameRoundState.RESULT && winningNumber == number)

    val btnWidth = if (isLandscape) 48.dp else 44.dp
    val btnHeight = if (isLandscape) 36.dp else 44.dp

    Box(
        modifier = Modifier
            .size(width = btnWidth, height = btnHeight)
            .clip(RoundedCornerShape(9.dp))
            .background(
                brush = when {
                    isWinningPip -> Brush.linearGradient(listOf(Color(0xFF39FF14), GudgudiEmerald))
                    isSelected -> Brush.linearGradient(listOf(GudgudiGold, GudgudiGoldAmber))
                    else -> Brush.linearGradient(listOf(GudgudiMaroonSurface, GudgudiMaroonDark))
                }
            )
            .border(
                width = if (isSelected || isWinningPip) 2.dp else 1.dp,
                color = when {
                    isWinningPip -> Color.White
                    isSelected -> GudgudiGoldLight
                    else -> GudgudiMaroonBorder
                },
                shape = RoundedCornerShape(9.dp)
            )
            .clickable(enabled = roundState == GameRoundState.BETTING) {
                onSelect()
            }
            .testTag("number_btn_$number"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$number",
            color = when {
                isWinningPip -> Color.Black
                isSelected -> GudgudiMaroonDark
                else -> GudgudiTextPrimary
            },
            fontSize = if (isLandscape) 15.sp else 17.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun StatusRibbonContent(
    state: GameRoundState,
    countdownSeconds: Int,
    winningNumber: Int?,
    isWin: Boolean,
    tierCoins: Long,
    onRepeatVoice: () -> Unit,
    isLandscape: Boolean
) {
    AnimatedContent(
        targetState = state,
        label = "banner_state"
    ) { currentRoundState ->
        Box(
            modifier = Modifier
                .then(if (isLandscape) Modifier.wrapContentWidth() else Modifier.fillMaxWidth())
                .background(
                    when (currentRoundState) {
                        GameRoundState.BETTING -> Brush.horizontalGradient(
                            listOf(Color(0xFF0F2E1E), Color(0xFF1B4D35), Color(0xFF0F2E1E))
                        )
                        GameRoundState.ROLLING -> Brush.horizontalGradient(
                            listOf(GudgudiCrimson, Color(0xFF6B1224), GudgudiCrimson)
                        )
                        GameRoundState.RESULT -> if (isWin) Brush.horizontalGradient(
                            listOf(GudgudiEmeraldDark, Color(0xFF15803D), GudgudiEmeraldDark)
                        ) else Brush.horizontalGradient(
                            listOf(GudgudiMaroonCard, GudgudiCrimson, GudgudiMaroonCard)
                        )
                        GameRoundState.WAITING -> Brush.horizontalGradient(
                            listOf(GudgudiMaroonCard, GudgudiMaroonDark)
                        )
                    },
                    shape = if (isLandscape) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                )
                .clickable {
                    if (state == GameRoundState.RESULT) {
                        onRepeatVoice()
                    }
                }
                .padding(vertical = if (isLandscape) 2.5.dp else 5.dp, horizontal = if (isLandscape) 8.dp else 12.dp),
            contentAlignment = Alignment.Center
        ) {
            when (currentRoundState) {
                GameRoundState.BETTING -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFF39FF14),
                            modifier = Modifier.size(if (isLandscape) 11.dp else 13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "APNA NUMBER CHUNIYE (1 - 6) • ${countdownSeconds}s BAKI",
                            color = Color(0xFF86EFAC),
                            fontSize = if (isLandscape) 9.sp else 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                GameRoundState.ROLLING -> {
                    Text(
                        text = "⚡ FLASH MODE: DICE GHOOM RAHA HAI...",
                        color = GudgudiGoldLight,
                        fontSize = if (isLandscape) 9.5.sp else 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
                GameRoundState.RESULT -> {
                    val winNum = winningNumber ?: 6
                    val rewardText = if (tierCoins > 0) "+85% POOL JEETE!" else "VICTORY!"
                    Text(
                        text = if (isWin) "🎉 AAP JEET GAYE! PANSA: #$winNum ($rewardText) 🔊" else "⚔️ PANSA NUMBER: #$winNum (${countdownSeconds}s Baki) 🔊",
                        color = if (isWin) Color.White else GudgudiGoldLight,
                        fontSize = if (isLandscape) 9.5.sp else 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
                GameRoundState.WAITING -> {
                    Text(
                        text = "Khilari jud rahe hain...",
                        color = GudgudiTextSecondary,
                        fontSize = if (isLandscape) 8.5.sp else 10.sp
                    )
                }
            }
        }
    }
}
