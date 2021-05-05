package com.ebner.roomdatabasebackup.core

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.room.RoomDatabase
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.io.Files.copy
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.comparator.LastModifiedFileComparator
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 *  MIT License
 *
 *  Copyright (c) 2021 Raphael Ebner
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
class RoomBackup {

    companion object {
        private const val SHARED_PREFS = "com.ebner.roomdatabasebackup"
        private var TAG = "debug_RoomBackup"
        private lateinit var INTERNAL_BACKUP_PATH: File
        private lateinit var TEMP_BACKUP_PATH: File
        private lateinit var TEMP_BACKUP_FILE: File
        private lateinit var EXTERNAL_BACKUP_PATH: File
        private lateinit var DATABASE_FILE: File
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbName: String

    private var context: Context? = null
    private var roomDatabase: RoomDatabase? = null
    private var enableLogDebug: Boolean = false
    private var restartIntent: Intent? = null
    private var onCompleteListener: OnCompleteListener? = null
    private var customRestoreDialogTitle: String = "Choose file to restore"
    private var customBackupFileName: String? = null
    private var useExternalStorage: Boolean = false
    private var backupIsEncrypted: Boolean = false
    private var maxFileCount: Int? = null
    private var encryptPassword: String? = null

    /**
     * Set Context
     *
     * @param context Context
     */
    fun context(context: Context): RoomBackup {
        this.context = context
        return this
    }

    /**
     * Set RoomDatabase instance
     *
     * @param roomDatabase RoomDatabase
     */
    fun database(roomDatabase: RoomDatabase): RoomBackup {
        this.roomDatabase = roomDatabase
        return this
    }

    /**
     * Set LogDebug enabled / disabled
     *
     * @param enableLogDebug Boolean
     */
    fun enableLogDebug(enableLogDebug: Boolean): RoomBackup {
        this.enableLogDebug = enableLogDebug
        return this
    }

    /**
     * Set Intent in which to boot after App restart
     *
     * @param restartIntent Intent
     */
    fun restartApp(restartIntent: Intent): RoomBackup {
        this.restartIntent = restartIntent
        restartApp()
        return this
    }

    /**
     * Set onCompleteListener, to run code when tasks completed
     *
     * @param onCompleteListener OnCompleteListener
     */
    fun onCompleteListener(onCompleteListener: OnCompleteListener): RoomBackup {
        this.onCompleteListener = onCompleteListener
        return this
    }

    /**
     * Set onCompleteListener, to run code when tasks completed
     *
     * @param listener (success: Boolean, message: String) -> Unit
     */
    fun onCompleteListener(listener: (success: Boolean, message: String) -> Unit): RoomBackup {
        this.onCompleteListener = object : OnCompleteListener {
            override fun onComplete(success: Boolean, message: String) {
                listener(success, message)
            }
        }
        return this
    }

    /**
     * Set custom log tag, for detailed debugging
     *
     * @param customLogTag String
     */
    fun customLogTag(customLogTag: String): RoomBackup {
        TAG = customLogTag
        return this
    }

    /**
     * Set custom Restore Dialog Title, default = "Choose file to restore"
     *
     * @param customRestoreDialogTitle String
     */
    fun customRestoreDialogTitle(customRestoreDialogTitle: String): RoomBackup {
        this.customRestoreDialogTitle = customRestoreDialogTitle
        return this
    }

    /**
     * Set custom Backup File Name, default = "$dbName-$currentTime.sqlite3"
     *
     * @param customBackupFileName String
     */
    fun customBackupFileName(customBackupFileName: String): RoomBackup {
        this.customBackupFileName = customBackupFileName
        return this
    }

    /**
     * Set export / import to External Storage enabled / disabled, if you want to export / import the backup to / from external storage
     * then you have access to the backup and can save it somewhere else
     *
     *
     * @param useExternalStorage Boolean, default = false
     */
    fun useExternalStorage(useExternalStorage: Boolean): RoomBackup {
        this.useExternalStorage = useExternalStorage
        return this
    }

    /**
     * Set file encryption to true / false
     * can be used for backup and restore
     *
     *
     * @param backupIsEncrypted Boolean, default = false
     */
    fun backupIsEncrypted(backupIsEncrypted: Boolean): RoomBackup {
        this.backupIsEncrypted = backupIsEncrypted
        return this
    }

    /**
     * Set max backup files count
     * if fileCount is > maxFileCount the oldest backup file will be deleted
     * is for both internal and external storage
     *
     *
     * @param maxFileCount Int, default = null
     */
    fun maxFileCount(maxFileCount: Int): RoomBackup {
        this.maxFileCount = maxFileCount
        return this
    }

    /**
     * Set custom backup encryption password
     *
     * @param encryptPassword String
     */

    fun customEncryptPassword(encryptPassword: String): RoomBackup {
        this.encryptPassword = encryptPassword
        return this
    }

    /**
     * Init vars, and return true if no error occurred
     */
    private fun initRoomBackup(): Boolean {
        if (context == null) {
            if (enableLogDebug) Log.d(TAG, "context is missing")
            onCompleteListener?.onComplete(false, "context is missing")
            //        throw IllegalArgumentException("context is not initialized")
            return false
        }
        if (roomDatabase == null) {
            if (enableLogDebug) Log.d(TAG, "roomDatabase is missing")
            onCompleteListener?.onComplete(false, "roomDatabase is missing")
            //       throw IllegalArgumentException("roomDatabase is not initialized")
            return false
        }

        //Create or retrieve the Master Key for encryption/decryption
        val masterKeyAlias = MasterKey.Builder(context!!)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        //Initialize/open an instance of EncryptedSharedPreferences
        //Encryption key is stored in plain text in an EncryptedSharedPreferences --> it is saved encrypted
        sharedPreferences = EncryptedSharedPreferences.create(
            context!!,
            SHARED_PREFS,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        dbName = roomDatabase!!.openHelper.databaseName
        INTERNAL_BACKUP_PATH = File("${context!!.filesDir}/databasebackup/")
        TEMP_BACKUP_PATH = File("${context!!.filesDir}/databasebackup-temp/")
        TEMP_BACKUP_FILE = File("$TEMP_BACKUP_PATH/tempbackup.sqlite3")
        EXTERNAL_BACKUP_PATH = File(context!!.getExternalFilesDir("backup")!!.toURI())
        DATABASE_FILE = File(context!!.getDatabasePath(dbName).toURI())

        //Create internal and temp backup directory if does not exist
        try {
            INTERNAL_BACKUP_PATH.mkdirs()
            TEMP_BACKUP_PATH.mkdirs()
        } catch (e: FileAlreadyExistsException) {
        } catch (e: IOException) {
        }

        if (enableLogDebug) {
            Log.d(TAG, "DatabaseName: $dbName")
            Log.d(TAG, "Database Location: $DATABASE_FILE")
            Log.d(TAG, "INTERNAL_BACKUP_PATH: $INTERNAL_BACKUP_PATH")
            Log.d(TAG, "EXTERNAL_BACKUP_PATH: $EXTERNAL_BACKUP_PATH")
        }
        return true

    }

    /**
     * restart App with custom Intent
     */
    private fun restartApp() {
        restartIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context!!.startActivity(restartIntent)
        //   finish()
        Runtime.getRuntime().exit(0)

    }

    /**
     * Start Backup process, and set onComplete Listener to success, if no error occurred, else onComplete Listener success is false and error message is passed
     */
    fun backup() {
        if (enableLogDebug) Log.d(TAG, "Starting Backup ...")
        val success = initRoomBackup()
        if (!success) return

        //Close the database
        // roomDatabase!!.close()
        roomDatabase!!.query(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))

        //Create name for backup file, if no custom name is set: Database name + currentTime + .sqlite3
        var filename = if (customBackupFileName == null) "$dbName-${getTime()}.sqlite3" else customBackupFileName as String
        //Add .aes extension to filename, if file is encrypted
        if (backupIsEncrypted) filename += ".aes"

        //Path to save current database
        val backuppath = if (useExternalStorage) File("$EXTERNAL_BACKUP_PATH/$filename") else File("$INTERNAL_BACKUP_PATH/$filename")

        if (backupIsEncrypted) encryptBackupFile(backuppath)
        else {
            //Copy current database to save location (/files dir)
            copy(DATABASE_FILE, backuppath)

            if (enableLogDebug) Log.d(TAG, "Saved to: $backuppath")

            //If maxFileCount is set and is reached, delete oldest file
            if (maxFileCount != null) {
                val deleted = deleteOldBackup()
                if (!deleted) return
            }

            onCompleteListener?.onComplete(true, "success")
        }
    }

    /**
     * Encrypt backup file, and save it
     *
     * @param backuppath Path, where to save the backup (internal or external storage)
     */
    private fun encryptBackupFile(backuppath: File) {
        try {

            //Copy database you want to backup to temp directory
            copy(DATABASE_FILE, TEMP_BACKUP_FILE)


            //encrypt temp file, and save it to backup location
            val encryptDecryptBackup = AESEncryptionHelper()
            val fileData = encryptDecryptBackup.readFile(TEMP_BACKUP_FILE)

            val aesEncryptionManager = AESEncryptionManager()
            val encryptedBytes = aesEncryptionManager.encryptData(sharedPreferences, encryptPassword, fileData)

            encryptDecryptBackup.saveFile(encryptedBytes, backuppath)

            //Delete temp file
            TEMP_BACKUP_FILE.delete()

            if (enableLogDebug) Log.d(TAG, "Saved and encrypted to: $backuppath")

            //If maxFileCount is set and is reached, delete oldest file
            if (maxFileCount != null) {
                val deleted = deleteOldBackup()
                if (!deleted) return
            }

            onCompleteListener?.onComplete(true, "saved and encrypted")

        } catch (e: Exception) {
            if (enableLogDebug) Log.d(TAG, "error during encryption: ${e.message}")
            onCompleteListener?.onComplete(false, "error during encryption")
            return
            //    throw Exception("error during encryption: $e")
        }
    }

    /**
     * Decrypt backup file, and save it
     *
     * @param backuppath Path, where to find the backup to restore (internal or external storage)
     */
    private fun decryptBackupFile(backuppath: File) {
        try {
            //Copy database you want to restore to temp directory
            copy(backuppath, TEMP_BACKUP_FILE)

            //Decrypt temp file, and save it to database location
            val encryptDecryptBackup = AESEncryptionHelper()
            val fileData = encryptDecryptBackup.readFile(TEMP_BACKUP_FILE)

            val aesEncryptionManager = AESEncryptionManager()
            val decryptedBytes = aesEncryptionManager.decryptData(sharedPreferences, encryptPassword, fileData)

            encryptDecryptBackup.saveFile(decryptedBytes, DATABASE_FILE)

            //Delete tem file
            TEMP_BACKUP_FILE.delete()

            if (enableLogDebug) Log.d(TAG, "restored and decrypted from / to $backuppath")
            onCompleteListener?.onComplete(true, "restored and decrypted")

        } catch (e: Exception) {
            if (enableLogDebug) Log.d(TAG, "error during decryption: ${e.message}")
            onCompleteListener?.onComplete(false, "error during decryption (maybe wrong password) see Log for more details (if enabled)")
            return
            //   throw Exception("error during decryption: $e")
        }

    }

    /**
     * @return current time formatted as String
     */
    private fun getTime(): String {

        val currentTime = Calendar.getInstance().time

        val sdf = if (android.os.Build.VERSION.SDK_INT <= 28 && useExternalStorage) {
            SimpleDateFormat("yyyy-MM-dd-HH_mm_ss", Locale.getDefault())
        } else {
            SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault())
        }

        return sdf.format(currentTime)

    }

