package com.caurix.distributorauto;

import com.caurix.distributor.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Journal extends Activity {

    private static final String AUCUNE_DONN = "Aucune donnÈe trouvÈe.";
    Cursor mHistoryCursor;
    ListView mLVHistory;

    public static final String ARG_DATE_PARAM = "arg_date_param",
            ARG_TOTAL = "total",
            ARG_DATE = "date";
    private String mstrDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        mLVHistory = (ListView) findViewById(R.id.lvHistory);
        mstrDate = getIntent().hasExtra(ARG_DATE_PARAM) ? getIntent().getStringExtra(ARG_DATE_PARAM) : "";

        String strTitle = "Journal ";

        if (mstrDate.length() > 0) {
            strTitle = strTitle + mstrDate;
        }

        if (getIntent().hasExtra(ARG_TOTAL))
            strTitle = strTitle + " - " + getIntent().getStringExtra(ARG_TOTAL);

        setTitle(strTitle);

//		Log.d(K.LOGTAG, strTitle);
    }

    public void onWindowFocusChanged(boolean isTrue) {
        super.onWindowFocusChanged(isTrue);

        if (!isTrue) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.history, menu);
        return true;
    }

    private class HistoryAdapter extends ArrayAdapter {

        public HistoryAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            if (mHistoryCursor == null) return 0;
            return mHistoryCursor.getCount();
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.row_history, null);
            if (mHistoryCursor == null) return convertView;

            TextView tvRTargetNumber = (TextView) convertView.findViewById(R.id.txtTargetNumber);
            TextView tvRStatus = (TextView) convertView.findViewById(R.id.txtStatus);
            TextView tvRDate = (TextView) convertView.findViewById(R.id.txtDate);
            TextView tvRAmount = (TextView) convertView.findViewById(R.id.txtAmount);
            TextView tvRTrxID = (TextView) convertView.findViewById(R.id.txtTrxID);
            TextView tvhTrxType = (TextView) convertView.findViewById(R.id.txtTrxType);
            TextView tvhTrxSDPhone = (TextView) convertView.findViewById(R.id.txtSDPhoneNumber);
            TextView tvhTrxSDName = (TextView) convertView.findViewById(R.id.txtSDName);

            if (mHistoryCursor.moveToPosition(position)) {
                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TARGET_NUMBER) >= 0) {
                    tvRTargetNumber.setText(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TARGET_NUMBER)));
                }

                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_STATUS) >= 0) {
                    tvRStatus.setText(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_STATUS)));
                    if (tvRStatus.getText().toString().equalsIgnoreCase(TRX_STATUS.OK.toString())) {
                        tvRStatus.setTextColor(Color.GREEN);
                    } else if (tvRStatus.getText().toString().equalsIgnoreCase(TRX_STATUS.REJET.toString())) {
                        tvRStatus.setTextColor(Color.RED);
                    } else if (tvRStatus.getText().toString().equalsIgnoreCase(TRX_STATUS.PENDING.toString())) {
                        tvRStatus.setTextColor(Color.BLUE);
                    }
                }

                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_DATE_TIME) >= 0) {
                    tvRDate.setText(mHistoryCursor.getString((mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_DATE_TIME))));
                }

                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_AMOUNT) >= 0) {
                    tvRAmount.setText(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_AMOUNT)) + " fcfa");
                }

                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_NOTES) >= 0) {
                    tvRTrxID.setText(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_NOTES)).toUpperCase());
                }

                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TRXTYPE) >= 0) {
                    tvhTrxType.setText(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TRXTYPE)));
                }
                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_SDNUMBER) >= 0) {
                    tvhTrxSDPhone.setText(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_SDNUMBER)));
                }
                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_SUBD_SDNAME) >= 0) {
                    tvhTrxSDName.setText(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_SUBD_SDNAME)));
                }
            }

            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d(K.LOGTAG, "Generate Receipt.");
                    TextView tvRTargetNumber = (TextView) v.findViewById(R.id.txtTargetNumber);
                    TextView tvRStatus = (TextView) v.findViewById(R.id.txtStatus);
                    TextView tvRDate = (TextView) v.findViewById(R.id.txtDate);
                    TextView tvRAmount = (TextView) v.findViewById(R.id.txtAmount);
                    if (!tvRStatus.getText().toString().equals(TRX_STATUS.SENT.toString())) {
                        Log.d(K.LOGTAG, "Transaction status not SENT.");
                        return;
                    }
                    try {
                        android.telephony.SmsManager.getDefault().sendTextMessage(tvRTargetNumber.getText().toString().trim(), null,
                                "Date: " + tvRDate.getText() + "\n" +
                                        "Montant: " + tvRAmount.getText() + " fcfa\n" +
                                        "Statut: " + tvRStatus.getText() + "\n" +
                                        SharedSingleton.getInstance().getFooter(Journal.this), null, null);
//					
//					Log.d(K.LOGTAG,
//							tvRDate.getText() + " " + tvRAmount.getText() + " " + tvRStatus.getText());

                        Toast.makeText(Journal.this, "SMS r�ception adress�e.", Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return convertView;
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {

        mHistoryCursor = SharedSingleton.getInstance().getDB(this).getHistory(mstrDate);
        mLVHistory.setAdapter(new HistoryAdapter(this, -1));

        if (mHistoryCursor == null) {
            showNoDataFoundMessage();
        } else if (mHistoryCursor.getCount() <= 0) {
            showNoDataFoundMessage();
        }

        super.onResume();
    }

    public void showNoDataFoundMessage() {
        Toast.makeText(this, AUCUNE_DONN, Toast.LENGTH_LONG).show();
        setTitle("Journal des transaction");
        TextView tvNoData = (TextView) findViewById(R.id.tvNoDataFound);
        tvNoData.setVisibility(TextView.VISIBLE);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        if (mHistoryCursor != null) mHistoryCursor.close();
        super.onPause();
    }

}
