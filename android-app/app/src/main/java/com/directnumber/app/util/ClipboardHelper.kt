package com.directnumber.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * Clipboard is only ever touched in direct response to a user tap — copy or paste buttons —
 * never read automatically on screen open or text change.
 */
object ClipboardHelper {

    fun copy(context: Context, label: String, text: String) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    /** Returns the clipboard's plain text, or null if empty/unavailable. Call only from a user action. */
    fun paste(context: Context): String? {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = manager.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        return clip.getItemAt(0).coerceToText(context)?.toString()
    }
}
