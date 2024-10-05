package com.caurix.distributorauto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.caurix.distributor.R;
import com.caurix.distributorauto.web.WebTask;
import com.caurix.distributorauto.web.WebTaskDelegate;

public class AddSDActivity extends Activity implements OnClickListener {

    EditText metAddress1, /* metAddress2, */
            metPhone1, /* metPhone2, */
            metEmail1,
    /* metEmail2, */metTrxPhone, metCashInPercent, metCashOutPercent,
    /* metSecret, */metSDName, metContactPerson, metCurrentBalance;
    Button btnSave;

    private String selectedPhone;
    private boolean isAddMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isAddMode = getIntent().getExtras().getBoolean(SubDistributor.ADD_MODE);

        selectedPhone = getIntent().getExtras().getString(
                SubDistributor.SELECTED_PHONE);

        setContentView(R.layout.activity_addsd);
        btnSave = (Button) findViewById(R.id.btnSaveSD);
        btnSave.setOnClickListener(this);

        metAddress1 = (EditText) findViewById(R.id.etSDAddress1);
        // metAddress2 = (EditText) findViewById(R.id.etSDAddress2);
        metPhone1 = (EditText) findViewById(R.id.etSDPhone1);
        // metPhone2 = (EditText) findViewById(R.id.etSDPhone2);
        metEmail1 = (EditText) findViewById(R.id.etEMail1);
        // metEmail2 = (EditText) findViewById(R.id.etEMail2);
        metTrxPhone = (EditText) findViewById(R.id.etSDTrxMobile);
        metCashInPercent = (EditText) findViewById(R.id.etSDCommissionCIn);
        metCashOutPercent = (EditText) findViewById(R.id.etSDCommissionCOut);
        // metSecret = (EditText) findViewById(R.id.etSDSecret);
        metSDName = (EditText) findViewById(R.id.etSDName);
        metContactPerson = (EditText) findViewById(R.id.etSDContactPerson);
        metCurrentBalance = (EditText) findViewById(R.id.etSDCurrentBalance);

