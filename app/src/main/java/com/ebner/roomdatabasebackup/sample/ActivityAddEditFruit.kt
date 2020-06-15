package com.ebner.roomdatabasebackup.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ActivityAddEditFruit : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "com.ebner.roomdatabasebackup.sample.EXTRA_ID"
        const val EXTRA_NAME = "com.ebner.roomdatabasebackup.sample.EXTRA_NAME"
    }

    private lateinit var tietFruit: TextInputEditText
    private lateinit var tilFruit: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_fruit)

        /*---------------------Add back Button to the toolbar--------------------------*/
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        /*---------------------Link items to Layout--------------------------*/
        tietFruit = findViewById(R.id.tiet_fruit)
        tilFruit = findViewById(R.id.til_fruit)

        /*---------------------when calling this Activity, are some extras passed?--------------------------*/
        if (intent.hasExtra(EXTRA_NAME)) {
            title = "Edit Fruit"
            tietFruit.setText(intent.getStringExtra(EXTRA_NAME))
        } else {
            title = "New Fruit"
        }

        //Remove the error message, if user starts typing
        tietFruit.addTextChangedListener {
            tilFruit.error = ""
        }

    }


    /*---------------------Save current entries, and go back--------------------------*/
    private fun saveFruit() {

        /*---------------------If EditText is empty--------------------------*/
        if (TextUtils.isEmpty(tietFruit.text.toString()) || TextUtils.getTrimmedLength(tietFruit.text.toString()) == 0) {
            tilFruit.error = "Fruit name is missing!"
            return
        }

        val tname = tietFruit.text.toString()

        val data = Intent()
        data.putExtra(EXTRA_NAME, tname)
        val id = intent.getIntExtra(EXTRA_ID, -1)
        if (id != -1) {
            data.putExtra(EXTRA_ID, id)
        }
        setResult(Activity.RESULT_OK, data)
        finish()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.save_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            //Save Button
            R.id.nav_save -> {
                saveFruit()
                true
            }
            //Back Button
            android.R.id.home -> {
                super.onBackPressed()
                true

            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}