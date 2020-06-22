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
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.Comparator

/**
 * Copyright 2020 Raphael Ebner

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class RoomBackup {

    companion object {
        private const val SHARED_PREFS = "com.ebner.roomdatabasebackup"
        private var TAG = "debug_RoomBackup"
        private lateinit var INTERNAL_BACKUP_PATH: Path
        private lateinit var TEMP_BACKUP_PATH: Path
        private lateinit var TEMP_BACKUP_FILE: Path
        private lateinit var EXTERNAL_BACKUP_PATH: Path
        private lateinit var DATABASE_FILE: Path
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
    private var exportToExternalStorage: Boolean = false
    private var importFromExternalStorage: Boolean = false
    private var fileIsEncrypted: Boolean = false

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
     * Set export To External Storage enabled / disabled, if you want to export the backup to external storage
     * then you have access to the backup and can save it somewhere else
     *
     *
     * @param exportToExternalStorage Boolean, default = false
     */
    fun exportToExternalStorage(exportToExternalStorage: Boolean): RoomBackup {
        this.exportToExternalStorage = exportToExternalStorage
        return this
    }

    /**
     * Set import From External Storage enabled / disabled, if you want to restore the backup from external storage
     *
     *
     * @param importFromExternalStorage Boolean, default = false
     */
    fun importFromExternalStorage(importFromExternalStorage: Boolean): RoomBackup {
        this.importFromExternalStorage = importFromExternalStorage
        return this
    }

    /**
     * Set file encryption to true / false
     * can be used for backup and restore
     *
     *
     * @param fileIsEncrypted Boolean, default = false
     */
    fun fileIsEncrypted(fileIsEncrypted: Boolean): RoomBackup {
        this.fileIsEncrypted = fileIsEncrypted
        return this
    }

    /**
     * Init vars, and return true if no error occurred
     */
    private fun initRoomBackup(): Boolean {
        if (context == null) {
            if (enableLogDebug) Log.d(TAG, "context is missing")
            onCompleteListener?.onComplete(false, "context is missing")
            return false
        }
        if (roomDatabase == null) {
            if (enableLogDebug) Log.d(TAG, "roomDatabase is missing")
            onCompleteListener?.onComplete(false, "roomDatabase is missing")
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
        INTERNAL_BACKUP_PATH = Paths.get("${context!!.filesDir}/databasebackup/")
        TEMP_BACKUP_PATH = Paths.get("${context!!.filesDir}/databasebackup-temp/")
        TEMP_BACKUP_FILE = Paths.get("$TEMP_BACKUP_PATH/tempbackup.sqlite3")
        EXTERNAL_BACKUP_PATH = Paths.get(context!!.getExternalFilesDir("backup")!!.toURI())
        DATABASE_FILE = Paths.get(context!!.getDatabasePath(dbName).toURI())

        //Create internal and temp backup directory if does not exist
        try {
            Files.createDirectory(INTERNAL_BACKUP_PATH)
            Files.createDirectory(TEMP_BACKUP_PATH)
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
        roomDatabase!!.close()

        //Create name for backup file, if no custom name is set: Database name + currentTime + .sqlite3
        var filename = if (customBackupFileName == null) "$dbName-${getTime()}.sqlite3" else customBackupFileName as String
        //Add .aes extension to filename, if file is encrypted
        if (fileIsEncrypted) filename += ".aes"

        //Path to save current database
        val backuppath = if (exportToExternalStorage) Paths.get("$EXTERNAL_BACKUP_PATH/$filename") else Paths.get("$INTERNAL_BACKUP_PATH/$filename")

        if (fileIsEncrypted) encryptBackupFile(backuppath)
        else {
            //Copy current database to save location (/files dir)
            Files.copy(DATABASE_FILE, backuppath, StandardCopyOption.REPLACE_EXISTING)

            if (enableLogDebug) Log.d(TAG, "Saved to: $backuppath")
            onCompleteListener?.onComplete(true, "success")
        }
    }

    /**
     * Encrypt backup file, and save it
     *
     * @param backuppath Path, where to save the backup (internal or external storage)
     */
    private fun encryptBackupFile(backuppath: Path) {
        try {

            //Copy database you want to backup to temp directory
            Files.copy(DATABASE_FILE, TEMP_BACKUP_FILE, StandardCopyOption.REPLACE_EXISTING)

            //encrypt temp file, and save it to backup location
            val encryptDecryptBackup = AESEncryptionHelper()
            val fileData = encryptDecryptBackup.readFile(TEMP_BACKUP_FILE)

            val aesEncryptionManager = AESEncryptionManager()
            val encryptedBytes = aesEncryptionManager.encryptData(sharedPreferences, fileData)

            encryptDecryptBackup.saveFile(encryptedBytes, backuppath)

            //Delete temp file
            Files.delete(TEMP_BACKUP_FILE)


            if (enableLogDebug) Log.d(TAG, "Saved and encrypted to: $backuppath")
            onCompleteListener?.onComplete(true, "saved and encrypted")

        } catch (e: Exception) {
            if (enableLogDebug) Log.d(TAG, "error during encryption: ${e.message}")
            onCompleteListener?.onComplete(false, "error during encryption")
        }
    }

    /**
     * Decrypt backup file, and save it
     *
     * @param backuppath Path, where to find the backup to restore (internal or external storage)
     */
    private fun decryptBackupFile(backuppath: Path) {
        try {
            //Copy database you want to restore to temp directory
            Files.copy(backuppath, TEMP_BACKUP_FILE, StandardCopyOption.REPLACE_EXISTING)

            //Decrypt temp file, and save it to database location
            val encryptDecryptBackup = AESEncryptionHelper()
            val fileData = encryptDecryptBackup.readFile(TEMP_BACKUP_FILE)

            val aesEncryptionManager = AESEncryptionManager()
            val decryptedBytes = aesEncryptionManager.decryptData(sharedPreferences, fileData)

            encryptDecryptBackup.saveFile(decryptedBytes, DATABASE_FILE)

            //Delete tem file
            Files.delete(TEMP_BACKUP_FILE)

            if (enableLogDebug) Log.d(TAG, "restored and decrypted from / to $backuppath")
            onCompleteListener?.onComplete(true, "restored and decrypted")

        } catch (e: Exception) {
            if (enableLogDebug) Log.d(TAG, "error during decryption: ${e.message}")
            onCompleteListener?.onComplete(false, "error during decryption")
        }

    }

    /**
     * @return current time formatted as String
     */
    private fun getTime(): String {

        val currentTime = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss")

        return currentTime.format(formatter)

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
        val backupDirectory = if (importFromExternalStorage) File("$EXTERNAL_BACKUP_PATH/") else File(INTERNAL_BACKUP_PATH.toUri())

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
        Arrays.sort(arrayOfFiles, Comparator.comparingLong { obj: File -> obj.lastModified() })

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
        MaterialAlertDialogBuilder(context)
            .setTitle(customRestoreDialogTitle)
            .setItems(filesStringArray) { _, which ->
                restoreSelectedFile(filesStringArray[which])
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

        val backuppath = if (importFromExternalStorage) {
            Paths.get("$EXTERNAL_BACKUP_PATH/$filename")
        } else {
            Paths.get("$INTERNAL_BACKUP_PATH/$filename")
        }

        val fileExtension = File(backuppath.toUri()).extension
        if (fileIsEncrypted) {

            if (fileExtension == "sqlite3") {
                //Copy back database and replace current database, if file is not encrypted
                Files.copy(backuppath, DATABASE_FILE, StandardCopyOption.REPLACE_EXISTING)
                if (enableLogDebug) Log.d(TAG, "File is not encrypted, trying to restore")
                if (enableLogDebug) Log.d(TAG, "Restored File: $backuppath")
                onCompleteListener?.onComplete(true, "success")
            } else decryptBackupFile(backuppath)
        } else {
            if (fileExtension == "aes") {
                if (enableLogDebug) Log.d(TAG, "Cannot restore database, it is encrypted. Maybe you forgot to add the property .fileIsEncrypted(true)")
                onCompleteListener?.onComplete(false, "cannot restore database, see more details in Log (if enabled)")
            }
            //Copy back database and replace current database
            Files.copy(backuppath, DATABASE_FILE, StandardCopyOption.REPLACE_EXISTING)
            if (enableLogDebug) Log.d(TAG, "Restored File: $backuppath")
            onCompleteListener?.onComplete(true, "success")
        }
    }

}