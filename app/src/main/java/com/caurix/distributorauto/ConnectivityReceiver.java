package com.caurix.distributorauto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.caurix.distributorauto.bean.TransactionRecord;
import com.caurix.distributorauto.web.WebTask;
import com.caurix.distributorauto.web.WebTaskDelegate;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnectivityReceiver extends BroadcastReceiver {
    Map<String, Integer> cursorTrack = new HashMap<>();
    String id = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(K.LOGTAG, "inside network reciver");
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {
            Log.d(K.LOGTAG, "network back");
            final Cursor cursor = SharedSingleton.getInstance().getDB(context)
                    .getOKPostNotOnServer();
            if (cursor != null && cursor.getCount() != 0) {
                Log.d(K.LOGTAG, "cursor not null");
                Log.d(K.LOGTAG, "cursor size: " + cursor.getCount());
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
//                    if (cursor.getInt((cursor
//                            .getColumnIndex("postedOnServer"))) == 1) {
                        final String url = K.DISTRIBUTOR_SERVICE_URL + "saveTransaction";
                        final Gson gson = new Gson();

                        final String distCellNo = SharedSingleton.getInstance().getDB(context)
                                .getGeneralSetting(DistributorDB.SETTING_NUMERO_DE_TELEPHONE);

                        if (distCellNo != null && !distCellNo.equals("")
                                && !distCellNo.equals("123")) {
                            TransactionRecord recordOk = new TransactionRecord(
                                    cursor.getString((cursor
                                            .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_ID))),
                                    cursor.getString((cursor
                                            .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_NOTES))),
                                    cursor.getString((cursor
                                            .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_AMOUNT))),
                                    TRX_STATUS.OK.toString(),
                                    cursor.getString((cursor
                                            .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TARGET_NUMBER))),
                                    cursor.getString((cursor
                                            .getColumnIndex(DistributorDB.COLUMN_SUBD_TRANSACTIONPHONE))),
                                    cursor.getString((cursor
                                            .getColumnIndex(DistributorDB.COLUMN_SUBD_SDNAME))),
                                    cursor.getString((cursor
                                            .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TRXTYPE))));

                            ArrayList<TransactionRecord> transactionRecordsOk = new ArrayList<TransactionRecord>();
                            transactionRecordsOk.add(recordOk);
                            final String recordsStrOk = gson.toJson(transactionRecordsOk);
                            Map<String, String> paramsOk = new HashMap<>();
                            paramsOk.put("dist_cell_no", distCellNo);
                            paramsOk.put("finalized_trx", recordsStrOk);
                            paramsOk.put("sdMobileNumber", cursor.getString((cursor
                                    .getColumnIndex(DistributorDB.COLUMN_SUBD_TRANSACTIONPHONE))));
                            paramsOk.put("currentBalance", cursor.getString((cursor
                                    .getColumnIndex(DistributorDB.COLUMN_SUBD_CURRENTBALANCE))));

                            Log.d(K.LOGTAG, "before webTask: ok");
                            Log.d(K.LOGTAG, "before webTask: " + distCellNo);
                            Log.d(K.LOGTAG, "before webTask: " + recordsStrOk);
                            Log.d(K.LOGTAG, "before webTask: " + cursor.getString((cursor
                                    .getColumnIndex(DistributorDB.COLUMN_SUBD_TRANSACTIONPHONE))));
                            Log.d(K.LOGTAG, "before webTask: " + cursor.getString((cursor
                                    .getColumnIndex(DistributorDB.COLUMN_SUBD_CURRENTBALANCE))));

                            WebTask webTask = new WebTask(url, paramsOk, new WebTaskDelegate() {

                                @Override
                                public void asynchronousPostExecute(String sr) {
                                    super.asynchronousPostExecute(sr);
                                    Log.d(K.LOGTAG, "inside async post execute " + sr);
                                }

                            });
                            webTask.execute();
                        }
//                    } else {
//
//                    }
                }
            }
        }
    }
}