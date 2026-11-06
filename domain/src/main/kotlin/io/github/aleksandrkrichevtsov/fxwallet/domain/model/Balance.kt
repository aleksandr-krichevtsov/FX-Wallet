package io.github.aleksandrkrichevtsov.fxwallet.domain.model

fun List<LedgerEntry>.balanceOf(account: Account): Money {
    val entries = filter { it.accountId == account.id }
    return entries.fold(Money.zero(account.currency)) { total, entry -> total + entry.money }
}
