package com.xinto.mauth.backup

import com.xinto.mauth.core.backup.crypto.BackupCrypto
import com.xinto.mauth.core.backup.importer.AccountImporter
import com.xinto.mauth.core.backup.importer.AegisImporter
import com.xinto.mauth.core.backup.importer.AndOtpImporter
import com.xinto.mauth.core.backup.importer.BitwardenImporter
import com.xinto.mauth.core.backup.importer.FreeOtpPlusImporter
import com.xinto.mauth.core.backup.importer.ImportFailure
import com.xinto.mauth.core.backup.importer.ImportResult
import com.xinto.mauth.core.backup.importer.ImportSource
import com.xinto.mauth.core.backup.importer.ImporterRegistry
import com.xinto.mauth.core.backup.importer.ProtonImporter
import com.xinto.mauth.core.backup.importer.StratumImporter
import com.xinto.mauth.core.backup.importer.TwoFasImporter
import com.xinto.mauth.core.backup.importer.UriListImporter
import com.xinto.mauth.core.backup.model.BackupOtpAlgorithm
import com.xinto.mauth.core.backup.model.BackupOtpType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = android.app.Application::class)
class ImporterTest {

    private val crypto = BackupCrypto()

    private val registry = ImporterRegistry(
        listOf(
            AegisImporter(crypto),
            TwoFasImporter(crypto),
            AndOtpImporter(crypto),
            BitwardenImporter(crypto),
            FreeOtpPlusImporter(),
            StratumImporter(),
            ProtonImporter(crypto),
            UriListImporter()
        )
    )

    private fun fixture(name: String): String {
        return checkNotNull(javaClass.classLoader?.getResourceAsStream("importers/$name")) {
            "Missing fixture importers/$name"
        }.use { it.readBytes().toString(Charsets.UTF_8) }
    }

    private fun parse(name: String): ImportResult.Success {
        val result = registry.parse(fixture(name), password = null)
        assertTrue("Expected success but got $result", result is ImportResult.Success)
        return result as ImportResult.Success
    }

    private fun assertSharedAccounts(result: ImportResult.Success) {
        assertEquals(2, result.accounts.size)

        val totp = result.accounts[0]
        assertEquals("alice@example.com", totp.name)
        assertEquals("GitHub", totp.issuer)
        assertEquals("ABCDEFGHIJKLMNOP", totp.secret)
        assertEquals(BackupOtpType.TOTP, totp.type)
        assertEquals(BackupOtpAlgorithm.SHA1, totp.algorithm)
        assertEquals(6, totp.digits)
        assertEquals(30, totp.period)
        assertEquals(null, totp.counter)

        val hotp = result.accounts[1]
        assertEquals("bob@example.com", hotp.name)
        assertEquals("Example", hotp.issuer)
        assertEquals("QRSTUVWXYZ234567", hotp.secret)
        assertEquals(BackupOtpType.HOTP, hotp.type)
        assertEquals(BackupOtpAlgorithm.SHA256, hotp.algorithm)
        assertEquals(8, hotp.digits)
        assertEquals(42, hotp.counter)
        assertEquals(null, hotp.period)
    }

    @Test
    fun `aegis vault is detected and read`() {
        val result = parse("aegis-plain.json")

        assertEquals(ImportSource.Aegis, result.source)
        assertSharedAccounts(result)
        assertEquals(1, result.groups.size)
        assertEquals("Work", result.groups[0].name)
        assertEquals(result.groups[0].id, result.accounts[0].group)
        assertEquals("01234567-89ab-cdef-0123-456789abcdef", result.accounts[0].id)
    }

    @Test
    fun `2fas backup is detected and read`() {
        val result = parse("2fas-plain.json")

        assertEquals(ImportSource.TwoFas, result.source)
        assertSharedAccounts(result)
    }

    @Test
    fun `andotp backup is detected and read`() {
        val result = parse("andotp-plain.json")

        assertEquals(ImportSource.AndOtp, result.source)
        assertSharedAccounts(result)
    }

