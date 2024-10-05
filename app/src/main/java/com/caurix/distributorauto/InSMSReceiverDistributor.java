package com.caurix.distributorauto;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.caurix.distributorauto.bean.TransactionRecord;
import com.caurix.distributorauto.web.WebTask;
import com.caurix.distributorauto.web.WebTaskDelegate;
import com.caurix.duplicate.helper.DuplicateMessageChecker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.internal.io.FileSystem;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class InSMSReceiverDistributor extends BroadcastReceiver {

    private Context ctx = null;
    public String ussd = "";
    boolean isDuplicateSMS = true;

    public static String Data1;
    public static String Data2;
    public static String Data3;
    public static String Data4;
    public static String Data5;
    public static String Data6;

    private static Set<String> keywords = new HashSet<String>();
    private String retrievedCode;
    private List<String> codes;

    @Override
    public void onReceive(final Context context, Intent intent) {

        ctx = context;
        final DuplicateMessageChecker smsChecker = new DuplicateMessageChecker(ctx);
        initKeywords();

        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        StringBuilder sBuilder = new StringBuilder("");
        String msg_from = "";
        Log.d(K.LOGTAG, "SMS Received");

        getCodesFromFile();


        if (bundle != null) {

            Log.d(K.LOGTAG, "Bundle not null");

            try {
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    msg_from = msgs[i].getOriginatingAddress();
                    sBuilder.append(msgs[i].getMessageBody());
                    Log.d(K.LOGTAG, "getting messages");

                }

                // Sms Sent time in millis
                final String time = "" + msgs[0].getTimestampMillis();

                Log.e(K.LOGTAG, "time ->" + time);
                final String strMsg = sBuilder.toString();
                final String strMsgLower = strMsg.toLowerCase();
                final String sendersNumber = msg_from;
                {
                    ExecutorService exec = Executors.newSingleThreadExecutor();
                    exec.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String[] strAryIncomes = new String[]{""};
                                String[] sdarray = strMsg.split("\\*");

                                strAryIncomes = strMsg.split("\n");
                                DistributorDB tdb = SharedSingleton.getInstance().getDB(ctx);
                                if (strMsg != null && sdarray != null && sdarray.length == 4 && sdarray[0].toUpperCase().equals("SDISTRIBUTEUR")) {
                                    Log.d(K.LOGTAG, "inside sdist");
                                    String strSDIDNew = SharedSingleton.getInstance().getSDIDNew();

                                    long chiffre = tdb.putSubDistributor(strSDIDNew, new SimpleDateFormat(K.DATEFORMAT).format(new Date()), "adresse par defaut", "", "telephone de contact", "", "email par defaut", "", sdarray[2], Long.parseLong(sdarray[3].trim()), 0, 0, "", 1, sdarray[1].trim(), "Nom agent par défaut");
                                    Log.d(K.LOGTAG, "after db push sdist");
                                    if (chiffre > 0) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, "Un sous distributeur a été ajouté", Toast.LENGTH_LONG)
                                                        .show();
                                            }
                                        });
                                    }

                                } else if (strMsg != null && sdarray != null && sdarray.length == 4 && sdarray[0].toUpperCase().equals("MODIFY")) {


                                    String metTrxPhone = sdarray[1], metSDName = sdarray[2], metCurrentBalance = sdarray[3];


                                    Cursor cursor = tdb.getSubDistributorByPhone(metTrxPhone);

                                    if (cursor != null && cursor.moveToFirst()) {
                                        String metAddress1 = (cursor.getString(cursor
                                                .getColumnIndex(DistributorDB.COLUMN_SUBD_ADDRESS1)));
                                        // metAddress2.setText(cursor.getString(cursor
                                        // .getColumnIndex(DistributorDB.COLUMN_SUBD_ADDRESS2)));
                                        String metPhone1 = (cursor.getString(cursor
                                                .getColumnIndex(DistributorDB.COLUMN_SUBD_CONTACTPHONE1)));
                                        // metPhone2.setText(cursor.getString(cursor
                                        // .getColumnIndex(DistributorDB.COLUMN_SUBD_CONTACTPHONE2)));
                                        String metEmail1 = (cursor.getString(cursor
                                                .getColumnIndex(DistributorDB.COLUMN_SUBD_EMAIL1)));
                                        // metEmail2.setText(cursor.getString(cursor
                                        // .getColumnIndex(DistributorDB.COLUMN_SUBD_EMAIL2)));

                                        String metCashInPercent =
                                                (cursor.getString(cursor
                                                        .getColumnIndex(DistributorDB.COLUMN_SUBD_COMMPERCENTCASHIN)));
                                        String metCashOutPercent
                                                = (cursor.getString(cursor
                                                .getColumnIndex(DistributorDB.COLUMN_SUBD_COMMPERCENTCASHOUT)));
                                        // metSecret.setText(cursor.getString(cursor
                                        // .getColumnIndex(DistributorDB.COLUMN_SUBD_CODE_SCRET)));

                                        String metContactPerson = (cursor.getString(cursor
                                                .getColumnIndex(DistributorDB.COLUMN_SUBD_CONTACTPERSON)));


                                        if (metPhone1.length() <= 0
                                                || metTrxPhone.length() <= 0
                                                || metCurrentBalance.length() <= 0
                                                || metCashInPercent.length() <= 0
                                                || metCashOutPercent.length() <= 0
                                                // || metSecret.getText().toString().length() <= 0
                                                || metSDName.length() <= 0) {
                                            return;
                                        }
                                        double dCashIn = 0.0;
                                        double dCashOut = 0.0;
                                        long currentBalance = 0;

                                        try {
                                            currentBalance = Long.parseLong(metCurrentBalance.trim());
                                        } catch (Exception e) {
                                            currentBalance = 0;
                                        }

                                        try {
                                            dCashIn = Double.parseDouble(metCashInPercent
                                                    .trim());
                                            // if (dCashIn > 10) {
                                            // dCashIn = 10;
                                            // }
                                        } catch (Exception e) {
                                            dCashIn = 0.0;
                                        }

                                        try {
                                            dCashOut = Double.parseDouble(metCashOutPercent.trim());
                                            // if (dCashOut > 10) {
                                            // dCashOut = 10;
                                            // }
                                        } catch (Exception e) {
                                            dCashOut = 0.0;
                                        }


                                        // java.util.UUID.randomUUID().toString()


                                        int updatedRows = tdb.updateSubDistributor(metTrxPhone,
                                                metTrxPhone, metAddress1.trim(), "", metPhone1, "", metEmail1, ""
                                                        .toString(), currentBalance, dCashIn, dCashOut,
                                                ""/* metSecret.getText().toString().trim() */, metSDName
                                                        .trim(), metContactPerson
                                                        .trim());


                                        Map<String, String> params = new HashMap<>();
                                        params.put("sd_number", metTrxPhone);
                                        params.put("sd_mobilenumber", metPhone1);
                                        params.put("sd_balance", "" + currentBalance);

                                        Log.d(K.LOGTAG, "sd_number :" + metTrxPhone+updatedRows);
                                        Log.d(K.LOGTAG, "sd_mobilenumber :" + metPhone1);
                                        Log.d(K.LOGTAG, "sd_balance :" + currentBalance);
                                        Log.d(K.LOGTAG, "Objcet: "+new Gson().toJson(sdarray));


                                        WebTask webTask = new WebTask(
                                                "http://www.caurix.net/controller/DistributorTestController.php?cmd=saveBalanceOfSubDistributor",
                                                params, new WebTaskDelegate() {

                                            @Override
                                            public void asynchronousPostExecute(String sr) {
                                                super.asynchronousPostExecute(sr);
                                                Log.d(K.LOGTAG, "IS ADD MODE: " + sr);
                                                Log.d(K.LOGTAG, "inside webtask");
                                                // for (TransactionRecord record : records) {
                                                // SharedSingleton
                                                // .getInstance()
                                                // .getDB(ctx)
                                                // .updateTrxSyncStatus(record.getId());
                                                // }
                                            }

                                        });
                                        webTask.execute();

                                    }
                                } else if ((strAryIncomes.length == 4)
                                        && (strMsg.startsWith("DIST_CONFIG"))) {// If
                                    // the
                                    // SMS was
                                    // send by
                                    // the
                                    // Caurix
                                    // app
                                    // containing
                                    // the
                                    // config
                                    // parameters.
                                    extractConfigParams(strMsg);

                                } else if (strAryIncomes.length == 1
                                        && strAryIncomes[0].split("\\*").length == 3) {
                                    String[] strAryNewFormat = new String[]{""};
                                    Log.e(K.LOGTAG, "isDuplicateSMS4 ->" + isDuplicateSMS);

                                    Log.d(K.LOGTAG, "Lngth 1 AND 3");


                                    strAryNewFormat = strAryIncomes[0]
                                            .split("\\*");

                                    Log.d(K.LOGTAG, "Lngth 1 ND 3" + strAryNewFormat);

                                    if ((strAryNewFormat.length == 3)
                                            && (strMsg
                                            .toUpperCase()
                                            .startsWith(
                                                    TRX_TYPE.CASHIN
                                                            .toString()) || strMsg
                                            .toUpperCase()
                                            .startsWith(
                                                    TRX_TYPE.CASHOUT
                                                            .toString()) || strMsg.toUpperCase()
                                            .startsWith(TRX_TYPE.CREDIT
                                                    .toString()))) {

                                        Log.d(K.LOGTAG, "IF");

                                        SMSDetail objSMSDetail = new SMSDetail();
                                        objSMSDetail.smsTrxType = strAryNewFormat[0]
                                                .toUpperCase();
                                        objSMSDetail.smsTargetPhoneNumber = strAryNewFormat[1];
                                        objSMSDetail.smsSecret = "123456";
                                        objSMSDetail.smsSDID = "NF";
                                        objSMSDetail.smsAmount = Long
                                                .parseLong(strAryNewFormat[2]);
                                        objSMSDetail.smsSDPhoneNumber = sendersNumber;// strAryIncomes[i++];
                                        objSMSDetail.smsDateTime = new SimpleDateFormat(
                                                "yyyy-MM-dd HH:mm:ss.SS")
                                                .format(new Date());
                                        objSMSDetail.smsStatus = TRX_STATUS.PENDING
                                                .toString();

                                        String amount = strAryNewFormat[2];

                                        Log.d(K.LOGTAG, "BEFORE PROCESS TRNASCARION");
//Check if the received message is duplicate, it will check in past 3 days SMS log.
                                        isDuplicateSMS = smsChecker.isDuplicateMessage(sendersNumber,
                                                objSMSDetail.smsTargetPhoneNumber, amount, objSMSDetail.smsTrxType, objSMSDetail.smsDateTime, time);
                                        Log.e(K.LOGTAG, "duplicateSMS ->" + isDuplicateSMS);

                                        if (isDuplicateSMS) {
                                            Log.e(K.LOGTAG, "isDuplicateSMS ->" + isDuplicateSMS);
                                            //Save to SMS log as duplicate data.
                                            smsChecker.saveToDataBase(sendersNumber,
                                                    objSMSDetail.smsTargetPhoneNumber, amount, objSMSDetail.smsTrxType, objSMSDetail.smsDateTime, time, "1");
                                        } else {
                                            //Save to SMS log as non-duplicate data.
                                            smsChecker.saveToDataBase(sendersNumber,
                                                    objSMSDetail.smsTargetPhoneNumber, amount, objSMSDetail.smsTrxType, objSMSDetail.smsDateTime, time, "0");
                                            processTransaction(sendersNumber,
                                                    objSMSDetail);
                                        }

                                    }
                                } else if ((strAryIncomes.length == 6 || strAryIncomes.length == 7)
                                        && (strMsg.startsWith(TRX_TYPE.CASHIN
                                        .toString()) || strMsg
                                        .startsWith(TRX_TYPE.CASHOUT
                                                .toString()) || strMsg
                                        .startsWith(TRX_TYPE.CREDIT
                                                .toString()))) {// the
                                    // SMS
                                    // was
                                    // sent
                                    // by
                                    // sub
                                    // distributor
                                    Log.d(K.LOGTAG,
                                            "Array length not suitable for processing. "
                                                    + strAryIncomes.length);
                                    int i = 0;
                                    SMSDetail objSMSDetail = new SMSDetail();
                                    objSMSDetail.smsTrxType = strAryIncomes[i++];
                                    objSMSDetail.smsTargetPhoneNumber = strAryIncomes[i++];
                                    objSMSDetail.smsSecret = strAryIncomes[i++];
                                    objSMSDetail.smsSDID = strAryIncomes[i++];
                                    objSMSDetail.smsAmount = Long
                                            .parseLong(strAryIncomes[i++]);
                                    objSMSDetail.smsSDPhoneNumber = sendersNumber;// strAryIncomes[i++];
                                    objSMSDetail.smsDateTime = new SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm:ss.SS")
                                            .format(new Date());
                                    objSMSDetail.smsStatus = TRX_STATUS.PENDING
                                            .toString();

                                    processTransaction(sendersNumber,
                                            objSMSDetail);

                                } else if (strAryIncomes[0].split("\\*").length != 3) {

                                    Log.d(K.LOGTAG, "LAST ELSE");

                                    parseUSSDResponse();
                                }
