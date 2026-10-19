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

    @Test
    fun `of rounds half-even to the currency scale`() {
        assertEquals(BigDecimal("10.02"), Money.of(BigDecimal("10.025"), eur).amount)
    }

    @Test
    fun `of normalise the scale of two amounts and their hashCode should be equal`() {
        val a = Money.of(BigDecimal("1"), eur)
        val b = Money.of(BigDecimal("1.00"), eur)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `plus operator should sum decimal part of addends`() {
        val a = Money.of(BigDecimal("1.10"), eur)
        val b = Money.of(BigDecimal("1.20"), eur)

        val sum = a + b

        assertEquals(Money.of(BigDecimal("2.30"), eur), sum)
    }

    @Test
    fun `minus operator should allow negative result`() {
        val a = Money.of(BigDecimal("1"), eur)
        val b = Money.of(BigDecimal("1.30"), eur)

        val diff = a - b

        assertEquals(Money.of(BigDecimal("-0.30"), eur), diff)
    }

    private val usd = Currency.getInstance("USD")

    @Test
    fun `plus with different currencies should throws IllegalArgumentException`() {
        val a = Money.of(BigDecimal("1.5"), eur)
        val b = Money.of(BigDecimal("1"), usd)

        assertThrows(IllegalArgumentException::class.java) { a + b }
    }

    @Test
    fun `minus with different currencies should throws IllegalArgumentException`() {
        val a = Money.of(BigDecimal("1.5"), eur)
        val b = Money.of(BigDecimal("1"), usd)

        assertThrows(IllegalArgumentException::class.java) { a - b }
    }

    @Test
    fun `compareTo with different currencies  should throws IllegalArgumentException`() {
        val a = Money.of(BigDecimal("1.5"), eur)
        val b = Money.of(BigDecimal("1"), usd)

        assertThrows(IllegalArgumentException::class.java) { a < b }
    }

    @Test
    fun `a positive amount should become negative`() {
        val positive = Money.of(BigDecimal("1.50"), eur)

        val negated = -positive

        assertEquals(Money.of(BigDecimal("-1.50"), eur), negated)
    }

    @Test
    fun `a negative amount should become positive`() {
        val negative = Money.of(BigDecimal("-1.50"), eur)

        val positive = -negative

        assertEquals(Money.of(BigDecimal("1.50"), eur), positive)
    }

    @Test
    fun `isZero method should return true if an argument is zero`() {
        val zero = Money.of(BigDecimal("0"), eur)

        assertTrue(zero.isZero)
    }

    @Test
    fun `isZero method should return false if an argument is a smallest positive amount`() {
        val zero = Money.of(BigDecimal("0.01"), eur)

        assertFalse(zero.isZero)
    }

    @Test
    fun `isZero should return false if an argument is a smallest negative amount`() {
        val zero = Money.of(BigDecimal("-0.01"), eur)

        assertFalse(zero.isZero)
    }

    @Test
    fun `isZero should return true if an argument has three digits after the decimal point`() {
        val zero = Money.of(BigDecimal("0.003"), eur)

        assertTrue(zero.isZero)
    }

    @Test
    fun `isNegative should return true if an argument is a negative amount`() {
        val negative = Money.of(BigDecimal("-0.01"), eur)

        assertTrue(negative.isNegative)
    }

    @Test
    fun `isNegative should return false if an argument is a positive amount`() {
        val positive = Money.of(BigDecimal("0.01"), eur)

        assertFalse(positive.isNegative)
    }

    @Test
    fun `isNegative should return false if an argument is zero`() {
        val zero = Money.of(BigDecimal("0.00"), eur)

        assertFalse(zero.isNegative)
    }

    @Test
    fun `isNegative should return false if an argument has three digits after the decimal point`() {
        val zero = Money.of(BigDecimal("-0.003"), eur)

        assertFalse(zero.isNegative)
    }

    val jpy = Currency.getInstance("JPY")

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
    fun `zero should be zero`() {
        val zero = Money.zero(eur)

        assertTrue(zero.isZero)
    }
}
