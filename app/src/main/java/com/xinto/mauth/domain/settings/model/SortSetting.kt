package com.xinto.mauth.domain.settings.model

enum class SortSetting {
    DateAsc,
    DateDesc,
    NameAsc,
    NameDesc,
    IssuerAsc,
    IssuerDesc;

    companion object {
        val DEFAULT = DateDesc
    }
}