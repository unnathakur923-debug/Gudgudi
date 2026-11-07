package com.example.model

enum class TransactionType {
    WELCOME_BONUS,
    DAILY_LOGIN,
    FREE_FAUCET,
    GAME_ENTRY,
    GAME_WIN,
    ADMIN_GRANT,
    ADMIN_COMMISSION
}

enum class GameRoundState {
    WAITING,    // Gathering players
    BETTING,    // Selecting numbers 1-6 (timer countdown)
    ROLLING,    // Dice is tumbling & rolling
    RESULT      // Winner announced, celebration & payout
}

data class UserProfile(
    val id: String = "usr_default",
    val fullName: String = "Vikram Singh",
    val mobileNumber: String = "+91 9876543210",
    val email: String = "vikram.singh@example.com",
    val dateOfBirth: String = "15/08/1998",
    val state: String = "Rajasthan",
    val district: String = "Jaipur",
    val avatarIndex: Int = 0,
    val title: String = "Gudgudi Champion",
    val isBlocked: Boolean = false,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class WalletState(
    val availableCoins: Long = 5000L, // Welcome bonus of 5,000 coins
    val totalGamesPlayed: Int = 0,
    val totalGamesWon: Int = 0,
    val totalGamesLost: Int = 0,
    val highestWin: Long = 0L,
    val lastDailyClaimTimestamp: Long = 0L,
    val streakDays: Int = 1
)

data class TransactionItem(
    val id: String,
    val title: String,
    val type: TransactionType,
    val amount: Long, // positive for credit, negative for debit
    val balanceAfter: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class SeatPlayer(
    val seatNumber: Int, // 1 to 6
    val playerId: String,
    val playerName: String,
    val avatarIndex: Int,
    val selectedNumber: Int? = null, // 1 to 6
    val isUser: Boolean = false,
    val isWinner: Boolean = false,
    val isReady: Boolean = true
)

data class PlayerRoundResult(
    val seatNumber: Int,
    val playerName: String,
    val isUser: Boolean,
    val chosenNumber: Int?,
    val isWinner: Boolean,
    val betAmount: Long,
    val winAmount: Long,
    val netChange: Long
)

data class RoundSettlement(
    val roundId: String,
    val tableName: String,
    val tierCoins: Long,
    val winningNumber: Int,
    val totalPot: Long,
    val adminCommission15Percent: Long,
    val netPrizePool85Percent: Long,
    val playerResults: List<PlayerRoundResult>,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameTable(
    val tableId: Int,
    val tableName: String,
    val tierCoins: Long, // 0 = Practice; 5, 10, 25, 50, 100, 500, 1000, 2500, 5000
    val seats: List<SeatPlayer> = emptyList(),
    val state: GameRoundState = GameRoundState.BETTING,
    val roundId: String = "RND-1001",
    val countdownSeconds: Int = 30,
    val winningNumber: Int? = null,
    val totalPot: Long = 0L,
    val lastSettlement: RoundSettlement? = null
) {
    val occupiedSeatsCount: Int
        get() = seats.size

    val isFull: Boolean
        get() = seats.size >= 6

    val isPractice: Boolean
        get() = tierCoins == 0L
}

data class GameHistoryRecord(
    val roundId: String,
    val tableName: String,
    val tierCoins: Long,
    val selectedNumber: Int,
    val winningNumber: Int,
    val isWin: Boolean,
    val coinChange: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val title: String,
    val state: String,
    val totalCoinsWon: Long,
    val winsCount: Int,
    val avatarIndex: Int
)

data class SupportTicket(
    val id: String,
    val category: String,
    val subject: String,
    val message: String,
    val status: String = "Open",
    val timestamp: Long = System.currentTimeMillis()
)
