package com.ebner.roomdatabasebackup.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
import com.ebner.roomdatabasebackup.sample.database.table.Fruit.Fruit;
import com.ebner.roomdatabasebackup.sample.database.table.Fruit.FruitListAdapter;
import com.ebner.roomdatabasebackup.sample.database.table.Fruit.FruitViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainActivityJava extends AppCompatActivity implements FruitListAdapter.OnItemClickListener {

    public static final int ADD_FRUIT_REQUEST = 1;
    public static final int EDIT_FRUIT_REQUEST = 2;
    private static final String TAG = "debug_MainActivityJava";
    private FruitViewModel fruitViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_java);

        /*---------------------Link items to Layout--------------------------*/
        RecyclerView recyclerView = findViewById(R.id.rv_fruits);
        FloatingActionButton fab = findViewById(R.id.btn_addFruit);
        Button btn_backup = findViewById(R.id.btn_backup);
        Button btn_restore = findViewById(R.id.btn_restore);
        Button btn_kotlin = findViewById(R.id.btn_kotlin);


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

        /*---------------------FAB Add Button--------------------------*/
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivityAddEditFruit.class);
                startActivityForResult(intent, ADD_FRUIT_REQUEST);
            }

        });

        /*---------------------go to Kotlin MainActivity--------------------------*/
        btn_kotlin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        /*---------------------Backup and Restore Database--------------------------*/
        btn_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RoomBackup roomBackup = new RoomBackup();
                roomBackup.context(MainActivityJava.this);
                roomBackup.database(FruitDatabase.Companion.getInstance(getApplicationContext()));
                roomBackup.enableLogDebug(true);
                roomBackup.onCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(boolean success, @NotNull String message) {
                        Log.d(TAG, "oncomplete + mesage " + success + message);
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
                roomBackup.enableLogDebug(true);
                roomBackup.onCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(boolean success, @NotNull String message) {
                        Log.d(TAG, "oncomplete + mesage " + success + message);
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