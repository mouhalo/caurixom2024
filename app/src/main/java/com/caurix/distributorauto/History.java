package com.caurix.distributorauto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.caurix.distributor.R;

public class History extends Activity {
    private static final String AUCUNE_DONN = "Aucune donnÈe trouvÈe.";
    private ViewFlipper historyFlipper = null;
    private ListView yearsList, monthsList, daysList = null;

    private static String[] months = {"January", "Frbruary", "March", "April",
            "May", "June", "July", "August", "September", "October",
            "November", "December"};

    private String selectedYear;

    private TextView totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

//		String strTitle = "History ";
        setTitle(R.string.title_activity_history);

        init();
    }

    public void onWindowFocusChanged(boolean isTrue) {
        super.onWindowFocusChanged(isTrue);

        if (!isTrue) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    private void init() {
        historyFlipper = (ViewFlipper) findViewById(R.id.historyFlipper);
        // historyFlipper.setOnClickListener(this);

        yearsList = (ListView) findViewById(R.id.yearsList);
        monthsList = (ListView) findViewById(R.id.monthsList);
        daysList = (ListView) findViewById(R.id.daysList);

        Cursor mHistoryCursor = SharedSingleton.getInstance().getDB(this)
                .getYearsHistory();
        yearsList.setAdapter(new HistoryAdapter(this, mHistoryCursor));
        totalAmount = (TextView) findViewById(R.id.totalAmount);
        showTotal(mHistoryCursor);
    }

    public void showTotal(Cursor mHistoryCursor) {
        long total = 0;
        if (mHistoryCursor.moveToFirst()) {
            while (!mHistoryCursor.isAfterLast()) {
                total += mHistoryCursor.getLong(mHistoryCursor
                        .getColumnIndex("amount"));
                mHistoryCursor.moveToNext();
            }
        }
        totalAmount.setText("" + total);
    }

    @Override
    public void onBackPressed() {
        View currentView = historyFlipper.getCurrentView();

        if (historyFlipper.getChildAt(0) != currentView) {
            historyFlipper.showPrevious();
            currentView = historyFlipper.getCurrentView();
            if (historyFlipper.indexOfChild(currentView) == 1) {// Month View
                setTitle(selectedYear);
                showTotal(SharedSingleton.getInstance().getDB(this)
                        .getMonthHistory(selectedYear));
            } else if (historyFlipper.indexOfChild(currentView) == 0) {// Year
                // View
                setTitle("History");
                showTotal(SharedSingleton.getInstance().getDB(this)
                        .getYearsHistory());
            }

        } else {
            super.onBackPressed();
        }
    }

    public void showNextList(String timeData) {
        View currentView = historyFlipper.getCurrentView();
        int count = historyFlipper.getChildCount();

        if (historyFlipper.getChildAt(count - 1) != currentView) {
            Cursor mHistoryCursor = null;
            this.setTitle(timeData);

            historyFlipper.showNext();
            currentView = historyFlipper.getCurrentView();
            int index = historyFlipper.indexOfChild(currentView);
            switch (index) {
                case 1:

                    mHistoryCursor = SharedSingleton.getInstance().getDB(this)
                            .getMonthHistory(timeData);
                    selectedYear = timeData;
                    HistoryAdapter adapter = new HistoryAdapter(this,
                            mHistoryCursor);
                    adapter.setShowMonthNames(true);
                    monthsList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
                case 2:
                    int monthNumber = 0;
                    for (int i = 0; i < months.length; i++) {
                        if (timeData.equalsIgnoreCase(months[i])) {
                            monthNumber = i + 1;
                            break;
                        }
                    }

                    String month = String.format("%02d", monthNumber);
                    mHistoryCursor = SharedSingleton.getInstance().getDB(this)
                            .getDayHistory(selectedYear, month);
                    daysList.setAdapter(new HistoryAdapter(this, mHistoryCursor));
                    break;
            }

            if (mHistoryCursor != null) {
                showTotal(mHistoryCursor);
            }
        }
    }

    public void showNoDataFoundMessage() {
        Toast.makeText(this, AUCUNE_DONN, Toast.LENGTH_LONG).show();
        setTitle(AUCUNE_DONN);
    }

    private class HistoryAdapter extends ArrayAdapter<String> {
        Cursor mHistoryCursor = null;

        boolean showMonthNames = false;

        public HistoryAdapter(Context context, Cursor mHistoryCursor) {
            super(context, -1);
            this.mHistoryCursor = mHistoryCursor;
        }

        @Override
        public int getCount() {
            int count = 0;
            if (mHistoryCursor != null) {
                count = mHistoryCursor.getCount();
            }
            return count;
        }

        public void setShowMonthNames(boolean showMonthNames) {
            this.showMonthNames = showMonthNames;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(
                    R.layout.history_list_item, null);

            mHistoryCursor.moveToPosition(position);
            TextView dataText = (TextView) convertView
                    .findViewById(R.id.history_item_data);

            if (!showMonthNames) {
                dataText.setText(mHistoryCursor.getString(mHistoryCursor
                        .getColumnIndex("timeduration")));
            } else {
                int duration = Integer.parseInt(mHistoryCursor
                        .getString(mHistoryCursor
                                .getColumnIndex("timeduration")));

                String monthName = History.months[duration - 1];
                dataText.setText(monthName);

            }
            TextView amountText = (TextView) convertView
                    .findViewById(R.id.history_item_amount);
            amountText.setText(mHistoryCursor.getString(mHistoryCursor
                    .getColumnIndex("amount")));
            final Context ctx = getContext();
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View convertView) {
                    History historyActivity = (History) ctx;
                    TextView dataText = (TextView) convertView
                            .findViewById(R.id.history_item_data);
                    historyActivity.showNextList(dataText.getText().toString());
                }
            });
            return convertView;
        }

    }

}
