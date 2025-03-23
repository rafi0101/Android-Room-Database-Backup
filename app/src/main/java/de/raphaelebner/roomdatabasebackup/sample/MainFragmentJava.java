package de.raphaelebner.roomdatabasebackup.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
 * Copyright (c) 2025 Raphael Ebner
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all
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
public class MainFragmentJava extends Fragment implements FruitListAdapter.OnItemClickListener {

    public MainFragmentJava() {
        // Required empty public constructor
    }

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
                        if (deleteFruit)
                            fruitViewModel.delete(fruit);
                        else
                            fruitViewModel.update(fruit);
                    }
                }
            });
    private boolean enableLog;
    private boolean useMaxFileCount;
    private int storageLocation;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.activity_main, container, false);

        /*---------------------Link items to Layout--------------------------*/
        RecyclerView recyclerView = root.findViewById(R.id.rv_fruits);
        FloatingActionButton fab = root.findViewById(R.id.btn_addFruit);
        Button btn_backup = root.findViewById(R.id.btn_backup);
        Button btn_restore = root.findViewById(R.id.btn_restore);
        Button btn_language = root.findViewById(R.id.btn_switch_language);
        Button btn_fragment_activity = root.findViewById(R.id.btn_switch_fragment_activity);
        Button btn_properties = root.findViewById(R.id.btn_properties);
        Button btn_backupLocation = root.findViewById(R.id.btn_backup_location);
        TextView tvFruits = root.findViewById(R.id.tv_fruits);

        final FruitListAdapter adapter = new FruitListAdapter(this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));

        fruitViewModel = new ViewModelProvider(this).get(FruitViewModel.class);

        fruitViewModel.getAllFruit().observe(getViewLifecycleOwner(), adapter::submitList);

        tvFruits.setText("Fruits List (Java Fragment)");
        btn_language.setText("switch to Kotlin");
        btn_fragment_activity.setText("switch to Activity");

        String SHARED_PREFS = "sampleBackup";
        final String spEncryptBackup = "encryptBackup";
        final String spStorageLocation = "storageLocation";
        final String spEnableLog = "enableLog";
        final String spUseMaxFileCount = "useMaxFileCount";
        final SharedPreferences sharedPreferences = root.getContext().getSharedPreferences(SHARED_PREFS,
                Context.MODE_PRIVATE);

        /*---------------------FAB Add Button--------------------------*/
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(root.getContext(), ActivityAddEditFruit.class);
            openAddEditActivity.launch(intent);
        });

        /*---------------------go to Kotlin Fragment--------------------------*/
        btn_language.setOnClickListener(v -> {
            requireActivity().finish();
            Intent intent = new Intent(root.getContext(), FragmentActivity.class);
            startActivity(intent);
        });

        /*---------------------go to Activity class--------------------------*/
        btn_fragment_activity.setOnClickListener(v -> {
            requireActivity().finish();
            Intent intent = new Intent(root.getContext(), MainActivityJava.class);
            startActivity(intent);
        });

        encryptBackup = sharedPreferences.getBoolean(spEncryptBackup, true);
        storageLocation = sharedPreferences.getInt(spStorageLocation, 1);
        enableLog = sharedPreferences.getBoolean(spEnableLog, true);
        useMaxFileCount = sharedPreferences.getBoolean(spUseMaxFileCount, false);

        final String[] multiItems = new String[] { "Encrypt Backup", "enable Log", "use maxFileCount = 5" };
        final boolean[] checkedItems = new boolean[] { encryptBackup, enableLog, useMaxFileCount };
        final String[] storageItems = new String[] { "Internal", "External", "Custom Dialog", "Custom File" };

        /*---------------------set Properties--------------------------*/
        btn_properties.setOnClickListener(v -> {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(root.getContext());
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
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(root.getContext());
            materialAlertDialogBuilder.setTitle("Change Storage");
            materialAlertDialogBuilder.setPositiveButton("Ok", null);

            materialAlertDialogBuilder.setSingleChoiceItems(storageItems, storageLocation - 1, (dialog, which) -> {
                switch (which) {
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

        FragmentActivityJava fragmentActivity = (FragmentActivityJava) getActivity();
        assert fragmentActivity != null;
        RoomBackup roomBackup = fragmentActivity.roomBackup;
        /*---------------------Backup and Restore Database--------------------------*/
        btn_backup.setOnClickListener(v -> {
            roomBackup.backupLocation(storageLocation);
            roomBackup.backupLocationCustomFile(
                    new File(root.getContext().getFilesDir() + "/databasebackup/geilesBackup.sqlite3"));
            roomBackup.database(FruitDatabase.Companion.getInstance(root.getContext()));
            roomBackup.enableLogDebug(enableLog);
            roomBackup.backupIsEncrypted(encryptBackup);
            roomBackup.customEncryptPassword(MainActivity.SECRET_PASSWORD);
            if (useMaxFileCount)
                roomBackup.maxFileCount(5);
            roomBackup.onCompleteListener((success, message, exitCode) -> {
                Log.d(TAG, "oncomplete: " + success + ", message: " + message + ", exitCode: " + exitCode);
                if (success)
                    roomBackup.restartApp(new Intent(root.getContext(), FragmentActivityJava.class));
            });
            roomBackup.backup();

        });

        btn_restore.setOnClickListener(v -> {
            roomBackup.backupLocation(storageLocation);
            roomBackup.backupLocationCustomFile(
                    new File(root.getContext().getFilesDir() + "/databasebackup/geilesBackup.sqlite3"));
            roomBackup.database(FruitDatabase.Companion.getInstance(root.getContext()));
            roomBackup.enableLogDebug(enableLog);
            roomBackup.backupIsEncrypted(encryptBackup);
            roomBackup.customEncryptPassword(MainActivity.SECRET_PASSWORD);
            roomBackup.onCompleteListener((success, message, exitCode) -> {
                Log.d(TAG, "oncomplete: " + success + ", message: " + message + ", exitCode: " + exitCode);
                if (success)
                    roomBackup.restartApp(new Intent(root.getContext(), FragmentActivityJava.class));
            });
            roomBackup.restore();

        });

        return root;
    }

    /*---------------------onItemClicked listener--------------------------*/
    @Override
    public void onItemClicked(@NotNull Fruit fruit) {
        Intent intent = new Intent(requireContext(), ActivityAddEditFruit.class);
        intent.putExtra(ActivityAddEditFruit.EXTRA_ID, fruit.getId());
        intent.putExtra(ActivityAddEditFruit.EXTRA_NAME, fruit.getName());
        openAddEditActivity.launch(intent);
    }
}
