package com.caurix.distributorauto;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okio.BufferedSink;
import okio.Okio;

public class DistributorautoApp extends Application {
    private ConnectivityReceiver connectivityReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        registerConnectivityReceiver();

        //writing test data
//        try {
//            JSONArray array = new JSONArray();
//            array.put("777049602");
//            array.put("555049602");
//            array.put("444049602");
//            array.put("222049602");
//            saveToDiskRx(new Gson().toJson(array.toString())).subscribe();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    // register here your filtters
    private void registerConnectivityReceiver() {
        try {
            // if (android.os.Build.VERSION.SDK_INT >= 26) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            //filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            //filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            //filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            registerReceiver(getConnectivityReceiver(), filter);
        } catch (Exception e) {
            Log.d(K.LOGTAG, e.getMessage());
        }
    }

    private ConnectivityReceiver getConnectivityReceiver() {
        if (connectivityReceiver == null)
            connectivityReceiver = new ConnectivityReceiver();

        return connectivityReceiver;
    }


    //here is file saving work

    private @NonNull Single<String> saveToDiskRx(final String data) {
        return Single.fromCallable(() -> {
                    try {

                        String filename = "codes.txt";

                        File file = new File(getFilesDir() + "/codes_folder");
                        if (!file.exists()) {
                            file.mkdir();
                        }

                        File destinationFile = new File(file, filename);
                        if (destinationFile.exists()) {
                            destinationFile.delete();
                        }

                        BufferedSink bufferedSink = Okio.buffer(Okio.sink(destinationFile));
                        bufferedSink.write(data.getBytes());
                        bufferedSink.close();

                        return destinationFile.getPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
