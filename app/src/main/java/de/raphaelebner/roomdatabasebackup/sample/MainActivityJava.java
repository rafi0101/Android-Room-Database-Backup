package de.raphaelebner.roomdatabasebackup.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import de.raphaelebner.roomdatabasebackup.core.RoomBackup;
import de.raphaelebner.roomdatabasebackup.sample.database.main.FruitDatabase;
import de.raphaelebner.roomdatabasebackup.sample.database.table.fruit.Fruit;
import de.raphaelebner.roomdatabasebackup.sample.database.table.fruit.FruitListAdapter;
import de.raphaelebner.roomdatabasebackup.sample.database.table.fruit.FruitViewModel;

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
public class MainActivityJava extends AppCompatActivity implements FruitListAdapter.OnItemClickListener {

    private static final String TAG = "debug_MainActivityJava";
    private FruitViewModel fruitViewModel;

    private boolean encryptBackup;
    /*---------------------when returning from |ActivityAddEditFruit| do something--------------------------*/
    ActivityResultLauncher<Intent> openAddEditActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                /*---------------------If the Request was successful--------------------------*/
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    String name = data.getStringExtra(ActivityAddEditFruit.EXTRA_NAME);
                    int id = data.getIntExtra(ActivityAddEditFruit.EXTRA_ID, -1);
                    boolean deleteFruit = data.getBooleanExtra(ActivityAddEditFruit.EXTRA_DELETE_FRUIT, false);
                    Fruit fruit = new Fruit(name);

