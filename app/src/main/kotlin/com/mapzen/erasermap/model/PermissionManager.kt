package com.mapzen.erasermap.model

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.mapzen.erasermap.R
import com.mapzen.erasermap.controller.MainActivity

/**
 * Manages all things runtime permissions.
 */
class PermissionManager {

    var activity: Activity? = null
    var granted: Boolean = false

    fun permissionsRequired(): Boolean {
        if (activity == null) {
            throw IllegalStateException("Must set activity for PermissionManager")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val fineLocation = ContextCompat.checkSelfPermission(activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (fineLocation != PackageManager.PERMISSION_GRANTED) {
                return true
            } else {
                return false
            }
        }
        return false
    }

    fun requestPermissions() {
        if (activity == null) {
            throw IllegalStateException("Must set activity for PermissionManager")
        }
        ActivityCompat.requestPermissions(activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MainActivity.PERMISSIONS_REQUEST);

    }

    fun permissionsGranted(): Boolean {
        return granted
    }

    fun grantPermissions() {
        granted = true
    }

    fun showPermissionRequired() {
        Toast.makeText(activity, R.string.need_permissions, Toast.LENGTH_SHORT).show()
    }
}