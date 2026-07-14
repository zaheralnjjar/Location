package com.directnumber.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.directnumber.app.data.model.WhatsAppTarget

sealed class WhatsAppLaunchResult {
    data object Success : WhatsAppLaunchResult()
    data class NotInstalled(val target: WhatsAppTarget) : WhatsAppLaunchResult()
}

/**
 * Opens a specific WhatsApp variant with an explicit package name (never an unqualified
 * ACTION_VIEW) so the correct app opens — regular or Business — with no ambiguity, and no
 * silent fallback to the other one if it's missing.
 */
object WhatsAppLauncher {
    const val PACKAGE_REGULAR = "com.whatsapp"
    const val PACKAGE_BUSINESS = "com.whatsapp.w4b"

    fun packageNameFor(target: WhatsAppTarget): String? = when (target) {
        WhatsAppTarget.REGULAR -> PACKAGE_REGULAR
        WhatsAppTarget.BUSINESS -> PACKAGE_BUSINESS
        WhatsAppTarget.ASK_EVERY_TIME -> null
    }

    fun isInstalled(context: Context, target: WhatsAppTarget): Boolean {
        val packageName = packageNameFor(target) ?: return false
        return AppPackageChecker.isInstalled(context, packageName)
    }

    fun open(context: Context, target: WhatsAppTarget, chatLink: String): WhatsAppLaunchResult {
        val packageName = packageNameFor(target) ?: return WhatsAppLaunchResult.NotInstalled(target)
        if (!AppPackageChecker.isInstalled(context, packageName)) {
            return WhatsAppLaunchResult.NotInstalled(target)
        }
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(chatLink)).apply {
                setPackage(packageName)
            }
            context.startActivity(intent)
            WhatsAppLaunchResult.Success
        } catch (e: ActivityNotFoundException) {
            WhatsAppLaunchResult.NotInstalled(target)
        }
    }

    fun playStoreLink(packageName: String): String = "https://play.google.com/store/apps/details?id=$packageName"
}