                    if (id == -1) {
                        fruitViewModel.insert(fruit);
                    } else {
                        fruit.setId(id);
                        if (deleteFruit) fruitViewModel.delete(fruit);
                        else fruitViewModel.update(fruit);
                    }
                }
            }
    );
    private boolean enableLog;
    private boolean useMaxFileCount;
    private int storageLocation;

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
        Button btn_backupLocation = findViewById(R.id.btn_backup_location);
        TextView tvFruits = findViewById(R.id.tv_fruits);


        final FruitListAdapter adapter = new FruitListAdapter(this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fruitViewModel = new ViewModelProvider(this).get(FruitViewModel.class);

        fruitViewModel.getAllFruit().observe(this, adapter::submitList);

        tvFruits.setText("Fruits List (Java)");
        btn_language.setText("switch to Kotlin");

        String SHARED_PREFS = "sampleBackup";
        final String spEncryptBackup = "encryptBackup";
        final String spStorageLocation = "storageLocation";
        final String spEnableLog = "enableLog";
        final String spUseMaxFileCount = "useMaxFileCount";
        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        /*---------------------FAB Add Button--------------------------*/
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityAddEditFruit.class);
            openAddEditActivity.launch(intent);
        });

        /*---------------------go to Kotlin MainActivity--------------------------*/
        btn_language.setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });


        encryptBackup = sharedPreferences.getBoolean(spEncryptBackup, true);
        storageLocation = sharedPreferences.getInt(spStorageLocation, 1);
        enableLog = sharedPreferences.getBoolean(spEnableLog, true);
        useMaxFileCount = sharedPreferences.getBoolean(spUseMaxFileCount, false);

        final String[] multiItems = new String[]{"Encrypt Backup", "enable Log", "use maxFileCount = 5"};
        final boolean[] checkedItems = new boolean[]{encryptBackup, enableLog, useMaxFileCount};
        final String[] storageItems = new String[]{"Internal", "External", "Custom Dialog", "Custom File"};

        /*---------------------set Properties--------------------------*/
        btn_properties.setOnClickListener(v -> {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivityJava.this);
            materialAlertDialogBuilder.setTitle("Change Properties");
            materialAlertDialogBuilder.setPositiveButton("Ok", null);

            materialAlertDialogBuilder.setMultiChoiceItems(multiItems, checkedItems, (dialog, which, isChecked) -> {
                switch (which) {
                    case 0:
                        encryptBackup = isChecked;
                        sharedPreferences.edit().putBoolean(spEncryptBackup, encryptBackup).apply();
                        break;
                    case 1:
                        enableLog = isChecked;
                        sharedPreferences.edit().putBoolean(spEnableLog, enableLog).apply();
                        break;
                    case 2:
                        useMaxFileCount = isChecked;
                        sharedPreferences.edit().putBoolean(spUseMaxFileCount, useMaxFileCount).apply();
                        break;
                    default:
                }
            });
            materialAlertDialogBuilder.show();

        });

        /*---------------------set Backup Location--------------------------*/
        btn_backupLocation.setOnClickListener(v -> {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivityJava.this);
            materialAlertDialogBuilder.setTitle("Change Storage");
            materialAlertDialogBuilder.setPositiveButton("Ok", null);

            materialAlertDialogBuilder.setSingleChoiceItems(storageItems, storageLocation-1, (dialog, which) -> {
                switch(which) {
                    case 0:
                        storageLocation = RoomBackup.BACKUP_FILE_LOCATION_INTERNAL;
                        sharedPreferences.edit().putInt(spStorageLocation, storageLocation).apply();
                        break;
                    case 1:
                        storageLocation = RoomBackup.BACKUP_FILE_LOCATION_EXTERNAL;
                        sharedPreferences.edit().putInt(spStorageLocation, storageLocation).apply();
                        break;
                    case 2:
                        storageLocation = RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG;
                        sharedPreferences.edit().putInt(spStorageLocation, storageLocation).apply();
                        break;
                    case 3:
                        storageLocation = RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_FILE;
                        sharedPreferences.edit().putInt(spStorageLocation, storageLocation).apply();
                        break;
                }
            });
            materialAlertDialogBuilder.show();

        });

        final RoomBackup roomBackup = new RoomBackup(MainActivityJava.this);
        /*---------------------Backup and Restore Database--------------------------*/
        btn_backup.setOnClickListener(v -> {
            roomBackup.backupLocation(storageLocation);
            roomBackup.backupLocationCustomFile(new File(this.getFilesDir()+"/databasebackup/geilesBackup.sqlite3"));
            roomBackup.database(FruitDatabase.Companion.getInstance(getApplicationContext()));
            roomBackup.enableLogDebug(enableLog);
            roomBackup.backupIsEncrypted(encryptBackup);
            roomBackup.customEncryptPassword(MainActivity.SECRET_PASSWORD);
            if (useMaxFileCount) roomBackup.maxFileCount(5);
            roomBackup.onCompleteListener((success, message, exitCode) -> {
                Log.d(TAG, "oncomplete: " + success + ", message: " + message);
                if (success)
                    roomBackup.restartApp(new Intent(getApplicationContext(), MainActivityJava.class));
            });
            roomBackup.backup();

        });

        btn_restore.setOnClickListener(v -> {
            roomBackup.backupLocation(storageLocation);
            roomBackup.backupLocationCustomFile(new File(this.getFilesDir()+"/databasebackup/geilesBackup.sqlite3"));
            roomBackup.database(FruitDatabase.Companion.getInstance(getApplicationContext()));
            roomBackup.enableLogDebug(enableLog);
            roomBackup.backupIsEncrypted(encryptBackup);
            roomBackup.customEncryptPassword(MainActivity.SECRET_PASSWORD);
            roomBackup.onCompleteListener((success, message, exitCode) -> {
                Log.d(TAG, "oncomplete: " + success + ", message: " + message);
                if (success)
                    roomBackup.restartApp(new Intent(getApplicationContext(), MainActivityJava.class));
            });
            roomBackup.restore();

        });


        /*---------------------Swiping on a row--------------------------*/
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Fruit fruit = adapter.getFruitAt(position);

                assert fruit != null;
                fruitViewModel.delete(fruit);
            }
        }) {


        };
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /*---------------------onItemClicked listener--------------------------*/
    @Override
    public void onItemClicked(@NotNull Fruit fruit) {
        Intent intent = new Intent(this, ActivityAddEditFruit.class);
        intent.putExtra(ActivityAddEditFruit.EXTRA_ID, fruit.getId());
        intent.putExtra(ActivityAddEditFruit.EXTRA_NAME, fruit.getName());
        openAddEditActivity.launch(intent);
    }
}
