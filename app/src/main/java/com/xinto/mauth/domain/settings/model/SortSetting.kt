package com.xinto.mauth.domain.settings.model

enum class SortSetting {
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