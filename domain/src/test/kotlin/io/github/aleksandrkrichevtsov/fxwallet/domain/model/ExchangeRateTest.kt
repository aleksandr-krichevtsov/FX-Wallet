package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency

class ExchangeRateTest {
    private val eur = Currency.getInstance("EUR")
    private val usd = Currency.getInstance("USD")
    private val gbp = Currency.getInstance("GBP")
    private val jpy = Currency.getInstance("JPY")
    private val eurToUsd = ExchangeRate(eur, usd, BigDecimal("1.0842"))
    private val eurToGbp = ExchangeRate(eur, gbp, BigDecimal("0.8571"))
    private val eurToJpy = ExchangeRate(eur, jpy, BigDecimal("162.37"))

    @Test
    fun `the same base and quote throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            ExchangeRate(
                base = eur,
                quote = eur,
                rate = BigDecimal("1.0")
            )
        }
    }

    @Test
    fun `a zero rate throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            ExchangeRate(
                base = eur,
                quote = usd,
                rate = BigDecimal("0.0")
            )
        }
    }

    @Test
    fun `a negative rate throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            ExchangeRate(
                base = eur,
                quote = usd,
                rate = BigDecimal("-1.0")
            )
        }
    }

    @Test
    fun `convert two currencies`() {
        val result = eurToUsd.convert(Money.of(BigDecimal("10.00"), eur))
        assertEquals(Money.of(BigDecimal("10.84"), usd), result)
    }

    @Test
    fun `convert two currencies with raw rounded`() {
        val result = eurToUsd.convert(Money.of(BigDecimal("0.01"), eur))
        assertEquals(Money.of(BigDecimal("0.01"), usd), result)
    }

    @Test
    fun `convert to jpy with no scale`() {
        val result = eurToJpy.convert(Money.of(BigDecimal("10.00"), eur))
        assertEquals(Money.of(BigDecimal("1624"), jpy), result)
    }

    @Test
    fun `convert negative amount`() {
        val result = eurToUsd.convert(Money.of(BigDecimal("-10.00"), eur))
        assertEquals(Money.of(BigDecimal("-10.84"), usd), result)
    }

    @Test
    fun `convert with a wrong currency throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            eurToUsd.convert(Money.of(BigDecimal("10.00"), usd))
        }
    }

    @Test
    fun `inverse two currencies`() {
        val result = eurToUsd.inverse()
        assertEquals(usd, result.base)
        assertEquals(eur, result.quote)
    }

    @Test
    fun `inverse currencies through a conversion`() {
        val usdToEur = eurToUsd.inverse()
        val usdMoney = Money.of(BigDecimal("10.84"), usd)

        val result = usdToEur.convert(usdMoney)
        assertEquals(Money.of(BigDecimal("10.00"), eur), result)
    }

    @Test
    fun `cross convert correct currencies`() {
        val result = eurToUsd.cross(eurToGbp)
        val base = result.base
        val quote = result.quote

        assertEquals(usd, base)
        assertEquals(gbp, quote)
    }

    @Test
    fun `cross convert with same base and different quotes`() {
        val usdToGpb = eurToUsd.cross(eurToGbp)
        val money = usdToGpb.convert(Money.of(BigDecimal("100.00"), usd))

        assertEquals(Money.of(BigDecimal("79.05"), gbp), money)
    }

    @Test
    fun `cross two rates with different bases throws`() {
        val usdToEur = eurToUsd.inverse()
        assertThrows(IllegalArgumentException::class.java) {
            eurToUsd.cross(usdToEur)
        }
    }
}
