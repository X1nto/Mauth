package com.xinto.mauth.domain.settings.model

enum class AccountsSort {
    DateAsc,
    DateDesc,
    LabelAsc,
    LabelDesc,
    IssuerAsc,
    IssuerDesc;

    companion object {
        val DEFAULT = DateDesc
    }
}