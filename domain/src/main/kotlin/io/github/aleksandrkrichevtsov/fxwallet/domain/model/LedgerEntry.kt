package io.github.aleksandrkrichevtsov.fxwallet.domain.model

data class LedgerEntry(
    val accountId: AccountId,
    val money: Money
) {
    init {
        
    }
}