        if (!isAddMode && selectedPhone != null && !selectedPhone.equals("")) {
            loadValues();
        }
    }

    public void onWindowFocusChanged(boolean isTrue) {
        super.onWindowFocusChanged(isTrue);

        if (!isTrue) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    private void loadValues() {
        DistributorDB tdb = SharedSingleton.getInstance().getDB(this);
        Cursor cursor = tdb.getSubDistributorByPhone(selectedPhone);

        if (cursor != null && cursor.moveToFirst()) {
            metAddress1.setText(cursor.getString(cursor
                    .getColumnIndex(DistributorDB.COLUMN_SUBD_ADDRESS1)));
            // metAddress2.setText(cursor.getString(cursor
            // .getColumnIndex(DistributorDB.COLUMN_SUBD_ADDRESS2)));
            metPhone1.setText(cursor.getString(cursor
                    .getColumnIndex(DistributorDB.COLUMN_SUBD_CONTACTPHONE1)));
            // metPhone2.setText(cursor.getString(cursor
            // .getColumnIndex(DistributorDB.COLUMN_SUBD_CONTACTPHONE2)));
            metEmail1.setText(cursor.getString(cursor
                    .getColumnIndex(DistributorDB.COLUMN_SUBD_EMAIL1)));
            // metEmail2.setText(cursor.getString(cursor
            // .getColumnIndex(DistributorDB.COLUMN_SUBD_EMAIL2)));
            metTrxPhone.setText(selectedPhone);
            metCurrentBalance.setText(cursor.getString(cursor
                    .getColumnIndex(DistributorDB.COLUMN_SUBD_CURRENTBALANCE)));
            metCashInPercent
                    .setText(cursor.getString(cursor
                            .getColumnIndex(DistributorDB.COLUMN_SUBD_COMMPERCENTCASHIN)));
            metCashOutPercent
                    .setText(cursor.getString(cursor
                            .getColumnIndex(DistributorDB.COLUMN_SUBD_COMMPERCENTCASHOUT)));
            // metSecret.setText(cursor.getString(cursor
            // .getColumnIndex(DistributorDB.COLUMN_SUBD_CODE_SCRET)));
            metSDName.setText(cursor.getString(cursor
                    .getColumnIndex(DistributorDB.COLUMN_SUBD_SDNAME)));
            metContactPerson.setText(cursor.getString(cursor
                    .getColumnIndex(DistributorDB.COLUMN_SUBD_CONTACTPERSON)));
        }
    }

    private boolean saveValues() {
        if (metPhone1.getText().toString().length() <= 0
                || metTrxPhone.getText().toString().length() <= 0
                || metCurrentBalance.getText().toString().length() <= 0
                || metCashInPercent.getText().toString().length() <= 0
                || metCashOutPercent.getText().toString().length() <= 0
                // || metSecret.getText().toString().length() <= 0
                || metSDName.getText().toString().length() <= 0) {
            return false;
        }
        double dCashIn = 0.0;
        double dCashOut = 0.0;
        long currentBalance = 0;

        try {
            currentBalance = Long.parseLong(metCurrentBalance.getText()
                    .toString().trim());
        } catch (Exception e) {
            currentBalance = 0;
        }

        try {
            dCashIn = Double.parseDouble(metCashInPercent.getText().toString()
                    .trim());
            // if (dCashIn > 10) {
            // dCashIn = 10;
            // }
        } catch (Exception e) {
            dCashIn = 0.0;
        }

        try {
            dCashOut = Double.parseDouble(metCashOutPercent.getText()
                    .toString().trim());
            // if (dCashOut > 10) {
            // dCashOut = 10;
            // }
        } catch (Exception e) {
            dCashOut = 0.0;
        }

        String strSDIDNew = SharedSingleton.getInstance().getSDIDNew();

        Log.d(K.LOGTAG, "New SDID created: " + strSDIDNew);

        DistributorDB tdb = SharedSingleton.getInstance().getDB(this);
        // java.util.UUID.randomUUID().toString()
        boolean success = false;

        System.out.println("IS ADD MODE :" + isAddMode);

        if (!isAddMode) {

            int updatedRows = tdb.updateSubDistributor(selectedPhone,
                    metTrxPhone.getText().toString(), metAddress1.getText()
                            .toString().trim(), "", metPhone1.getText()
                            .toString(), "", metEmail1.getText().toString(), ""
                            .toString(), currentBalance, dCashIn, dCashOut,
                    ""/* metSecret.getText().toString().trim() */, metSDName
                            .getText().toString().trim(), metContactPerson
                            .getText().toString().trim());

            if (updatedRows <= 0) {
                Toast.makeText(this, "L'op3e4eeeeeeeeeeeeeeer  vration a vchour", Toast.LENGTH_LONG)
                        .show();
            } else {
                success = true;
            }

            Map<String, String> params = new HashMap<>();
            params.put("sd_number", metTrxPhone.getText().toString());
            params.put("sd_mobilenumber", metPhone1.getText().toString());
            params.put("sd_balance", "" + currentBalance);

            System.out.println("IS ADD MODE sd_number :" + metTrxPhone.getText().toString());
            System.out.println("IS ADD MODE sd_mobilenumber :" + metPhone1.getText().toString());

            System.out.println("IS ADD MODE sd_balance :" + currentBalance);


            WebTask webTask = new WebTask(
                    "http://www.caurix.net/controller/DistributorTestController.php?cmd=saveBalanceOfSubDistributor",
                    params, new WebTaskDelegate() {

                @Override
                public void asynchronousPostExecute(String sr) {
                    super.asynchronousPostExecute(sr);
                    System.out.println("IS ADD MODE :" + sr);
                    System.out.println("It added");

                    // for (TransactionRecord record : records) {
                    // SharedSingleton
                    // .getInstance()
                    // .getDB(ctx)
                    // .updateTrxSyncStatus(record.getId());
                    // }
                }

            });
            webTask.execute();

        } else if (tdb.putSubDistributor(strSDIDNew, new SimpleDateFormat(
                        K.DATEFORMAT).format(new Date()), metAddress1.getText()
                        .toString().trim(), "", metPhone1.getText().toString(), "",
                metEmail1.getText().toString(), "", metTrxPhone.getText()
                        .toString(), currentBalance, dCashIn, dCashOut, ""/*
                                                                         * metSecret
																		 * .
																		 * getText
																		 * ()
																		 * .toString
																		 * (
																		 * ).trim
																		 * ()
																		 */, 1,
                metSDName.getText().toString().trim(), metContactPerson
                        .getText().toString().trim()) <= 0) {
            Toast.makeText(this, "L'op�ration a �chou�", Toast.LENGTH_LONG)
                    .show();
        } else {

            System.out.println(" LAST ELSW");
            success = true;
        }

        if (success) {
            Toast.makeText(this, "Op�ration r�ussie", Toast.LENGTH_LONG).show();
            finish();
        }
        tdb = null;

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // loadValues();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#finish()
     */
    @Override
    public void finish() {
        // TODO Auto-generated method stub
        // saveValues();
        super.finish();
    }

    @Override
    public void onClick(View v) {
        boolean saved = saveValues();

        if (!saved) {
            Toast.makeText(this,
                    "S'il vous pla�t fournir des valeurs obligatoires",
                    Toast.LENGTH_LONG).show();
        }

    }
}
