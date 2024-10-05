package com.caurix.distributorauto;

import com.caurix.distributor.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

public class ConfigurationActivity extends Activity {

    EditText metCodeSecret,
            metUnattendedAmountThreshold, metNumeroDeTelephone;//metNomDuMerchand, metCommissionThreshold
    CheckBox mchbCodeSecretInc,
            mchbDelaiExpirationInc, mchUnattendedMode;//mchbNomDuMerchandInc, mchbNumeroDeTelephoneInc,

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

//		metNomDuMerchand = (EditText) findViewById(R.id.etNomDuMarchand);
        metNumeroDeTelephone = (EditText) findViewById(R.id.etNumeroDeTel);
        metCodeSecret = (EditText) findViewById(R.id.etCodeSecret);

//		mchbNomDuMerchandInc = (CheckBox) findViewById(R.id.chbNomDuMarchand);
//		mchbNumeroDeTelephoneInc = (CheckBox) findViewById(R.id.chbNumeroDeTel);
        mchUnattendedMode = (CheckBox) findViewById(R.id.chbUnattendedMode);

//		metCommissionThreshold = (EditText) findViewById(R.id.txt_commision_threshold);
        metUnattendedAmountThreshold = (EditText) findViewById(R.id.txt_amount_threshold_for_unattended);
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

//		metNomDuMerchand.setText(tdb
//				.getGeneralSetting(DistributorDB.SETTING_NOM_DU_MARCHAND));
//		metCommissionThreshold.setText(tdb
//				.getGeneralSetting(DistributorDB.SETTING_COMMISSION_THRESHOLD));
        metUnattendedAmountThreshold
                .setText(tdb
                        .getGeneralSetting(DistributorDB.SETTING_AMOUNT_THRESHOLD_FOR_UNATTENDED_MODE));

        metNumeroDeTelephone.setText(tdb
                .getGeneralSetting(DistributorDB.SETTING_NUMERO_DE_TELEPHONE));
        metCodeSecret.setText(tdb
                .getGeneralSetting(DistributorDB.SETTING_CODE_SECRET));
//		mchbNomDuMerchandInc.setChecked(tdb
//				.getAsBoolean(DistributorDB.SETTING_INCLURE_NOM_DU_MARCHAND));
//		mchbNumeroDeTelephoneInc
//				.setChecked(tdb
//						.getAsBoolean(DistributorDB.SETTING_INCLURE_NUMERO_DE_TELEPHONE));
        mchUnattendedMode
                .setChecked(tdb
                        .getAsBoolean(DistributorDB.SETTING_UNATTENDED_MODE));

        tdb = null;

    }

    private void saveValues() {

        DistributorDB tdb = SharedSingleton.getInstance().getDB(this);

//		tdb.updateGeneralSetting(DistributorDB.SETTING_NOM_DU_MARCHAND,
//				metNomDuMerchand.getText().toString().trim());
        tdb.updateGeneralSetting(DistributorDB.SETTING_NUMERO_DE_TELEPHONE,
                metNumeroDeTelephone.getText().toString().trim());
        tdb.updateGeneralSetting(DistributorDB.SETTING_CODE_SECRET,
                metCodeSecret.getText().toString().trim());

//		tdb.updateGeneralSetting(DistributorDB.SETTING_COMMISSION_THRESHOLD,
//				metCommissionThreshold.getText().toString().trim());
        tdb.updateGeneralSetting(
                DistributorDB.SETTING_AMOUNT_THRESHOLD_FOR_UNATTENDED_MODE,
                metUnattendedAmountThreshold.getText().toString().trim());

//		tdb.updateFromBoolean(DistributorDB.SETTING_INCLURE_NOM_DU_MARCHAND,
//				mchbNomDuMerchandInc.isChecked());
//		tdb.updateFromBoolean(
//				DistributorDB.SETTING_INCLURE_NUMERO_DE_TELEPHONE,
//				mchbNumeroDeTelephoneInc.isChecked());
        tdb.updateFromBoolean(
                DistributorDB.SETTING_UNATTENDED_MODE,
                mchUnattendedMode.isChecked());

        tdb = null;
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
        loadValues();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#finish()
     */
    @Override
    public void finish() {
        // TODO Auto-generated method stub
        saveValues();
        super.finish();
    }
}
