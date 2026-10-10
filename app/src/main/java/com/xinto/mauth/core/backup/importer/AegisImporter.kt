package com.xinto.mauth.core.backup.importer

import com.xinto.mauth.core.backup.crypto.BackupCrypto
import com.xinto.mauth.core.backup.schema.AegisGroup
import com.xinto.mauth.core.backup.schema.AegisParams
import com.xinto.mauth.core.backup.schema.AegisSlot
import com.xinto.mauth.core.backup.schema.AegisVault
import com.xinto.mauth.core.backup.model.BackupAccount
import com.xinto.mauth.core.backup.model.BackupGroup
import com.xinto.mauth.core.backup.model.BackupOtpAlgorithm
import com.xinto.mauth.core.backup.model.BackupOtpType
import com.xinto.mauth.core.backup.schema.AegisSchema
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import java.util.UUID

class AegisImporter(
    private val crypto: BackupCrypto
) : AccountImporter {

    override val source = ImportSource.Aegis

    override fun sniff(content: String): Boolean {
        val trimmed = content.trimStart()
        if (!trimmed.startsWith("{"))
            return false

        return trimmed.contains("\"db\"") && trimmed.contains("\"header\"")
    }

    override fun parse(content: String, password: CharArray?): ImportResult {
        val vault = AegisSchema.json.decodeFromString<AegisVault>(content)

        if (vault.version > AegisSchema.VAULT_VERSION)
            return ImportResult.Failure(source, ImportFailure.UnsupportedVersion)

        val db = vault.db
        val slots = vault.header.slots
        val params = vault.header.params

        val database = if (slots == null || params == null) {
            AegisSchema.json.decodeFromJsonElement<AegisDb>(db)
        } else {
            if (password == null)
                return ImportResult.NeedsPassword(source)

            val plaintext = decryptDb(db, slots, params, password)
                ?: return ImportResult.Failure(source, ImportFailure.WrongPassword)

            try {
                AegisSchema.json.decodeFromString<AegisDb>(plaintext.toString(Charsets.UTF_8))
            } finally {
                plaintext.fill(0)
            }
        }

        if (database.version > AegisSchema.DB_VERSION) {
            return ImportResult.Failure(source, ImportFailure.UnsupportedVersion)
        }

        val accounts = mutableListOf<BackupAccount>()
        val rejected = mutableListOf<RejectedEntry>()

        for (entry in database.entries) {
            val type = when (entry.type.lowercase()) {
                "totp" -> BackupOtpType.TOTP
                "hotp" -> BackupOtpType.HOTP
                else -> {
                    rejected.add(RejectedEntry(entry.name, entry.issuer, ImportFailure.Unrecognized))
                    continue
                }
            }

            accounts.add(
                BackupAccount(
                    id = entry.uuid ?: UUID.randomUUID().toString(),
                    name = entry.name,
                    issuer = entry.issuer,
                    secret = entry.info.secret,
                    type = type,
                    algorithm = BackupOtpAlgorithm.fromName(entry.info.algo),
                    digits = entry.info.digits,
                    period = if (type == BackupOtpType.TOTP) entry.info.period ?: 30 else null,
                    counter = if (type == BackupOtpType.HOTP) entry.info.counter ?: 0 else null,
                    group = entry.groups.firstOrNull()
                )
            )
        }

        return ImportResult.Success(
            source = source,
            accounts = accounts,
            groups = database.groups.map { BackupGroup(id = it.uuid, name = it.name) },
            rejected = rejected
        )
    }

    private fun decryptDb(
        db: JsonElement,
        slots: List<AegisSlot>,
        params: AegisParams,
        password: CharArray
    ): ByteArray? {
        val ciphertext = Base64.Mime.decode(db.jsonPrimitive.content)

        for (slot in slots) {
            if (slot.type != AegisSchema.SLOT_TYPE_PASSWORD)
                continue

            val n = slot.n ?: continue
            val r = slot.r ?: continue
            val p = slot.p ?: continue
            val salt = slot.salt ?: continue
            val keyParams = slot.keyParams ?: continue

            val slotKey = try {
                crypto.deriveScrypt(password, salt.hexToByteArray(), n, r, p)
            } catch (_: Exception) {
                continue
            }

            val masterKey = try {
                crypto.decryptRaw(
                    ciphertext = slot.key.hexToByteArray() + keyParams.tag.hexToByteArray(),
                    nonce = keyParams.nonce.hexToByteArray(),
                    key = slotKey
                )
            } catch (_: Exception) {
                continue
            } finally {
                slotKey.fill(0)
            }

            return try {
                crypto.decryptRaw(
                    ciphertext = ciphertext + params.tag.hexToByteArray(),
                    nonce = params.nonce.hexToByteArray(),
                    key = masterKey
                )
            } catch (_: Exception) {
                null
            } finally {
                masterKey.fill(0)
            }
        }

        return null
    }

    @Serializable
    private data class AegisDb(
        val version: Int,
        val entries: List<AegisEntry> = emptyList(),
        val groups: List<AegisGroup> = emptyList()
    )

    @Serializable
    private data class AegisEntry(
        val type: String,
        val uuid: String? = null,
        val name: String = "",
        val issuer: String = "",
        val info: AegisEntryInfo,
        val groups: List<String> = emptyList()
    )

    @Serializable
    private data class AegisEntryInfo(
        val secret: String,
        val algo: String? = null,
        val digits: Int = 6,
        val period: Int? = null,
        val counter: Int? = null
    )

}
