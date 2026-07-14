package com.directnumber.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

private const val VCARD_DIR = "vcards"
private const val MAX_AGE_MILLIS = 60 * 60 * 1000L // 1 hour

/**
 * Writes a vCard string to a temp file under cache/vcards/ and exposes it as a content://
 * Uri through FileProvider (declared in res/xml/file_paths.xml) — never a raw file:// Uri,
 * which the platform blocks for cross-app sharing since Android 7.
 */
object VCardGenerator {

    /** @return the shareable content:// Uri, or null if the file could not be written. */
    fun writeToCache(context: Context, vCardContent: String, fileNameHint: String): Uri? {
        return try {
            val dir = File(context.cacheDir, VCARD_DIR).apply { mkdirs() }
            deleteOlderThan(dir, MAX_AGE_MILLIS)
            val safeHint = fileNameHint.ifBlank { "contact" }
                .filter { it.isLetterOrDigit() || it == '_' || it == '-' }
                .take(40)
                .ifBlank { "contact" }
            val file = File(dir, "${safeHint}_${System.currentTimeMillis()}.vcf")
            file.writeText(vCardContent, Charsets.UTF_8)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: IOException) {
            null
        }
    }

    fun buildShareIntent(uri: Uri): Intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/x-vcard"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    /** Best-effort cleanup of stale temp cards; failures are silently ignored. */
    private fun deleteOlderThan(dir: File, maxAgeMillis: Long) {
        val now = System.currentTimeMillis()
        dir.listFiles()?.forEach { file ->
            if (now - file.lastModified() > maxAgeMillis) {
                file.delete()
            }
        }
    }
}
