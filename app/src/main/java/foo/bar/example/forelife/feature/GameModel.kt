package foo.bar.example.forelife.feature

import co.early.fore.core.WorkMode
import co.early.fore.core.observer.Observable
import co.early.fore.kt.core.logging.Logger
import co.early.fore.kt.core.observer.ObservableImp
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Copyright © 2019 early.co. All rights reserved.
 */
@Singleton
class GameModel @Inject constructor(
        private val random: Random,
        private val logger: Logger,
        workMode: WorkMode ) : Observable by ObservableImp(workMode) {

    //we need at least 3 players
    private val players = listOf(Player(), Player(), Player(), Player())
    private var currentPlayer = 0;
    private var hasBankruptPlayers = false;
    private var round = 1;

    fun next() {
        giftRandomly(currentPlayer)
        hasBankruptPlayers = calculateBankruptPlayers()
        currentPlayer = (currentPlayer + 1) % players.size
        if (currentPlayer == 0) {
            round++
        }
        notifyObservers()
    }

    fun clear() {
        for (player in players) {
            player.amount = startAmount
        }
        currentPlayer = 0
        hasBankruptPlayers = false;
        round = 1
        notifyObservers()
    }

    fun getPlayerAmount(playerNumber: Int): CashAmount {
        return players[playerNumber].amount
    }

    fun isPlayersTurn(playerNumber: Int): Boolean {
        return currentPlayer == playerNumber
    }

    fun hasBankruptPlayers(): Boolean {
        return hasBankruptPlayers
    }

    fun getRound(): Int {
        return round
    }

    //move a coin forward, donate it to another player at random
    private fun giftRandomly(playerIndex: Int) {

        val donatingPlayer = players[playerIndex]
        val moveCoinBy = 1 + random.nextInt(players.size - 2)
        val receivingPlayer = players[(playerIndex + moveCoinBy) % players.size]

        if (hasCoins(donatingPlayer) && !hasMaxCoins(receivingPlayer)) {
            //add coin
            receivingPlayer.amount = CashAmount.values()[receivingPlayer.amount.ordinal + 1]
            //remove coin
            donatingPlayer.amount = CashAmount.values()[donatingPlayer.amount.ordinal - 1]

            logger.i(TAG, "player " + (playerIndex + 1) + " >> one coin to >> player " + (((playerIndex + moveCoinBy) % players.size) + 1))
        }
    }

    private fun calculateBankruptPlayers(): Boolean {
        for (player in players) {
            if (player.amount == CashAmount.ZERO) {
                return true
            }
        }
        return false
    }

    private fun hasCoins(player: GameModel.Player): Boolean {
        return player.amount != CashAmount.ZERO
    }

    private fun hasMaxCoins(player: GameModel.Player): Boolean {
        return player.amount.ordinal == CashAmount.values().size - 1
    }

    class Player(var amount: CashAmount = startAmount)

    companion object {
        val TAG = GameModel::class.java.simpleName
        val startAmount = CashAmount.THREE
    }
}
