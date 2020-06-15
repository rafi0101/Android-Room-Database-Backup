package com.ebner.roomdatabasebackup.sample.database.table.Fruit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.ebner.roomdatabasebackup.sample.database.main.FruitDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by raphael on 14.06.2020.
 * Android Room Database Backup Created in com.ebner.roomdatabasebackup.sample.database.table.Fruit
 */
class FruitViewModel(application: Application) : AndroidViewModel(application) {

    private val fruitRepository: FruitRepository

    val allFruit: LiveData<List<Fruit>>

    /*---------------------Define the Database, and the Repository--------------------------*/
    init {
        val fruitDao = FruitDatabase.getInstance(application).fruitDao()
        fruitRepository = FruitRepository(fruitDao)
        allFruit = fruitRepository.getAllFruit

    }

    /*---------------------Define default queries--------------------------*/
    fun insert(fruit: Fruit) = viewModelScope.launch(Dispatchers.IO) {
        fruitRepository.insert(fruit)
    }

    fun update(fruit: Fruit) = viewModelScope.launch(Dispatchers.IO) {
        fruitRepository.update(fruit)
    }

    fun delete(fruit: Fruit) = viewModelScope.launch(Dispatchers.IO) {
        fruitRepository.delete(fruit)
    }



}