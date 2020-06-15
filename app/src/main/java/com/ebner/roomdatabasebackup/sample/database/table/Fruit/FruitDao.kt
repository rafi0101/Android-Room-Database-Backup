package com.ebner.roomdatabasebackup.sample.database.table.Fruit

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Created by raphael on 14.06.2020.
 * Android Room Database Backup Created in com.ebner.roomdatabasebackup.sample.database.table
 */
@Dao
interface FruitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(fruit: Fruit)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(fruit: Fruit)

    @Delete
    fun delete(fruit: Fruit)

    @Query("SELECT * FROM fruit ORDER BY name ASC")
    fun getAllFruit(): LiveData<List<Fruit>>
}