package com.xinto.mauth

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.xinto.mauth.db.AccountDatabase
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.util.Locale
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    private val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AccountDatabase::class.java
    )

    @Test
    fun migrate1To2() {
        val id = uuid(1)
        migrationTestHelper.createDatabase(testDb, 1).use { db ->
            db.execSQL(
                "INSERT INTO accounts (id, secret, label, issuer) VALUES (?, ?, ?, ?)",
                arrayOf(uuidBytes(id), "JBSWY3DPEHPK3PXP", "Alice", "Example")
            )
        }

        val db = migrationTestHelper.runMigrationsAndValidate(testDb, 2, true)

        db.query(
            "SELECT label, issuer, algorithm, type, digits, counter, period FROM accounts"
        ).use { c ->
            Assert.assertEquals(1, c.count)
            Assert.assertTrue(c.moveToFirst())
            Assert.assertEquals("Alice", c.getString(0))
            Assert.assertEquals("Example", c.getString(1))
            Assert.assertEquals(0, c.getInt(2)) // algorithm
            Assert.assertEquals(0, c.getInt(3)) // type
            Assert.assertEquals(6, c.getInt(4)) // digits
            Assert.assertEquals(0, c.getInt(5)) // counter
            Assert.assertEquals(30, c.getInt(6)) // period
        }
        db.close()
    }

    @Test
    fun migrate2To3() {
        val id = uuid(2)
        migrationTestHelper.createDatabase(testDb, 2).use { db ->
            db.execSQL(
                "INSERT INTO accounts " +
                    "(id, secret, label, issuer, algorithm, type, digits, counter, period) " +
                    "VALUES (?, ?, ?, ?, 1, 1, 8, 5, 60)",
                arrayOf(uuidBytes(id), "SECRET", "Bob", "Acme")
            )
        }

        val db = migrationTestHelper.runMigrationsAndValidate(testDb, 3, true)

        db.query(
            "SELECT icon, label, algorithm, type, digits, counter, period FROM accounts"
        ).use { c ->
            Assert.assertEquals(1, c.count)
            Assert.assertTrue(c.moveToFirst())
            Assert.assertTrue("icon should be NULL after migration", c.isNull(0))
            Assert.assertEquals("Bob", c.getString(1))
            Assert.assertEquals(1, c.getInt(2))   // algorithm preserved
            Assert.assertEquals(1, c.getInt(3))   // type preserved
            Assert.assertEquals(8, c.getInt(4))   // digits preserved
            Assert.assertEquals(5, c.getInt(5))   // counter preserved
            Assert.assertEquals(60, c.getInt(6))  // period preserved
        }
        db.close()
    }

    @Test
    fun migrate3To4() {
        val id = uuid(3)
        migrationTestHelper.createDatabase(testDb, 3).use { db ->
            db.execSQL(
                "INSERT INTO accounts " +
                    "(id, icon, secret, label, issuer, algorithm, type, digits, counter, period) " +
                    "VALUES (?, ?, ?, ?, ?, 0, 1, 6, 7, 30)",
                arrayOf(uuidBytes(id), "https://example.com/icon.png", "SECRET", "Carol", "HOTPCorp")
            )
        }

        val db = migrationTestHelper.runMigrationsAndValidate(testDb, 4, true, AccountDatabase.Migrate3to4)

        db.query("SELECT count FROM countdata WHERE account_id = ?", arrayOf(uuidBytes(id))).use { c ->
            Assert.assertEquals(1, c.count)
            Assert.assertTrue(c.moveToFirst())
            Assert.assertEquals(7, c.getInt(0))
        }

        db.query("SELECT icon, label, type FROM accounts").use { c ->
            Assert.assertEquals(1, c.count)
            Assert.assertTrue(c.moveToFirst())
            Assert.assertEquals("https://example.com/icon.png", c.getString(0))
            Assert.assertEquals("Carol", c.getString(1))
            Assert.assertEquals(1, c.getInt(2))
        }
        db.close()
    }

    @Test
    fun migrate4To5() {
        val id = uuid(4)
        migrationTestHelper.createDatabase(testDb, 4).use { db ->
            db.execSQL(
                "INSERT INTO accounts " +
                    "(id, icon, secret, label, issuer, algorithm, type, digits, period) " +
                    "VALUES (?, NULL, ?, ?, ?, 0, 0, 6, 30)",
                arrayOf(uuidBytes(id), "SECRET", "Dave", "Example")
            )
        }

        val db = migrationTestHelper.runMigrationsAndValidate(testDb, 5, true, AccountDatabase.Migrate4To5)

        db.query("SELECT label, create_date FROM accounts").use { c ->
            Assert.assertEquals(1, c.count)
            Assert.assertTrue(c.moveToFirst())
            Assert.assertEquals("Dave", c.getString(0))
            Assert.assertTrue(
                "create_date should be backfilled to a real timestamp, not the 0 default",
                c.getLong(1) > 0L
            )
        }
        db.close()
    }

    @Test
    fun migrate5To6() {
        val id = uuid(5)
        migrationTestHelper.createDatabase(testDb, 5).use { db ->
            db.execSQL(
                "INSERT INTO accounts " +
                    "(id, icon, secret, label, issuer, algorithm, type, digits, period, create_date) " +
                    "VALUES (?, NULL, ?, ?, ?, 0, 0, 6, 30, 1700000000)",
                arrayOf(uuidBytes(id), "JBSWY3DPEHPK3PXP", "Eve", "Example")
            )
        }

        val db = migrationTestHelper.runMigrationsAndValidate(testDb, 6, true)

        db.query("SELECT label, group_id FROM accounts").use { c ->
            Assert.assertEquals(1, c.count)
            Assert.assertTrue(c.moveToFirst())
            Assert.assertEquals("Eve", c.getString(0))
            Assert.assertTrue("group_id should be NULL after migration", c.isNull(1))
        }
        db.query("SELECT COUNT(*) FROM `groups`").use { c ->
            Assert.assertTrue(c.moveToFirst())
            Assert.assertEquals(0, c.getInt(0))
        }
        db.close()
    }

    @Test
    fun migrate1To6() {
        val id = uuid(6)
        migrationTestHelper.createDatabase(testDb, 1).use { db ->
            db.execSQL(
                "INSERT INTO accounts (id, secret, label, issuer) VALUES (?, ?, ?, ?)",
                arrayOf(uuidBytes(id), "JBSWY3DPEHPK3PXP", "Frank", "Example")
            )
        }

        val db = migrationTestHelper.runMigrationsAndValidate(
            testDb, 6, true,
            AccountDatabase.Migrate3to4, AccountDatabase.Migrate4To5
        )

        db.query(
            "SELECT label, issuer, icon, algorithm, type, digits, period, create_date, group_id " +
                "FROM accounts"
        ).use { c ->
            Assert.assertEquals(1, c.count)
            Assert.assertTrue(c.moveToFirst())
            Assert.assertEquals("Frank", c.getString(0))
            Assert.assertEquals("Example", c.getString(1))
            Assert.assertTrue("icon added at v3 as NULL", c.isNull(2))
            Assert.assertEquals(0, c.getInt(3))   // algorithm default
            Assert.assertEquals(0, c.getInt(4))   // type default
            Assert.assertEquals(6, c.getInt(5))   // digits default
            Assert.assertEquals(30, c.getInt(6))  // period default
            Assert.assertTrue("create_date backfilled at 4->5", c.getLong(7) > 0L)
            Assert.assertTrue("group_id added at v6 as NULL", c.isNull(8))
        }

        db.query("SELECT count FROM countdata WHERE account_id = ?", arrayOf(uuidBytes(id))).use { c ->
            Assert.assertEquals(1, c.count)
            Assert.assertTrue(c.moveToFirst())
            Assert.assertEquals(0, c.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM `groups`").use { c ->
            Assert.assertTrue(c.moveToFirst())
            Assert.assertEquals(0, c.getInt(0))
        }
        db.close()
    }

    private fun uuidBytes(uuid: UUID): ByteArray =
        ByteBuffer.allocate(16)
            .putLong(uuid.mostSignificantBits)
            .putLong(uuid.leastSignificantBits)
            .array()

    private fun uuid(last: Int): UUID =
        UUID.fromString("00000000-0000-0000-0000-%012d".format(Locale.ROOT, last))
}