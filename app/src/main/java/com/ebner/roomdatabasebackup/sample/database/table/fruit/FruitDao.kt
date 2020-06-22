package com.ebner.roomdatabasebackup.sample.database.table.fruit

import androidx.lifecycle.LiveData
import androidx.room.*

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