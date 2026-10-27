package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency
import kotlin.time.Instant

class BalanceTest {
    private val eur: Currency = Currency.getInstance("EUR")
    private val createdAt: Instant = Instant.parse("2026-10-15T10:00:00Z")
    private val userEurId: AccountId = AccountId("user-eur")
    private val topUpEurId: AccountId = AccountId("top-up-eur")
    private val userAccount: Account = Account(userEurId, AccountType.USER, eur)
    private fun createMoneyObject(amount: String, currency: Currency): Money {
        return Money.of(BigDecimal(amount), currency)
    }

    @Test
    fun `no entries have zero money`() {
        val entries = emptyList<LedgerEntry>()

        val accountWithZeroMoney = entries.balanceOf(userAccount)

        assertEquals(Money.zero(eur), accountWithZeroMoney)
    }

    @Test
    fun `account's entries count is 100EUR`() {
        val userEntry = LedgerEntry(userEurId, createMoneyObject("100.00", eur))
        val topUpEntry = LedgerEntry(topUpEurId, createMoneyObject("-100.00", eur))

        val entries = listOf(userEntry, topUpEntry)

        val balance = entries.balanceOf(userAccount)

        assertEquals(createMoneyObject("100.00", eur), balance)
    }

    @Test
    fun `positive and negative entries add up`() {
        val userEntryTopUp = LedgerEntry(userEurId, createMoneyObject("100.00", eur))
        val userEntryWithdrawal = LedgerEntry(userEurId, createMoneyObject("-30.00", eur))

        val entries = listOf(userEntryTopUp, userEntryWithdrawal)

        val balance = entries.balanceOf(userAccount)

        assertEquals(createMoneyObject("70.00", eur), balance)
    }
}
