package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.model.TransactionItem
import com.example.model.TransactionType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WalletScreen(
    repository: GameRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wallet by repository.walletState.collectAsState()
    val transactions by repository.transactions.collectAsState()

    val dayRewards = listOf(500L, 750L, 1000L, 1500L, 2000L, 3000L, 5000L)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GudgudiMaroonDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Screen Title
        item {
            Column {
                Text(
                    text = "ROYAL VIRTUAL WALLET",
                    color = GudgudiGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Internal Game Points & Rewards",
                    color = GudgudiTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // 2. Large Balance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_balance_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GudgudiGoldAmber)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(GudgudiCrimson.copy(alpha = 0.6f), GudgudiMaroonCard)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "AVAILABLE VIRTUAL BALANCE",
                            color = GudgudiGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coin",
                                tint = GudgudiGold,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "%,d".format(wallet.availableCoins),
                                color = GudgudiGold,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Text(
                            text = "Non-redeemable game coins",
                            color = GudgudiTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Faucet Quick Claim
                        Button(
                            onClick = {
                                val coins = repository.claimFreeFaucet()
                                Toast.makeText(context, "+$coins Free Faucet Coins Credited!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GudgudiGoldAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("claim_faucet_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = GudgudiMaroonDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CLAIM FREE 250 COINS NOW",
                                color = GudgudiMaroonDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. 7-Day Login Streak Rewards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GudgudiMaroonSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiMaroonBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Login Streak (Day ${wallet.streakDays}/7)",
                            color = GudgudiGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                val ok = repository.claimDailyBonus()
                                if (ok) {
                                    Toast.makeText(context, "Streak Bonus Claimed!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Already claimed for today!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GudgudiCrimson),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Claim",
                                color = GudgudiGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dayRewards.size) { index ->
                            val dayNum = index + 1
                            val coins = dayRewards[index]
                            val isReached = (dayNum <= wallet.streakDays)
                            val isCurrent = (dayNum == wallet.streakDays)

                            Card(
                                modifier = Modifier.width(72.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) GudgudiCrimson else GudgudiMaroonCard
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrent) GudgudiGold else GudgudiMaroonBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Day $dayNum",
                                        color = if (isCurrent) GudgudiGoldLight else GudgudiTextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Icon(
                                        imageVector = if (isReached) Icons.Default.CheckCircle else Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = if (isReached) GudgudiEmeraldLight else GudgudiGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+$coins",
                                        color = GudgudiGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Important Policy Notice
        item {
            Surface(
                color = GudgudiMaroonCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiGoldDark.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = GudgudiGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Virtual coins are for entertainment only. They cannot be purchased, transferred, redeemed, or withdrawn for real cash.",
                        color = GudgudiTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 5. Transaction History Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History",
                    color = GudgudiGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${transactions.size} records",
                    color = GudgudiTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // 6. Transaction Items
        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "No transactions recorded yet.",
                    color = GudgudiTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(transactions) { tx ->
                TransactionRowCard(tx = tx)
            }
        }
    }
}

@Composable
private fun TransactionRowCard(tx: TransactionItem) {
    val isCredit = tx.amount >= 0
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(tx.timestamp) { dateFormat.format(Date(tx.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_${tx.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GudgudiMaroonCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, GudgudiMaroonBorder)
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
                        .size(38.dp)
                        .background(
                            if (isCredit) GudgudiEmeraldDark.copy(alpha = 0.5f) else Color(0xFF4A1010),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isCredit) GudgudiEmeraldLight else Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = tx.title,
                        color = GudgudiTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$formattedDate • Bal: %,d".format(tx.balanceAfter),
                        color = GudgudiTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = "${if (isCredit) "+" else ""}${tx.amount} Coins",
                color = if (isCredit) GudgudiEmeraldLight else Color(0xFFEF4444),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
