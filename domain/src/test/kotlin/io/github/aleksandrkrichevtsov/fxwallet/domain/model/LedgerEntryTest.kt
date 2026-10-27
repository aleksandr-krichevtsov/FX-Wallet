package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency

class LedgerEntryTest {
    private val eur: Currency = Currency.getInstance("EUR")
    private val userEurId: AccountId = AccountId("user-eur")
    private fun createMoneyObject(amount: String, currency: Currency): Money {
        return Money.of(BigDecimal(amount), currency)
    }

    @Test
    fun `a zero amount throws IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            LedgerEntry(userEurId, createMoneyObject("0.00", eur))
        }
    }
}
