package com.xinto.mauth.core.backup.crypto

class Argon2Params(
    val memoryKib: Int,
    val iterations: Int,
    val parallelism: Int,
    val salt: ByteArray,
    val nonce: ByteArray,
)
