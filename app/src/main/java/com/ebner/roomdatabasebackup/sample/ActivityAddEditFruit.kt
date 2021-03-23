package com.ebner.roomdatabasebackup.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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
class ActivityAddEditFruit : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "com.ebner.roomdatabasebackup.sample.EXTRA_ID"
        const val EXTRA_NAME = "com.ebner.roomdatabasebackup.sample.EXTRA_NAME"
        const val EXTRA_DELETE_FRUIT = "com.ebner.roomdatabasebackup.sample.EXTRA_DELETE_FRUIT"
    }

    private lateinit var tietFruit: TextInputEditText
    private lateinit var tilFruit: TextInputLayout
    private lateinit var btnDelete: Button
    private var deleteFruit = false

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
        btnDelete = findViewById(R.id.btn_delete)

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

        //Delete Fruit
        btnDelete.setOnClickListener {
            if (intent.hasExtra(EXTRA_ID)) {
                deleteFruit = true
                saveFruit()
            } else {
                super.onBackPressed()
            }
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
        data.putExtra(EXTRA_DELETE_FRUIT, deleteFruit)
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