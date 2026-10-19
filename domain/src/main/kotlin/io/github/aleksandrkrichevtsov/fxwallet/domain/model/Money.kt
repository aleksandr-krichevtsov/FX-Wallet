package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

class Money private constructor(val amount: BigDecimal, val currency: Currency) :
    Comparable<Money> {

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)

        val result = amount + other.amount

        return of(result, currency)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)

        val result = amount - other.amount

        return of(result, currency)
    }

    operator fun unaryMinus(): Money = of(amount.negate(), currency)

    override fun compareTo(other: Money): Int {
        requireSameCurrency(other)

        return amount.compareTo(other.amount)
    }

    val isZero: Boolean get() = amount.signum() == 0
    val isNegative: Boolean get() = amount.signum() < 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Money) return false
        return amount == other.amount && currency == other.currency
    }
    override fun hashCode(): Int = 31 * amount.hashCode() + currency.hashCode()

    override fun toString(): String =
        "amount: ${amount.toPlainString()}, currency: ${currency.currencyCode}"

    companion object {
        fun of(amount: BigDecimal, currency: Currency): Money {
            val digits = currency.defaultFractionDigits
            require(digits >= 0) { "${currency.currencyCode} has no minor units." }

            val normalizedAmount = amount.setScale(digits, RoundingMode.HALF_EVEN)
            return Money(normalizedAmount, currency)
        }
        fun zero(currency: Currency): Money = of(BigDecimal.ZERO, currency)
    }

    private fun requireSameCurrency(other: Money) {
        require(currency == other.currency)
        {
            "${currency.currencyCode} and ${other.currency.currencyCode} " +
                "are not the same. It requires the same currency."
        }
    }
}
