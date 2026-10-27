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
        val userEurEntry = LedgerEntry(userEurId, createMoneyObject("-10.00", eur))
        val fxEurEntry = LedgerEntry(fxEurId, createMoneyObject("10.00", eur))

        val userUsdEntry = LedgerEntry(userUsdId, createMoneyObject("10.84", usd))
        val fxUsdEntry = LedgerEntry(fxUsdId, createMoneyObject("-10.84", usd))

        val entries = listOf(
            fxUsdEntry,
            userUsdEntry,
            fxEurEntry,
            userEurEntry
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
        val topUpEntry = LedgerEntry(topUpEurId, createMoneyObject("-100.00", eur))
        val userEntry = LedgerEntry(userEurId, createMoneyObject("90.00", eur))

        val entries = listOf(topUpEntry, userEntry)

        assertThrows(IllegalArgumentException::class.java) {
            Transaction(
                id = TransactionId("t1"),
                type = TransactionType.TOP_UP,
                entries = entries,
                createdAt = createdAt
            )
        }
    }

    @Test
    fun `an unbalanced in one currency throws`() {
        val userEurEntry = LedgerEntry(userEurId, createMoneyObject("-10.00", eur))
        val fxEurEntry = LedgerEntry(fxEurId, createMoneyObject("10.00", eur))

        val userUsdEntry = LedgerEntry(userUsdId, createMoneyObject("10.00", usd))
        val fxUsdEntry = LedgerEntry(fxUsdId, createMoneyObject("-10.84", usd))

        val entries = listOf(
            fxUsdEntry,
            userUsdEntry,
            fxEurEntry,
            userEurEntry
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
        val userEntry = LedgerEntry(userEurId, createMoneyObject("100", eur))

        val entries = listOf(userEntry)

        assertThrows(IllegalArgumentException::class.java) {
            Transaction(
                id = TransactionId("t1"),
                type = TransactionType.TOP_UP,
                entries = entries,
                createdAt = createdAt
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
                createdAt = createdAt
            )
        }
    }
}
