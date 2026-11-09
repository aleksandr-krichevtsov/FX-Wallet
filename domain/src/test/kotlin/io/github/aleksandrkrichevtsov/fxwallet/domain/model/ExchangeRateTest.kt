package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import java.math.BigDecimal
import java.util.Currency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

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
    fun `convert multiplies by the rate and rounds to the quote currency`() {
        val result = eurToUsd.convert(Money.of(BigDecimal("10.00"), eur))
        assertEquals(Money.of(BigDecimal("10.84"), usd), result)
    }

    @Test
    fun `convert rounds a sub-cent result half-even`() {
        val result = eurToUsd.convert(Money.of(BigDecimal("0.01"), eur))
        assertEquals(Money.of(BigDecimal("0.01"), usd), result)
    }

    @Test
    fun `convert rounds to a currency without decimals`() {
        val result = eurToJpy.convert(Money.of(BigDecimal("10.00"), eur))
        assertEquals(Money.of(BigDecimal("1624"), jpy), result)
    }

    @Test
    fun `convert keeps the sign of a negative amount`() {
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
    fun `inverse swaps base and quote`() {
        val result = eurToUsd.inverse()
        assertEquals(usd, result.base)
        assertEquals(eur, result.quote)
    }

    @Test
    fun `inverse rate converts 10_84 USD back to 10 EUR`() {
        val usdToEur = eurToUsd.inverse()
        val usdMoney = Money.of(BigDecimal("10.84"), usd)

        val result = usdToEur.convert(usdMoney)
        assertEquals(Money.of(BigDecimal("10.00"), eur), result)
    }

    @Test
    fun `cross gives a rate from the first quote to the second quote`() {
        val result = eurToUsd.cross(eurToGbp)

        assertEquals(usd, result.base)
        assertEquals(gbp, result.quote)
    }

    @Test
    fun `cross rate converts 100 USD to 79_05 GBP`() {
        val usdToGbp = eurToUsd.cross(eurToGbp)
        val money = usdToGbp.convert(Money.of(BigDecimal("100.00"), usd))

        assertEquals(Money.of(BigDecimal("79.05"), gbp), money)
    }

    @Test
    fun `cross two rates with different bases throws`() {
        val usdToGbp = ExchangeRate(usd, gbp, BigDecimal("0.7905"))
        assertThrows(IllegalArgumentException::class.java) {
            eurToUsd.cross(usdToGbp)
        }
    }
}
