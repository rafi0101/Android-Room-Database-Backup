package com.ebner.roomdatabasebackup.core

import android.content.Context
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


    private val TAG = "debug_RoomBackup"

    private var context: Context? = null
    private var roomDatabase: RoomDatabase? = null
    private var secretKey: String? = null
    private var enableToastDebug: Boolean = false
    private var enableLogDebug: Boolean = false
    private var onCompleteListener: OnCompleteListener? = null

    fun context(context: Context): RoomBackup {
        this.context = context
        return this
    }

    fun database(roomDatabase: RoomDatabase): RoomBackup {
        this.roomDatabase = roomDatabase
        return this
    }

    fun secretKey(secretKey: String): RoomBackup {
        this.secretKey = secretKey
        return this
    }

    fun enableToastDebug(enableToastDebug: Boolean): RoomBackup {
        this.enableToastDebug = enableToastDebug
        return this
    }

    fun enableLogDebug(enableLogDebug: Boolean): RoomBackup {
        this.enableLogDebug = enableLogDebug
        return this
    }

    fun onCompleteListener(onCompleteListener: OnCompleteListener): RoomBackup {
        this.onCompleteListener = onCompleteListener
        return this
    }

    private lateinit var dbName: String
    private lateinit var BACKUP_PATH: String
    private lateinit var DATABASE_PATH: Path

    private fun initRoomBackup(): Boolean {
        if (context == null) {
            onCompleteListener?.onComplete(false, "context is missing")
            return false
        }
        if (roomDatabase == null) {
            onCompleteListener?.onComplete(false, "roomDatabase is missing")
            return false
        }

        dbName = roomDatabase!!.openHelper.databaseName
        BACKUP_PATH = "${context!!.filesDir}/databasebackup/"
        DATABASE_PATH = Paths.get(context!!.getDatabasePath(dbName).toURI())
        return true
    }


    fun backup() {
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

        //Create name for backup file: Databasename + currentTime + .sqlite3
        val filename = "$dbName-${getTime()}.sqlite3"

        //Path to save current database
        val backuppath = Paths.get("$BACKUP_PATH$filename")


        //Copy current database to save location (/files dir)
        Files.copy(DATABASE_PATH, backuppath, StandardCopyOption.REPLACE_EXISTING)


        onCompleteListener?.onComplete(true, "success")

        if (enableToastDebug) Toast.makeText(context, "Saved to: $backuppath", Toast.LENGTH_LONG).show()
        if (enableLogDebug) Log.d(TAG, "Saved to: $backuppath")
    }


    /*---------------------Get current Date / Time --------------------------*/
    private fun getTime(): String {

        val currentTime = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss")

        return currentTime.format(formatter)

    }


    fun restore() {

        val success = initRoomBackup()
        if (!success) return

        val backupDirectory = File(BACKUP_PATH)
        //All Files in an Array of type File
        val arrayOfFiles = backupDirectory.listFiles()

        //If array is null or empty show "error" and return
        if (arrayOfFiles.isNullOrEmpty()) {
            onCompleteListener?.onComplete(false, "No Backups available")
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
            .setTitle("Choose File to Restore")
            .setItems(filesStringArray) { _, which ->
                restoreSelectedFile(filesStringArray[which])
            }
            .show()
    }

    /*---------------------restore selected file--------------------------*/
    private fun restoreSelectedFile(filename: String) {
        //Close the database
        roomDatabase!!.close()


        //Backup location
        val backuppath = Paths.get("$BACKUP_PATH$filename")

        //Copy back database and replace current database
        Files.copy(backuppath, DATABASE_PATH, StandardCopyOption.REPLACE_EXISTING)


        if (enableToastDebug) Toast.makeText(context, "Restored File: $backuppath", Toast.LENGTH_LONG).show()
        if (enableLogDebug) Log.d(TAG, "Restored File: $backuppath")


        onCompleteListener?.onComplete(true, "success")


    }

}