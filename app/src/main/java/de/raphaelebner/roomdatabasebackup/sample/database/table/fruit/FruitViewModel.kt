package de.raphaelebner.roomdatabasebackup.sample.database.table.fruit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import de.raphaelebner.roomdatabasebackup.sample.database.main.FruitDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * MIT License
 *
 * Copyright (c) 2025 Raphael Ebner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
    fun insert(fruit: Fruit) =
            viewModelScope.launch(Dispatchers.IO) { fruitRepository.insert(fruit) }

    fun update(fruit: Fruit) =
            viewModelScope.launch(Dispatchers.IO) { fruitRepository.update(fruit) }

    fun delete(fruit: Fruit) =
            viewModelScope.launch(Dispatchers.IO) { fruitRepository.delete(fruit) }
}
