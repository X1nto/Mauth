package com.xinto.mauth.core.backup

import com.xinto.mauth.ui.util.encodeToByteArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import java.util.UUID

object AegisBackup : BackupImporter, BackupExporter {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = true
    }

    private const val VAULT_VERSION = 1
    private const val DB_VERSION = 3
    private const val SLOT_TYPE_PASSWORD = 1

    private const val KEY_LENGTH = 32
    private const val NONCE_LENGTH = 12
    private const val TAG_LENGTH = 16
    private const val SCRYPT_SALT_LENGTH = 32

    private const val SCRYPT_N = 32768
    private const val SCRYPT_R = 8
    private const val SCRYPT_P = 1

    override val mimeType = MimeType.Json

    override val supportsEncryption = true

    override fun sniff(content: String): Boolean {
        val trimmed = content.trimStart()
        if (!trimmed.startsWith("{"))
            return false

        return trimmed.contains("\"db\"") && trimmed.contains("\"header\"")
    }

    override fun import(content: String, password: CharArray?): ImportResult {
        val vault = json.decodeFromString<AegisVault>(content)

        if (vault.version > VAULT_VERSION)
            return ImportResult.Failure(ImportFailure.UnsupportedVersion)

        val db = vault.db
        val slots = vault.header.slots
        val params = vault.header.params

        val database = if (slots == null || params == null) {
            json.decodeFromJsonElement<AegisDb>(db)
        } else {
            if (password == null)
                return ImportResult.NeedsPassword

            val plaintext = decryptDb(db, slots, params, password)
                ?: return ImportResult.Failure(ImportFailure.WrongPassword)

            try {
                json.decodeFromString<AegisDb>(plaintext.toString(Charsets.UTF_8))
            } finally {
                plaintext.fill(0)
            }
        }

        if (database.version > DB_VERSION) {
            return ImportResult.Failure(ImportFailure.UnsupportedVersion)
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
            accounts = accounts,
            groups = database.groups.map { BackupGroup(id = it.uuid, name = it.name) },
            rejected = rejected
        )
    }

    override fun export(contents: BackupData, password: CharArray?): String {
        val db = AegisDb(
            version = DB_VERSION,
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
            return json.encodeToString(
                AegisVault(
                    version = VAULT_VERSION,
                    header = AegisHeader(slots = null, params = null),
                    db = json.encodeToJsonElement(db)
                )
            )
        }

        val masterKey = BackupCrypto.randomBytes(KEY_LENGTH)
        val slotSalt = BackupCrypto.randomBytes(SCRYPT_SALT_LENGTH)
        val slotNonce = BackupCrypto.randomBytes(NONCE_LENGTH)
        val slotKey = BackupCrypto.deriveScrypt(password, slotSalt, SCRYPT_N, SCRYPT_R, SCRYPT_P)
        val sealedKey = try {
            BackupCrypto.encryptRaw(plaintext = masterKey, nonce = slotNonce, key = slotKey)
        } finally {
            slotKey.fill(0)
        }

        val nonce = BackupCrypto.randomBytes(NONCE_LENGTH)
        val sealed = try {
            BackupCrypto.encryptRaw(
                plaintext = json.encodeToByteArray(db),
                nonce = nonce,
                key = masterKey
            )
        } finally {
            masterKey.fill(0)
        }

        val (wrappedKey, keyTag) = sealedKey.splitTag()
        val (ciphertext, tag) = sealed.splitTag()

        return json.encodeToString(
            AegisVault(
                version = VAULT_VERSION,
                header = AegisHeader(
                    slots = listOf(
                        AegisSlot(
                            type = SLOT_TYPE_PASSWORD,
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

    private fun decryptDb(
        db: JsonElement,
        slots: List<AegisSlot>,
        params: AegisParams,
        password: CharArray
    ): ByteArray? {
        val ciphertext = Base64.Mime.decode(db.jsonPrimitive.content)

        for (slot in slots) {
            if (slot.type != SLOT_TYPE_PASSWORD)
                continue

            val n = slot.n ?: continue
            val r = slot.r ?: continue
            val p = slot.p ?: continue
            val salt = slot.salt ?: continue
            val keyParams = slot.keyParams ?: continue

            val slotKey = try {
                BackupCrypto.deriveScrypt(password, salt.hexToByteArray(), n, r, p)
            } catch (_: Exception) {
                continue
            }

            val masterKey = try {
                BackupCrypto.decryptRaw(
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
                BackupCrypto.decryptRaw(
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

    private fun ByteArray.splitTag(): Pair<ByteArray, ByteArray> {
        return copyOfRange(0, size - TAG_LENGTH) to copyOfRange(size - TAG_LENGTH, size)
    }
}

@Serializable
private data class AegisVault(
    val version: Int,
    val header: AegisHeader,
    val db: JsonElement
)

@Serializable
private data class AegisHeader(
    val slots: List<AegisSlot>? = null,
    val params: AegisParams? = null
)

@Serializable
private data class AegisSlot(
    val type: Int,
    val uuid: String? = null,
    val key: String,
    @SerialName("key_params")
    val keyParams: AegisParams? = null,
    val n: Int? = null,
    val r: Int? = null,
    val p: Int? = null,
    val salt: String? = null
)

@Serializable
private data class AegisParams(
    val nonce: String,
    val tag: String
)

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
    val note: String = "",
    val favorite: Boolean = false,
    val icon: String? = null,
    @SerialName("icon_mime")
    val iconMime: String? = null,
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

@Serializable
private data class AegisGroup(
    val uuid: String,
    val name: String
)
