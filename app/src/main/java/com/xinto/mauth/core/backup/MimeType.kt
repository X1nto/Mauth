package com.xinto.mauth.core.backup

enum class MimeType(
    val value: String,
    val label: String? = null,
    val extension: String? = null,
    val aliases: List<String> = emptyList()
) {
    Json("application/json", label = "JSON", extension = "json"),
    Csv("text/csv", label = "CSV", extension = "csv", aliases = listOf("text/comma-separated-values")),
    PlainText("text/plain", label = "TXT", extension = "txt"),
    Binary("application/octet-stream");
}

fun List<MimeType>.toFilterArray(): Array<String> {
    return this
        .flatMap { it.aliases + it.value }
        .distinct()
        .toTypedArray()
}

fun List<MimeType>.labels(): String? {
    return this
        .mapNotNull { it.label }
        .distinct()
        .takeIf { it.isNotEmpty() }
        ?.joinToString(", ")
}
