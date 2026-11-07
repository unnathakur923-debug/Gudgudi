package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class AppNavTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    TABLES("Tables", Icons.Default.Casino),
    WALLET("Wallet", Icons.Default.AccountBalanceWallet),
    PROFILE("Profile", Icons.Default.Person),
    ADMIN("Admin", Icons.Default.AdminPanelSettings)
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: GameRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = GameRepository(applicationContext)

        setContent {
            GudgudiGamingTheme {
                GudgudiDiceApp(repository = repository)
            }
        }
    }
}

@Composable
fun GudgudiDiceApp(repository: GameRepository) {
    val currentUser by repository.currentUser.collectAsState()
    val activeTable by repository.activeTable.collectAsState()

    var isAuthenticated by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(AppNavTab.HOME) }
    var selectedTierForTables by remember { mutableLongStateOf(5L) }

    // If active game table is open, show the 6-player GameRoomScreen
    if (activeTable != null) {
        GameRoomScreen(
            repository = repository,
            onLeaveTable = {
                // Return to tables
            }
        )
        return
    }

    if (!isAuthenticated) {
        AuthScreen(
            repository = repository,
            onAuthSuccess = {
                isAuthenticated = true
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = GudgudiMaroonSurface,
                contentColor = GudgudiGold,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = GudgudiMaroonBorder,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .testTag("bottom_nav_bar")
            ) {
                AppNavTab.values().forEach { tab ->
                    val isSelected = (currentTab == tab)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) GudgudiGold else GudgudiTextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) GudgudiGold else GudgudiTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = GudgudiCrimson,
                            selectedIconColor = GudgudiGold,
                            unselectedIconColor = GudgudiTextSecondary,
                            selectedTextColor = GudgudiGold,
                            unselectedTextColor = GudgudiTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = GudgudiMaroonDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.HOME -> {
                    HomeScreen(
                        repository = repository,
                        onNavigateToTables = { tier ->
                            selectedTierForTables = tier
                            currentTab = AppNavTab.TABLES
                        },
                        onNavigateToWallet = {
                            currentTab = AppNavTab.WALLET
                        },
                        onJoinTableDirectly = { table ->
                            repository.joinTable(table)
                        }
                    )
                }
                AppNavTab.TABLES -> {
                    BackHandler {
                        currentTab = AppNavTab.HOME
                    }
                    TablesScreen(
                        repository = repository,
                        initialTier = selectedTierForTables,
                        onJoinTable = { table ->
                            // Handled by activeTable state flow
                        }
                    )
                }
                AppNavTab.WALLET -> {
                    BackHandler {
                        currentTab = AppNavTab.HOME
                    }
                    WalletScreen(repository = repository)
                }
                AppNavTab.PROFILE -> {
                    BackHandler {
                        currentTab = AppNavTab.HOME
                    }
                    ProfileScreen(
                        repository = repository,
                        onLogout = {
                            isAuthenticated = false
                        }
                    )
                }
                AppNavTab.ADMIN -> {
                    BackHandler {
                        currentTab = AppNavTab.HOME
                    }
                    AdminScreen(repository = repository)
                }
            }
        }
    }
}
