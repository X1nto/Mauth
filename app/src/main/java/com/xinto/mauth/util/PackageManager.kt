package com.xinto.mauth.util

import android.content.pm.PackageManager
import android.os.Build
import com.xinto.mauth.BuildConfig

val PackageManager.installerPackageName: String?
    get() {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getInstallSourceInfo(BuildConfig.APPLICATION_ID).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                getInstallerPackageName(BuildConfig.APPLICATION_ID)
            }
        } catch (e: Exception) {
            null
        }
    }