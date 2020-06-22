package com.ebner.roomdatabasebackup.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ebner.roomdatabasebackup.core.RoomBackup
import com.ebner.roomdatabasebackup.sample.database.main.FruitDatabase
import com.ebner.roomdatabasebackup.sample.database.table.Fruit.Fruit
import com.ebner.roomdatabasebackup.sample.database.table.Fruit.FruitListAdapter
import com.ebner.roomdatabasebackup.sample.database.table.Fruit.FruitViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

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
class MainActivity : AppCompatActivity(), FruitListAdapter.OnItemClickListener {

    private lateinit var fruitViewModel: FruitViewModel
    private lateinit var clMain: CoordinatorLayout

    companion object {
        private const val ADD_FRUIT_REQUEST = 1
        private const val EDIT_FRUIT_REQUEST = 2
    }

    private val TAG = "debug_MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*---------------------Link items to Layout--------------------------*/
        clMain = findViewById(R.id.cl_main)
        val recyclerView: RecyclerView = findViewById(R.id.rv_fruits)
        val fab: FloatingActionButton = findViewById(R.id.btn_addFruit)
        val btn_backup_intern: Button = findViewById(R.id.btn_backup_intern)
        val btn_restore_intern: Button = findViewById(R.id.btn_restore_intern)
        val btn_backup_extern: Button = findViewById(R.id.btn_backup_extern)
        val btn_restore_extern: Button = findViewById(R.id.btn_restore_extern)
        val btn_java: Button = findViewById(R.id.btn_java)

        val adapter = FruitListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fruitViewModel = ViewModelProvider(this).get(FruitViewModel::class.java)

        fruitViewModel.allFruit.observe(this, Observer { fruits ->
            adapter.submitList(fruits)
        })


        /*---------------------FAB Add Button--------------------------*/
        fab.setOnClickListener {
            val intent = Intent(this, ActivityAddEditFruit::class.java)
            startActivityForResult(intent, ADD_FRUIT_REQUEST)
        }

        /*---------------------go to Java MainActivity--------------------------*/
        btn_java.setOnClickListener {
            finish()
            val intent = Intent(this, MainActivityJava::class.java)
            startActivity(intent)
        }

        /*---------------------Backup and Restore Database--------------------------*/
        btn_backup_intern.setOnClickListener {
            RoomBackup()
                .context(this)
                .database(FruitDatabase.getInstance(this))
                .enableLogDebug(true)
                .apply {
                    onCompleteListener { success, _ ->
                        if (success) restartApp(Intent(this@MainActivity, MainActivity::class.java))
                    }
                }
                .backup()


        }
        btn_restore_intern.setOnClickListener {
            RoomBackup()
                .context(this)
                .database(FruitDatabase.getInstance(this))
                .enableLogDebug(true)
                .apply {
                    onCompleteListener { success, _ ->
                        if (success) restartApp(Intent(this@MainActivity, MainActivity::class.java))
                    }

                }
                .restore()
        }

        btn_backup_extern.setOnClickListener {
            RoomBackup()
                .context(this)
                .database(FruitDatabase.getInstance(this))
                .enableLogDebug(true)
                .exportToExternalStorage(true)
                .apply {
                    onCompleteListener { success, _ ->
                        if (success) restartApp(Intent(this@MainActivity, MainActivity::class.java))
                    }
                }
                .backup()


        }
        btn_restore_extern.setOnClickListener {
            RoomBackup()
                .context(this)
                .database(FruitDatabase.getInstance(this))
                .enableLogDebug(true)
                .importFromExternalStorage(true)
                .apply {
                    onCompleteListener { success, _ ->
                        if (success) restartApp(Intent(this@MainActivity, MainActivity::class.java))
                    }

                }
                .restore()
        }


        /*---------------------Swiping on a row--------------------------*/
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            /*---------------------do action on swipe--------------------------*/
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //Item in recyclerview
                val position = viewHolder.adapterPosition
                val fruit = adapter.getFruitAt(position)!!

                fruitViewModel.delete(fruit)
                //  adapter.notifyItemChanged(position)
                // show snack bar with Undo option
                val snackbar = Snackbar
                    .make(clMain, "${fruit.name} deleted", 8000)
                snackbar.setAction("UNDO") {
                    // undo is selected, restore the deleted item
                    fruitViewModel.insert(fruit)
                }
                snackbar.setActionTextColor(Color.YELLOW)
                snackbar.show()

            }

            /*---------------------ADD trash bin icon to background--------------------------*/
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                val icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_delete)

                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                val iconBottom = iconTop + icon.intrinsicHeight

                if (dX > 0) { // Swiping to the right
                    val iconLeft = itemView.left + iconMargin + icon.intrinsicWidth
                    val iconRight = itemView.left + iconMargin
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                } else if (dX < 0) { // Swiping to the left
                    val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                }

                icon.draw(c)
            }
        }).attachToRecyclerView(recyclerView)

    }


    /*---------------------when returning from |ActivityAddEditFruit| do something--------------------------*/
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /*---------------------If the Request was successful--------------------------*/
        if (resultCode == Activity.RESULT_OK) {
            val name = data!!.getStringExtra(ActivityAddEditFruit.EXTRA_NAME)
            val fruit = Fruit(name = name)

            /*---------------------If the Request was a ADD fruit request--------------------------*/
            if (requestCode == ADD_FRUIT_REQUEST && resultCode == Activity.RESULT_OK) {

                fruitViewModel.insert(fruit)

                /*---------------------If the Request was a EDIT fruit request--------------------------*/
            } else if (requestCode == EDIT_FRUIT_REQUEST && resultCode == Activity.RESULT_OK) {
                val id = data.getIntExtra(ActivityAddEditFruit.EXTRA_ID, -1)

                if (id == -1) {
                    val snackbar = Snackbar
                        .make(clMain, "Failed to update Fruit!", Snackbar.LENGTH_LONG)
                    snackbar.show()
                    return
                }

                fruit.id = id
                fruitViewModel.update(fruit)
            }
        }
    }

    /*---------------------onItemClicked listener--------------------------*/
    override fun onItemClicked(fruit: Fruit) {
        val intent = Intent(this, ActivityAddEditFruit::class.java)
        intent.putExtra(ActivityAddEditFruit.EXTRA_ID, fruit.id)
        intent.putExtra(ActivityAddEditFruit.EXTRA_NAME, fruit.name)
        startActivityForResult(intent, EDIT_FRUIT_REQUEST)
    }
}