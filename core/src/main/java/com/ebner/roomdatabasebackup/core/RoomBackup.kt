package com.ebner.roomdatabasebackup.core

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Created by raphael on 11.06.2020.
 * Android Room Database Backup Created in com.ebner.roomdatabasebackup.core
 */

class RoomBackup(

    var roomDatabase: RoomDatabase? = null,
    var dbName: String = "db123",
    var secretKey: String = "",
    var context: Context? = null
) {
    private val TAG = "debug_RoomBackup"

    fun backup() {

        //Close the database
        roomDatabase!!.close()
        val file = context!!.getDatabasePath(dbName)


        //val name = "$dbName-${getTime()}.sqlite3"
        val name = "$dbName.sqlite3"

        Log.d(TAG, "file: $file")
        Log.d(TAG, "filename: $name")


        //Current database location
        val tobackuppath = Paths.get(file.toURI())
        //Path to save current database
        val tosavepath = Paths.get("${context!!.filesDir}/$name")

        Log.d(TAG, "tobackuppath: $tobackuppath")
        Log.d(TAG, "tosavepath: $tosavepath")

        //Copy current database to save location (/files dir)
        Files.copy(tobackuppath, tosavepath, StandardCopyOption.REPLACE_EXISTING)

    }

    /*---------------------Get current Date / Time --------------------------*/
    private fun getTime(): String {

        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss")
        val formatted = current.format(formatter)

        return formatted

    }

    fun restore() {
        //Close the database
        roomDatabase!!.close()
        val file = context!!.getDatabasePath(dbName)


        val name = "$dbName.sqlite3"

        Log.d(TAG, "file: $file")
        Log.d(TAG, "filename: $name")

        //Backup location
        val torestorepath = Paths.get("${context!!.filesDir}/$name")
        //Path to db to replace
        val toreplacepath = Paths.get(file.toURI())

        Log.d(TAG, "torestorepath: $torestorepath")
        Log.d(TAG, "toreplacepath: $toreplacepath")

        //Copy back database and replace current database
        Files.copy(torestorepath, toreplacepath, StandardCopyOption.REPLACE_EXISTING)


    }
}