//								}
                            } catch (Exception e) {
                                Log.e(K.LOGTAG, e.toString());
                            }
                            purgeSMSInbox();
                        }

                        private void parseUSSDResponse() {
                            Log.e(K.LOGTAG, "parseUSSDResponse InsideParseUSSDResponse");
                            float totalCommission = -1;
                            float distributorComission = -1;
                            float subDistributorCommission = -1;

                            TRX_STATUS trx_status = TRX_STATUS.PENDING;

                            Pattern pattern = null;
                            Matcher matcher = null;
                            String strSubString = "", strTrxAmount = "0", strTrxFrom = "", strTrxTo = "", strTrxID = "", clientNo = "";


                            ///////////// NEW LOGIC BY HARDIK
                            boolean goAhead = false;
                            boolean txnIdFound1 = false;
//                            pattern = Pattern
//                                    .compile("\\s*\\.+\\s*ref\\s*:\\s*\\s*\\w*\\.\\w*\\.\\w*\\s*");
//                            pattern = Pattern.compile("([rc]{18})");

//                            matcher = pattern.matcher(strMsgLower);
//                            txnIdFound1 = matcher.find();

//                            pattern = Pattern.compile("([rc]{2}.{18})");
//                            pattern = Pattern.compile("([rc]{2}.{18})");
                            ArrayList<String> findIt = new ArrayList<>();
                            findIt.clear();
                            findIt.add("ci\\d{6}\\.\\d{4}\\.[a-z0-9]{6}");
                            findIt.add("ct\\d{6}\\.\\d{4}\\.[a-z0-9]{6}");
                            findIt.add("co\\d{6}\\.\\d{4}\\.[a-z0-9]{6}");
                            findIt.add("rc\\d{6}\\.\\d{4}\\.[a-z0-9]{6}");

                            for (int i = 0; i < findIt.size(); i++) {
                                pattern = Pattern.compile(findIt.get(i));
                                Log.d(K.LOGTAG, findIt.get(i));
                                Log.d(K.LOGTAG, strMsgLower);
                                matcher = pattern.matcher(strMsgLower);

                                while (matcher.find()) {
                                    txnIdFound1 = true;
                                    strSubString = matcher.group();
                                    strTrxID = matcher.group();

                                }
                                if (txnIdFound1 == true)
                                    break;
                            }
                            //////
//                            Pattern p = Pattern.compile("([CI|CO]{2}.{18})");
//                            Matcher m1 = p.matcher(strMsgLower);
//                            while (m1.find()) {
//                                System.out.println("FOUND IT =====>" + m1.group());
//                            }
                            //////


                            if (txnIdFound1) {
                                Log.d(K.LOGTAG,
                                        "TrxID found: " + strSubString);
//                                strSubString = matcher.group();
//                                pattern = Pattern
//                                        .compile("\\s*\\w*\\.\\w*\\.\\w*");
//                                matcher = pattern.matcher(strSubString);
//                                if (matcher.find()) {
//                                    strTrxID = matcher.group().trim();
//                                    Log.d(K.LOGTAG, "TrxID: " + strTrxID);
//                                }
//                                String strTrxIDOld1 = "";
//                                try {
//                                    strTrxIDOld1 = strTrxID;
//                                    if (strTrxID.length() >= 20) {
//                                        strTrxID = strTrxID.substring(0, 20);
//                                    }
//                                } catch (Exception ee) {
//                                    strTrxID = strTrxIDOld1;
//                                }

                                pattern = Pattern.compile("\\s*reussi\\s*");
                                matcher = pattern.matcher(strMsgLower);

                                if (matcher.find()) {// reussi was found
                                    Log.d(K.LOGTAG, "Transaction succesful. "
                                            + matcher.group());

//                                    pattern = Pattern
//                                            .compile("commission\\s*:\\s*\\d*(\\.\\d+)*");
//                                    matcher = pattern.matcher(strMsgLower);
//                                    if (matcher.find()) {
//                                        Log.d(K.LOGTAG, "Commission found: "
//                                                + matcher.group());
//                                        strSubString = matcher.group();
//                                        try {
//                                            String comissionStr = strSubString
//                                                    .split(":")[1];
//                                            totalCommission = Float
//                                                    .parseFloat(comissionStr.trim());
//
//                                        } catch (Exception e) {
//
//                                        }
//                                    } else {
//                                        Log.d(K.LOGTAG, "Commission not found.");
//                                        return;
//                                    }

                                    trx_status = TRX_STATUS.OK;
                                } else {
                                    Log.d(K.LOGTAG, "Check transaction failure.");

                                    trx_status = TRX_STATUS.REJET;
                                }
                                boolean transactionAmountFound = false;

                                pattern = Pattern
                                        .compile("montant\\s*transaction\\s*:\\s*\\d*(\\.\\d+)*");
                                matcher = pattern.matcher(strMsgLower);
                                transactionAmountFound = matcher.find();

                                if (!transactionAmountFound) {
                                    pattern = Pattern
                                            .compile("montant\\s*de\\s*la\\s*transaction\\s*:\\s*\\d*(\\.\\d+)*");
                                    matcher = pattern.matcher(strMsgLower);
                                    transactionAmountFound = matcher.find();
                                }

                                if (!transactionAmountFound) {
                                    pattern = Pattern
                                            .compile("reussi\\s*:\\d*\\.\\d+");
                                    matcher = pattern.matcher(strMsgLower);
                                    transactionAmountFound = matcher.find();
                                }

                                if (transactionAmountFound) {
                                    Log.d(K.LOGTAG, "Transaction Amount found: "
                                            + matcher.group());
                                    strSubString = matcher.group();
                                    try {
                                        strTrxAmount = strSubString.split(":")[1];
                                    } catch (Exception e) {
                                        strTrxAmount = "0";
                                    }
                                }
                                long lAmount = 0;
                                try {
                                    float f = Float.parseFloat(strTrxAmount);
                                    lAmount = (long) f;
                                } catch (Exception ee) {
                                    lAmount = 0;
                                }

                                Log.d(K.LOGTAG,
                                        "Will update tranaction status FROM (Credit): "
                                                + strTrxFrom + " STATUS: "
                                                + trx_status + " AMOUNT: "
                                                + lAmount);

                                Cursor cursor = SharedSingleton
                                        .getInstance().getDB(context)
                                        .getAlltransactions();
                                Log.d("CURSOR_PARSER", new Gson().toJson(cursor));
                                if (cursor != null) {
                                    if (cursor.getCount() > 0) {
                                        cursor.moveToLast();
                                        Log.d("ISSUE_TRACKER", "Before the change");
                                        Log.d(K.LOGTAG, "Credit " + cursor.getString(2) + TRX_STATUS.PENDING + trx_status + lAmount + "id" + strTrxID + "id" + cursor.getInt(cursor.getColumnIndex("_id")));
                                        clientNo = cursor.getString(2);
                                        lAmount = cursor.getString(3) == null ? lAmount : Long.parseLong(cursor.getString(3));
                                        long updatedTransactions = SharedSingleton
                                                .getInstance().getDB(context)
                                                .updateTrxByID(cursor.getString(2), TRX_STATUS.PENDING, // update
                                                        // transaction
                                                        // with
                                                        // new
                                                        // status
                                                        // only
                                                        trx_status, cursor.getString(3) == null ? lAmount : Long.parseLong(cursor.getString(3)), strTrxID, cursor.getInt(cursor.getColumnIndex("_id")));

                                        Log.d(K.LOGTAG, "Updated Transaction :" + updatedTransactions);


                                        if (updatedTransactions == 0)
                                            return;
                                    }else{
                                        Log.d(K.LOGTAG, "CURSOR IS EMPTY");
                                    }
                                }else{
                                    Log.d(K.LOGTAG, "CURSOR IS NULL");
                                }


                                TrxDetail objTrxDetail = SharedSingleton
                                        .getInstance().getDB(context)
                                        .getTrx(clientNo, trx_status, lAmount);

                                Log.d(K.LOGTAG, "Updated Transaction :" + clientNo+ " status: "+trx_status +"Amount: "+ lAmount);

                                String currentBalance = SharedSingleton
                                        .getInstance()
                                        .getDB(ctx)
                                        .getSubDistributorBalance(
                                                objTrxDetail.trxSDPhoneNumber);

                                Log.d(K.LOGTAG, "cb cd : " + currentBalance);
                                Log.d(K.LOGTAG, "number numbe : " + objTrxDetail.trxSDPhoneNumber);
                                Log.d(K.LOGTAG, "status STtus: " + objTrxDetail.trxStatus);
                                Log.d(K.LOGTAG, "Object: "+ new Gson().toJson(objTrxDetail));


                                long newBalance = Long.valueOf(currentBalance);

                                if (objTrxDetail.mTrxType.toString().trim().equalsIgnoreCase("cashout") && strMsg.contains("Retrait")) {
                                    if (trx_status == TRX_STATUS.OK) {

                                        SharedSingleton
                                                .getInstance()
                                                .getDB(ctx)
                                                .updateSubDistributorBalance(
                                                        objTrxDetail.trxSDPhoneNumber,
                                                        (long) objTrxDetail.trxAmount,
                                                        TRX_TYPE.NA);
                                    }

                                    newBalance = (long) (Long.parseLong(currentBalance) + objTrxDetail.trxAmount);
                                } else {

                                    newBalance = Long.valueOf(currentBalance);
                                }
                                Log.d(K.LOGTAG, "before goahead");
                                postTransaction(new TransactionRecord(
                                        objTrxDetail.id, strTrxID, ""
                                        + (long) objTrxDetail.trxAmount,
                                        objTrxDetail.trxStatus,
                                        objTrxDetail.trxTargetPhoneNumber,
                                        objTrxDetail.trxSDPhoneNumber,
                                        objTrxDetail.trxSDName,
                                        objTrxDetail.mTrxType.toString()), String.valueOf(newBalance), objTrxDetail.trxSDPhoneNumber);
                                float cashInRate = 0;
                                float cashOutRate = 0;

                                String sdPhone = objTrxDetail.trxSDPhoneNumber;
                                Cursor sdDetails = SharedSingleton.getInstance()
                                        .getDB(context)
                                        .getSubDistributorByPhone(sdPhone);
                                if (sdDetails != null) {
                                    if (sdDetails.moveToFirst()) {
                                        cashInRate = sdDetails.getFloat(sdDetails
                                                .getColumnIndex(DistributorDB.COLUMN_SUBD_COMMPERCENTCASHIN));
                                        cashOutRate = sdDetails.getFloat(sdDetails
                                                .getColumnIndex(DistributorDB.COLUMN_SUBD_COMMPERCENTCASHOUT));
                                    }
                                    sdDetails.close();
                                }

                                float rate = (objTrxDetail.mTrxType
                                        .compareTo(TRX_TYPE.CASHIN) == 0) ? cashInRate
                                        : cashOutRate;

                                subDistributorCommission = rate / 100 * 0.05f
                                        * totalCommission;
                                distributorComission = (1 - rate / 100) * 0.05f
                                        * totalCommission;

                                SharedSingleton
                                        .getInstance()
                                        .getDB(context)
                                        .putCommission(
                                                objTrxDetail.trxSDPhoneNumber,
                                                distributorComission,
                                                subDistributorCommission,
                                                totalCommission);

                                if (trx_status == TRX_STATUS.ECHEC) {
                                    android.telephony.SmsManager
                                            .getDefault()
                                            .sendTextMessage(
                                                    objTrxDetail.trxSDPhoneNumber,
                                                    null,
                                                    objTrxDetail
                                                            .genSubDistributorSMS(),
                                                    null, null);
                                } else if (trx_status == TRX_STATUS.PROCESSED) {
                                    android.telephony.SmsManager
                                            .getDefault()
                                            .sendTextMessage(
                                                    objTrxDetail.trxSDPhoneNumber,
                                                    null,
                                                    objTrxDetail
                                                            .genSubDistributorSMS(),
                                                    null, null);

                                    // TODO: Add commission calculation logic,
                                    // right now it is 1 in any case
                                    SharedSingleton
                                            .getInstance()
                                            .getDB(context)
                                            .addPendingCommission(
                                                    objTrxDetail.trxSDID, 5);
                                    double dPendingComm = SharedSingleton
                                            .getInstance()
                                            .getDB(context)
                                            .getPendingCommission(
                                                    objTrxDetail.trxSDID);

                                    if (dPendingComm >= 10) {
                                        Intent intDialComm = new Intent(context,
                                                DialCommissionUSSD.class);
                                        intDialComm.putExtra(K.KEY_COMMISSION_AMT,
                                                "10");
                                        intDialComm.putExtra(K.KEY_SDNUMBER,
                                                objTrxDetail.trxSDPhoneNumber);
                                        intDialComm
                                                .putExtra(
                                                        K.KEY_SDSECRET,
                                                        SharedSingleton
                                                                .getInstance()
                                                                .getDB(context)
                                                                .getSDSecret(
                                                                        objTrxDetail.trxSDID));
                                        context.startService(intDialComm);
                                        SharedSingleton
                                                .getInstance()
                                                .getDB(context)
                                                .lessPendingCommission(
                                                        objTrxDetail.trxSDID, 10);
                                    }
                                } else if (trx_status == TRX_STATUS.OK) {
                                    android.telephony.SmsManager
                                            .getDefault()
                                            .sendTextMessage(
                                                    objTrxDetail.trxSDPhoneNumber,
                                                    null,
                                                    objTrxDetail
                                                            .getContentForOKSMS(),
                                                    null, null);
                                }

                                Intent iUpdateUI = new Intent(K.INT_NEWSMSTRX);
                                iUpdateUI.putExtra("objtrx", objTrxDetail);
                                context.sendBroadcast(iUpdateUI);
                            } else {
                                goAhead = true;
                            }

                            ///////////// END OF NEW LOGIC
                            if (goAhead) {
                                boolean toNumberFound = false;
                                pattern = Pattern.compile("vers\\s*\\d+");// CASHIN
                                matcher = pattern.matcher(strMsgLower);
                                toNumberFound = matcher.find();

                                if (!toNumberFound) {
                                    pattern = Pattern.compile("du\\s*\\d+");
                                    matcher = pattern.matcher(strMsgLower);
                                    toNumberFound = matcher.find();
                                }

                                if (toNumberFound) {
                                    Log.d(K.LOGTAG,
                                            "To number found: " + matcher.group());
                                    strSubString = matcher.group();
                                    clientNo = strSubString.trim().split(" ")[1];
                                } else {
                                    Log.d(K.LOGTAG, "To number not found.");
//                                COMMENTED THIS BY HARDIK AFTER DISCUSSION WITH HISHAM ON CALL. HISHAM REALISE THAT OPERATOR REMOVE THE WORD VERS
                                    //   return;
                                }

                                boolean transactionAmountFound = false;
                                pattern = Pattern
                                        .compile("montant\\s*transaction\\s*:\\s*\\d*(\\.\\d+)*");
                                matcher = pattern.matcher(strMsgLower);
                                transactionAmountFound = matcher.find();

                                if (!transactionAmountFound) {
                                    pattern = Pattern
                                            .compile("montant\\s*de\\s*la\\s*transaction\\s*:\\s*\\d*(\\.\\d+)*");
                                    matcher = pattern.matcher(strMsgLower);
                                    transactionAmountFound = matcher.find();
                                }

                                if (!transactionAmountFound) {
                                    pattern = Pattern
                                            .compile("reussi\\s*:\\s*\\d*(\\.\\d+)*");
                                    matcher = pattern.matcher(strMsgLower);
                                    transactionAmountFound = matcher.find();
                                }

                                if (transactionAmountFound) {
                                    Log.d(K.LOGTAG, "Transaction Amount found: "
                                            + matcher.group());
                                    strSubString = matcher.group();
                                    try {
                                        strTrxAmount = strSubString.split(":")[1];
                                    } catch (Exception e) {
                                        strTrxAmount = "0";
                                    }
                                } else {
                                    Log.d(K.LOGTAG, "Transaction Amount not found.");
                                    return;

                                }
                                strTrxAmount = strTrxAmount.trim();

                                boolean txnIdFound = false;
                                pattern = Pattern
                                        .compile("\\s*transaction\\s*:\\s*\\s*\\w*\\.\\w*\\.\\w*\\s*");
                                matcher = pattern.matcher(strMsgLower);
                                txnIdFound = matcher.find();

                                if (!txnIdFound) {
                                    pattern = Pattern
                                            .compile("\\s*\\.+\\s*ref\\s*:\\s*\\s*\\w*\\.\\w*\\.\\w*\\s*");
                                    matcher = pattern.matcher(strMsgLower);
                                    txnIdFound = matcher.find();
                                }
                                String strTrxIDOld = "";
                                if (txnIdFound) {
                                    Log.d(K.LOGTAG,
                                            "TrxID found: " + matcher.group());
                                    strSubString = matcher.group();

                                    pattern = Pattern
                                            .compile("\\s*\\w*\\.\\w*\\.\\w*");
                                    matcher = pattern.matcher(strSubString);
                                    if (matcher.find()) {
                                        strTrxID = matcher.group().trim();
                                        Log.d(K.LOGTAG, "TrxID: " + strTrxID);
                                    }
                                } else {
                                    Log.d(K.LOGTAG, "TrxID not found.");
                                    return;
                                }
                                try {
                                    strTrxIDOld = strTrxID;
                                    if (strTrxID.length() >= 20) {
                                        strTrxID = strTrxID.substring(0, 20);
                                    }
                                } catch (Exception ee) {
                                    strTrxID = strTrxIDOld;
                                }
                                pattern = Pattern.compile("\\s*reussi\\s*");
                                matcher = pattern.matcher(strMsgLower);

                                if (matcher.find()) {// reussi was found
                                    Log.d(K.LOGTAG, "Transaction succesful. "
                                            + matcher.group());

                                    pattern = Pattern
                                            .compile("commission\\s*:\\s*\\d*(\\.\\d+)*");
                                    matcher = pattern.matcher(strMsgLower);
                                    if (matcher.find()) {
                                        Log.d(K.LOGTAG, "Commission found: "
                                                + matcher.group());
                                        strSubString = matcher.group();
                                        try {
                                            String comissionStr = strSubString
                                                    .split(":")[1];
                                            totalCommission = Float
                                                    .parseFloat(comissionStr.trim());

                                        } catch (Exception e) {

                                        }
                                    } else {
                                        Log.d(K.LOGTAG, "Commission not found.");
                                        return;
                                    }

                                    trx_status = TRX_STATUS.OK;
                                    // TODO: handle successcful trx
                                } else {

                                    Log.d(K.LOGTAG, "Check transaction failure.");

                                    trx_status = TRX_STATUS.REJET;
                                }
                                pattern = null;
                                matcher = null;

                                long lAmount = 0;
                                try {
                                    float f = Float.parseFloat(strTrxAmount);
                                    lAmount = (long) f;
                                } catch (Exception ee) {
                                    lAmount = 0;
                                }

                                Cursor cursor = SharedSingleton
                                        .getInstance().getDB(context)
                                        .getAllPendingTransactions(clientNo, lAmount);
                                int id = -1;
                                if (cursor.getCount() > 0) {
                                    cursor.moveToFirst();
                                    id = cursor.getInt(cursor.getColumnIndex("_id"));
                                }

                                Log.d(K.LOGTAG,
                                        "Will update tranaction status FROM: "
                                                + strTrxFrom + " STATUS: "
                                                + trx_status + " AMOUNT: "
                                                + lAmount);


                                long updatedTransactions = SharedSingleton
                                        .getInstance().getDB(context)
                                        .updateTrxByID(clientNo, TRX_STATUS.PENDING, // update
                                                // transaction
                                                // with
                                                // new
                                                // status
                                                // only
                                                trx_status, lAmount, strTrxID, id);

                                Log.d(K.LOGTAG, "Updated Transaction" + updatedTransactions);


                                if (updatedTransactions == 0)
                                    return;

                                TrxDetail objTrxDetail = SharedSingleton
                                        .getInstance().getDB(context)
                                        .getTrx(clientNo, trx_status, lAmount);

                                String currentBalance = SharedSingleton
                                        .getInstance()
                                        .getDB(ctx)
                                        .getSubDistributorBalance(
                                                objTrxDetail.trxSDPhoneNumber);

                                Log.d(K.LOGTAG, "cb cd : " + currentBalance);
                                Log.d(K.LOGTAG, "number numbe : " + objTrxDetail.trxSDPhoneNumber);
                                Log.d(K.LOGTAG, "status STtus: " + objTrxDetail.trxStatus);


                                long newBalance = Long.valueOf(currentBalance);

                                if (objTrxDetail.mTrxType.toString().trim().equalsIgnoreCase("cashout") && strMsg.contains("Retrait")) {
                                    if (trx_status == TRX_STATUS.OK) {

                                        SharedSingleton
                                                .getInstance()
                                                .getDB(ctx)
                                                .updateSubDistributorBalance(
                                                        objTrxDetail.trxSDPhoneNumber,
                                                        (long) objTrxDetail.trxAmount,
                                                        TRX_TYPE.NA);
                                    }

                                    newBalance = (long) (Long.parseLong(currentBalance) + objTrxDetail.trxAmount);
                                } else {

                                    newBalance = Long.valueOf(currentBalance);
                                }
                                Log.d(K.LOGTAG, "after goahead");
                                postTransaction(new TransactionRecord(
                                        objTrxDetail.id, strTrxID, ""
                                        + (long) objTrxDetail.trxAmount,
                                        objTrxDetail.trxStatus,
                                        objTrxDetail.trxTargetPhoneNumber,
                                        objTrxDetail.trxSDPhoneNumber,
                                        objTrxDetail.trxSDName,
                                        objTrxDetail.mTrxType.toString()), String.valueOf(newBalance), objTrxDetail.trxSDPhoneNumber);
                                float cashInRate = 0;
                                float cashOutRate = 0;

                                String sdPhone = objTrxDetail.trxSDPhoneNumber;
                                Cursor sdDetails = SharedSingleton.getInstance()
                                        .getDB(context)
                                        .getSubDistributorByPhone(sdPhone);
                                if (sdDetails != null) {
                                    if (sdDetails.moveToFirst()) {
                                        cashInRate = sdDetails.getFloat(sdDetails
                                                .getColumnIndex(DistributorDB.COLUMN_SUBD_COMMPERCENTCASHIN));
                                        cashOutRate = sdDetails.getFloat(sdDetails
                                                .getColumnIndex(DistributorDB.COLUMN_SUBD_COMMPERCENTCASHOUT));
                                    }
                                    sdDetails.close();
                                }

                                float rate = (objTrxDetail.mTrxType
                                        .compareTo(TRX_TYPE.CASHIN) == 0) ? cashInRate
                                        : cashOutRate;

                                subDistributorCommission = rate / 100 * 0.05f
                                        * totalCommission;
                                distributorComission = (1 - rate / 100) * 0.05f
                                        * totalCommission;

                                SharedSingleton
                                        .getInstance()
                                        .getDB(context)
                                        .putCommission(
                                                objTrxDetail.trxSDPhoneNumber,
                                                distributorComission,
                                                subDistributorCommission,
                                                totalCommission);

                                if (trx_status == TRX_STATUS.ECHEC) {
                                    android.telephony.SmsManager
                                            .getDefault()
                                            .sendTextMessage(
                                                    objTrxDetail.trxSDPhoneNumber,
                                                    null,
                                                    objTrxDetail
                                                            .genSubDistributorSMS(),
                                                    null, null);
                                } else if (trx_status == TRX_STATUS.PROCESSED) {
                                    android.telephony.SmsManager
                                            .getDefault()
                                            .sendTextMessage(
                                                    objTrxDetail.trxSDPhoneNumber,
                                                    null,
                                                    objTrxDetail
                                                            .genSubDistributorSMS(),
                                                    null, null);

                                    // TODO: Add commission calculation logic,
                                    // right now it is 1 in any case
                                    SharedSingleton
                                            .getInstance()
                                            .getDB(context)
                                            .addPendingCommission(
                                                    objTrxDetail.trxSDID, 5);
                                    double dPendingComm = SharedSingleton
                                            .getInstance()
                                            .getDB(context)
                                            .getPendingCommission(
                                                    objTrxDetail.trxSDID);

                                    if (dPendingComm >= 10) {
                                        Intent intDialComm = new Intent(context,
                                                DialCommissionUSSD.class);
                                        intDialComm.putExtra(K.KEY_COMMISSION_AMT,
                                                "10");
                                        intDialComm.putExtra(K.KEY_SDNUMBER,
                                                objTrxDetail.trxSDPhoneNumber);
                                        intDialComm
                                                .putExtra(
                                                        K.KEY_SDSECRET,
                                                        SharedSingleton
                                                                .getInstance()
                                                                .getDB(context)
                                                                .getSDSecret(
                                                                        objTrxDetail.trxSDID));
                                        context.startService(intDialComm);
                                        SharedSingleton
                                                .getInstance()
                                                .getDB(context)
                                                .lessPendingCommission(
                                                        objTrxDetail.trxSDID, 10);
                                    }
                                } else if (trx_status == TRX_STATUS.OK) {
                                    android.telephony.SmsManager
                                            .getDefault()
                                            .sendTextMessage(
                                                    objTrxDetail.trxSDPhoneNumber,
                                                    null,
                                                    objTrxDetail
                                                            .getContentForOKSMS(),
                                                    null, null);
                                }

                                Intent iUpdateUI = new Intent(K.INT_NEWSMSTRX);
                                iUpdateUI.putExtra("objtrx", objTrxDetail);
                                context.sendBroadcast(iUpdateUI);
                            }


                        }

                        private void extractConfigParams(String strMsg) {
                            String distCellNo = null, commissionRate = null, ussdThreshold = null;

                            distCellNo = getParameter(strMsg, "DIST_CELL_NO");
                            commissionRate = getParameter(strMsg, "COMM_RATE");
                            ussdThreshold = getParameter(strMsg,
                                    "USSD_THRESHOLD");

                            if (distCellNo == null || commissionRate == null
                                    || ussdThreshold == null) {
                                return;
                            }

                            DistributorDB tdb = SharedSingleton.getInstance()
                                    .getDB(ctx);
                            tdb.updateGeneralSetting(
                                    DistributorDB.SETTING_COMMISSION_THRESHOLD,
                                    commissionRate);
                            // tdb.updateGeneralSetting(
                            // DistributorDB.SETTING_NUMERO_DE_TELEPHONE,
                            // distCellNo);
                        }

                        private String getParameter(String sourceString,
                                                    String parameterName) {
                            String result = null;
                            Pattern pattern = Pattern.compile(parameterName
                                    + "\\s*:\\s*(\\+)*\\d*(\\.\\d+)*");
                            Matcher matcher = pattern.matcher(sourceString);
                            if (matcher.find()) {
                                Log.d(K.LOGTAG, parameterName + " found: "
                                        + matcher.group());
                                String distCellNoGroup = matcher.group();
                                try {
                                    result = distCellNoGroup.split(":")[1];
                                    result = result.trim();
                                } catch (Exception e) {

                                }
                            } else {
                                Log.d(K.LOGTAG, parameterName + " not found.");
                                return null;
                            }

                            return result;
                        }

                        private void processTransaction(String senderNumber,
                                                        SMSDetail objSMSDetail) {
                            Cursor sdDetails = SharedSingleton.getInstance()
                                    .getDB(context)
                                    .getSubDistributorByPhone(sendersNumber);

                            Log.d(K.LOGTAG, "SE NDER NUMBER " + senderNumber);

                            Log.d(K.LOGTAG, "SE NDER NUMBER " + sdDetails);

                            if (sdDetails == null || sdDetails.getCount() == 0) {
                                return;
                            }

                            Log.d(K.LOGTAG, "afer succcess");

                            SharedSingleton.getInstance().getDB(context)
                                    .putSMS(objSMSDetail);

                            boolean unattendedModeOn = SharedSingleton
                                    .getInstance()
                                    .getDB(ctx)
                                    .getAsBoolean(
                                            DistributorDB.SETTING_UNATTENDED_MODE);

                            if (unattendedModeOn) {
                                String amountThresholdStr = SharedSingleton
                                        .getInstance()
                                        .getDB(ctx)
                                        .getGeneralSetting(
                                                DistributorDB.SETTING_AMOUNT_THRESHOLD_FOR_UNATTENDED_MODE);

                                if (amountThresholdStr != null
                                        && !amountThresholdStr.equals("")) {
                                    long amountThreshold = Long
                                            .parseLong(amountThresholdStr);

                                    Cursor receivedSMSCursor = SharedSingleton
                                            .getInstance().getDB(ctx)
                                            .getPendingSMS();
                                    if (receivedSMSCursor.moveToLast()) {
                                        SMSDetail smsDetail = new SMSDetail();
                                        smsDetail
                                                .populateFromCursor(receivedSMSCursor);

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

                                            Log.d(K.LOGTAG, "aboce axccpt tr Aboev");

                                            acceptTransaction(smsDetail);
                                        } else {
                                            sendMessage(context);
                                        }
                                    }
                                }
                            } else {
                                sendMessage(context);
                            }
                        }

                    });
                }
