package com.xinto.mauth.core.backup.importer

class ImporterRegistry(private val importers: List<AccountImporter>) {

    fun detect(content: String): AccountImporter? {
        return importers.firstOrNull {
            try {
                it.sniff(content)
            } catch (_: Exception) {
                false
            }
        }
    }

    fun parse(source: ImportSource, content: String, password: CharArray?): ImportResult {
        val importer = importers.firstOrNull { it.source == source }
            ?: return ImportResult.Failure(source, ImportFailure.Unrecognized)

        val result = try {
            importer.parse(content, password)
        } catch (_: Exception) {
            ImportResult.Failure(source, ImportFailure.Malformed)
        }

        if (result !is ImportResult.Failure || result.reason == ImportFailure.WrongPassword) {
            return result
        }

        val detected = detect(content)
        if (detected != null && detected.source != source) {
            return ImportResult.WrongFormat(expected = source, actual = detected.source)
        }

        return result
    }

    fun parse(content: String, password: CharArray?): ImportResult {
        val importer = detect(content)
            ?: return ImportResult.Failure(ImportSource.Unknown, ImportFailure.Unrecognized)

        return try {
            importer.parse(content, password)
        } catch (_: Exception) {
            ImportResult.Failure(importer.source, ImportFailure.Malformed)
        }
    }
}
