package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import java.math.BigDecimal
import java.util.Currency

data class ExchangeRate(
    val base: Currency,
    val quote: Currency,
    val rate: BigDecimal
    ) {
    init {

    }

    fun convert(money: Money): Money = TODO()
    fun inverse(): ExchangeRate = TODO()
    fun cross(rate: ExchangeRate): ExchangeRate = TODO()
}
