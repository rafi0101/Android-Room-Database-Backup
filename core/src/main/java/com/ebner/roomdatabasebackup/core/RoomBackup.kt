package com.ebner.roomdatabasebackup.core

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.room.RoomDatabase
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
 * Created by raphael on 11.06.2020.
 * Android Room Database Backup Created in com.ebner.roomdatabasebackup.core
 */
class RoomBackup {


    private var TAG = "debug_RoomBackup"
    private lateinit var dbName: String
    private lateinit var BACKUP_PATH: String
    private lateinit var DATABASE_PATH: Path

    private var context: Context? = null
    private var roomDatabase: RoomDatabase? = null
    private var enableLogDebug: Boolean = false
    private var restartIntent: Intent? = null
    private var onCompleteListener: OnCompleteListener? = null
    private var customRestoreDialogTitle: String = "Choose file to restore"
    private var customBackupFileName: String? = null

    fun context(context: Context): RoomBackup {
        this.context = context
        return this
    }

    fun database(roomDatabase: RoomDatabase): RoomBackup {
        this.roomDatabase = roomDatabase
        return this
    }

    fun enableLogDebug(enableLogDebug: Boolean): RoomBackup {
        this.enableLogDebug = enableLogDebug
        return this
    }

    fun restartApp(restartIntent: Intent): RoomBackup {
        this.restartIntent = restartIntent
        restartApp()
        return this
    }

    fun onCompleteListener(onCompleteListener: OnCompleteListener): RoomBackup {
        this.onCompleteListener = onCompleteListener
        return this
    }

    fun onCompleteListener(listener: (success: Boolean, message: String) -> Unit): RoomBackup {
        this.onCompleteListener = object : OnCompleteListener {
            override fun onComplete(success: Boolean, message: String) {
                listener(success, message)
            }
        }
        return this
    }

    fun customLogTag(customLogTag: String): RoomBackup {
        TAG = customLogTag
        return this
    }

    fun customRestoreDialogTitle(customRestoreDialogTitle: String): RoomBackup {
        this.customRestoreDialogTitle = customRestoreDialogTitle
        return this
    }

    fun customBackupFileName(customBackupFileName: String): RoomBackup {
        this.customBackupFileName = customBackupFileName
        return this
    }

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

        dbName = roomDatabase!!.openHelper.databaseName
        BACKUP_PATH = "${context!!.filesDir}/databasebackup/"
        DATABASE_PATH = Paths.get(context!!.getDatabasePath(dbName).toURI())
        if (enableLogDebug) {
            Log.d(TAG, "DatabaseName: $dbName")
            Log.d(TAG, "Database Location: $DATABASE_PATH")
        }
        return true
    }

    private fun restartApp() {
        restartIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context!!.startActivity(restartIntent)
        //   finish()
        Runtime.getRuntime().exit(0)

    }

    fun backup() {
        if (enableLogDebug) Log.d(TAG, "Starting Backup ...")
        val success = initRoomBackup()
        if (!success) return

        //Close the database
        roomDatabase!!.close()

        //Create backup directory if does not exist
        try {
            Files.createDirectory(Paths.get((BACKUP_PATH)))
        } catch (e: FileAlreadyExistsException) {
        } catch (e: IOException) {
        }

        //Create name for backup file, if no custom name is set: Databasename + currentTime + .sqlite3
        val filename = if (customBackupFileName == null) "$dbName-${getTime()}.sqlite3" else customBackupFileName as String

        //Path to save current database
        val backuppath = Paths.get("$BACKUP_PATH$filename")


        //Copy current database to save location (/files dir)
        Files.copy(DATABASE_PATH, backuppath, StandardCopyOption.REPLACE_EXISTING)

        if (enableLogDebug) Log.d(TAG, "Saved to: $backuppath")
        onCompleteListener?.onComplete(true, "success")
    }

    /*---------------------Get current Date / Time --------------------------*/
    private fun getTime(): String {

        val currentTime = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss")

        return currentTime.format(formatter)

    }

    fun restore() {
        if (enableLogDebug) Log.d(TAG, "Starting Restore ...")
        val success = initRoomBackup()
        if (!success) return

        val backupDirectory = File(BACKUP_PATH)
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

    /*---------------------restore selected file--------------------------*/
    private fun restoreSelectedFile(filename: String) {
        if (enableLogDebug) Log.d(TAG, "Restore selected file...")
        //Close the database
        roomDatabase!!.close()


        //Backup location
        val backuppath = Paths.get("$BACKUP_PATH$filename")

        //Copy back database and replace current database
        Files.copy(backuppath, DATABASE_PATH, StandardCopyOption.REPLACE_EXISTING)

        if (enableLogDebug) Log.d(TAG, "Restored File: $backuppath")
        onCompleteListener?.onComplete(true, "success")

    }

}