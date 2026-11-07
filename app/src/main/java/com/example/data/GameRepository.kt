package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class GameRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("gudgudi_dice_prefs", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Current logged in user
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // Wallet state
    private val _walletState = MutableStateFlow(WalletState())
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    // Admin Treasury (15% platform commission pool)
    private val _adminTreasuryCoins = MutableStateFlow(125400L)
    val adminTreasuryCoins: StateFlow<Long> = _adminTreasuryCoins.asStateFlow()

    // Transactions list
    private val _transactions = MutableStateFlow<List<TransactionItem>>(emptyList())
    val transactions: StateFlow<List<TransactionItem>> = _transactions.asStateFlow()

    // Game History
    private val _gameHistory = MutableStateFlow<List<GameHistoryRecord>>(emptyList())
    val gameHistory: StateFlow<List<GameHistoryRecord>> = _gameHistory.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Available Tables for each tier
    // Tiers: 0 (Practice), 5, 10, 25, 50, 100, 500, 1000, 2500, 5000
    val availableTiers = listOf(0L, 5L, 10L, 25L, 50L, 100L, 500L, 1000L, 2500L, 5000L)
    private val _tables = MutableStateFlow<Map<Long, List<GameTable>>>(emptyMap())
    val tables: StateFlow<Map<Long, List<GameTable>>> = _tables.asStateFlow()

    // Currently joined table by user
    private val _activeTable = MutableStateFlow<GameTable?>(null)
    val activeTable: StateFlow<GameTable?> = _activeTable.asStateFlow()

    // Registered users (for Admin panel)
    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()

    // Leaderboard
    val leaderboard = listOf(
        LeaderboardUser(1, "Maharaja Gajendra Singh", "Maharaja", "Jodhpur", 148500L, 214, 0),
        LeaderboardUser(2, "Kunwar Devavrat Rathore", "Senapati", "Udaipur", 112000L, 185, 1),
        LeaderboardUser(3, "Thakur Samarveer Bhati", "Durgadhyaksh", "Jaisalmer", 94500L, 142, 2),
        LeaderboardUser(4, "Rawat Digvijay Chauhan", "Khiladi", "Ajmer", 78200L, 120, 3),
        LeaderboardUser(5, "Banna Aditya Shekhawat", "Khiladi", "Jaipur", 62400L, 95, 4),
        LeaderboardUser(6, "Baisa Meera Kumari", "Ranbanka", "Chittorgarh", 58900L, 88, 5),
        LeaderboardUser(7, "Sardar Harshvardhan Tomar", "Senani", "Gwalior", 44300L, 71, 1),
        LeaderboardUser(8, "Banna Rudrapratap Sisodia", "Khiladi", "Bhilwara", 39500L, 64, 2)
    )

    private val playerNamesPool = listOf(
        "Kunwar Vikram", "Ranveer Rathore", "Samarveer Singh", "Prithvi Shekhawat",
        "Harshvardhan", "Digvijay Sisodia", "Banna Kuldeep", "Jaiveer Chauhan",
        "Udaybhan Bhati", "Fateh Singh", "Abhayraj Hada", "Bhanupratap"
    )

    private var tableTickerJob: Job? = null

    init {
        loadPersistedData()
        initializeTables()
        startTableTicker()
    }

    private fun loadPersistedData() {
        val hasUser = prefs.getBoolean("has_registered_user", false)
        if (hasUser) {
            val user = UserProfile(
                id = prefs.getString("user_id", "usr_101") ?: "usr_101",
                fullName = prefs.getString("user_name", "Kunwar Vikram Singh") ?: "Kunwar Vikram Singh",
                mobileNumber = prefs.getString("user_mobile", "+91 9876543210") ?: "+91 9876543210",
                email = prefs.getString("user_email", "vikram.singh@gudgudi.com") ?: "vikram.singh@gudgudi.com",
                dateOfBirth = prefs.getString("user_dob", "15/08/1998") ?: "15/08/1998",
                state = prefs.getString("user_state", "Rajasthan") ?: "Rajasthan",
                district = prefs.getString("user_district", "Jaipur") ?: "Jaipur",
                avatarIndex = prefs.getInt("user_avatar", 0),
                isBlocked = prefs.getBoolean("user_blocked", false)
            )
            _currentUser.value = user
        } else {
            // Default guest user with instant 5,000 Welcome coins
            val defaultUser = UserProfile()
            _currentUser.value = defaultUser
        }

        val coins = prefs.getLong("wallet_coins", 5000L)
        val played = prefs.getInt("stats_played", 12)
        val won = prefs.getInt("stats_won", 7)
        val lost = prefs.getInt("stats_lost", 5)
        val highest = prefs.getLong("stats_highest", 1250L)
        val adminTreasury = prefs.getLong("admin_treasury", 125400L)
        _adminTreasuryCoins.value = adminTreasury

        _walletState.value = WalletState(
            availableCoins = coins,
            totalGamesPlayed = played,
            totalGamesWon = won,
            totalGamesLost = lost,
            highestWin = highest
        )

        // Initial welcome transaction if empty
        _transactions.value = listOf(
            TransactionItem(
                id = "TX-1001",
                title = "Welcome Gudgudi Bonus",
                type = TransactionType.WELCOME_BONUS,
                amount = 5000L,
                balanceAfter = 5000L,
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            TransactionItem(
                id = "TX-1002",
                title = "Daily Attendance Bonus",
                type = TransactionType.DAILY_LOGIN,
                amount = 500L,
                balanceAfter = 5500L,
                timestamp = System.currentTimeMillis() - 43200000L
            )
        )

        // Sample initial notifications
        _notifications.value = listOf(
            AppNotification(
                id = "NOTIF-1",
                title = "Welcome to Gudgudi Online Gaming! 🎲",
                message = "Padharo Sa! Enjoy 5,000 free virtual welcome coins to test your luck on 6-player Gudgudi Dice tables.",
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            AppNotification(
                id = "NOTIF-2",
                title = "Daily Reward Available",
                message = "Claim your daily Gudgudi rewards in the wallet section.",
                timestamp = System.currentTimeMillis() - 3600000L
            )
        )

        _allUsers.value = listOf(
            _currentUser.value ?: UserProfile(),
            UserProfile(id = "usr_102", fullName = "Devavrat Rathore", mobileNumber = "+91 9822334455", email = "devavrat@gudgudi.com", state = "Rajasthan", district = "Udaipur", avatarIndex = 1),
            UserProfile(id = "usr_103", fullName = "Samarveer Bhati", mobileNumber = "+91 9711223344", email = "samar@gudgudi.com", state = "Rajasthan", district = "Jaisalmer", avatarIndex = 2),
            UserProfile(id = "usr_104", fullName = "Meera Kumari", mobileNumber = "+91 9655443322", email = "meera@gudgudi.com", state = "Rajasthan", district = "Chittorgarh", avatarIndex = 5),
            UserProfile(id = "usr_105", fullName = "Digvijay Chauhan", mobileNumber = "+91 9544332211", email = "digvijay@gudgudi.com", state = "Rajasthan", district = "Ajmer", avatarIndex = 3)
        )
    }

    private fun persistWallet() {
        val ws = _walletState.value
        prefs.edit()
            .putLong("wallet_coins", ws.availableCoins)
            .putInt("stats_played", ws.totalGamesPlayed)
            .putInt("stats_won", ws.totalGamesWon)
            .putInt("stats_lost", ws.totalGamesLost)
            .putLong("stats_highest", ws.highestWin)
            .putLong("admin_treasury", _adminTreasuryCoins.value)
            .apply()
    }

    private fun persistUser(user: UserProfile) {
        prefs.edit()
            .putBoolean("has_registered_user", true)
            .putString("user_id", user.id)
            .putString("user_name", user.fullName)
            .putString("user_mobile", user.mobileNumber)
            .putString("user_email", user.email)
            .putString("user_dob", user.dateOfBirth)
            .putString("user_state", user.state)
            .putString("user_district", user.district)
            .putInt("user_avatar", user.avatarIndex)
            .putBoolean("user_blocked", user.isBlocked)
            .apply()
    }

    // Initialize 10 tables per tier
    private fun initializeTables() {
        val tableMap = mutableMapOf<Long, List<GameTable>>()
        for (tier in availableTiers) {
            val list = mutableListOf<GameTable>()
            for (i in 1..10) {
                val tableId = (tier * 100 + i).toInt()
                val tableName = if (i < 10) "Table 0$i" else "Table $i"
                val botCount = Random.nextInt(2, 5) // 2 to 4 bots sitting already
                val seats = mutableListOf<SeatPlayer>()
                val shuffledNames = playerNamesPool.shuffled()
                for (s in 1..botCount) {
                    seats.add(
                        SeatPlayer(
                            seatNumber = s,
                            playerId = "bot_$s",
                            playerName = shuffledNames[s - 1],
                            avatarIndex = (s % 6),
                            selectedNumber = Random.nextInt(1, 7),
                            isUser = false
                        )
                    )
                }
                list.add(
                    GameTable(
                        tableId = tableId,
                        tableName = tableName,
                        tierCoins = tier,
                        seats = seats,
                        state = GameRoundState.BETTING,
                        roundId = "RND-${Random.nextInt(10000, 99999)}",
                        countdownSeconds = 30,
                        totalPot = tier * seats.size
                    )
                )
            }
            tableMap[tier] = list
        }
        _tables.value = tableMap
    }

    // Ticker engine for real-time table countdown & dice rolling
    private fun startTableTicker() {
        tableTickerJob?.cancel()
        tableTickerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                tickTables()
            }
        }
    }

    private fun tickTables() {
        val currentMap = _tables.value.toMutableMap()
        var activeUpdated: GameTable? = _activeTable.value

        for ((tier, list) in currentMap) {
            val updatedList = list.map { table ->
                var currentTable = table
                // If this is the active table joined by user, tick it carefully
                if (activeUpdated != null && activeUpdated.tableId == table.tableId) {
                    currentTable = activeUpdated
                }

                when (currentTable.state) {
                    GameRoundState.BETTING -> {
                        val newCountdown = currentTable.countdownSeconds - 1
                        if (newCountdown <= 0) {
                            // Switch to ROLLING state (Flash Mode & Rolling Dice)
                            val winningNum = Random.nextInt(1, 7) // Fair 1-6 dice roll
                            val rollingTable = currentTable.copy(
                                state = GameRoundState.ROLLING,
                                countdownSeconds = 3,
                                winningNumber = winningNum
                            )
                            if (activeUpdated?.tableId == table.tableId) {
                                activeUpdated = rollingTable
                            }
                            rollingTable
                        } else {
                            val bettingTable = currentTable.copy(countdownSeconds = newCountdown)
                            if (activeUpdated?.tableId == table.tableId) {
                                activeUpdated = bettingTable
                            }
                            bettingTable
                        }
                    }
                    GameRoundState.ROLLING -> {
                        val newCountdown = currentTable.countdownSeconds - 1
                        if (newCountdown <= 0) {
                            // Transition to RESULT with 15% Admin Commission accounting
                            val winningNum = currentTable.winningNumber ?: Random.nextInt(1, 7)
                            val evaluatedSeats = currentTable.seats.map { seat ->
                                seat.copy(isWinner = (seat.selectedNumber == winningNum))
                            }
                            val winners = evaluatedSeats.filter { it.isWinner }
                            val totalPot = currentTable.tierCoins * evaluatedSeats.size
                            val adminFee = if (currentTable.tierCoins > 0) (totalPot * 15L) / 100L else 0L
                            val netPrizePool = if (currentTable.tierCoins > 0) totalPot - adminFee else 0L

                            if (adminFee > 0) {
                                _adminTreasuryCoins.value += adminFee
                            }

                            val winPerPlayer = if (winners.isNotEmpty() && currentTable.tierCoins > 0) {
                                netPrizePool / winners.size
                            } else 0L

                            val playerResults = evaluatedSeats.map { seat ->
                                val won = seat.isWinner
                                val bet = currentTable.tierCoins
                                val win = if (won) winPerPlayer else 0L
                                val net = if (won) (win - bet) else -bet
                                PlayerRoundResult(
                                    seatNumber = seat.seatNumber,
                                    playerName = seat.playerName,
                                    isUser = seat.isUser,
                                    chosenNumber = seat.selectedNumber,
                                    isWinner = won,
                                    betAmount = bet,
                                    winAmount = win,
                                    netChange = net
                                )
                            }

                            val settlement = RoundSettlement(
                                roundId = currentTable.roundId,
                                tableName = currentTable.tableName,
                                tierCoins = currentTable.tierCoins,
                                winningNumber = winningNum,
                                totalPot = totalPot,
                                adminCommission15Percent = adminFee,
                                netPrizePool85Percent = netPrizePool,
                                playerResults = playerResults
                            )

                            val resultTable = currentTable.copy(
                                state = GameRoundState.RESULT,
                                seats = evaluatedSeats,
                                countdownSeconds = 8, // 8 seconds to review full player scoreboard & hisab
                                lastSettlement = settlement
                            )

                            // If user is playing in this active table, resolve user winnings!
                            if (activeUpdated?.tableId == table.tableId) {
                                activeUpdated = resultTable
                                resolveUserRoundResult(resultTable, winningNum, settlement)
                            }

                            resultTable
                        } else {
                            val rollingTable = currentTable.copy(countdownSeconds = newCountdown)
                            if (activeUpdated?.tableId == table.tableId) {
                                activeUpdated = rollingTable
                            }
                            rollingTable
                        }
                    }
                    GameRoundState.RESULT -> {
                        val newCountdown = currentTable.countdownSeconds - 1
                        if (newCountdown <= 0) {
                            // Prepare NEXT round (30s betting timer)
                            val nextRoundId = "RND-${Random.nextInt(10000, 99999)}"
                            val resetSeats = currentTable.seats.map { seat ->
                                if (seat.isUser) {
                                    seat.copy(selectedNumber = null, isWinner = false)
                                } else {
                                    seat.copy(
                                        selectedNumber = Random.nextInt(1, 7),
                                        isWinner = false
                                    )
                                }
                            }
                            val nextTable = currentTable.copy(
                                state = GameRoundState.BETTING,
                                roundId = nextRoundId,
                                countdownSeconds = 30,
                                winningNumber = null,
                                seats = resetSeats,
                                totalPot = currentTable.tierCoins * resetSeats.size,
                                lastSettlement = null
                            )
                            if (activeUpdated?.tableId == table.tableId) {
                                activeUpdated = nextTable
                                deductUserEntryFeeIfCoinTable(nextTable)
                            }
                            nextTable
                        } else {
                            val resultTable = currentTable.copy(countdownSeconds = newCountdown)
                            if (activeUpdated?.tableId == table.tableId) {
                                activeUpdated = resultTable
                            }
                            resultTable
                        }
                    }
                    GameRoundState.WAITING -> {
                        currentTable
                    }
                }
            }
            currentMap[tier] = updatedList
        }

        _tables.value = currentMap
        _activeTable.value = activeUpdated
    }

    private fun resolveUserRoundResult(
        table: GameTable,
        winningNum: Int,
        settlement: RoundSettlement
    ) {
        val userSeat = table.seats.firstOrNull { it.isUser } ?: return
        val userSelected = userSeat.selectedNumber ?: return
        val isWin = (userSelected == winningNum)

        val tier = table.tierCoins
        val currentWallet = _walletState.value

        if (tier == 0L) {
            // Practice Mode: Free practice, track win/loss scoreboard
            val newPlayed = currentWallet.totalGamesPlayed + 1
            val newWon = currentWallet.totalGamesWon + (if (isWin) 1 else 0)
            val newLost = currentWallet.totalGamesLost + (if (isWin) 0 else 1)

            _walletState.value = currentWallet.copy(
                totalGamesPlayed = newPlayed,
                totalGamesWon = newWon,
                totalGamesLost = newLost
            )
            persistWallet()

            addHistoryRecord(
                GameHistoryRecord(
                    roundId = table.roundId,
                    tableName = "${table.tableName} (Practice)",
                    tierCoins = 0L,
                    selectedNumber = userSelected,
                    winningNumber = winningNum,
                    isWin = isWin,
                    coinChange = 0L
                )
            )
            return
        }

        // Virtual Coin Mode:
        val userResult = settlement.playerResults.firstOrNull { it.isUser }
        val winAmount = userResult?.winAmount ?: 0L

        if (isWin) {
            val newBalance = currentWallet.availableCoins + winAmount
            val newPlayed = currentWallet.totalGamesPlayed + 1
            val newWon = currentWallet.totalGamesWon + 1
            val newHighest = maxOf(currentWallet.highestWin, winAmount)

            _walletState.value = currentWallet.copy(
                availableCoins = newBalance,
                totalGamesPlayed = newPlayed,
                totalGamesWon = newWon,
                highestWin = newHighest
            )
            persistWallet()

            recordTransaction(
                title = "Won ${table.tableName} (Net 85% Pool Payout)",
                type = TransactionType.GAME_WIN,
                amount = winAmount,
                balanceAfter = newBalance
            )

            addHistoryRecord(
                GameHistoryRecord(
                    roundId = table.roundId,
                    tableName = table.tableName,
                    tierCoins = tier,
                    selectedNumber = userSelected,
                    winningNumber = winningNum,
                    isWin = true,
                    coinChange = winAmount - tier
                )
            )
        } else {
            val newPlayed = currentWallet.totalGamesPlayed + 1
            val newLost = currentWallet.totalGamesLost + 1

            _walletState.value = currentWallet.copy(
                totalGamesPlayed = newPlayed,
                totalGamesLost = newLost
            )
            persistWallet()

            addHistoryRecord(
                GameHistoryRecord(
                    roundId = table.roundId,
                    tableName = table.tableName,
                    tierCoins = tier,
                    selectedNumber = userSelected,
                    winningNumber = winningNum,
                    isWin = false,
                    coinChange = -tier
                )
            )
        }
    }

    private fun deductUserEntryFeeIfCoinTable(table: GameTable) {
        val tier = table.tierCoins
        if (tier <= 0L) return

        val currentWallet = _walletState.value
        if (currentWallet.availableCoins >= tier) {
            val newBalance = currentWallet.availableCoins - tier
            _walletState.value = currentWallet.copy(availableCoins = newBalance)
            persistWallet()

            recordTransaction(
                title = "Entry Fee ${table.tableName} ($tier Coins)",
                type = TransactionType.GAME_ENTRY,
                amount = -tier,
                balanceAfter = newBalance
            )
        }
    }

    fun joinTable(table: GameTable): Boolean {
        val user = _currentUser.value ?: return false
        val currentWallet = _walletState.value

        // If coin table, check balance
        if (table.tierCoins > 0 && currentWallet.availableCoins < table.tierCoins) {
            return false // insufficient coins
        }

        // Deduct entry fee for first round if coin table
        if (table.tierCoins > 0) {
            val newBalance = currentWallet.availableCoins - table.tierCoins
            _walletState.value = currentWallet.copy(availableCoins = newBalance)
            persistWallet()

            recordTransaction(
                title = "Entry Fee ${table.tableName} (${table.tierCoins} Coins)",
                type = TransactionType.GAME_ENTRY,
                amount = -table.tierCoins,
                balanceAfter = newBalance
            )
        }

        // Fill remaining seats with players up to 6 players
        val seats = mutableListOf<SeatPlayer>()
        val userSeatNumber = 1
        seats.add(
            SeatPlayer(
                seatNumber = userSeatNumber,
                playerId = user.id,
                playerName = user.fullName,
                avatarIndex = user.avatarIndex,
                selectedNumber = null,
                isUser = true,
                isWinner = false
            )
        )

        val playerNames = playerNamesPool.shuffled()
        for (i in 2..6) {
            seats.add(
                SeatPlayer(
                    seatNumber = i,
                    playerId = "bot_$i",
                    playerName = playerNames[(i - 2) % playerNames.size],
                    avatarIndex = (i % 6),
                    selectedNumber = Random.nextInt(1, 7),
                    isUser = false,
                    isWinner = false
                )
            )
        }

        val joinedTable = table.copy(
            seats = seats,
            state = GameRoundState.BETTING,
            countdownSeconds = 30,
            totalPot = table.tierCoins * 6
        )

        _activeTable.value = joinedTable
        return true
    }

    fun selectUserNumber(number: Int) {
        val active = _activeTable.value ?: return
        if (active.state != GameRoundState.BETTING) return

        val updatedSeats = active.seats.map { seat ->
            if (seat.isUser) seat.copy(selectedNumber = number) else seat
        }
        _activeTable.value = active.copy(seats = updatedSeats)
    }

    fun leaveTable() {
        _activeTable.value = null
    }

    fun claimDailyBonus(): Boolean {
        val ws = _walletState.value
        val now = System.currentTimeMillis()
        val oneDayMs = 86400000L

        // Allow claim once per day or for first time
        if (now - ws.lastDailyClaimTimestamp < oneDayMs && ws.lastDailyClaimTimestamp != 0L) {
            return false // Already claimed today
        }

        val streak = if (now - ws.lastDailyClaimTimestamp < oneDayMs * 2) {
            (ws.streakDays % 7) + 1
        } else {
            1
        }

        val bonusMap = listOf(500L, 750L, 1000L, 1500L, 2000L, 3000L, 5000L)
        val bonus = bonusMap[(streak - 1).coerceIn(0, 6)]

        val newBalance = ws.availableCoins + bonus
        _walletState.value = ws.copy(
            availableCoins = newBalance,
            lastDailyClaimTimestamp = now,
            streakDays = streak
        )
        persistWallet()

        recordTransaction(
            title = "Day $streak Daily Gudgudi Bonus",
            type = TransactionType.DAILY_LOGIN,
            amount = bonus,
            balanceAfter = newBalance
        )

        addNotification(
            title = "Daily Bonus Claimed! 💰",
            message = "You received +$bonus virtual coins for Day $streak attendance!"
        )

        return true
    }

    fun claimFreeFaucet(): Long {
        val freeCoins = 250L
        val ws = _walletState.value
        val newBalance = ws.availableCoins + freeCoins
        _walletState.value = ws.copy(availableCoins = newBalance)
        persistWallet()

        recordTransaction(
            title = "Free Gudgudi Faucet Coins",
            type = TransactionType.FREE_FAUCET,
            amount = freeCoins,
            balanceAfter = newBalance
        )

        return freeCoins
    }

    // Admin grant coins to user
    fun adminGrantCoins(amount: Long, reason: String = "Platform Admin Courtesy") {
        val ws = _walletState.value
        val newBalance = ws.availableCoins + amount
        _walletState.value = ws.copy(availableCoins = newBalance)
        persistWallet()

        recordTransaction(
            title = "Admin Bonus: $reason",
            type = TransactionType.ADMIN_GRANT,
            amount = amount,
            balanceAfter = newBalance
        )

        addNotification(
            title = "Admin Coin Grant",
            message = "Admin granted +$amount virtual coins to your wallet ($reason)."
        )
    }

    fun updateUserProfile(user: UserProfile) {
        _currentUser.value = user
        persistUser(user)
    }

    fun registerUser(
        fullName: String,
        mobile: String,
        email: String,
        dob: String,
        state: String,
        district: String,
        avatarIndex: Int
    ): UserProfile {
        val newUser = UserProfile(
            id = "usr_${System.currentTimeMillis() % 100000}",
            fullName = fullName,
            mobileNumber = mobile,
            email = email,
            dateOfBirth = dob,
            state = state,
            district = district,
            avatarIndex = avatarIndex,
            createdAt = System.currentTimeMillis()
        )
        _currentUser.value = newUser
        persistUser(newUser)

        // Reset or initialize welcome 5,000 coins
        val newBalance = 5000L
        _walletState.value = _walletState.value.copy(availableCoins = newBalance)
        persistWallet()

        recordTransaction(
            title = "Welcome Gudgudi Bonus",
            type = TransactionType.WELCOME_BONUS,
            amount = 5000L,
            balanceAfter = 5000L
        )

        val updatedAll = _allUsers.value.toMutableList()
        updatedAll.add(0, newUser)
        _allUsers.value = updatedAll

        return newUser
    }

    fun toggleBlockUser(userId: String) {
        val list = _allUsers.value.map { u ->
            if (u.id == userId) u.copy(isBlocked = !u.isBlocked) else u
        }
        _allUsers.value = list
        if (_currentUser.value?.id == userId) {
            val updated = _currentUser.value!!.copy(isBlocked = !_currentUser.value!!.isBlocked)
            _currentUser.value = updated
            persistUser(updated)
        }
    }

    fun broadcastAdminNotification(title: String, message: String) {
        addNotification(title, message)
    }

    private fun addNotification(title: String, message: String) {
        val notif = AppNotification(
            id = "NOTIF-${System.currentTimeMillis() % 100000}",
            title = title,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    private fun recordTransaction(title: String, type: TransactionType, amount: Long, balanceAfter: Long) {
        val item = TransactionItem(
            id = "TX-${Random.nextInt(10000, 99999)}",
            title = title,
            type = type,
            amount = amount,
            balanceAfter = balanceAfter,
            timestamp = System.currentTimeMillis()
        )
        _transactions.value = listOf(item) + _transactions.value
    }

    private fun addHistoryRecord(record: GameHistoryRecord) {
        _gameHistory.value = listOf(record) + _gameHistory.value
    }
}
