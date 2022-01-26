package de.raphaelebner.roomdatabasebackup.sample

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import de.raphaelebner.roomdatabasebackup.sample.database.main.FruitDatabase
import de.raphaelebner.roomdatabasebackup.sample.database.table.fruit.Fruit
import de.raphaelebner.roomdatabasebackup.sample.database.table.fruit.FruitListAdapter
import de.raphaelebner.roomdatabasebackup.sample.database.table.fruit.FruitViewModel
import java.io.File

/**
 * MIT License
 * <p>
 * Copyright (c) 2021 Raphael Ebner
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class MainFragment : Fragment(), FruitListAdapter.OnItemClickListener {

    private lateinit var fruitViewModel: FruitViewModel

    companion object {
        private const val TAG = "debug_MainActivity"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.activity_main, container, false)

        /*---------------------Link items to Layout--------------------------*/
        val recyclerView: RecyclerView = root.findViewById(R.id.rv_fruits)
        val fab: FloatingActionButton = root.findViewById(R.id.btn_addFruit)
        val btnBackup: Button = root.findViewById(R.id.btn_backup)
        val btnRestore: Button = root.findViewById(R.id.btn_restore)
        val btnProperties: Button = root.findViewById(R.id.btn_properties)
        val btnLanguage: Button = root.findViewById(R.id.btn_switch_language)
        val btnFragmentActivity: Button = root.findViewById(R.id.btn_switch_fragment_activity)
        val btnBackupLocation: Button = root.findViewById(R.id.btn_backup_location)
        val tvFruits: TextView = root.findViewById(R.id.tv_fruits)


        val adapter = FruitListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(root.context)

        fruitViewModel = ViewModelProvider(this)[FruitViewModel::class.java]

        fruitViewModel.allFruit.observe(viewLifecycleOwner) { fruits ->
            adapter.submitList(fruits)
        }

        tvFruits.text = "Fruits List (Kotlin Fragment)"
        btnLanguage.text = "switch to Java"
        btnFragmentActivity.text = "switch to Activity"

        val sharedPrefs = "sampleBackup"
        val spEncryptBackup = "encryptBackup"
        val spStorageLocation = "storageLocation"
        val spEnableLog = "enableLog"
        val spUseMaxFileCount = "useMaxFileCount"
        val sharedPreferences = root.context.getSharedPreferences(sharedPrefs, Context.MODE_PRIVATE)

        /*---------------------FAB Add Button--------------------------*/
        fab.setOnClickListener {
            val intent = Intent(root.context, ActivityAddEditFruit::class.java)
            openAddEditActivity.launch(intent)
        }

        /*---------------------go to Java Fragment--------------------------*/
        btnLanguage.setOnClickListener {
            activity?.finish()
            val intent = Intent(root.context, FragmentActivityJava::class.java)
            startActivity(intent)
        }

        /*---------------------go to Kotlin Activity class--------------------------*/
        btnFragmentActivity.setOnClickListener {
            activity?.finish()
            val intent = Intent(root.context, MainActivity::class.java)
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

            MaterialAlertDialogBuilder(root.context).setTitle("Change Properties").setPositiveButton("Ok", null)
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
                }.show()
        }

        /*---------------------set Backup Location--------------------------*/
        btnBackupLocation.setOnClickListener {
            val storageItems = arrayOf("Internal", "External", "Custom Dialog", "Custom File")
            MaterialAlertDialogBuilder(root.context).setTitle("Change Storage").setPositiveButton("Ok", null).setSingleChoiceItems(storageItems, storageLocation - 1) { _, which ->
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
                }.show()
        }

        val fragmentActivity = (activity as FragmentActivity)
        val backup = fragmentActivity.backup

        /*---------------------Backup and Restore Database--------------------------*/
        btnBackup.setOnClickListener {
            backup.backupLocation(storageLocation).backupLocationCustomFile(File("${root.context.filesDir}/databasebackup/geilesBackup.sqlite3")).database(FruitDatabase.getInstance(root.context)).enableLogDebug(enableLog).backupIsEncrypted(encryptBackup).customEncryptPassword(MainActivity.SECRET_PASSWORD)
                //maxFileCount: else 1000 because i cannot surround it with if condition
                .maxFileCount(if (useMaxFileCount) 5 else 1000).apply {
                    onCompleteListener { success, message, exitCode ->
                        Log.d(TAG, "success: $success, message: $message, exitCode: $exitCode")
                        Toast.makeText(
                            root.context, "success: $success, message: $message, exitCode: $exitCode", Toast.LENGTH_LONG
                        ).show()
                        if (success) restartApp(Intent(root.context, FragmentActivity::class.java))
                    }
                }.backup()


        }
        btnRestore.setOnClickListener {
            backup.backupLocation(storageLocation).backupLocationCustomFile(File("${root.context.filesDir}/databasebackup/geilesBackup.sqlite3")).database(FruitDatabase.getInstance(root.context)).enableLogDebug(enableLog).backupIsEncrypted(encryptBackup).customEncryptPassword(MainActivity.SECRET_PASSWORD).apply {
                    onCompleteListener { success, message, exitCode ->
                        Log.d(TAG, "success: $success, message: $message, exitCode: $exitCode")
                        Toast.makeText(
                            root.context, "success: $success, message: $message, exitCode: $exitCode", Toast.LENGTH_LONG
                        ).show()
                        if (success) restartApp(Intent(root.context, FragmentActivity::class.java))
                    }

                }.restore()
        }

        return root
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
        val intent = Intent(requireContext(), ActivityAddEditFruit::class.java)
        intent.putExtra(ActivityAddEditFruit.EXTRA_ID, fruit.id)
        intent.putExtra(ActivityAddEditFruit.EXTRA_NAME, fruit.name)
        openAddEditActivity.launch(intent)
    }

}
