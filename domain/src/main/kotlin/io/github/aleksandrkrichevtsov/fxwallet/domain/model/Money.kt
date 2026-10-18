package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import java.math.BigDecimal
import java.util.Currency

class Money private constructor(
    val amount: BigDecimal,
    val currency: Currency
) : Comparable<Money> {

    operator fun plus(other: Money): Money = TODO()
    operator fun minus(other: Money): Money = TODO()
    operator fun unaryMinus(): Money = TODO()
    override fun compareTo(other: Money): Int = TODO()

    val isZero: Boolean get() = TODO()
    val isNegative: Boolean get() = TODO()

    override fun equals(other: Any?): Boolean = TODO()
    override fun hashCode(): Int = TODO()
    override fun toString(): String = TODO()

    companion object {
        fun of(amount: BigDecimal, currency: Currency): Money = TODO()
        fun zero(currency: Currency): Money = TODO()
    }
}
