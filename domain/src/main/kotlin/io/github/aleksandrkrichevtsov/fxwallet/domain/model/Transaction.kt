package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import kotlin.time.Instant

data class Transaction(
    val id: TransactionId,
    val type: TransactionType,
    val entries: List<LedgerEntry>,
    val createdAt: Instant
) {
    init {
        require(entries.size > 1) {
            "Transaction has to have at least two entries."

            entries.groupBy { it.money.currency }
        }
    }
}
