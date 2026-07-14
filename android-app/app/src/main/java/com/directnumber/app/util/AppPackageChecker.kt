package com.directnumber.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Checks whether a package is installed. Relies on the <queries> declarations in the
 * manifest so this keeps working under Android 11+ package-visibility restrictions.
 */
object AppPackageChecker {

    fun isInstalled(context: Context, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
