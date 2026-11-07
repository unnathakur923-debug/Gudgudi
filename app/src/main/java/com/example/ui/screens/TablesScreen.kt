package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.model.GameRoundState
import com.example.model.GameTable
import com.example.ui.theme.*

@Composable
fun TablesScreen(
    repository: GameRepository,
    initialTier: Long = 5L,
    onJoinTable: (table: GameTable) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTier by remember { mutableLongStateOf(initialTier) }
    val tablesMap by repository.tables.collectAsState()
    val wallet by repository.walletState.collectAsState()

    val tablesForTier = tablesMap[selectedTier] ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GudgudiMaroonDark)
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GudgudiMaroonSurface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MULTIPLAYER TABLES",
                        color = GudgudiGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "6-Player Live Gudgudi Dice Darbar",
                        color = GudgudiTextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Balance Chip
                Surface(
                    color = GudgudiMaroonCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGoldAmber)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = GudgudiGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%,d".format(wallet.availableCoins),
                            color = GudgudiGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tier Selector Horizontal Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(repository.availableTiers) { tier ->
                    val isSelected = (tier == selectedTier)
                    val label = if (tier == 0L) "Practice (Free)" else "$tier Coins"

                    Surface(
                        onClick = { selectedTier = tier },
                        color = if (isSelected) GudgudiCrimson else GudgudiMaroonCard,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) GudgudiGold else GudgudiMaroonBorder
                        ),
                        modifier = Modifier.testTag("tier_tab_$tier")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) GudgudiGold else GudgudiTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Tables List for Selected Tier (10 tables)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTier == 0L) "Practice Arenas (10 Tables)" else "$selectedTier Coins Level (10 Tables)",
                        color = GudgudiGoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Auto-refreshed live",
                        color = GudgudiTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            items(tablesForTier) { table ->
                val canAfford = (selectedTier == 0L) || (wallet.availableCoins >= selectedTier)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("table_item_${table.tableId}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (table.isFull) GudgudiMaroonBorder else GudgudiGoldDark.copy(alpha = 0.5f)
                    )
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
                                    .size(46.dp)
                                    .background(GudgudiMaroonSurface, RoundedCornerShape(12.dp))
                                    .border(1.dp, GudgudiGoldDark, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = table.tableName.replace("Table ", "#"),
                                    color = GudgudiGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${table.occupiedSeatsCount}/6 Seats Filled",
                                        color = if (table.isFull) Color(0xFFEF4444) else GudgudiEmeraldLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "•",
                                        color = GudgudiTextMuted,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (table.state) {
                                            GameRoundState.BETTING -> "Rolling in ${table.countdownSeconds}s"
                                            GameRoundState.ROLLING -> "Rolling Dice..."
                                            GameRoundState.RESULT -> "Round Result"
                                            GameRoundState.WAITING -> "Waiting"
                                        },
                                        color = GudgudiGoldAmber,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "Round: ${table.roundId}  |  Pot: ${table.tierCoins * 6} Coins",
                                    color = GudgudiTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (!canAfford) {
                                    Toast.makeText(
                                        context,
                                        "Insufficient coins! Claim daily bonus or free faucet.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val ok = repository.joinTable(table)
                                    if (ok) {
                                        onJoinTable(table)
                                    } else {
                                        Toast.makeText(context, "Cannot join table right now", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) GudgudiCrimson else Color(0xFF333333)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (canAfford) GudgudiGoldAmber else Color.Gray
                            ),
                            modifier = Modifier.testTag("join_btn_${table.tableId}")
                        ) {
                            Text(
                                text = if (canAfford) "Join" else "Locked",
                                color = if (canAfford) GudgudiGold else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
