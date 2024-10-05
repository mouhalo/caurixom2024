package com.caurix.distributorauto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.caurix.distributor.R;
import com.caurix.distributorauto.adapter.TrxLogAdapter;
import com.caurix.distributorauto.model.TrxLog;
import com.caurix.distributorauto.model.TrxLogGroupItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupedJournal extends Activity {

    private static final String AUCUNE_DONN = "Aucune donnee trouvee.";
    Cursor mHistoryCursor;
    RecyclerView mLVHistory;
    List<TrxLog> trxLogs = new ArrayList<>();
    List<TrxLogGroupItem> trxLogGroupItems = new ArrayList<>();
    public static final String ARG_DATE_PARAM = "arg_date_param",
            ARG_TOTAL = "total",
            ARG_DATE = "date";
    private String mstrDate;
    private TrxLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grouped_journal);
        mLVHistory = (RecyclerView) findViewById(R.id.lvHistory);
        mstrDate = getIntent().hasExtra(ARG_DATE_PARAM) ? getIntent().getStringExtra(ARG_DATE_PARAM) : "";

        String strTitle = "Journal ";

        if (mstrDate.length() > 0) {
            strTitle = strTitle + mstrDate;
        }

        if (getIntent().hasExtra(ARG_TOTAL))
            strTitle = strTitle + " - " + getIntent().getStringExtra(ARG_TOTAL);

        setTitle(strTitle);
        mHistoryCursor = SharedSingleton.getInstance().getDB(this).getHistory(mstrDate);

        processData(mHistoryCursor);

        if (mHistoryCursor == null) {
            showNoDataFoundMessage();
        } else if (mHistoryCursor.getCount() <= 0) {
            showNoDataFoundMessage();
        }
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


    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Process data get from database
     * */
    private void processData(Cursor mHistoryCursor) {
        HashMap<String, List<TrxLog>> hashMap = new HashMap<>();
        trxLogs.clear();
        trxLogGroupItems.clear();
        for (int i = 0; i < mHistoryCursor.getCount(); i++) {
            if (mHistoryCursor.moveToPosition(i)) {
                TrxLog trxLog = new TrxLog();
                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TARGET_NUMBER) >= 0) {
                    trxLog.setTrxTargetNumber(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TARGET_NUMBER)));
                }
                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_STATUS) >= 0) {
                    trxLog.setTrxStatus(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_STATUS)));
                }

                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_DATE_TIME) >= 0) {
                    trxLog.setTrxDateTime(mHistoryCursor.getString((mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_DATE_TIME))));
                }

                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_AMOUNT) >= 0) {
                    trxLog.setTrxAmount(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_AMOUNT)) + " fcfa");
                }

                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_NOTES) >= 0) {
                    trxLog.setTrxNotes(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_NOTES)).toUpperCase());
                }

                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TRXTYPE) >= 0) {
                    trxLog.setTrxType(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_TRXTYPE)));
                }
                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_SDNUMBER) >= 0) {
                    trxLog.setTrxSDNumber(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_TRX_LOG_SDNUMBER)));
                }
                if (mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_SUBD_SDNAME) >= 0) {
                    trxLog.setSdName(mHistoryCursor.getString(mHistoryCursor.getColumnIndex(DistributorDB.COLUMN_SUBD_SDNAME)));
                }
                if (!hashMap.containsKey(trxLog.getTrxSDNumber())) {
                    List<TrxLog> list = new ArrayList<>();
                    list.add(trxLog);
                    hashMap.put(trxLog.getTrxSDNumber(), list);
                } else {
                    hashMap.get(trxLog.getTrxSDNumber()).add(trxLog);
                }
                //When cursor go to end set data to adapter
                if(i == mHistoryCursor.getCount() -1){
                    for(String k :hashMap.keySet()){
                        trxLogGroupItems.add(new TrxLogGroupItem(hashMap.get(k).get(0).getSdName(),k,hashMap.get(k)));
                    }
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
                    mLVHistory.setLayoutManager(layoutManager);
                    adapter = new TrxLogAdapter(trxLogGroupItems,this );
                    mLVHistory.setAdapter(adapter);
                }

            }




        }


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
