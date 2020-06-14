package com.ebner.roomdatabasebackup.core

import android.content.Context
import android.widget.Toast
import androidx.room.RoomDatabase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Created by raphael on 11.06.2020.
 * Android Room Database Backup Created in com.ebner.roomdatabasebackup.core
 */
class RoomBackup(
    private var context: Context,
    private var dbName: String

) {
    lateinit var roomDatabase: RoomDatabase
    lateinit var secretKey: String

    private val BACKUP_PATH = "${context.filesDir}/databasebackup/"
    private val DATABASE_PATH = Paths.get(context.getDatabasePath(dbName).toURI())

    /*---------------------Create Backup of current database with timestamp--------------------------*/
    fun backup() {

        //Close the database
        roomDatabase.close()

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

    }

    /*---------------------Public method, to show dialog with all files to restore--------------------------*/
    fun restore() {


        val backupDirectory = File(BACKUP_PATH)
        //All Files in an Array of type File
        val arrayOfFiles = backupDirectory.listFiles()

        //If array is null or empty show "error" and return
        if (arrayOfFiles.isNullOrEmpty()) {
            Toast.makeText(context, "Bisher gibt es noch keine Backups", Toast.LENGTH_LONG).show()
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

    /*---------------------Get current Date / Time --------------------------*/
    private fun getTime(): String {

        val currentTime = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss")

        return currentTime.format(formatter)

    }

    /*---------------------restore selected file--------------------------*/
    private fun restoreSelectedFile(filename: String) {
        //Close the database
        roomDatabase.close()

        //Backup location
        val backuppath = Paths.get("$BACKUP_PATH$filename")

        //Copy back database and replace current database
        Files.copy(backuppath, DATABASE_PATH, StandardCopyOption.REPLACE_EXISTING)

    }

}