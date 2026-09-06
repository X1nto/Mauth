package com.xinto.mauth.core.backup.exporter

import com.xinto.mauth.core.backup.model.BackupData

interface BackupExporter {

    fun export(contents: BackupData, password: CharArray?): String

}
