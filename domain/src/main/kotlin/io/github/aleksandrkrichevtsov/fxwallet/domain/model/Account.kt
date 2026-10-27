package io.github.aleksandrkrichevtsov.fxwallet.domain.model

import java.util.Currency

data class Account(
    val id: AccountId,
    val type: AccountType,
    val currency: Currency
)
