package com.example

import com.example.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testGameTableLogic() {
    val seats = listOf(
      SeatPlayer(seatNumber = 1, playerId = "u1", playerName = "Vikram", avatarIndex = 0, selectedNumber = 4, isUser = true),
      SeatPlayer(seatNumber = 2, playerId = "u2", playerName = "Ranveer", avatarIndex = 1, selectedNumber = 2, isUser = false)
    )
    val table = GameTable(
      tableId = 501,
      tableName = "Table 01",
      tierCoins = 5L,
      seats = seats,
      state = GameRoundState.BETTING,
      roundId = "RND-9912"
    )

    assertEquals(2, table.occupiedSeatsCount)
    assertFalse(table.isFull)
    assertFalse(table.isPractice)
  }

  @Test
  fun testPracticeTableFreePlay() {
    val practiceTable = GameTable(
      tableId = 1,
      tableName = "Table 01",
      tierCoins = 0L,
      state = GameRoundState.BETTING
    )
    assertTrue(practiceTable.isPractice)
    assertEquals(0L, practiceTable.tierCoins)
  }

  @Test
  fun testFifteenPercentCommissionCalculation() {
    val tier = 100L
    val playerCount = 6
    val totalPot = tier * playerCount // 600 Coins
    val adminCommission = (totalPot * 15L) / 100L // 90 Coins
    val netPrizePool = totalPot - adminCommission // 510 Coins

    assertEquals(600L, totalPot)
    assertEquals(90L, adminCommission)
    assertEquals(510L, netPrizePool)
  }

  @Test
  fun testRoundSettlementDataModel() {
    val playerResults = listOf(
      PlayerRoundResult(1, "Vikram", isUser = true, chosenNumber = 4, isWinner = true, betAmount = 100L, winAmount = 510L, netChange = 410L),
      PlayerRoundResult(2, "Ranveer", isUser = false, chosenNumber = 2, isWinner = false, betAmount = 100L, winAmount = 0L, netChange = -100L)
    )
    val settlement = RoundSettlement(
      roundId = "RND-1001",
      tableName = "Table 01",
      tierCoins = 100L,
      winningNumber = 4,
      totalPot = 600L,
      adminCommission15Percent = 90L,
      netPrizePool85Percent = 510L,
      playerResults = playerResults
    )

    assertEquals(4, settlement.winningNumber)
    assertEquals(90L, settlement.adminCommission15Percent)
    assertEquals(510L, settlement.netPrizePool85Percent)
    assertTrue(settlement.playerResults[0].isWinner)
    assertFalse(settlement.playerResults[1].isWinner)
  }
}
