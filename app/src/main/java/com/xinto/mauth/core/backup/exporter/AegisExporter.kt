package com.xinto.mauth.core.backup.exporter

import com.xinto.mauth.ui.util.encodeToByteArray
import com.xinto.mauth.core.backup.crypto.BackupCrypto
import com.xinto.mauth.core.backup.schema.AegisGroup
import com.xinto.mauth.core.backup.schema.AegisHeader
import com.xinto.mauth.core.backup.schema.AegisParams
import com.xinto.mauth.core.backup.schema.AegisSlot
import com.xinto.mauth.core.backup.schema.AegisVault
import com.xinto.mauth.core.backup.model.BackupData
import com.xinto.mauth.core.backup.model.BackupOtpType
import com.xinto.mauth.core.backup.schema.AegisSchema
import kotlin.io.encoding.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.util.UUID

class AegisExporter(
    private val crypto: BackupCrypto
) : BackupExporter {

    @Serializable
    private data class AegisDb(
        val version: Int,
        val entries: List<AegisEntry>,
        val groups: List<AegisGroup>
    )

    @Serializable
    private data class AegisEntry(
        val type: String,
        val uuid: String,
        val name: String,
        val issuer: String,
        val note: String = "",
        val favorite: Boolean = false,
        val icon: String? = null,
        @SerialName("icon_mime")
        val iconMime: String? = null,
        val info: AegisEntryInfo,
        val groups: List<String>
    )

    @Serializable
    private data class AegisEntryInfo(
        val secret: String,
        val algo: String,
        val digits: Int,
        val period: Int? = null,
        val counter: Int? = null
    )


    override fun export(contents: BackupData, password: CharArray?): String {
        val db = AegisDb(
            version = AegisSchema.DB_VERSION,
            entries = contents.accounts.map { account ->
                AegisEntry(
                    type = when (account.type) {
                        BackupOtpType.TOTP -> "totp"
                        BackupOtpType.HOTP -> "hotp"
                    },
                    uuid = account.id,
                    name = account.name,
                    issuer = account.issuer,
                    info = AegisEntryInfo(
                        secret = account.secret,
                        algo = account.algorithm.name,
                        digits = account.digits,
                        period = account.period,
                        counter = account.counter
                    ),
                    groups = listOfNotNull(account.group)
                )
            },
            groups = contents.groups.map { AegisGroup(uuid = it.id, name = it.name) }
        )

        if (password == null) {
            return AegisSchema.json.encodeToString(
                AegisVault(
                    version = AegisSchema.VAULT_VERSION,
                    header = AegisHeader(slots = null, params = null),
                    db = AegisSchema.json.encodeToJsonElement(db)
                )
            )
        }

        val masterKey = crypto.randomBytes(KEY_LENGTH)
        val slotSalt = crypto.randomBytes(SCRYPT_SALT_LENGTH)
        val slotNonce = crypto.randomBytes(NONCE_LENGTH)
        val slotKey = crypto.deriveScrypt(password, slotSalt, SCRYPT_N, SCRYPT_R, SCRYPT_P)
        val sealedKey = try {
            crypto.encryptRaw(plaintext = masterKey, nonce = slotNonce, key = slotKey)
        } finally {
            slotKey.fill(0)
        }

        val nonce = crypto.randomBytes(NONCE_LENGTH)
        val sealed = try {
            crypto.encryptRaw(
                plaintext = AegisSchema.json.encodeToByteArray(db),
                nonce = nonce,
                key = masterKey
            )
        } finally {
            masterKey.fill(0)
        }

        val (wrappedKey, keyTag) = sealedKey.splitTag()
        val (ciphertext, tag) = sealed.splitTag()

        return AegisSchema.json.encodeToString(
            AegisVault(
                version = AegisSchema.VAULT_VERSION,
                header = AegisHeader(
                    slots = listOf(
                        AegisSlot(
                            type = AegisSchema.SLOT_TYPE_PASSWORD,
                            uuid = UUID.randomUUID().toString(),
                            key = wrappedKey.toHexString(),
                            keyParams = AegisParams(
                                nonce = slotNonce.toHexString(),
                                tag = keyTag.toHexString()
                            ),
                            n = SCRYPT_N,
                            r = SCRYPT_R,
                            p = SCRYPT_P,
                            salt = slotSalt.toHexString()
                        )
                    ),
                    params = AegisParams(nonce = nonce.toHexString(), tag = tag.toHexString())
                ),
                db = JsonPrimitive(Base64.encode(ciphertext))
            )
        )
    }

    private fun ByteArray.splitTag(): Pair<ByteArray, ByteArray> {
        return copyOfRange(0, size - TAG_LENGTH) to copyOfRange(size - TAG_LENGTH, size)
    }

    private companion object {
        const val KEY_LENGTH = 32
        const val NONCE_LENGTH = 12
        const val TAG_LENGTH = 16
        const val SCRYPT_SALT_LENGTH = 32

        const val SCRYPT_N = 32768
        const val SCRYPT_R = 8
        const val SCRYPT_P = 1
    }
}
