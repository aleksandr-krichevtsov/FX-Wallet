package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import java.math.BigDecimal
import java.util.Currency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {
    private val eur = Currency.getInstance("EUR")
    private val usd = Currency.getInstance("USD")
    private val jpy = Currency.getInstance("JPY")
    private val xau = Currency.getInstance("XAU")

    @Test
    fun `of rounds a tie down when the kept digits is even`() {
        assertEquals(BigDecimal("10.02"), Money.of(BigDecimal("10.025"), eur).amount)
    }

    @Test
    fun `of rounds a tie down to a zero kept digit`() {
        assertEquals(BigDecimal("10.00"), Money.of(BigDecimal("10.005"), eur).amount)
    }

    @Test
    fun `of rounds a tie up when kept digits is odd`() {
        assertEquals(BigDecimal("10.02"), Money.of(BigDecimal("10.015"), eur).amount)
    }

    @Test
    fun `of rounds a negative tie toward the even digit`() {
        assertEquals(BigDecimal("-10.00"), Money.of(BigDecimal("-10.005"), eur).amount)
    }

    @Test
    fun `of uses the currency scale for a currency without decimals`() {
        assertEquals(BigDecimal("101"), Money.of(BigDecimal("100.6"), jpy).amount)
    }

    @Test
    fun `of rejects a currency without minor units`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money.of(BigDecimal("1"), xau)
        }
    }

    @Test
    fun `of normalise the scale of two amounts and their hashCode be equal`() {
        val a = Money.of(BigDecimal("1"), eur)
        val b = Money.of(BigDecimal("1.00"), eur)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `plus adds amounts of the same currency`() {
        val a = Money.of(BigDecimal("1.10"), eur)
        val b = Money.of(BigDecimal("1.20"), eur)

        val sum = a + b

        assertEquals(Money.of(BigDecimal("2.30"), eur), sum)
    }

    @Test
    fun `minus operator allow negative result`() {
        val a = Money.of(BigDecimal("1"), eur)
        val b = Money.of(BigDecimal("1.30"), eur)

        val diff = a - b

        assertEquals(Money.of(BigDecimal("-0.30"), eur), diff)
    }

    @Test
    fun `plus with different currencies throws IllegalArgumentException`() {
        val a = Money.of(BigDecimal("1.5"), eur)
        val b = Money.of(BigDecimal("1"), usd)

        assertThrows(IllegalArgumentException::class.java) { a + b }
    }

    @Test
    fun `minus with different currencies throws IllegalArgumentException`() {
        val a = Money.of(BigDecimal("1.5"), eur)
        val b = Money.of(BigDecimal("1"), usd)

        assertThrows(IllegalArgumentException::class.java) { a - b }
    }

    @Test
    fun `compareTo with different currencies throws IllegalArgumentException`() {
        val a = Money.of(BigDecimal("1.5"), eur)
        val b = Money.of(BigDecimal("1"), usd)

        assertThrows(IllegalArgumentException::class.java) { a < b }
    }

    @Test
    fun `a positive amount become negative`() {
        val positive = Money.of(BigDecimal("1.50"), eur)

        val negated = -positive

        assertEquals(Money.of(BigDecimal("-1.50"), eur), negated)
    }

    @Test
    fun `a minusCent become positive`() {
        val negative = Money.of(BigDecimal("-1.50"), eur)

        val positive = -negative

        assertEquals(Money.of(BigDecimal("1.50"), eur), positive)
    }

    @Test
    fun `isZero is true for zero`() {
        val zero = Money.of(BigDecimal("0"), eur)

        assertTrue(zero.isZero)
    }

    @Test
    fun `isZero is false for a cent amount`() {
        val zero = Money.of(BigDecimal("0.01"), eur)

        assertFalse(zero.isZero)
    }

    @Test
    fun `isZero is false a smallest minusCent`() {
        val zero = Money.of(BigDecimal("-0.01"), eur)

        assertFalse(zero.isZero)
    }

    @Test
    fun `isZero is true for three digits after the decimal point`() {
        val zero = Money.of(BigDecimal("0.003"), eur)

        assertTrue(zero.isZero)
    }

    @Test
    fun `isNegative is true for a minusCent`() {
        val negative = Money.of(BigDecimal("-0.01"), eur)

        assertTrue(negative.isNegative)
    }

    @Test
    fun `isNegative is false for a positive amount`() {
        val positive = Money.of(BigDecimal("0.01"), eur)

        assertFalse(positive.isNegative)
    }

    @Test
    fun `isNegative is false for zero`() {
        val zero = Money.of(BigDecimal("0.00"), eur)

        assertFalse(zero.isNegative)
    }

    @Test
    fun `isNegative is false for three digits after the decimal point`() {
        val zero = Money.of(BigDecimal("-0.003"), eur)

        assertFalse(zero.isNegative)
    }

    @Test
    fun `zero create a zero amount in the given currency`() {
        val zero = Money.zero(eur)

        assertEquals(Money.of(BigDecimal("0"), eur), zero)
    }

    @Test
    fun `zero uses the scale of the currency`() {
        val zero = Money.zero(jpy)

        assertEquals(Money.of(BigDecimal("0"), jpy), zero)
    }

    @Test
    fun `zero be zero`() {
        val zero = Money.zero(eur)

        assertTrue(zero.isZero)
    }
}
