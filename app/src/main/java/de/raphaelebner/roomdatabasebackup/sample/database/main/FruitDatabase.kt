package de.raphaelebner.roomdatabasebackup.sample.database.main

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import de.raphaelebner.roomdatabasebackup.sample.database.table.fruit.Fruit
import de.raphaelebner.roomdatabasebackup.sample.database.table.fruit.FruitDao
import java.util.concurrent.Executors

/**
 *  MIT License
 *
 *  Copyright (c) 2023 Raphael Ebner
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
@Database(
    entities = [
        Fruit::class],
    version = 1
)
abstract class FruitDatabase : RoomDatabase() {
    abstract fun fruitDao(): FruitDao

    companion object {

        const val DATABASE_NAME = "fruitdb"

        @Volatile
        private var INSTANCE: FruitDatabase? = null

        /*---------------------Create one (only one) instance of the Database--------------------------*/

        fun getInstance(context: Context): FruitDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, FruitDatabase::class.java, DATABASE_NAME)
                //Delete Database, when something changed
                .fallbackToDestructiveMigration()
                // prepopulate the database after onCreate was called
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // insert the data on the IO Thread
                        ioThread {
                            //Some sample data
                            getInstance(context).fruitDao().insert(Fruit("Apple"))
                            getInstance(context).fruitDao().insert(Fruit("Banana"))
                            getInstance(context).fruitDao().insert(Fruit("Lime"))
                            getInstance(context).fruitDao().insert(Fruit("Grapes"))
                            getInstance(context).fruitDao().insert(Fruit("Lemon"))
                            getInstance(context).fruitDao().insert(Fruit("Cherry"))
                            getInstance(context).fruitDao().insert(Fruit("Blueberry"))
                            getInstance(context).fruitDao().insert(Fruit("Watermelon"))
                            getInstance(context).fruitDao().insert(Fruit("Peach"))
                            getInstance(context).fruitDao().insert(Fruit("Pineapple"))
                            getInstance(context).fruitDao().insert(Fruit("Orange"))
                            getInstance(context).fruitDao().insert(Fruit("Strawberry"))
                            getInstance(context).fruitDao().insert(Fruit("Coconut"))
                            getInstance(context).fruitDao().insert(Fruit("Raspberry"))
                            getInstance(context).fruitDao().insert(Fruit("Mandarine"))
                            getInstance(context).fruitDao().insert(Fruit("Grapefruit"))
                            getInstance(context).fruitDao().insert(Fruit("Plum"))
                            getInstance(context).fruitDao().insert(Fruit("Pear"))
                            getInstance(context).fruitDao().insert(Fruit("Passionfruit"))

                        }
                    }
                })
                .build()

        /*---------------------This runs a background task--------------------------*/
        private val IO_EXECUTOR = Executors.newSingleThreadExecutor()

        /**
         * Utility method to run blocks on a dedicated background thread, used for io/database work.
         */
        fun ioThread(f: () -> Unit) {
            IO_EXECUTOR.execute(f)
        }

    }
}
