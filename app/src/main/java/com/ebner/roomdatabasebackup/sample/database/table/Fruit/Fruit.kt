package com.ebner.roomdatabasebackup.sample.database.table.Fruit

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by raphael on 14.06.2020.
 * Android Room Database Backup Created in com.ebner.roomdatabasebackup.sample.database.table
 */
@Entity(tableName = "fruit")
data class Fruit(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "name")
    var name: String
)