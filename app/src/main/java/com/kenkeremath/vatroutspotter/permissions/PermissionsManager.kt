package com.kenkeremath.vatroutspotter.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface PermissionsManager {
    val hasNotificationPermission: StateFlow<Boolean>
    fun checkNotificationPermission()
    fun shouldShowNotificationPermissionRationale(activity: android.app.Activity): Boolean
    fun requestNotificationPermission(permissionLauncher: ActivityResultLauncher<String>)
}

@Singleton
class AndroidPermissionsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PermissionsManager {

    private val _hasNotificationPermission = MutableStateFlow(false)
    override val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission

    override fun checkNotificationPermission() {
        _hasNotificationPermission.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Prior to Android 13, notifications were enabled by default
        }
    }

    override fun shouldShowNotificationPermissionRationale(activity: android.app.Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            false
        }
    }

    override fun requestNotificationPermission(permissionLauncher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
} 