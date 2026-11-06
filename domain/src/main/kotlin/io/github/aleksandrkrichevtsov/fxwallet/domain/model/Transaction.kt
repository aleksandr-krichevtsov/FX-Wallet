package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import java.util.Currency
import kotlin.time.Instant

data class Transaction(
    val id: TransactionId,
    val type: TransactionType,
    val entries: List<LedgerEntry>,
    val createdAt: Instant
) {
    init {
        require(entries.size > 1) {
            "Transaction ${id.value} has to have at least two entries."
        }

        val unbalanced = unbalancedCurrencies()
        require(unbalanced.isEmpty()) {
            "Transaction ${id.value} is unbalanced in: ${unbalanced.joinToString()}"
        }
    }

    private fun unbalancedCurrencies(): Set<Currency> = entries
        .groupBy { it.money.currency }
        .filter { (currency, group) ->
            val sum = group.fold(Money.zero(currency)) { total, entry ->
                total + entry.money
            }
            !sum.isZero
        }
        .keys
}
