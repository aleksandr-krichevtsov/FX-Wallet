package io.github.aleksandrkrichevtsov.fxwallet.domain.model

data class LedgerEntry(val accountId: AccountId, val money: Money) {
    init {
        require(!money.isZero) {
            "Account with id: $accountId has zero money amount."
        }
    }
}
