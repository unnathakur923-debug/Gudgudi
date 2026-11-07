package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.GameRepository
import com.example.model.AppNotification
import com.example.model.GameHistoryRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    repository: GameRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by repository.currentUser.collectAsState()
    val wallet by repository.walletState.collectAsState()
    val history by repository.gameHistory.collectAsState()
    val notifications by repository.notifications.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview/Stats, 1: Leaderboard, 2: History, 3: Support
    var showSupportDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GudgudiMaroonDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Royal Profile Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GudgudiGoldAmber)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(GudgudiCrimson.copy(alpha = 0.5f), GudgudiMaroonCard)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.dp, GudgudiGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_gudgudi_emblem),
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = user?.fullName ?: "Gudgudi Warrior",
                            color = GudgudiGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "${user?.title ?: "Gudgudi Yoddha"} • ${user?.district}, ${user?.state}",
                            color = GudgudiGoldLight,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = GudgudiEmeraldLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verified Darbar Player",
                                color = GudgudiEmeraldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Contact info pill
                        Surface(
                            color = GudgudiMaroonSurface,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${user?.mobileNumber}  |  ${user?.email}",
                                color = GudgudiTextSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Tab Navigation
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GudgudiMaroonSurface, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                val tabs = listOf("Overview", "Champions", "History", "Support")
                tabs.forEachIndexed { index, title ->
                    val isSelected = (selectedTab == index)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) GudgudiCrimson else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) GudgudiGold else GudgudiTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 3. Tab Contents
        when (selectedTab) {
            0 -> {
                // Overview & Stats
                item {
                    Text(
                        text = "Lifetime Gudgudi Battle Stats",
                        color = GudgudiGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatsDetailRow("Total Games Played", "${wallet.totalGamesPlayed}")
                        StatsDetailRow("Total Games Won", "${wallet.totalGamesWon}", GudgudiEmeraldLight)
                        StatsDetailRow("Total Games Lost", "${wallet.totalGamesLost}", Color(0xFFEF4444))
                        val winRatio = if (wallet.totalGamesPlayed > 0) {
                            (wallet.totalGamesWon * 100) / wallet.totalGamesPlayed
                        } else 0
                        StatsDetailRow("Win Ratio", "$winRatio%", GudgudiGold)
                        StatsDetailRow("Highest Single Win", "${wallet.highestWin} Coins", GudgudiGoldAmber)
                    }
                }

                item {
                    Text(
                        text = "Notifications Inbox",
                        color = GudgudiGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(notifications) { notif ->
                    NotificationCard(notif = notif)
                }

                item {
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("logout_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E1218)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7F1D1D)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Logout From All Devices",
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            1 -> {
                // Leaderboard
                item {
                    Text(
                        text = "🏆 Gudgudi Champions Leaderboard",
                        color = GudgudiGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(repository.leaderboard) { champ ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (champ.rank <= 3) GudgudiGold else GudgudiMaroonBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            when (champ.rank) {
                                                1 -> GudgudiGold
                                                2 -> Color(0xFFD1D5DB)
                                                3 -> Color(0xFFB45309)
                                                else -> GudgudiMaroonSurface
                                            },
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${champ.rank}",
                                        color = if (champ.rank <= 3) GudgudiMaroonDark else Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = champ.name,
                                        color = GudgudiTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${champ.title} • ${champ.state}",
                                        color = GudgudiGoldLight,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "%,d Coins".format(champ.totalCoinsWon),
                                    color = GudgudiGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${champ.winsCount} Wins",
                                    color = GudgudiEmeraldLight,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // Game History
                if (history.isEmpty()) {
                    item {
                        Text(
                            text = "No games played yet. Join any table to begin your battle history!",
                            color = GudgudiTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(history) { rec ->
                        GameHistoryRowCard(rec = rec)
                    }
                }
            }
            3 -> {
                // Support & FAQs
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Help & Gudgudi Darbar Support",
                                color = GudgudiGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "For any inquiries regarding tables, virtual bonuses, or account queries, our 24/7 gaming support team is here to assist.",
                                color = GudgudiTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showSupportDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GudgudiGoldAmber),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("open_support_dialog_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = GudgudiMaroonDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SUBMIT SUPPORT TICKET",
                                    color = GudgudiMaroonDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Frequently Asked Questions",
                        color = GudgudiGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    FaqItem(
                        question = "Are virtual coins redeemable for real cash?",
                        answer = "No. Gudgudi Online Dice Gaming is 100% free-to-play. Virtual coins have zero monetary value, cannot be redeemed, deposited, or withdrawn."
                    )
                }

                item {
                    FaqItem(
                        question = "How are dice results generated?",
                        answer = "Dice results are computed using an unbiased cryptographic server-side random algorithm, ensuring fair and transparent odds for every player."
                    )
                }

                item {
                    FaqItem(
                        question = "How do I get more free coins?",
                        answer = "You can claim daily login bonuses, maintain your 7-day attendance streak, or use the Darbar Faucet button in your wallet anytime."
                    )
                }
            }
        }
    }

    if (showSupportDialog) {
        var subject by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Submit Support Ticket", color = GudgudiGold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        colors = royalTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Describe your issue...") },
                        colors = royalTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Support Ticket #TK-9821 lodged successfully!", Toast.LENGTH_LONG).show()
                        showSupportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GudgudiGoldAmber)
                ) {
                    Text("Submit Ticket", color = GudgudiMaroonDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text("Cancel", color = GudgudiTextSecondary)
                }
            },
            containerColor = GudgudiMaroonCard
        )
    }
}

@Composable
private fun StatsDetailRow(label: String, value: String, valueColor: Color = GudgudiTextPrimary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = GudgudiTextSecondary, fontSize = 12.sp)
            Text(text = value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NotificationCard(notif: AppNotification) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formatted = remember(notif.timestamp) { dateFormat.format(Date(notif.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiMaroonBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notif.title,
                    color = GudgudiGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatted,
                    color = GudgudiTextMuted,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notif.message,
                color = GudgudiTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun GameHistoryRowCard(rec: GameHistoryRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (rec.isWin) GudgudiEmeraldDark else GudgudiMaroonBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${rec.tableName} • Round ${rec.roundId}",
                    color = GudgudiTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your Pick: #${rec.selectedNumber}  |  Winning Dice: #${rec.winningNumber}",
                    color = GudgudiGoldLight,
                    fontSize = 11.sp
                )
            }

            Surface(
                color = if (rec.isWin) GudgudiEmeraldDark else Color(0xFF4A1010),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (rec.isWin) "+${rec.coinChange} Coins" else "${rec.coinChange} Coins",
                    color = if (rec.isWin) GudgudiEmeraldLight else Color(0xFFFCA5A5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Q: $question", color = GudgudiGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = answer, color = GudgudiTextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}