    /**
     * If maxFileCount is set, and reached, all old files will be deleted
     *
     * @return true if old files deleted or nothing to do
     */
    private fun deleteOldBackup(): Boolean {
        //Path of Backup Directory
        val backupDirectory = if (useExternalStorage) File("$EXTERNAL_BACKUP_PATH/") else INTERNAL_BACKUP_PATH

        //All Files in an Array of type File
        val arrayOfFiles = backupDirectory.listFiles()

        //If array is null or empty nothing to do and return
        if (arrayOfFiles.isNullOrEmpty()) {
            if (enableLogDebug) Log.d(TAG, "")
            onCompleteListener?.onComplete(false, "maxFileCount: Failed to get list of backups")
            return false
        } else if (arrayOfFiles.size > maxFileCount!!) {
            //Sort Array: lastModified
            Arrays.sort(arrayOfFiles, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR)

            //Get count of files to delete
            val fileCountToDelete = arrayOfFiles.size - maxFileCount!!

            for (i in 1..fileCountToDelete) {
                //Delete all old files (i-1 because array starts a 0)
                arrayOfFiles[i - 1].delete()

                if (enableLogDebug) Log.d(TAG, "maxFileCount reached: ${arrayOfFiles[i - 1]} deleted")
            }
        }
        return true
    }

