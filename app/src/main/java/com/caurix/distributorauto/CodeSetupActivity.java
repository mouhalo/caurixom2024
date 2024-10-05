package com.caurix.distributorauto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.caurix.distributor.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.internal.io.FileSystem;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class CodeSetupActivity extends Activity {
    ListView listView;
    Button btnDelete, btnSave, btnAdd;
    private int position = -1;
    private ArrayList<String> myList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.codes_setup);
        listView = findViewById(R.id.listView);
        btnDelete = findViewById(R.id.btnDelete);
        btnSave = findViewById(R.id.btnSave);
        btnAdd = findViewById(R.id.btnAdd);


        Single.fromCallable((Callable<List<String>>) () -> {
                    File file = new File(getFilesDir() + "/codes_folder/codes.txt");
                    if (!file.exists()) {
                        return Collections.emptyList();
                    }

                    try (final BufferedSource source = Okio.buffer(FileSystem.SYSTEM.source(file))) {
                        String s = source.readUtf8();
                        Log.e("CodeSetupActivity", "onCreate: " + s);

                        List<String> codes = new Gson().fromJson(s, new TypeToken<List<String>>() {
                        }.getType());
                        return codes;
                    } catch (final IOException exception) {
                        //ignored exception
                        exception.printStackTrace();
                        return Collections.emptyList();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    myList.clear();
                    myList.addAll(list);
                    ArrayAdapter<String> arrayAdapter =
                            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, myList);
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            position = i;
                        }
                    });
                    listView.setAdapter(arrayAdapter);

                }, Throwable::printStackTrace);


        btnDelete.setOnClickListener(view -> {
            if (position != -1) {
                myList.remove(position);
                ((ArrayAdapter<?>) listView.getAdapter()).notifyDataSetChanged();
            }
        });


        btnAdd.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Title");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    myList.add(input.getText().toString());
                    ((ArrayAdapter<?>) listView.getAdapter()).notifyDataSetChanged();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        });

        btnSave.setOnClickListener(view -> {
            reWriteFile();
        });

    }

    private void reWriteFile() {
        Single.fromCallable(() -> {
            String s = new Gson().toJson(myList);
            File dir = new File(getFilesDir() + "/codes_folder");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, "codes.txt");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            BufferedSink bufferedSink = Okio.buffer(Okio.sink(file));
            bufferedSink.write(s.getBytes());
            bufferedSink.close();
            return "";
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(s -> {
            Toast.makeText(this, "saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        }, Throwable::printStackTrace);
    }


}
