package com.xinto.mauth.domain.settings.model

enum class Theme {
    System,
    Dark,
    Light;

    companion object {
        val DEFAULT = System
    }
}