    /**
     * Start Restore process, and set onComplete Listener to success, if no error occurred, else onComplete Listener success is false and error message is passed
     * this function shows a list of all available backup files in a MaterialAlertDialog
     * and calls restoreSelectedFile(filename) to restore selected file
     */
    fun restore() {
        if (enableLogDebug) Log.d(TAG, "Starting Restore ...")
        val success = initRoomBackup()
        if (!success) return

        //Path of Backup Directory
        val backupDirectory = if (useExternalStorage) File("$EXTERNAL_BACKUP_PATH/") else INTERNAL_BACKUP_PATH

        //All Files in an Array of type File
        val arrayOfFiles = backupDirectory.listFiles()

        //If array is null or empty show "error" and return
        if (arrayOfFiles.isNullOrEmpty()) {
            if (enableLogDebug) Log.d(TAG, "No backups available to restore")
            onCompleteListener?.onComplete(false, "No backups available")
            Toast.makeText(context, "No backups available to restore", Toast.LENGTH_SHORT).show()
            return
        }

        //Sort Array: lastModified
        Arrays.sort(arrayOfFiles, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR)

        //New empty MutableList of String
        val mutableListOfFilesAsString = mutableListOf<String>()

        //Add each filename to mutablelistOfFilesAsString
        runBlocking {
            for (i in arrayOfFiles.indices) {
                mutableListOfFilesAsString.add(arrayOfFiles[i].name)
            }
        }

        //Convert MutableList to Array
        val filesStringArray = mutableListOfFilesAsString.toTypedArray()

        //Show MaterialAlertDialog, with all available files, and on click Listener
        MaterialAlertDialogBuilder(context!!)
            .setTitle(customRestoreDialogTitle)
            .setItems(filesStringArray) { _, which ->
                restoreSelectedFile(filesStringArray[which])
            }
            .setOnCancelListener {
                if (enableLogDebug) Log.d(TAG, "Restore dialog canceled")
                onCompleteListener?.onComplete(false, "Restore dialog canceled")
            }
            .show()
    }

