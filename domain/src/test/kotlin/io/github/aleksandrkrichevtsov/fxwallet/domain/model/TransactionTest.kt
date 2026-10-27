package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency
import kotlin.time.Instant

class TransactionTest {
    private val usd: Currency = Currency.getInstance("USD")
    private val eur: Currency = Currency.getInstance("EUR")
    private val createdAt: Instant = Instant.parse("2026-10-15T10:00:00Z")
    private val userEurId: AccountId = AccountId("user-eur")
    private val userUsdId: AccountId = AccountId("user-usd")
    private val topUpEurId: AccountId = AccountId("top-up-eur")
    private val topUpUsdId: AccountId = AccountId("top-up-usd")
    private val fxEurId: AccountId = AccountId("fx-eur")
    private val fxUsdId: AccountId = AccountId("fx-usd")
    private fun createMoneyObject(amount: String, currency: Currency): Money {
        return Money.of(BigDecimal(amount), currency)
    }

    @Test
    fun `a balanced top-up is accepted`() {
        val topUpEntry = LedgerEntry(topUpEurId, createMoneyObject("-100.00", eur))
        val userEntry = LedgerEntry(userEurId, createMoneyObject("100.00", eur))
        val entries = listOf(userEntry, topUpEntry)
        val transaction = Transaction(
            id = TransactionId("t1"),
            type = TransactionType.TOP_UP,
            entries = entries,
            createdAt = createdAt)

        assertEquals(entries, transaction.entries)
    }

    @Test
    fun `a balanced exchange is accepted`() {
        val userEurNegativeEntry = LedgerEntry(userEurId, createMoneyObject("-10.00", eur))
        val userEurPositiveEntry = LedgerEntry(fxEurId, createMoneyObject("10.00", eur))

        val userUsdNegativeEntry = LedgerEntry(userUsdId, createMoneyObject("10.84", usd))
        val userUsdPositiveEntry = LedgerEntry(fxUsdId, createMoneyObject("-10.84", usd))

        val entries = listOf(
            userUsdPositiveEntry,
            userUsdNegativeEntry,
            userEurPositiveEntry,
            userEurNegativeEntry
        )

        val transaction = Transaction(
            id = TransactionId("t1"),
            type = TransactionType.EXCHANGE,
            entries = entries,
            createdAt = createdAt
        )

        assertEquals(entries, transaction.entries)
    }

    @Test
    fun `an unbalanced transaction throws`() {
        val userNegativeEntry = LedgerEntry(topUpEurId, createMoneyObject("-100.00", eur))
        val userPositiveEntry = LedgerEntry(userEurId, createMoneyObject("90.00", eur))

        val entries = listOf(userNegativeEntry, userPositiveEntry)

        assertThrows(IllegalArgumentException::class.java) {
            Transaction(
                id = TransactionId("t1"),
                type = TransactionType.EXCHANGE,
                entries = entries,
                createdAt = createdAt
            )
        }
    }

    @Test
    fun `an unbalanced in one currency throws`() {
        val userEurNegativeEntry = LedgerEntry(userUsdId, createMoneyObject("-10.00", eur))
        val userEurPositiveEntry = LedgerEntry(fxUsdId, createMoneyObject("10.00", eur))

        val userUsdNegativeEntry = LedgerEntry(userUsdId, createMoneyObject("10.00", usd))
        val userUsdPositiveEntry = LedgerEntry(fxUsdId, createMoneyObject("-10.84", usd))

        val entries = listOf(
            userUsdPositiveEntry,
            userUsdNegativeEntry,
            userEurPositiveEntry,
            userEurNegativeEntry
        )

        assertThrows(IllegalArgumentException::class.java) {
            Transaction(
                id = TransactionId("t1"),
                type = TransactionType.EXCHANGE,
                entries = entries,
                createdAt = createdAt
            )
        }
    }

    @Test
    fun `a single entry throws`() {
        val userEntry = LedgerEntry(topUpEurId, createMoneyObject("100", eur))

        val entries = listOf(userEntry)

        assertThrows(IllegalArgumentException::class.java) {
            Transaction(
                id = TransactionId("t1"),
                type = TransactionType.TOP_UP,
                entries = entries,
                createdAt
            )
        }
    }

    @Test
    fun `no entries throws`() {
        val entries = emptyList<LedgerEntry>()

        assertThrows(IllegalArgumentException::class.java) {
            Transaction(
                id = TransactionId("id"),
                type = TransactionType.TOP_UP,
                entries = entries,
                createdAt
            )
        }
    }
}