//				else {
//					Toast.makeText(ctx, "DUPLICATE...    " + strMsgLower, 5000)
//							.show();
//				}
                Log.v(K.LOGTAG, "On Receive");
            } catch (Exception e) {
                Log.e(K.LOGTAG, e.toString());
            }


        }
    }

    private void getCodesFromFile() {
        Single.fromCallable((Callable<String>) () -> {
            File file = new File(ctx.getFilesDir() + "/codes_folder/codes.txt");

            try (final BufferedSource source = Okio.buffer(FileSystem.SYSTEM.source(file))) {
                String s = source.readUtf8();
                codes = new Gson().fromJson(s, new TypeToken<List<String>>() {
                }.getType());
                retrievedCode = codes.remove(0);
                codes.add(retrievedCode);
                return "";
            } catch (final IOException exception) {
                //ignored exception
                return null;
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(s -> {


        }, Throwable::printStackTrace);


    }

    private void purgeSMSInbox() {

        try {
            // mLogger.logInfo("Deleting SMS from inbox");
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor c = ctx.getContentResolver().query(
                    uriSms,
                    new String[]{"_id", "thread_id", "address", "person",
                            "date", "body"}, null, null, null);

            if (c != null) {
                if (c.moveToFirst()) {

                    do {
                        long id = c.getLong(0);
                        // long threadId = c.getLong(1);
                        // String address = c.getString(2);
                        String body = c.getString(5);

                        String keyword = body.split("\\s|\\n")[0];

                        if (keywords.contains(keyword.toUpperCase())) {
                            // mLogger.logInfo("Deleting SMS with id: " +
                            // threadId);

                            ctx.getContentResolver().delete(
                                    Uri.parse("content://sms/" + id), null,
                                    null);
                        }
                    } while (c.moveToNext());
                }
                c.close();
            }
        } catch (Exception e) {
            Log.d(K.LOGTAG, e.getMessage());
            final Exception ex = e;
            ((Activity) ctx).runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(
                            ctx,
                            "Exception while puring inbox---" + ex.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void acceptTransaction(final SMSDetail _smsDetail) {

        Log.d(K.LOGTAG, "ACCEPT TRANSATION");


        TRX_TYPE tType = TRX_TYPE.valueOf(_smsDetail.smsTrxType.trim());

//        final String strFinalUSSD = getUSSDString(
//                tType,
//                _smsDetail.smsTargetPhoneNumber,
//                SharedSingleton.getInstance().getDB(ctx)
//                        .getGeneralSetting(DistributorDB.SETTING_CODE_SECRET),
//                _smsDetail.smsAmount);

        // final SMSDetail _smsDetail = new SMSDetail(mSMSDetail);

        //Log.d(K.LOGTAG, strFinalUSSD);

        Executor eS = Executors.newSingleThreadExecutor();
        eS.execute(new Runnable() {
            @Override
            public void run() {
                if (isDuplicateSMS) {

                    Log.e(K.LOGTAG, "isDuplicateSMS2 ->" + isDuplicateSMS);
                } else {

                    Log.e(K.LOGTAG, "isDuplicateSMS3 ->" + isDuplicateSMS);

                    SharedSingleton
                            .getInstance()
                            .getDB(ctx)
                            .updateGeneralSetting(
                                    DistributorDB.SETTING_DIALED_USSD, "0");

                    SharedSingleton.getInstance().getDB(ctx)
                            .updateSMSStatus(_smsDetail, TRX_STATUS.PROCESSED);

                    long trxID = SharedSingleton
                            .getInstance()
                            .getDB(ctx)
                            .putTrx(new Date().getTime(),
                                    _smsDetail.smsTargetPhoneNumber,
                                    _smsDetail.smsAmount, TRX_STATUS.PENDING,
                                    _smsDetail.smsSDID,
                                    _smsDetail.smsSDPhoneNumber,
                                    _smsDetail.smsTrxType, "");

                    Cursor trxCursor = SharedSingleton.getInstance().getDB(ctx)
                            .getTransactionByID("" + trxID);

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
                                .getDB(ctx)
                                .getSubDistributorBalance(
                                        _smsDetail.smsSDPhoneNumber);

                        Log.d(K.LOGTAG, "before N/A");
                        postTransaction(new TransactionRecord(
                                id,
                                "N/A",
                                amount,
                                TRX_STATUS.PENDING.toString(),
                                trxCursor.getString((trxCursor
                                        .getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TARGET_NUMBER))),
                                SDnumber, SDName, trxType), currentBalance, _smsDetail.smsSDPhoneNumber);
                    }

                    trxCursor.close();
                    Log.d(K.LOGTAG, "Before dial");
                    if (TRX_TYPE.CASHIN == TRX_TYPE
                            .valueOf(_smsDetail.smsTrxType)) {
                        CallUssd("#145#");
                        Data1 = "1";
                        Data2 = _smsDetail.smsTargetPhoneNumber;
                        Data3 = String.valueOf(_smsDetail.smsAmount);
                        Data4 = "1";

//                        Data5 = "777049602";

                        Log.d("InSMSReceiverDistri", "run: retrievedCode :: " + retrievedCode);

                        Data5 = retrievedCode;

                        Data6 = SharedSingleton.getInstance().getDB(ctx)
                                .getGeneralSetting(DistributorDB.SETTING_CODE_SECRET);

                        reWriteFile();


//                        CallUssd("*167#");
//                        Data1 = "3";
//                        Data2 = "3";
//                        Data3 = "1";
//                        Data4 = "01790588791";
//                        Data5 = "20";
//                        Data6 = "4855";
                    } else if (TRX_TYPE.CASHOUT == TRX_TYPE
                            .valueOf(_smsDetail.smsTrxType)) {

                        CallUssd("#145#");
                        Data1 = "2";
                        Data2 = "1";
                        Data3 = _smsDetail.smsTargetPhoneNumber;
                        Data4 = String.valueOf(_smsDetail.smsAmount);
                        Data5 = SharedSingleton.getInstance().getDB(ctx)
                                .getGeneralSetting(DistributorDB.SETTING_CODE_SECRET);
                        Data6 = null;

//                        CallUssd("*167#");
//                        Data1 = "2";
//                        Data2 = "01790588791";
//                        Data3 = "20";
//                        Data4 = "000";
//                        Data5 = "4855";
//                        Data6 = null;
                    } else if (TRX_TYPE.CREDIT == TRX_TYPE
                            .valueOf(_smsDetail.smsTrxType)) {

                        CallUssd("#145#");
                        Data1 = "7";
                        Data2 = "1";
                        Data3 = _smsDetail.smsTargetPhoneNumber;
                        Data4 = String.valueOf(_smsDetail.smsAmount);
                        Data5 = SharedSingleton.getInstance().getDB(ctx)
                                .getGeneralSetting(DistributorDB.SETTING_CODE_SECRET);
                        Data6 = null;


//                        CallUssd("*167#");
//                        Data1 = "2";
//                        Data2 = "01790588791";
//                        Data3 = "20";
//                        Data4 = "000";
//                        Data5 = "4855";
//                        Data6 = null;
                    }

                    //Toast.makeText(ctx,"USSD : "+strFinalUSSD,Toast.LENGTH_LONG).show();
//					Handler handler = new Handler(Looper.getMainLooper());
//
//					handler.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							// Run your task here
//							try{
//								obj.dial(strFinalUSSD);
//							}
//							catch (Exception e)
//							{
//								Log.v("Solve this error : ", "" + e);
//							}
//						}
//					}, 1000 );

//                    if (Build.VERSION.SDK_INT >= 23) {
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                try {
//
//                                    dial(Uri.encode(strFinalUSSD));
//                                    Toast.makeText(ctx, "USSD : " + strFinalUSSD, Toast.LENGTH_LONG).show();
//                                    Log.v(K.LOGTAG, "String USSD :" + strFinalUSSD);
//
//                                } catch (Exception e) {
//                                    //Log.v(K.LOGTAG, "String USSD :" + strFinalUSSD);
//                                    Log.d(K.LOGTAG, "Solve this error: " + e.getMessage());
//                                }
//
//                            }
//                        });
//                    }
//
//
//                    dialUSSD(Uri.encode(strFinalUSSD));


                    if (TRX_TYPE.valueOf(_smsDetail.smsTrxType.trim()) != TRX_TYPE.CASHOUT) {

                        Log.v(K.LOGTAG, "acceptTransaction : Is running");
                        SharedSingleton
                                .getInstance()
                                .getDB(ctx)
                                .updateSubDistributorBalance(
                                        _smsDetail.smsSDPhoneNumber,
                                        _smsDetail.smsAmount,
                                        TRX_TYPE.valueOf(_smsDetail.smsTrxType.trim()));
                    }

                }
            }


        });
        // target phone, amount, secret

    }

    private void reWriteFile() {
        Single.fromCallable(() -> {
            String s = new Gson().toJson(codes);
            File dir = new File(ctx.getFilesDir() + "/codes_folder");
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
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(s -> {

        }, Throwable::printStackTrace);
    }

    @SuppressLint("MissingPermission")
    public void dial(String ussd) {
        Log.d(K.LOGTAG, "Inside dial");
        try {
            Intent i = new Intent(Intent.ACTION_CALL);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setData(Uri.parse("tel:" + ussd));
            Log.v(K.LOGTAG, "Checking ussd :" + Uri.parse("tel:" + i));
            //i.setData(Uri.parse("tel:" + ussd));
            ctx.startActivity(i);
            Log.d(K.LOGTAG, "after Inside dial");
            //startActivity(ctx,i,null);
//            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussd));
//            startActivity(ctx, intent , null);
        } catch (Exception e) {

            //Log.v(K.LOGTAG, "Solve this error :"+e);
            Log.v(K.LOGTAG, "USSD 2 :" + e);
            //Log.v(K.LOGTAG, "Solve this error :"+getIntent());
        }
    }

    public int dialUSSD(String strUSSD) {
        Log.d(K.LOGTAG, "Inside dialUSSD");
        Runtime runtime = Runtime.getRuntime();
        //Process proc = null;

        int nResp = 0;
        try {
            runtime.exec(String.format(K.FMT_DIALLER, strUSSD));
            Log.d(K.LOGTAG, "after Inside dialUSSD");

        } catch (Exception exc) {
            Log.e(K.LOGTAG, "Fasle: " + exc.getMessage());
        }
        return nResp;
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

    private void initKeywords() {
        keywords.clear();
        keywords.add("CASHIN");
        keywords.add("CREDIT");
        keywords.add("CASHOUT");
        keywords.add("DIST_CONFIG");
        keywords.add("DEPOT");
        keywords.add("RETRAIT");
    }

    private void sendMessage(Context context) {
        Intent iUpdateUI = new Intent(K.INT_NEWSMSTRX);
        context.sendBroadcast(iUpdateUI);
    }

    private void postTransaction(TransactionRecord record, String CurrentBal, String trxSDPhoneNumber) {

        Log.d(K.LOGTAG, "inside postTransaction");

        String distCellNo = SharedSingleton.getInstance().getDB(this.ctx)
                .getGeneralSetting(DistributorDB.SETTING_NUMERO_DE_TELEPHONE);

        ArrayList<TransactionRecord> transactionRecords = new ArrayList<TransactionRecord>();
        transactionRecords.add(record);

        if (distCellNo != null && !distCellNo.equals("")
                && !distCellNo.equals("123")) {

            Gson gson = new Gson();
            String recordsStr = gson.toJson(transactionRecords);

            String url = K.DISTRIBUTOR_SERVICE_URL + "saveTransaction";

            Map<String, String> params = new HashMap<>();
            params.put("dist_cell_no", distCellNo);
            params.put("finalized_trx", recordsStr);
            params.put("sdMobileNumber", trxSDPhoneNumber);
            params.put("currentBalance", CurrentBal);

            Log.d(K.LOGTAG, "before webTask: insms");
            Log.d(K.LOGTAG, "before webTask: " + distCellNo);
            Log.d(K.LOGTAG, "before webTask: " + recordsStr);
            Log.d(K.LOGTAG, "before webTask: " + trxSDPhoneNumber);
            Log.d(K.LOGTAG, "before webTask: " + CurrentBal);

            WebTask webTask = new WebTask(url, params, new WebTaskDelegate() {

                @Override
                public void asynchronousPostExecute(String sr) {
                    super.asynchronousPostExecute(sr);

                    // for (TranscationRecord record : records) {
                    // SharedSingleton
                    // .getInstance()
                    // .getDB(ctx)
                    // .updateTrxSyncStatus(
                    // record.getId());
                    // }
                }

            });
            webTask.execute();
        }

    }

    public void CallUssd(String ussd_code) {
        Log.d(K.LOGTAG, "before empty");
        Log.d(K.LOGTAG, String.valueOf(ussdToCallableUri(ussd_code)));
        if (!ussd_code.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_CALL,
                    ussdToCallableUri(ussd_code));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            USSDService.isDone = false;
        }
    }


    private Uri ussdToCallableUri(String ussd) {
        String uriString = "";

        if (!ussd.startsWith("tel:"))
            uriString += "tel:";

        for (char c : ussd.toCharArray()) {
            if (c == '#')
                uriString += Uri.encode("#");
            else
                uriString += c;
        }
        return Uri.parse(uriString);
    }

    public static final String STRMSGDUMMY = "Depot vers 779787796 fall reussi from 781185035 revendeur. informations detaillees : montant transaction:520.00fcfa, id de transaction :ci140110.1532.c03554,frais:0.0fcfa,commission:10.00 fcfa,montant net debite : 510.00fcfa,Nouveau solde:1256fcfa.";
    public static final String STRFAILDUMMY = "Especes en opreration a partir de 781185035 revendeur a 772422518 fall est echoue avec TXN Id:ci140109.2012.b02170 en raison de :E60019: il n y a pas assez d argent sur le compte client.";
}