    /**
     * Restores the selected file
     *
     * @param filename String
     */
    private fun restoreSelectedFile(filename: String) {
        if (enableLogDebug) Log.d(TAG, "Restore selected file...")
        //Close the database
        roomDatabase!!.close()

        val backuppath = if (useExternalStorage) {
            File("$EXTERNAL_BACKUP_PATH/$filename")
        } else {
            File("$INTERNAL_BACKUP_PATH/$filename")
        }


        val fileExtension = backuppath.extension
        if (backupIsEncrypted) {

            if (fileExtension == "sqlite3") {
                //Copy back database and replace current database, if file is not encrypted
                copy(backuppath, DATABASE_FILE)
                if (enableLogDebug) Log.d(TAG, "File is not encrypted, trying to restore")
                if (enableLogDebug) Log.d(TAG, "Restored File: $backuppath")
                onCompleteListener?.onComplete(true, "success")
            } else decryptBackupFile(backuppath)
        } else {
            if (fileExtension == "aes") {
                if (enableLogDebug) Log.d(TAG, "Cannot restore database, it is encrypted. Maybe you forgot to add the property .fileIsEncrypted(true)")
                onCompleteListener?.onComplete(false, "cannot restore database, see Log for more details (if enabled)")
                return
                //      throw Exception("You are trying to restore an encrypted Database, but you did not add the property .fileIsEncrypted(true)")
            }
            //Copy back database and replace current database
            copy(backuppath, DATABASE_FILE)
            if (enableLogDebug) Log.d(TAG, "Restored File: $backuppath")
            onCompleteListener?.onComplete(true, "success")
        }
    }

}
