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
import androidx.compose.ui.draw.shadow
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
import com.example.model.GameTable
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    repository: GameRepository,
    onNavigateToTables: (tier: Long) -> Unit,
    onNavigateToWallet: () -> Unit,
    onJoinTableDirectly: (table: GameTable) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by repository.currentUser.collectAsState()
    val wallet by repository.walletState.collectAsState()
    val tablesMap by repository.tables.collectAsState()

    // Grab first few tables from 5-coin or 10-coin tier for the dashboard preview
    val previewTables = (tablesMap[5L] ?: emptyList()).take(4)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GudgudiMaroonDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Royal Header with Gudgudi Title & Online Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.dp, GudgudiGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_gudgudi_emblem),
                            contentDescription = "Gudgudi Gaming",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "GUDGUDI GAMING",
                            color = GudgudiGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(GudgudiEmerald, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "ONLINE",
                                color = GudgudiEmeraldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ${user?.fullName ?: "Gudgudi Warrior"}",
                                color = GudgudiTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Title badge
                Surface(
                    color = GudgudiMaroonCard,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGoldDark)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = GudgudiGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user?.title ?: "Yoddha",
                            color = GudgudiGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. MY VIRTUAL WALLET CARD (Hero Dashboard Card)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_dashboard_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GudgudiGoldDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    GudgudiCrimson.copy(alpha = 0.85f),
                                    GudgudiMaroonCard,
                                    Color(0xFF3B1228)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MY VIRTUAL WALLET",
                                color = GudgudiGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                color = Color(0x33000000),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "FREE TO PLAY",
                                    color = GudgudiEmeraldLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Coins Balance
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = GudgudiGold,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "%,d".format(wallet.availableCoins),
                                color = GudgudiGold,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Coins",
                                color = GudgudiGoldLight,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Mandatory Policy Disclaimer
                        Text(
                            text = "Non-redeemable game points",
                            color = GudgudiTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action buttons row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val success = repository.claimDailyBonus()
                                    if (success) {
                                        Toast.makeText(context, "Daily Bonus Claimed! +Coins", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Already claimed today! Come back tomorrow.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("claim_daily_bonus_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = GudgudiGoldAmber),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = GudgudiMaroonDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Daily Bonus",
                                    color = GudgudiMaroonDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    val coins = repository.claimFreeFaucet()
                                    Toast.makeText(context, "+$coins Free Faucet Coins Credited!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("claim_free_coins_btn"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = GudgudiMaroonSurface,
                                    contentColor = GudgudiGold
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGoldAmber),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = GudgudiGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Free Coins",
                                    color = GudgudiGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = onNavigateToWallet,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(GudgudiMaroonSurface, RoundedCornerShape(10.dp))
                                    .border(1.dp, GudgudiGoldDark, RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Wallet History",
                                    tint = GudgudiGoldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Select Game Mode (Practice & Coin Game)
        item {
            Column {
                Text(
                    text = "Select Game Mode",
                    color = GudgudiGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Practice Game Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .testTag("practice_game_card")
                            .clickable { onNavigateToTables(0L) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGoldDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF2A4365), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = GudgudiGoldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Practice Game",
                                    color = GudgudiTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Free Play • 0 Coins",
                                    color = GudgudiEmeraldLight,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Coin Game Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .testTag("coin_game_card")
                            .clickable { onNavigateToTables(10L) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GudgudiGoldAmber)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(GudgudiCrimson.copy(alpha = 0.4f), Color.Transparent)
                                    )
                                )
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(GudgudiCrimson, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = null,
                                    tint = GudgudiGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Coin Game",
                                    color = GudgudiGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Virtual Coins • 5x Wins",
                                    color = GudgudiGoldLight,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Quick Battle Stats Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GudgudiMaroonSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiMaroonBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMetricItem(label = "Played", value = "${wallet.totalGamesPlayed}")
                    StatMetricItem(label = "Won", value = "${wallet.totalGamesWon}", valueColor = GudgudiEmeraldLight)
                    StatMetricItem(label = "Lost", value = "${wallet.totalGamesLost}", valueColor = Color(0xFFEF4444))
                    val winRate = if (wallet.totalGamesPlayed > 0) {
                        (wallet.totalGamesWon * 100) / wallet.totalGamesPlayed
                    } else 0
                    StatMetricItem(label = "Win Rate", value = "$winRate%", valueColor = GudgudiGold)
                }
            }
        }

        // 5. Online Tables Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Online Tables",
                    color = GudgudiGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All Tiers →",
                    color = GudgudiGoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToTables(5L) }
                )
            }
        }

        // 6. Online Tables List (Table 01, Table 02, etc.)
        items(previewTables) { table ->
            TableListItemCard(
                table = table,
                onJoin = { onJoinTableDirectly(table) }
            )
        }
    }
}

@Composable
fun TableListItemCard(
    table: GameTable,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("table_card_${table.tableId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiMaroonBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(GudgudiMaroonSurface, RoundedCornerShape(10.dp))
                        .border(1.dp, GudgudiGoldDark, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = GudgudiGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = table.tableName,
                        color = GudgudiTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${table.occupiedSeatsCount}/6 Players Online",
                        color = GudgudiEmeraldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (table.tierCoins == 0L) "Free Play Practice" else "Waiting for players • ${table.tierCoins} Coins",
                        color = GudgudiTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onJoin,
                colors = ButtonDefaults.buttonColors(containerColor = GudgudiCrimson),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGoldAmber),
                modifier = Modifier.testTag("join_table_${table.tableId}")
            ) {
                Text(
                    text = "Join",
                    color = GudgudiGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StatMetricItem(
    label: String,
    value: String,
    valueColor: Color = GudgudiTextPrimary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = GudgudiTextSecondary,
            fontSize = 10.sp
        )
    }
}
