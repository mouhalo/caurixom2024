package com.caurix.distributorauto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.caurix.distributor.R;
import com.caurix.distributorauto.bean.TransactionRecord;
import com.caurix.distributorauto.web.WebTask;
import com.caurix.distributorauto.web.WebTaskDelegate;
import com.google.gson.Gson;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Distributor extends Activity implements OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, PermissionResultCallback {


    //list of permissions

    ArrayList<String> permissions = new ArrayList<>();
    PermissionUtils permissionUtils;
    Context context;
    String Ussd = "";
    //activity_addsd

    TextView mTVSMSType, mTVSMSAmount, mTVSubDistributorName, mTVAmtBalance,
            mTVSDPhone, mTVTargetNumber, mTVUnattendedMode;
    Button mbtnAccept, mbtnDecline, mbtnNext, mbtnPrev;
    SMSDetail mSMSDetail;

    Cursor mCursorPendSMS;
    UpdateUIReciver mUIUpdateRecv;
    Boolean mRecRegistered = false;
    private boolean unattendedModeOn = false;

    private Timer summaryTimer = null;
    private TouchTask touchTask = null;
    private TransactionStatusTask transactionStatusTask = null;


    private TransactionPostTask transactionPostTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        permissionUtils = new PermissionUtils(this);

        permissions.add(Manifest.permission.CALL_PHONE);
        permissions.add(Manifest.permission.SEND_SMS);
        permissions.add(Manifest.permission.RECEIVE_SMS);
        permissions.add(Manifest.permission.READ_SMS);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.INTERNET);
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);


        permissionUtils.check_permission(permissions, "Explain here why the app needs permissions", 1);

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                report("adil.farooq@gmail.com",
                        "Distributor App Exception Trace", getStackTrace(ex));
                android.os.Process.killProcess(android.os.Process.myPid());
            }

            public String getStackTrace(Throwable e) {
                StringWriter s = new StringWriter();
                PrintWriter p = new PrintWriter(s);
                e.printStackTrace(p);
                return s.getBuffer().toString();
            }

            private void report(String mailTo, String emailSubject,
                                String emailBody) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                sendIntent
                        .putExtra(Intent.EXTRA_EMAIL, new String[]{mailTo});
                emailBody += "\n\n\n";
                emailBody += "******Do not change below********\n\n";

                sendIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

                sendIntent.setType("message/rfc822");
                Distributor.this.startActivity(Intent.createChooser(sendIntent,
                        "Send Email"));
            }
        });
        this.setTitle(R.string.liste_de_requetes);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_transfert_argent);

        // Register Alarm to be triggered daily to delete messages from local
        // database
        // used to avoid duplication of SMS
