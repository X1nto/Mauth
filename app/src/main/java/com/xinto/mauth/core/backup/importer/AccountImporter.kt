package com.xinto.mauth.core.backup.importer

interface AccountImporter {

    val source: ImportSource

    fun sniff(content: String): Boolean

    fun parse(content: String, password: CharArray?): ImportResult
}
