package com.xinto.mauth.domain

import com.xinto.mauth.core.settings.Settings

class SettingsRepository(private val settings: Settings) : Settings by settings