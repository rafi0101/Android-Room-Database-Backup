package com.ebner.roomdatabasebackup.sample.database.table.fruit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.ebner.roomdatabasebackup.sample.database.main.FruitDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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