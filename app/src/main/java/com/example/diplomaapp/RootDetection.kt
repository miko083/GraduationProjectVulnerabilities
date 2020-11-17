package com.example.diplomaapp

import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class RootDetection {
        fun isDeviceRooted(): Boolean {
            return checkKeys() || checkBinaries() || checkXbin()
        }

        private fun checkKeys(): Boolean {
            val buildTags = Build.TAGS
            return buildTags != null && buildTags.contains("test-keys")
        }

        private fun checkBinaries(): Boolean {
            val paths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/su/bin/su"
            )
            for (path in paths) {
                if (File(path).exists()) return true
            }
            return false
        }

        private fun checkXbin(): Boolean {
            var process: Process? = null
            return try {
                process = Runtime.getRuntime()
                    .exec(arrayOf("/system/xbin/which", "su"))
                val `in` = BufferedReader(InputStreamReader(process.inputStream))
                if (`in`.readLine() != null) true else false
            } catch (t: Throwable) {
                false
            } finally {
                process?.destroy()
            }
        }
}