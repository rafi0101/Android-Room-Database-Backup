package de.raphaelebner.roomdatabasebackup.sample

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import de.raphaelebner.roomdatabasebackup.sample.database.main.FruitDatabase
import de.raphaelebner.roomdatabasebackup.sample.database.table.fruit.Fruit
import de.raphaelebner.roomdatabasebackup.sample.database.table.fruit.FruitListAdapter
import de.raphaelebner.roomdatabasebackup.sample.database.table.fruit.FruitViewModel
import java.io.File


/**
 *  MIT License
 *
 *  Copyright (c) 2021 Raphael Ebner
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
class MainActivity : AppCompatActivity(), FruitListAdapter.OnItemClickListener {

    private lateinit var fruitViewModel: FruitViewModel
    private lateinit var clMain: CoordinatorLayout

    companion object {
        private const val TAG = "debug_MainActivity"
        const val SECRET_PASSWORD = "verySecretEncryptionKey"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*---------------------Link items to Layout--------------------------*/
        clMain = findViewById(R.id.cl_main)
        val recyclerView: RecyclerView = findViewById(R.id.rv_fruits)
        val fab: FloatingActionButton = findViewById(R.id.btn_addFruit)
        val btnBackup: Button = findViewById(R.id.btn_backup)
        val btnRestore: Button = findViewById(R.id.btn_restore)
        val btnProperties: Button = findViewById(R.id.btn_properties)
        val btnLanguage: Button = findViewById(R.id.btn_switch_language)
        val btnBackupLocation: Button = findViewById(R.id.btn_backup_location)
        val tvFruits: TextView = findViewById(R.id.tv_fruits)

        val adapter = FruitListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fruitViewModel = ViewModelProvider(this)[FruitViewModel::class.java]

        fruitViewModel.allFruit.observe(this, { fruits ->
            adapter.submitList(fruits)
        })

        tvFruits.text = "Fruits List (Kotlin)"
        btnLanguage.text = "switch to Java"

        val sharedPrefs = "sampleBackup"
        val spEncryptBackup = "encryptBackup"
        val spStorageLocation = "storageLocation"
        val spEnableLog = "enableLog"
        val spUseMaxFileCount = "useMaxFileCount"
        val sharedPreferences = getSharedPreferences(sharedPrefs, Context.MODE_PRIVATE)

        /*---------------------FAB Add Button--------------------------*/
        fab.setOnClickListener {
            val intent = Intent(this, ActivityAddEditFruit::class.java)
            openAddEditActivity.launch(intent)
        }

        /*---------------------go to Java MainActivity--------------------------*/
        btnLanguage.setOnClickListener {
            finish()
            val intent = Intent(this, MainActivityJava::class.java)
            startActivity(intent)
        }

        var encryptBackup = sharedPreferences.getBoolean(spEncryptBackup, true)
        var storageLocation = sharedPreferences.getInt(spStorageLocation, 1)
        var enableLog = sharedPreferences.getBoolean(spEnableLog, true)
        var useMaxFileCount = sharedPreferences.getBoolean(spUseMaxFileCount, false)

        /*---------------------set Properties--------------------------*/
        btnProperties.setOnClickListener {
            val multiItems = arrayOf("Encrypt Backup", "enable Log", "use maxFileCount = 5")
            val checkedItems = booleanArrayOf(encryptBackup, enableLog, useMaxFileCount)

            MaterialAlertDialogBuilder(this)
                .setTitle("Change Properties")
                .setPositiveButton("Ok", null)
                //Multi-choice items (initialized with checked items)
                .setMultiChoiceItems(multiItems, checkedItems) { _, which, checked ->
                    // Respond to item chosen
                    when (which) {
                        0 -> {
                            encryptBackup = checked
                            sharedPreferences.edit().putBoolean(spEncryptBackup, encryptBackup).apply()
                        }
                        1 -> {
                            enableLog = checked
                            sharedPreferences.edit().putBoolean(spEnableLog, enableLog).apply()
                        }
                        2 -> {
                            useMaxFileCount = checked
                            sharedPreferences.edit().putBoolean(spUseMaxFileCount, useMaxFileCount).apply()
                        }
                    }
                }
                .show()
        }

        /*---------------------set Backup Location--------------------------*/
        btnBackupLocation.setOnClickListener {
            val storageItems = arrayOf("Internal", "External", "Custom Dialog", "Custom File")
            MaterialAlertDialogBuilder(this)
                .setTitle("Change Storage")
                .setPositiveButton("Ok", null)
                .setSingleChoiceItems(storageItems, storageLocation-1) { _, which ->
                    when (which) {
                        0 -> {
                            storageLocation = RoomBackup.BACKUP_FILE_LOCATION_INTERNAL
                            sharedPreferences.edit().putInt(spStorageLocation, storageLocation).apply()

                        }
                        1 -> {
                            storageLocation = RoomBackup.BACKUP_FILE_LOCATION_EXTERNAL
                            sharedPreferences.edit().putInt(spStorageLocation, storageLocation).apply()
                        }
                        2 -> {
                            storageLocation = RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG
                            sharedPreferences.edit().putInt(spStorageLocation, storageLocation).apply()
                        }
                        3 -> {
                            storageLocation = RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_FILE
                            sharedPreferences.edit().putInt(spStorageLocation, storageLocation).apply()
                        }
                    }
                }
                .show()
        }

        val backup = RoomBackup(this)
        /*---------------------Backup and Restore Database--------------------------*/
        btnBackup.setOnClickListener {
            backup
                .backupLocation(storageLocation)
                .backupLocationCustomFile(File("${this.filesDir}/databasebackup/geilesBackup.sqlite3"))
                .database(FruitDatabase.getInstance(this))
                .enableLogDebug(enableLog)
                .backupIsEncrypted(encryptBackup)
                .customEncryptPassword(SECRET_PASSWORD)
                //maxFileCount: else 1000 because i cannot surround it with if condition
                .maxFileCount(if (useMaxFileCount) 5 else 1000)
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        Log.d(TAG, "success: $success, message: $message, exitCode: $exitCode")
                        Toast.makeText(
                            this@MainActivity,
                            "success: $success, message: $message, exitCode: $exitCode",
                            Toast.LENGTH_LONG
                        ).show()
                        if (success) restartApp(Intent(this@MainActivity, MainActivity::class.java))
                    }
                }
                .backup()


        }
        btnRestore.setOnClickListener {
            backup
                .backupLocation(storageLocation)
                .backupLocationCustomFile(File("${this.filesDir}/databasebackup/geilesBackup.sqlite3"))
                .database(FruitDatabase.getInstance(this))
                .enableLogDebug(enableLog)
                .backupIsEncrypted(encryptBackup)
                .customEncryptPassword(SECRET_PASSWORD)
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        Log.d(TAG, "success: $success, message: $message, exitCode: $exitCode")
                        Toast.makeText(
                            this@MainActivity,
                            "success: $success, message: $message, exitCode: $exitCode",
                            Toast.LENGTH_LONG
                        ).show()
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
                val position = viewHolder.bindingAdapterPosition
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
    private val openAddEditActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        /*---------------------If the Request was successful--------------------------*/
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val name = data!!.getStringExtra(ActivityAddEditFruit.EXTRA_NAME)!!
            val id = data.getIntExtra(ActivityAddEditFruit.EXTRA_ID, -1)
            val deleteFruit = data.getBooleanExtra(ActivityAddEditFruit.EXTRA_DELETE_FRUIT, false)

            val fruit = Fruit(name)

            if (id == -1) {
                fruitViewModel.insert(fruit)
            } else {
                fruit.id = id
                if (deleteFruit) fruitViewModel.delete(fruit) else {
                    fruitViewModel.update(fruit)
                }
            }

        }
    }

    /*---------------------onItemClicked listener--------------------------*/
    override fun onItemClicked(fruit: Fruit) {
        val intent = Intent(this, ActivityAddEditFruit::class.java)
        intent.putExtra(ActivityAddEditFruit.EXTRA_ID, fruit.id)
        intent.putExtra(ActivityAddEditFruit.EXTRA_NAME, fruit.name)
        openAddEditActivity.launch(intent)
    }
}
