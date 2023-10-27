package com.xinto.mauth.domain

import com.xinto.mauth.core.auth.AuthManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val authManager: AuthManager
) {

    private val liveCode = authManager.getCode()

    fun observeIsProtected(): Flow<Boolean> {
        return liveCode.map { it != null }
    }

    suspend fun isProtected(): Boolean {
        return liveCode.first() != null
    }

    suspend fun validate(code: String): Boolean {
        return liveCode.first() == code
    }

    fun updateCode(code: String) {
        authManager.setCode(code)
    }

    fun removeCode() {
        authManager.removeCode()
    }
}