//		DuplicateMessageChecker mMessageChecker = new DuplicateMessageChecker(Distributor.this);
//		mMessageChecker.setDeleteDBAlarm();

        mTVSMSType = (TextView) findViewById(R.id.tvSMSType);
        mTVSMSAmount = (TextView) findViewById(R.id.tvSMSAmount);
        mTVSubDistributorName = (TextView) findViewById(R.id.tvSDName);
        mTVAmtBalance = (TextView) findViewById(R.id.tvAmtBalance);
        mTVSDPhone = (TextView) findViewById(R.id.tvSDPhone);
        mTVTargetNumber = (TextView) findViewById(R.id.tvValNumber);
        mTVUnattendedMode = (TextView) findViewById(R.id.txtunattended_mode);

        mbtnAccept = (Button) findViewById(R.id.btnAccept);
        mbtnDecline = (Button) findViewById(R.id.btnDecline);
        mbtnNext = (Button) findViewById(R.id.btnNextRec);
        mbtnPrev = (Button) findViewById(R.id.btnPrevRec);

        mUIUpdateRecv = new UpdateUIReciver();
        mSMSDetail = new SMSDetail();

        try {
            mbtnAccept.setOnClickListener(this);
            mbtnDecline.setOnClickListener(this);
            mbtnNext.setOnClickListener(this);
            mbtnPrev.setOnClickListener(this);
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey("LSP")) {
                    mSMSDetail = (SMSDetail) savedInstanceState
                            .getSerializable("LSP");
                }
            }
        } catch (Exception excp) {
            Log.e(K.LOGTAG, excp.getMessage());
        }

        SchedulerTask schedulerTask = new SchedulerTask();
        SummarySenderTask summarySenderTask = new SummarySenderTask(this, 30);
        summarySenderTask.setEnabled(true);

        touchTask = new TouchTask(this, 3);
        touchTask.setEnabled(false);

        transactionStatusTask = new TransactionStatusTask(this, 30);
        transactionPostTask = new TransactionPostTask(this, 600);

        schedulerTask.registerTask(summarySenderTask);
        schedulerTask.registerTask(touchTask);
        schedulerTask.registerTask(transactionStatusTask);
        schedulerTask.registerTask(transactionPostTask);

        summaryTimer = new Timer();
        summaryTimer.scheduleAtFixedRate(schedulerTask, 3, 1000);// Run this
        // after

        startService(new Intent(getBaseContext(), UpdateBalanceService.class));

        if (isAccessibilityServiceEnabled(this, USSDService.class) == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Accessibility permission needed")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("Allow this app to use Accessibility services")
                    .setPositiveButton("OK", (dialog, which) -> {
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    })
                    .create();
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        // redirects to utils

        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    // Callback functions


    @Override
    public void PermissionGranted(int request_code) {
        Log.i("PERMISSION", "GRANTED");
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        Log.i("PERMISSION PARTIALLY", "GRANTED");
    }

    @Override
    public void PermissionDenied(int request_code) {
        Log.i("PERMISSION", "DENIED");
    }

    @Override
    public void NeverAskAgain(int request_code) {
        Log.i("PERMISSION", "NEVER ASK AGAIN");
    }

    public void onWindowFocusChanged(boolean isTrue) {
        super.onWindowFocusChanged(isTrue);

        // touchTask.setEnabled(!isTrue);
        if (!isTrue) {
            dispatchTouchEvent(MotionEvent.obtain(1, 2, MotionEvent.ACTION_UP,
                    0, 0, 0));
        }
    }

    private class TouchTask extends AbstractTask {

        public TouchTask(Context ctx, int executeAfterSecs) {
            super(ctx, executeAfterSecs);
        }

        @Override
        protected void execute() {
            onBackPressed();
            setEnabled(false);
        }

    }

    private class TransactionStatusTask extends AbstractTask {

        public TransactionStatusTask(Context ctx, int executeAfterSecs) {
            super(ctx, executeAfterSecs);
        }

        @Override
        protected void execute() {
            //updatePendingTransactions();
        }

    }

    private class SummarySenderTask extends AbstractTask {

        public SummarySenderTask(Context ctx, int executeAfterSecs) {
            super(ctx, executeAfterSecs);
        }

        @Override
        protected void execute() {
            Cursor cursor = SharedSingleton.getInstance()
                    .getDB(Distributor.this).getPastDayCommission();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String subDistributorPhone = cursor.getString(cursor
                            .getColumnIndex("sub_distributor_phone"));
                    String forDate = cursor.getString(cursor
                            .getColumnIndex("for_date"));
                    int transactionCount = cursor.getInt(cursor
                            .getColumnIndex("trx_count"));
                    float commissionSum = cursor.getFloat(cursor
                            .getColumnIndex("commission_sum"));

                    String smsSummaryMessage = "..:: COMMISSION SUMMARY ::..";
                    smsSummaryMessage += "\n Summary for Date: " + forDate;
                    smsSummaryMessage += "\n Transaction Count: "
                            + transactionCount;
                    smsSummaryMessage += "\n Commission Earned: "
                            + commissionSum;
                    try {
                        android.telephony.SmsManager.getDefault()
                                .sendTextMessage(subDistributorPhone, null,
                                        smsSummaryMessage, null, null);
                    } catch (Exception e) {
                    }

                } while (cursor.moveToNext());

                SharedSingleton.getInstance().getDB(Distributor.this)
                        .setSummarySent();
            }

            cursor.close();

        }

    }

    private static class TransactionPostTask extends AbstractTask {

        private ArrayList<TransactionRecord> records = null;
        private String distCellNo;

        public TransactionPostTask(Context ctx, int executeAfterSecs) {
            super(ctx, executeAfterSecs);

        }

        @Override
        protected void execute() {
            distCellNo = SharedSingleton
                    .getInstance()
                    .getDB(this.ctx)
                    .getGeneralSetting(
                            DistributorDB.SETTING_NUMERO_DE_TELEPHONE);

            String url = K.DISTRIBUTOR_SERVICE_URL + "saveTransaction";

            if (distCellNo != null && !distCellNo.equals("")
                    && !distCellNo.equals("123")) {
                records = getFinalizedTransactions();
            }

            if (records != null) {
                ((Distributor) ctx).showToastAsync("Posting Records: "
                        + records.size());
                for (int i = 0; i < records.size(); i++) {

                    if (records != null && records.size() > 0) {
                        Gson gson = new Gson();
                        TransactionRecord record = records.get(i);
                        if (record != null) {
                            ArrayList<TransactionRecord> transactionRecords = new ArrayList<TransactionRecord>();
                            transactionRecords.add(record);
                            String recordsStr = gson.toJson(transactionRecords);

                            Map<String, String> params = new HashMap<>();
                            params.put("dist_cell_no", distCellNo);
                            params.put("finalized_trx", recordsStr);
                            params.put("sdMobileNumber", record.getSdNumber());

                            String currentBalance = SharedSingleton
                                    .getInstance()
                                    .getDB(ctx)
                                    .getSubDistributorBalance(
                                            record.getSdNumber());

                            params.put("currentBalance", currentBalance);

                            WebTask webTask = new WebTask(url, params,
                                    new WebTaskDelegate() {

                                        @Override
                                        public void asynchronousPostExecute(String sr) {
                                            super.asynchronousPostExecute(sr);

                                            //											for (TransactionRecord record : records) {
                                            //												SharedSingleton
                                            //														.getInstance()
                                            //														.getDB(ctx)
                                            //														.updateTrxSyncStatus(record.getId());
                                            //											}
                                        }

                                    });
                            webTask.execute();
                        }
                    }
                }
            }


        }

        private ArrayList<TransactionRecord> getFinalizedTransactions() {
            Cursor cursor = SharedSingleton.getInstance().getDB(this.ctx)
                    .getPendingPostOnServerTransactions();
            ArrayList<TransactionRecord> recordList = new ArrayList<TransactionRecord>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String _id = cursor.getString(cursor
                            .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_ID));
                    String transactionId = cursor
                            .getString(cursor
                                    .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_NOTES));
                    if (transactionId.equals("")) {
                        transactionId = "N/A";
                    }
                    String amount = cursor
                            .getString(cursor
                                    .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_AMOUNT));
                    String status = cursor
                            .getString(cursor
                                    .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_STATUS));
                    String clientNumber = cursor
                            .getString(cursor
                                    .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TARGET_NUMBER));
                    String sdNumber = cursor
                            .getString(cursor
                                    .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_SDNUMBER));
                    String sdName = cursor
                            .getString(cursor
                                    .getColumnIndex(DistributorDB.COLUMN_SUBD_SDNAME));
                    String transactionType = cursor
                            .getString(cursor
                                    .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TRXTYPE));

                    TransactionRecord record = new TransactionRecord(_id,
                            transactionId, amount, status, clientNumber,
                            sdNumber, sdName, transactionType);
                    recordList.add(record);

                } while (cursor.moveToNext());
            }

            return recordList;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.transfert_argent, menu);

        return true;
    }

    public void updatePendingTransactions() {
        Cursor cursor = SharedSingleton.getInstance().getDB(Distributor.this)
                .getTransactionByStatus(TRX_STATUS.PENDING.toString());

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    String dateOfTransaction = cursor
                            .getString((cursor
                                    .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_DATE_TIME)));
                    String id = cursor
                            .getString((cursor.getColumnIndex("_id")));

                    SimpleDateFormat formatter = new SimpleDateFormat(
                            K.DATEFORMAT);
                    Date convertedDate = (Date) formatter
                            .parse(dateOfTransaction);
                    long now = Calendar.getInstance().getTimeInMillis();
                    long then = convertedDate.getTime();

                    long diff = (now - then) / 1000;

                    if (diff >= 90) {
//						SharedSingleton.getInstance().getDB(Distributor.this)
//								.updateTrxStatus(id, TRX_STATUS.ECHEC);
//
//						String SDnumber = cursor
//								.getString((cursor
//										.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_SDNUMBER)));
//						String SDName = cursor
//								.getString((cursor
//										.getColumnIndex(DistributorDB.COLUMN_SUBD_SDNAME)));
//
//						String amount = cursor
//								.getString((cursor
//										.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_AMOUNT)));
//						String trxType = cursor
//								.getString((cursor
//										.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TRXTYPE)));
//						TRX_TYPE type = TRX_TYPE.valueOf(trxType);
//
//						postTransaction(new TransactionRecord(
//								id,
//								"N/A",
//								amount,
//								TRX_STATUS.ECHEC.toString(),
//								cursor.getString((cursor
//										.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TARGET_NUMBER))),
//								SDnumber, SDName, trxType));
//
//						SharedSingleton
//								.getInstance()
//								.getDB(Distributor.this)
//								.updateSubDistributorBalance(
//										SDnumber,
//										Long.parseLong(amount),
//										(type == TRX_TYPE.CASHIN) ? TRX_TYPE.CASHOUT
//												: TRX_TYPE.CASHIN);

                        // Give the balance back
                    }
                    System.out.println(diff);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }
        // long updatedRecords = SharedSingleton.getInstance()
        // .getDB(Distributor.this).updatePendingTrxStatus();
        //
        // Log.e(K.LOGTAG, "" + updatedRecords);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnNextRec) {
            displayCurrentCursorValues(true);
        } else if (v.getId() == R.id.btnPrevRec) {
            displayPreviousCursorValues();
        } else if (v.getId() == R.id.btnDecline) {
            SharedSingleton.getInstance().getDB(this)
                    .updateSMSStatus(mSMSDetail, TRX_STATUS.DECLINED);
            closeAndReopenCursor(false);
        } else if (v.getId() == R.id.btnAccept) {

            if (mTVSMSType.getText().toString().trim().length() > 0) {

//
                acceptTransaction(mSMSDetail);// if accept button clicked. send
                // the USSD command
            }
        }
    }

    private void acceptTransaction(final SMSDetail _smsDetail) {

        // TODO: Write logic
        // TODO: check for dealer's balance, if sufficient execute
        // transaction otherwise show message & return

        // s'pose we always have enough balance ;)
        TRX_TYPE tType = TRX_TYPE.valueOf(_smsDetail.smsTrxType.trim());

        // String strUSSDTemp = ((tType ==
        // TRX_TYPE.CASHIN)?K.FORMAT_USSD_CASH_IN:K.FORMAT_USSD_CASH_OUT);
        //
        // if(tType == TRX_TYPE.CASHIN){
        // strUSSDTemp = String.format(strUSSDTemp,
        // mSMSDetail.smsAmount, mSMSDetail.smsTargetPhoneNumber,
        // SharedSingleton.getInstance().getDB(this).getGeneralSetting(DistributorDB.SETTING_CODE_SECRET));
        // }else{
        // strUSSDTemp = String.format(strUSSDTemp,
        // mSMSDetail.smsTargetPhoneNumber, mSMSDetail.smsAmount,
        // SharedSingleton.getInstance().getDB(this).getGeneralSetting(DistributorDB.SETTING_CODE_SECRET));
        // }
        // mSMSDetail.smsSDPhoneNumber

        final String strFinalUSSD = getUSSDString(
                tType,
                _smsDetail.smsTargetPhoneNumber,
                SharedSingleton.getInstance().getDB(this)
                        .getGeneralSetting(DistributorDB.SETTING_CODE_SECRET),
                _smsDetail.smsAmount); // strUSSDTemp;

        // final SMSDetail _smsDetail = new SMSDetail(mSMSDetail);

        showToastAsync(strFinalUSSD);
        Log.d(K.LOGTAG, strFinalUSSD);

        Executor eS = Executors.newSingleThreadExecutor();
        eS.execute(new Runnable() {
            @Override
            public void run() {
                toggleUI(false);
                SharedSingleton
                        .getInstance()
                        .getDB(Distributor.this)
                        .updateGeneralSetting(
                                DistributorDB.SETTING_DIALED_USSD, "1");
                long trxID = SharedSingleton
                        .getInstance()
                        .getDB(Distributor.this)
                        .putTrx(new Date().getTime(),
                                _smsDetail.smsTargetPhoneNumber,
                                _smsDetail.smsAmount, TRX_STATUS.PENDING,
                                _smsDetail.smsSDID,
                                _smsDetail.smsSDPhoneNumber,
                                _smsDetail.smsTrxType, "");

                Cursor trxCursor = SharedSingleton.getInstance()
                        .getDB(Distributor.this).getTransactionByID("" + trxID);

                if (trxCursor != null && trxCursor.moveToFirst()) {
                    String id = "" + trxID;

                    String SDnumber = trxCursor.getString((trxCursor
                            .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_SDNUMBER)));
                    String SDName = trxCursor.getString((trxCursor
                            .getColumnIndex(DistributorDB.COLUMN_SUBD_SDNAME)));
                    String amount = trxCursor.getString((trxCursor
                            .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_AMOUNT)));
                    String trxType = trxCursor.getString((trxCursor
                            .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TRXTYPE)));
                    String currentBalance = SharedSingleton
                            .getInstance()
                            .getDB(Distributor.this)
                            .getSubDistributorBalance(
                                    _smsDetail.smsSDPhoneNumber);

                    postTransaction(new TransactionRecord(
                            id,
                            "N/A",
                            amount,
                            TRX_STATUS.PENDING.toString(),
                            trxCursor.getString((trxCursor
                                    .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TARGET_NUMBER))),
                            SDnumber, SDName, trxType), currentBalance, SDnumber);
                }

                trxCursor.close();


                //Toast.makeText(getApplicationContext(),"USSD : "+strFinalUSSD,Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicatiosianContext(),"USSD : "+strFinalUSSD,Toast.LENGTH_LONG).show();

                if (Build.VERSION.SDK_INT >= 23) {

                    dial(Uri.encode(strFinalUSSD));
                    //dial(Uri.encode(strFinalUSSD));
                    Toast.makeText(getApplicationContext(), "USSD : " + strFinalUSSD, Toast.LENGTH_LONG).show();
                }

                dialUSSD(Uri.encode(strFinalUSSD));


                SharedSingleton
                        .getInstance()
                        .getDB(Distributor.this)
                        .updateSubDistributorBalance(
                                _smsDetail.smsSDPhoneNumber,
                                _smsDetail.smsAmount,
                                TRX_TYPE.valueOf(_smsDetail.smsTrxType.trim()));
            }
        });
        // target phone, amount, secret

    }


    @SuppressLint("MissingPermission")
    public void dial(String ussd) {

        try {
            Intent i = new Intent(Intent.ACTION_CALL);
            i.setData(Uri.parse("tel:" + ussd));
            startActivity(i);
            Log.v("String USSD 2 :", "" + ussd);
//            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Ussd));
//            startActivity(intent);
        } catch (Exception e) {

            Log.v("Solve this error :", "" + e);
            Log.v("Error USSD :", "" + ussd);

        }
    }


    public int dialUSSD(String strUSSD) {
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;

        int nResp = 0;
        try {
            runtime.exec(String.format(K.FMT_DIALLER, strUSSD));

        } catch (Exception exc) {
            Log.e(K.LOGTAG, exc.getMessage());
            exc.printStackTrace();
        }
        return nResp;
    }

    private void closeAndReopenCursor(boolean checkForUnattendedMode) {

        if (mCursorPendSMS != null)
            if (!mCursorPendSMS.isClosed())
                mCursorPendSMS.close();

        mCursorPendSMS = SharedSingleton.getInstance().getDB(this)
                .getPendingSMS();
        if (mCursorPendSMS != null) {
            if (!mCursorPendSMS.isClosed())
                mCursorPendSMS.moveToFirst();
            displayCurrentCursorValues(true);

            unattendedModeOn = SharedSingleton.getInstance()
                    .getDB(Distributor.this)
                    .getAsBoolean(DistributorDB.SETTING_UNATTENDED_MODE);

            if (checkForUnattendedMode && unattendedModeOn) {
                String amountThresholdStr = SharedSingleton
                        .getInstance()
                        .getDB(Distributor.this)
                        .getGeneralSetting(
                                DistributorDB.SETTING_AMOUNT_THRESHOLD_FOR_UNATTENDED_MODE);

                if (amountThresholdStr != null
                        && !amountThresholdStr.equals("")) {
                    long amountThreshold = Long.parseLong(amountThresholdStr);

                    Cursor receivedSMSCursor = SharedSingleton.getInstance()
                            .getDB(this).getPendingSMS();
                    if (receivedSMSCursor.moveToLast()) {
                        SMSDetail smsDetail = new SMSDetail();
                        smsDetail.populateFromCursor(receivedSMSCursor);

                        if (smsDetail.smsAmount <= amountThreshold) {
                            long balance = -1;
                            try {
                                balance = Long
                                        .parseLong(smsDetail.subDistributorBalance);
                            } catch (Exception e) {
                            }

                            if (TRX_TYPE.CASHIN == TRX_TYPE
                                    .valueOf(smsDetail.smsTrxType)
                                    && smsDetail.smsAmount > balance) {
                                return;
                            }

                            mCursorPendSMS.moveToLast();
                            displayCurrentCursorValues(true);

                            // acceptTransaction(smsDetail);
                        }
                    }
                }
            }

        }
    }

    public void toggleUI(final boolean bEnable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mbtnAccept.setEnabled(bEnable);
                mbtnDecline.setEnabled(bEnable);
                mbtnNext.setEnabled(bEnable);
                mbtnPrev.setEnabled(bEnable);
            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();
        unattendedModeOn = SharedSingleton.getInstance()
                .getDB(Distributor.this)
                .getAsBoolean(DistributorDB.SETTING_UNATTENDED_MODE);

        if (unattendedModeOn) {
            mTVUnattendedMode.setText("ON");
            mTVUnattendedMode.setTextColor(Color.GREEN);
        } else {
            mTVUnattendedMode.setText("OFF");
            mTVUnattendedMode.setTextColor(Color.RED);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (SharedSingleton.getInstance().getDB(this)
                    .getAsBoolean(DistributorDB.SETTING_DIALED_USSD)) {
                Log.d(K.LOGTAG, mSMSDetail.smsDateTime);

                SharedSingleton
                        .getInstance()
                        .getDB(this)
                        .updateGeneralSetting(
                                DistributorDB.SETTING_DIALED_USSD, "0");

                SharedSingleton.getInstance().getDB(this)
                        .updateSMSStatus(mSMSDetail, TRX_STATUS.PROCESSED);
                closeAndReopenCursor(false);
            }
            if (!mRecRegistered) {
                registerReceiver(mUIUpdateRecv, new IntentFilter(
                        K.INT_NEWSMSTRX));
                mRecRegistered = true;
            }

            mCursorPendSMS = SharedSingleton.getInstance().getDB(this)
                    .getPendingSMS();
            if (mCursorPendSMS != null)
                mCursorPendSMS.moveToFirst();
            displayCurrentCursorValues(true);
        } catch (Exception excp) {
            Log.e(K.LOGTAG, excp.toString());
        }

        toggleUI(true);
    }

    private void displayCurrentCursorValues(boolean bMoveToNext) {
        if (mCursorPendSMS != null) {
            if (!mCursorPendSMS.isClosed()) {
                if (mCursorPendSMS.getCount() <= 0) {
                    mTVSMSType.setText("");
                    mTVSMSAmount.setText("");
                    // TODO: Handle sub-distributor name.
                    // TODO: Handle SD Balance.
                    mTVSubDistributorName.setText("");
                    mTVAmtBalance.setText("0");

                    mTVSDPhone.setText("");
                    mTVTargetNumber.setText("");
                    return;
                }

                if (mCursorPendSMS.isBeforeFirst()
                        || mCursorPendSMS.isAfterLast()) {
                    mCursorPendSMS.moveToFirst();
                }

                mSMSDetail.populateFromCursor(mCursorPendSMS);

                mTVSMSType.setText(mSMSDetail.smsTrxType);
                mTVSMSType.setTextColor((mSMSDetail.smsTrxType
                        .equals(TRX_TYPE.CASHIN.toString()) ? Color.GREEN
                        : Color.RED));
                mTVSMSAmount.setText(mSMSDetail.smsAmount + "");
                mTVTargetNumber.setText(mSMSDetail.smsTargetPhoneNumber);
                mTVSDPhone.setText(mSMSDetail.smsSDPhoneNumber);

                mTVSubDistributorName.setText(mSMSDetail.subDistributorName);
                mTVAmtBalance.setText(mSMSDetail.subDistributorBalance);

                if (bMoveToNext)
                    mCursorPendSMS.moveToNext();
                else
                    mCursorPendSMS.moveToPrevious();
            }
        }
    }

    private void displayPreviousCursorValues() {
        try {
            if (mCursorPendSMS != null) {
                if (!mCursorPendSMS.isClosed()) {
                    if (mCursorPendSMS.getCount() > 0) {
                        if (mCursorPendSMS.isBeforeFirst()
                                || mCursorPendSMS.isAfterLast()) {
                            mCursorPendSMS.moveToLast();
                        }
                        displayCurrentCursorValues(false);

                        // mTVSMSType.setText(mCursorPendSMS.getString(mCursorPendSMS.getColumnIndex(DistributorDB.COLUMN_SMS_TRX_TYPE)));
                        // mTVSMSAmount.setText(mCursorPendSMS.getString(mCursorPendSMS.getColumnIndex(DistributorDB.COLUMN_SMS_AMOUNT)));
                        // TODO: Handle sub-distributor name.
                        // TODO: Handle SD Balance.
                        // mTVSDPhone.setText(mCursorPendSMS.getString(mCursorPendSMS.getColumnIndex(DistributorDB.COLUMN_SMS_SDPHONENUMBER)));
                        // mTVTargetNumber.setText(mCursorPendSMS.getString(mCursorPendSMS.getColumnIndex(DistributorDB.COLUMN_SMS_TARGET_PHONE)));
                        // mCursorPendSMS.moveToPrevious();
                    }
                }
            }
        } catch (Exception exc) {
            Log.e(K.LOGTAG, exc.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_settings) {
            Intent intConfig = new Intent(this, ConfigurationActivity.class);
            startActivity(intConfig);
        } else if (item.getItemId() == R.id.itmJournal) {
            Intent intHist = new Intent(this, GroupedJournal.class);
            startActivity(intHist);
        } else if (item.getItemId() == R.id.itmSubDistributor) {
            Intent intSubD = new Intent(this, SubDistributor.class);
            startActivity(intSubD);
        } else if (item.getItemId() == R.id.itmHistory) {
            Intent intHist = new Intent(this, History.class);
            startActivity(intHist);
        } else if (item.getItemId() == R.id.itmExport) {
            Intent intexport = new Intent(this, Export_Activity.class);
            startActivity(intexport);
        } else if (item.getItemId() == R.id.itmAboutUs) {
            Intent intAboutUs = new Intent(this, AboutActivity.class);
            startActivity(intAboutUs);
        } else if (item.getItemId() == R.id.itemSetCodesInFiles) {
            Intent intAboutUs = new Intent(this, CodeSetupActivity.class);
            startActivity(intAboutUs);
        }

        return super.onOptionsItemSelected(item);
    }

    public void showToastAsync(final String strText) {
        if (strText == null)
            return;
        if (strText.trim().length() <= 0)
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Distributor.this, strText, Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#finish()
     */
    @Override
    public void finish() {
        try {
            if (mCursorPendSMS != null) {

                mCursorPendSMS.close();
                mCursorPendSMS = null;
            }
        } catch (Exception e) {
            Log.e(K.LOGTAG, e.getMessage());
        }
        super.finish();
    }

    private class UpdateUIReciver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {

            if (intent.hasExtra("objtrx")) {
                Log.d(K.LOGTAG, "Response received for transaction.");
                String commissionThresholdStr = SharedSingleton
                        .getInstance()
                        .getDB(Distributor.this)
                        .getGeneralSetting(
                                DistributorDB.SETTING_COMMISSION_THRESHOLD);

                long commissionThreshold = Long
                        .parseLong(commissionThresholdStr);

                if (commissionThreshold > 0) {
                    Cursor commissionTallyCursor = SharedSingleton
                            .getInstance().getDB(Distributor.this)
                            .getTotalCommissionTally();
                    if (commissionTallyCursor != null
                            && commissionTallyCursor.moveToFirst()) {
                        long tally = commissionTallyCursor
                                .getLong(commissionTallyCursor
                                        .getColumnIndex("commission_tally"));
                        if (tally >= commissionThreshold) {

                            final String strFinalUSSD = getUSSDString(
                                    TRX_TYPE.CASHIN,
                                    SharedSingleton
                                            .getInstance()
                                            .getDB(Distributor.this)
                                            .getGeneralSetting(
                                                    DistributorDB.SETTING_NUMERO_DE_TELEPHONE),
                                    SharedSingleton
                                            .getInstance()
                                            .getDB(Distributor.this)
                                            .getGeneralSetting(
                                                    DistributorDB.SETTING_CODE_SECRET),
                                    tally); // strUSSDTemp;


                            //Toast.makeText(getApplicationContext(),"USSD : "+strFinalUSSD,Toast.LENGTH_LONG).show();

                            if (Build.VERSION.SDK_INT >= 23) {
                                dial(Uri.encode(strFinalUSSD));
                                Toast.makeText(getApplicationContext(), "USSD : " + strFinalUSSD, Toast.LENGTH_LONG).show();
                            }


                            dialUSSD(Uri.encode(strFinalUSSD));


                            SharedSingleton.getInstance()
                                    .getDB(Distributor.this).setUSSDDialed();
                        }
                    }
                }
            } else {
                closeAndReopenCursor(true);
            }
            // if(mCursorPendSMS != null){

            // if(!mCursorPendSMS.isClosed()){
            // mCursorPendSMS.close();
            // mCursorPendSMS =
            // SharedSingleton.getInstance().getDB(Distributor.this).getPendingSMS();
            // if(mCursorPendSMS != null) mCursorPendSMS.moveToFirst();
            // }
            // }
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mRecRegistered) {
                unregisterReceiver(mUIUpdateRecv);
                mRecRegistered = false;
            }
        } catch (Exception e) {
            Log.e(K.LOGTAG, e.getMessage());
        }

        try {
            if (mCursorPendSMS != null) {
                mCursorPendSMS.close();
                mCursorPendSMS = null;
            }
        } catch (Exception e2) {
            Log.e(K.LOGTAG, e2.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        outState.putSerializable("LSP", mSMSDetail);
        super.onSaveInstanceState(outState);
    }

    private String getUSSDString(TRX_TYPE _tType, String _strPhone,
                                 String _strSecret, long _strAmount) {

        String strRet = "";
        if (_tType == TRX_TYPE.CASHIN) {
            strRet = K.FORMAT_USSD_CASH_IN;
        } else if (_tType == TRX_TYPE.CASHOUT) {
            strRet = K.FORMAT_USSD_CASH_OUT;
        } else {
            strRet = K.FORMAT_USSD_CREDIT;
        }
//        String strRet = (_tType == TRX_TYPE.CASHIN) ? K.FORMAT_USSD_CASH_IN
//                : K.FORMAT_USSD_CASH_OUT;

        strRet = strRet.replace(K.PHONE, _strPhone);
        strRet = strRet.replace(K.SEC, _strSecret);
        strRet = strRet.replace(K.AMT, _strAmount + "");

        return strRet;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            summaryTimer.cancel();
        } catch (Exception exception) {

        }
    }

    private void postTransaction(TransactionRecord record, String CurrentBal, String Number) {

        String distCellNo = SharedSingleton.getInstance().getDB(this)
                .getGeneralSetting(DistributorDB.SETTING_NUMERO_DE_TELEPHONE);

        final ArrayList<TransactionRecord> transactionRecords = new ArrayList<TransactionRecord>();
        transactionRecords.add(record);

        if (distCellNo != null && !distCellNo.equals("")
                && !distCellNo.equals("123")) {

            Gson gson = new Gson();
            String recordsStr = gson.toJson(transactionRecords);

            String url = K.DISTRIBUTOR_SERVICE_URL + "saveTransaction";

            Map<String, String> params = new HashMap<>();
            params.put("dist_cell_no", distCellNo);
            params.put("finalized_trx", recordsStr);
            params.put("sdMobileNumber", Number);
            params.put("currentBalance", CurrentBal);

            Log.d(K.LOGTAG, "before webTask: distributor");

            WebTask webTask = new WebTask(url, params, new WebTaskDelegate() {

                @Override
                public void asynchronousPostExecute(String sr) {
                    super.asynchronousPostExecute(sr);

                    for (TransactionRecord record : transactionRecords) {
                        SharedSingleton
                                .getInstance()
                                .getDB(Distributor.this)
                                .updateTrxStatus(record.getId(),
                                        TRX_STATUS.valueOf(record.getStatus()));
                    }
                }

            });
            webTask.execute();
        }

    }

    private boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        ComponentName expectedComponentName = new ComponentName(context, accessibilityService);

        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }

        return false;
    }

}
