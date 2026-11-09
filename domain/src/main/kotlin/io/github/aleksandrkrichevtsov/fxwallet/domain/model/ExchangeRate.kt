package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import java.math.BigDecimal
import java.math.MathContext
import java.util.Currency

data class ExchangeRate(val base: Currency, val quote: Currency, val rate: BigDecimal) {
    init {
        requireBaseAndQuoteToBeDifferentCurrency()
        requireRateToBeGreaterThanOne()
    }

    private fun requireBaseAndQuoteToBeDifferentCurrency() {
        require(base != quote) {
            "The rate has the base: $base and the quote: $quote. They must be different!"
        }
    }

    private fun requireRateToBeGreaterThanOne() {
        require(rate.signum() > 0) {
            "The rate has to be greater than 0. The current rate is: $rate."
        }
    }

    fun convert(money: Money): Money {
        require(base == money.currency) {
            "The base currency and the money currency must be the same. The current base is:" +
                " $base, the current money currency is: ${money.currency}."
        }

        val multipliedRawAmount = money.amount.multiply(rate)

        return Money.of(multipliedRawAmount, quote)
    }
    fun inverse(): ExchangeRate {
        val inversedRate = BigDecimal("1.0").divide(rate, MathContext.DECIMAL128)

        return ExchangeRate(
            base = quote,
            quote = base,
            inversedRate
        )
    }
    fun cross(other: ExchangeRate): ExchangeRate {
        require(base == other.base) {
            "The base of the two rates must be the same. The current base is: $base," +
                " the other base is: ${other.base}"
        }

        val crossRate = other
            .rate
            .divide(rate, MathContext.DECIMAL128)

        return ExchangeRate(
            base = quote,
            quote = other.quote,
            rate = crossRate
        )
    }
}