    @Test
    fun `bitwarden csv export is detected and read`() {
        val result = parse("bitwarden-plain.csv")

        assertEquals(ImportSource.Bitwarden, result.source)
        assertSharedAccounts(result)
    }

    @Test
    fun `stratum backup is detected and read`() {
        val result = parse("stratum-plain.json")

        assertEquals(ImportSource.Stratum, result.source)
        assertSharedAccounts(result)
    }

    @Test
    fun `stratum rejects generators mauth cannot produce`() {
        val result = parse("stratum-plain.json")

        assertEquals(1, result.rejected.size)
        assertEquals("Steam", result.rejected[0].issuer)
    }

    @Test
    fun `proton export is detected and read`() {
        val result = parse("proton-plain.json")

        assertEquals(ImportSource.Proton, result.source)
        assertSharedAccounts(result)
    }

    @Test
    fun `proton rejects steam entries`() {
        val result = parse("proton-plain.json")

        assertEquals(1, result.rejected.size)
        assertEquals("Steam", result.rejected[0].name)
    }

    @Test
    fun `bitwarden export is detected and read`() {
        val result = parse("bitwarden-plain.json")

        assertEquals(ImportSource.Bitwarden, result.source)
        assertEquals(2, result.accounts.size)
        assertEquals("ABCDEFGHIJKLMNOP", result.accounts[0].secret)
        assertEquals("GitHub", result.accounts[0].issuer)
        assertEquals("QRSTUVWXYZ234567", result.accounts[1].secret)
    }

    @Test
    fun `freeotp plus signed secret bytes are decoded`() {
        val result = parse("freeotpplus-plain.json")

        assertEquals(ImportSource.FreeOtpPlus, result.source)
        assertEquals(2, result.accounts.size)
        assertEquals("ABCDEFGHIJKLMNOP", result.accounts[0].secret)
        assertEquals("QRSTUVWXYZ234567", result.accounts[1].secret)
        assertEquals(42, result.accounts[1].counter)
    }

    @Test
    fun `a plain list of links is read`() {
        val result = parse("urilist.txt")

        assertEquals(ImportSource.UriList, result.source)
        assertSharedAccounts(result)
    }

    @Test
    fun `a list saved on windows is read and stray lines are rejected`() {
        val content = "﻿" + fixture("urilist.txt").lines()
            .filter { it.isNotEmpty() }
            .joinToString("\r\n", postfix = "\r\n\r\nnot a link\r\notpauth://totp/Acme%20Corp:carol?digits=6\r\n")

        val result = registry.parse(content, password = null) as ImportResult.Success

        assertEquals(ImportSource.UriList, result.source)
        assertSharedAccounts(result)
        assertEquals(2, result.rejected.size)
        assertEquals("", result.rejected[0].name)
        assertEquals(ImportFailure.Unrecognized, result.rejected[0].reason)
        assertEquals("carol", result.rejected[1].name)
        assertEquals("Acme Corp", result.rejected[1].issuer)
        assertEquals(ImportFailure.Malformed, result.rejected[1].reason)
    }

    @Test
    fun `an unknown file is not claimed by any importer`() {
        val result = registry.parse("hello, world", password = null)

        assertTrue(result is ImportResult.Failure)
    }

    @Test
    fun `each fixture is claimed by exactly the expected importer`() {
        val expected = mapOf(
            "aegis-plain.json" to ImportSource.Aegis,
            "2fas-plain.json" to ImportSource.TwoFas,
            "andotp-plain.json" to ImportSource.AndOtp,
            "bitwarden-plain.json" to ImportSource.Bitwarden,
            "freeotpplus-plain.json" to ImportSource.FreeOtpPlus,
            "urilist.txt" to ImportSource.UriList
        )

        for ((file, source) in expected) {
            val importer: AccountImporter? = registry.detect(fixture(file))
            assertNotNull("Nothing claimed $file", importer)
            assertEquals(file, source, importer!!.source)
        }
    }
}
