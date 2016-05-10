package com.mapzen.erasermap.mock

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.view.Display
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

public class MockContext : Context() {

    public var startedActivity: Intent? = null

    override fun getPackageCodePath(): String? {
        throw UnsupportedOperationException()
    }

    override fun getNoBackupFilesDir(): File? {
        throw UnsupportedOperationException()
    }

    override fun getPackageResourcePath(): String? {
        throw UnsupportedOperationException()
    }

    override fun createConfigurationContext(overrideConfiguration: Configuration?): Context? {
        throw UnsupportedOperationException()
    }

    override fun getFileStreamPath(name: String?): File? {
        throw UnsupportedOperationException()
    }

    override fun unbindService(conn: ServiceConnection?) {
        throw UnsupportedOperationException()
    }

    override fun deleteDatabase(name: String?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun sendStickyOrderedBroadcastAsUser(intent: Intent?, user: UserHandle?, resultReceiver: BroadcastReceiver?, scheduler: Handler?, initialCode: Int, initialData: String?, initialExtras: Bundle?) {
        throw UnsupportedOperationException()
    }

    override fun unregisterReceiver(receiver: BroadcastReceiver?) {
        throw UnsupportedOperationException()
    }

    override fun getTheme(): Resources.Theme? {
        throw UnsupportedOperationException()
    }

    override fun enforcePermission(permission: String?, pid: Int, uid: Int, message: String?) {
        throw UnsupportedOperationException()
    }

    override fun openFileInput(name: String?): FileInputStream? {
        throw UnsupportedOperationException()
    }

    override fun getWallpaperDesiredMinimumWidth(): Int {
        throw UnsupportedOperationException()
    }

    override fun setWallpaper(bitmap: Bitmap?) {
        throw UnsupportedOperationException()
    }

    override fun setWallpaper(data: InputStream?) {
        throw UnsupportedOperationException()
    }

    override fun getPackageManager(): PackageManager? {
        throw UnsupportedOperationException()
    }

    override fun getDir(name: String?, mode: Int): File? {
        throw UnsupportedOperationException()
    }

    override fun getObbDir(): File? {
        throw UnsupportedOperationException()
    }

    override fun revokeUriPermission(uri: Uri?, modeFlags: Int) {
        throw UnsupportedOperationException()
    }

    override fun grantUriPermission(toPackage: String?, uri: Uri?, modeFlags: Int) {
        throw UnsupportedOperationException()
    }

    override fun getObbDirs(): Array<out File>? {
        throw UnsupportedOperationException()
    }

    override fun getDatabasePath(name: String?): File? {
        throw UnsupportedOperationException()
    }

    override fun getSystemService(name: String?): Any? {
        throw UnsupportedOperationException()
    }

    override fun getWallpaperDesiredMinimumHeight(): Int {
        throw UnsupportedOperationException()
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? {
        throw UnsupportedOperationException()
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?, broadcastPermission: String?, scheduler: Handler?): Intent? {
        throw UnsupportedOperationException()
    }

    override fun openFileOutput(name: String?, mode: Int): FileOutputStream? {
        throw UnsupportedOperationException()
    }

    override fun enforceCallingOrSelfUriPermission(uri: Uri?, modeFlags: Int, message: String?) {
        throw UnsupportedOperationException()
    }

    override fun getCacheDir(): File? {
        throw UnsupportedOperationException()
    }

    override fun enforceCallingOrSelfPermission(permission: String?, message: String?) {
        throw UnsupportedOperationException()
    }

    override fun removeStickyBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        throw UnsupportedOperationException()
    }

    override fun checkCallingOrSelfUriPermission(uri: Uri?, modeFlags: Int): Int {
        throw UnsupportedOperationException()
    }

    override fun getApplicationInfo(): ApplicationInfo? {
        throw UnsupportedOperationException()
    }

    override fun peekWallpaper(): Drawable? {
        throw UnsupportedOperationException()
    }

    override fun startActivities(intents: Array<out Intent>?) {
        throw UnsupportedOperationException()
    }

    override fun startActivities(intents: Array<out Intent>?, options: Bundle?) {
        throw UnsupportedOperationException()
    }

    override fun createPackageContext(packageName: String?, flags: Int): Context? {
        throw UnsupportedOperationException()
    }

    override fun enforceCallingPermission(permission: String?, message: String?) {
        throw UnsupportedOperationException()
    }

    override fun bindService(service: Intent?, conn: ServiceConnection?, flags: Int): Boolean {
        throw UnsupportedOperationException()
    }

    override fun sendOrderedBroadcastAsUser(intent: Intent?, user: UserHandle?, receiverPermission: String?, resultReceiver: BroadcastReceiver?, scheduler: Handler?, initialCode: Int, initialData: String?, initialExtras: Bundle?) {
        throw UnsupportedOperationException()
    }

    override fun startInstrumentation(className: ComponentName?, profileFile: String?, arguments: Bundle?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getApplicationContext(): Context? {
        throw UnsupportedOperationException()
    }

    override fun enforceUriPermission(uri: Uri?, pid: Int, uid: Int, modeFlags: Int, message: String?) {
        throw UnsupportedOperationException()
    }

    override fun enforceUriPermission(uri: Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int, message: String?) {
        throw UnsupportedOperationException()
    }

    override fun getContentResolver(): ContentResolver? {
        throw UnsupportedOperationException()
    }

    override fun getExternalFilesDir(type: String?): File? {
        throw UnsupportedOperationException()
    }

    override fun getExternalCacheDirs(): Array<out File>? {
        throw UnsupportedOperationException()
    }

    override fun checkCallingPermission(permission: String?): Int {
        throw UnsupportedOperationException()
    }

    override fun sendStickyBroadcast(intent: Intent?) {
        throw UnsupportedOperationException()
    }

    override fun deleteFile(name: String?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getMainLooper(): Looper? {
        throw UnsupportedOperationException()
    }

    override fun getWallpaper(): Drawable? {
        throw UnsupportedOperationException()
    }

    override fun sendBroadcast(intent: Intent?) {
        throw UnsupportedOperationException()
    }

    override fun sendBroadcast(intent: Intent?, receiverPermission: String?) {
        throw UnsupportedOperationException()
    }

    override fun createDisplayContext(display: Display?): Context? {
        throw UnsupportedOperationException()
    }

    override fun startService(service: Intent?): ComponentName? {
        throw UnsupportedOperationException()
    }

    override fun checkCallingOrSelfPermission(permission: String?): Int {
        throw UnsupportedOperationException()
    }

    override fun getExternalCacheDir(): File? {
        throw UnsupportedOperationException()
    }

    override fun fileList(): Array<out String>? {
        throw UnsupportedOperationException()
    }

    override fun openOrCreateDatabase(name: String?, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase? {
        throw UnsupportedOperationException()
    }

    override fun openOrCreateDatabase(name: String?, mode: Int, factory: SQLiteDatabase.CursorFactory?, errorHandler: DatabaseErrorHandler?): SQLiteDatabase? {
        throw UnsupportedOperationException()
    }

    override fun clearWallpaper() {
        throw UnsupportedOperationException()
    }

    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences? {
        throw UnsupportedOperationException()
    }

    override fun enforceCallingUriPermission(uri: Uri?, modeFlags: Int, message: String?) {
        throw UnsupportedOperationException()
    }

    override fun checkPermission(permission: String?, pid: Int, uid: Int): Int {
        throw UnsupportedOperationException()
    }

    override fun databaseList(): Array<out String>? {
        throw UnsupportedOperationException()
    }

    override fun getFilesDir(): File? {
        throw UnsupportedOperationException()
    }

    override fun sendStickyOrderedBroadcast(intent: Intent?, resultReceiver: BroadcastReceiver?, scheduler: Handler?, initialCode: Int, initialData: String?, initialExtras: Bundle?) {
        throw UnsupportedOperationException()
    }

    override fun startActivity(intent: Intent?) {
        startedActivity = intent
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        throw UnsupportedOperationException()
    }

    override fun getExternalFilesDirs(type: String?): Array<out File>? {
        throw UnsupportedOperationException()
    }

    override fun checkUriPermission(uri: Uri?, pid: Int, uid: Int, modeFlags: Int): Int {
        throw UnsupportedOperationException()
    }

    override fun checkUriPermission(uri: Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int): Int {
        throw UnsupportedOperationException()
    }

    override fun getExternalMediaDirs(): Array<out File>? {
        throw UnsupportedOperationException()
    }

    override fun getClassLoader(): ClassLoader? {
        throw UnsupportedOperationException()
    }

    override fun getAssets(): AssetManager? {
        throw UnsupportedOperationException()
    }

    override fun setTheme(resid: Int) {
        throw UnsupportedOperationException()
    }

    override fun checkCallingUriPermission(uri: Uri?, modeFlags: Int): Int {
        throw UnsupportedOperationException()
    }

    override fun startIntentSender(intent: IntentSender?, fillInIntent: Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int) {
        throw UnsupportedOperationException()
    }

    override fun startIntentSender(intent: IntentSender?, fillInIntent: Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int, options: Bundle?) {
        throw UnsupportedOperationException()
    }

    override fun removeStickyBroadcast(intent: Intent?) {
        throw UnsupportedOperationException()
    }

    override fun sendBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        throw UnsupportedOperationException()
    }

    override fun sendBroadcastAsUser(intent: Intent?, user: UserHandle?, receiverPermission: String?) {
        throw UnsupportedOperationException()
    }

    override fun sendOrderedBroadcast(intent: Intent?, receiverPermission: String?) {
        throw UnsupportedOperationException()
    }

    override fun sendOrderedBroadcast(intent: Intent?, receiverPermission: String?, resultReceiver: BroadcastReceiver?, scheduler: Handler?, initialCode: Int, initialData: String?, initialExtras: Bundle?) {
        throw UnsupportedOperationException()
    }

    override fun getResources(): Resources? {
        throw UnsupportedOperationException()
    }

    override fun getCodeCacheDir(): File? {
        throw UnsupportedOperationException()
    }

    override fun getPackageName(): String? {
        throw UnsupportedOperationException()
    }

    override fun stopService(service: Intent?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun sendStickyBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        throw UnsupportedOperationException()
    }

    override fun checkSelfPermission(permission: String?): Int {
        throw UnsupportedOperationException()
    }

    override fun getSystemServiceName(serviceClass: Class<*>?): String? {
        throw UnsupportedOperationException()
    }
}
