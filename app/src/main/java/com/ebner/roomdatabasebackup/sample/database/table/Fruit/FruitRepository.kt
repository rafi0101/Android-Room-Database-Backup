package com.ebner.roomdatabasebackup.sample.database.table.Fruit

import androidx.lifecycle.LiveData

/**
 * Created by raphael on 14.06.2020.
 * Android Room Database Backup Created in com.ebner.roomdatabasebackup.sample.database.table.Fruit
 */
class FruitRepository(
    private val fruitDao: FruitDao
) {
    /*---------------------Just pass the queries to the DAO--------------------------*/
    fun insert(fruit: Fruit) = fruitDao.insert(fruit)
    fun update(fruit: Fruit) = fruitDao.update(fruit)
    fun delete(fruit: Fruit) = fruitDao.delete(fruit)

    //Live data view
    val getAllFruit: LiveData<List<Fruit>> = fruitDao.getAllFruit()

}