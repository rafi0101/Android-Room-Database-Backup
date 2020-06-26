package com.ebner.roomdatabasebackup.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ebner.roomdatabasebackup.core.OnCompleteListener;
import com.ebner.roomdatabasebackup.core.RoomBackup;
import com.ebner.roomdatabasebackup.sample.database.main.FruitDatabase;
import com.ebner.roomdatabasebackup.sample.database.table.fruit.Fruit;
import com.ebner.roomdatabasebackup.sample.database.table.fruit.FruitListAdapter;
import com.ebner.roomdatabasebackup.sample.database.table.fruit.FruitViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Copyright 2020 Raphael Ebner
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class MainActivityJava extends AppCompatActivity implements FruitListAdapter.OnItemClickListener {

    public static final int ADD_FRUIT_REQUEST = 1;
    public static final int EDIT_FRUIT_REQUEST = 2;
    private static final String TAG = "debug_MainActivityJava";
    private FruitViewModel fruitViewModel;

    private boolean encryptBackup;
    private boolean useExternalStorage;
    private boolean enableLog;
    private boolean useMaxFileCount;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*---------------------Link items to Layout--------------------------*/
        RecyclerView recyclerView = findViewById(R.id.rv_fruits);
        FloatingActionButton fab = findViewById(R.id.btn_addFruit);
        Button btn_backup = findViewById(R.id.btn_backup);
        Button btn_restore = findViewById(R.id.btn_restore);
        Button btn_language = findViewById(R.id.btn_switch_language);
        Button btn_properties = findViewById(R.id.btn_properties);
        TextView tvFruits = findViewById(R.id.tv_fruits);


        final FruitListAdapter adapter = new FruitListAdapter(this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fruitViewModel = new ViewModelProvider(this).get(FruitViewModel.class);

        fruitViewModel.getAllFruit().observe(this, new Observer<List<Fruit>>() {
            @Override
            public void onChanged(List<Fruit> fruits) {
                adapter.submitList(fruits);
            }
        });

        tvFruits.setText("Fruits List (Java)");
        btn_language.setText("switch to Kotlin");

        String SHARED_PREFS = "sampleBackup";
        final String spEncryptBackup = "encryptBackup";
        final String spUseExternalStorage = "useExternalStorage";
        final String spEnableLog = "enableLog";
        final String spUseMaxFileCount = "useMaxFileCount";
        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        /*---------------------FAB Add Button--------------------------*/
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivityAddEditFruit.class);
                startActivityForResult(intent, ADD_FRUIT_REQUEST);
            }

        });

        /*---------------------go to Kotlin MainActivity--------------------------*/
        btn_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });


        encryptBackup = sharedPreferences.getBoolean(spEncryptBackup, true);
        useExternalStorage = sharedPreferences.getBoolean(spUseExternalStorage, false);
        enableLog = sharedPreferences.getBoolean(spEnableLog, true);
        useMaxFileCount = sharedPreferences.getBoolean(spUseMaxFileCount, false);

        final String[] multiItems = new String[]{"Encrypt Backup", "use External Storage", "enable Log", "use maxFileCount = 5"};
        final boolean[] checkedItems = new boolean[]{encryptBackup, useExternalStorage, enableLog, useMaxFileCount};

        /*---------------------set Properties--------------------------*/
        btn_properties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivityJava.this);
                materialAlertDialogBuilder.setTitle("Change Properties");
                materialAlertDialogBuilder.setPositiveButton("Ok", null);

                materialAlertDialogBuilder.setMultiChoiceItems(multiItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        switch (which) {
                            case 0:
                                encryptBackup = isChecked;
                                sharedPreferences.edit().putBoolean(spEncryptBackup, encryptBackup).apply();
                                break;
                            case 1:
                                useExternalStorage = isChecked;
                                sharedPreferences.edit().putBoolean(spUseExternalStorage, useExternalStorage).apply();
                                break;
                            case 2:
                                enableLog = isChecked;
                                sharedPreferences.edit().putBoolean(spEnableLog, enableLog).apply();
                                break;
                            case 3:
                                useMaxFileCount = isChecked;
                                sharedPreferences.edit().putBoolean(spUseMaxFileCount, useMaxFileCount).apply();
                                break;
                            default:
                        }
                    }
                });
                materialAlertDialogBuilder.show();

            }
        });



        /*---------------------Backup and Restore Database--------------------------*/
        btn_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RoomBackup roomBackup = new RoomBackup();
                roomBackup.context(MainActivityJava.this);
                roomBackup.database(FruitDatabase.Companion.getInstance(getApplicationContext()));
                roomBackup.enableLogDebug(enableLog);
                roomBackup.backupIsEncrypted(encryptBackup);
                roomBackup.useExternalStorage(useExternalStorage);
                if (useMaxFileCount) roomBackup.maxFileCount(5);
                roomBackup.onCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(boolean success, @NotNull String message) {
                        Log.d(TAG, "oncomplete: " + success + ", message: " + message);
                        if (success) roomBackup.restartApp(new Intent(getApplicationContext(), MainActivityJava.class));
                    }
                });
                roomBackup.backup();

            }
        });

        btn_restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RoomBackup roomBackup = new RoomBackup();
                roomBackup.context(MainActivityJava.this);
                roomBackup.database(FruitDatabase.Companion.getInstance(getApplicationContext()));
                roomBackup.enableLogDebug(enableLog);
                roomBackup.backupIsEncrypted(encryptBackup);
                roomBackup.useExternalStorage(useExternalStorage);
                roomBackup.onCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(boolean success, @NotNull String message) {
                        Log.d(TAG, "oncomplete: " + success + ", message: " + message);
                        if (success) roomBackup.restartApp(new Intent(getApplicationContext(), MainActivityJava.class));
                    }
                });
                roomBackup.restore();

            }
        });


        /*---------------------Swiping on a row--------------------------*/
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Fruit fruit = adapter.getFruitAt(position);

                assert fruit != null;
                fruitViewModel.delete(fruit);
            }
        }) {


        };
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    /*---------------------when returning from |ActivityAddEditFruit| do something--------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*---------------------If the Request was successful--------------------------*/
        if (resultCode == Activity.RESULT_OK) {
            assert data != null;
            String name = data.getStringExtra(ActivityAddEditFruit.EXTRA_NAME);
            Fruit fruit = null;
            fruit.setName(name);

            /*---------------------If the Request was a ADD fruit request--------------------------*/
            if (requestCode == ADD_FRUIT_REQUEST) {

                fruitViewModel.insert(fruit);

                /*---------------------If the Request was a EDIT fruit request--------------------------*/
            } else if (requestCode == EDIT_FRUIT_REQUEST) {
                int id = data.getIntExtra(ActivityAddEditFruit.EXTRA_ID, -1);

                if (id == -1) {
                    return;
                }

                fruit.setId(id);
                fruitViewModel.update(fruit);
            }
        }

    }

    /*---------------------onItemClicked listener--------------------------*/
    @Override
    public void onItemClicked(@NotNull Fruit fruit) {
        Intent intent = new Intent(this, ActivityAddEditFruit.class);
        intent.putExtra(ActivityAddEditFruit.EXTRA_ID, fruit.getId());
        intent.putExtra(ActivityAddEditFruit.EXTRA_NAME, fruit.getName());
        startActivityForResult(intent, EDIT_FRUIT_REQUEST);
    }
}