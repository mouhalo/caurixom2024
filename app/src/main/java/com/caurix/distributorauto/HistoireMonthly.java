package com.caurix.distributorauto;

import java.util.Locale;

import com.caurix.distributor.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
//import android.animation.ArgbEvaluator;

public class HistoireMonthly extends Activity {

    String m_strMonth, m_strNumMonth, m_strYear, m_strTotalFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histoire_monthly);
        try {
            if (getIntent().hasExtra(K.ARG_MONTH)) {
                m_strMonth = getIntent().getStringExtra(K.ARG_MONTH);
                Log.d(K.LOGTAG, m_strMonth);
            }

            if (getIntent().hasExtra(K.ARG_YEAR)) {
                m_strYear = getIntent().getStringExtra(K.ARG_YEAR);
                Log.d(K.LOGTAG, m_strMonth);
            }

            m_strTotalFull = (getIntent().hasExtra(K.ARG_TOTAL_MONTH)) ? getIntent().getStringExtra(K.ARG_TOTAL_MONTH) : "0.00";

            setTitle(m_strMonth + " " + m_strYear);
        } catch (Exception e) {
            Log.e(K.LOGTAG, e.getMessage() + "");
        }
    }

    public void onWindowFocusChanged(boolean isTrue) {
        super.onWindowFocusChanged(isTrue);

        if (!isTrue) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {

        m_strNumMonth = getNumberByMonth(m_strMonth);
        ListView lvMnthly = (ListView) findViewById(R.id.lvHistoireMonthly);
        TextView tvTotal = (TextView) findViewById(R.id.tvAmtTotalMonthly);

        HistoireByMonthAndYearAdapter lAdap = new HistoireByMonthAndYearAdapter(this, -1);
        try {
            lAdap.populateData(m_strYear, getNumberByMonth(m_strMonth), m_strTotalFull, this);
            lvMnthly.setAdapter(lAdap);
            tvTotal.setText(m_strTotalFull);
        } catch (Exception e) {
            Log.e(K.LOGTAG, e.getMessage() + "");
        }
        super.onResume();
    }


    private String getNumberByMonth(String strMonth) {

        String strNumber = "01";
        if (strNumber != null) {
            if (strNumber.length() > 0) {
                if (strMonth.toLowerCase(Locale.FRENCH).equals("janvier"))
                    strNumber = "01";
                else if (strMonth.toLowerCase(Locale.FRENCH).equals("février"))
                    strNumber = "02";
                else if (strMonth.toLowerCase(Locale.FRENCH).equals("mars"))
                    strNumber = "03";
                else if (strMonth.toLowerCase(Locale.FRENCH).equals("avril"))
                    strNumber = "04";
                else if (strMonth.toLowerCase(Locale.FRENCH).equals("mai"))
                    strNumber = "05";
                else if (strMonth.toLowerCase(Locale.FRENCH).equals("juin"))
                    strNumber = "06";
                else if (strMonth.toLowerCase(Locale.FRENCH).equals("juillet"))
                    strNumber = "07";
                else if (strMonth.toLowerCase(Locale.FRENCH).equals("août"))
                    strNumber = "08";
                else if (strMonth.toLowerCase(Locale.FRENCH).equals("septembre"))
                    strNumber = "09";
                else if (strMonth.toLowerCase(Locale.FRENCH).equals("octobre"))
                    strNumber = "10";
                else if (strMonth.toLowerCase(Locale.FRENCH).equals("novembre"))
                    strNumber = "11";
                else if (strMonth.toLowerCase(Locale.FRENCH).equals("décembre"))
                    strNumber = "12";
            }
        }
        return strNumber;
    }

    public static class HistoireByMonthAndYearAdapter extends ArrayAdapter {

        Cursor _mHistoireCursor;
        String _strYear, _strMonth, _strTotal;
        Context _mContext;

        public HistoireByMonthAndYearAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return (_mHistoireCursor == null) ? 0 : _mHistoireCursor.getCount();
        }

        public void populateData(String strYear, String strMonth, String strTotal, Context ctx) {
            try {
                _mContext = ctx;
                _strYear = strYear;
                _strMonth = strMonth;
                _strTotal = strTotal;

                if (_mHistoireCursor != null) {
                    if (!_mHistoireCursor.isClosed()) {
                        _mHistoireCursor.close();
                    }
                }
                _mHistoireCursor = null;
                _mHistoireCursor = SharedSingleton.getInstance().getDB(ctx).getDaysTotalByMonthAndYear(_strYear, _strMonth);
            } catch (Exception e) {
                Log.e(K.LOGTAG, e.getMessage() + "");
            }
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {

            convertView = ((Activity) _mContext).getLayoutInflater().inflate(R.layout.row_histoire_amount, null);

            String strMonth = "";
            double dAmount = 0.0;

            if (_mHistoireCursor == null) return convertView;
            if (position <= _mHistoireCursor.getCount()) {
                if (_mHistoireCursor.moveToPosition(position)) {
                    try {
                        strMonth = _mHistoireCursor.getString(_mHistoireCursor.getColumnIndex(DistributorDB.COLUMN_DYN_VALDAY));
                        dAmount = _mHistoireCursor.getDouble(_mHistoireCursor.getColumnIndex(DistributorDB.COLUMN_DYN_VALTOTALDAY));

                        final TextView tvRowHistoireMonth = (TextView) convertView.findViewById(R.id.tvRowHistoireMonth);
                        final TextView tvRowHistoireTotal = (TextView) convertView.findViewById(R.id.tvRowHistoireTotalMonth);

                        tvRowHistoireMonth.setText(strMonth);
                        tvRowHistoireTotal.setText(dAmount + "");

                        OnClickListener ocl = new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                String strDate = _strYear + "-" + _strMonth + "-" + tvRowHistoireMonth.getText().toString().trim();
                                Intent intJournal = new Intent(_mContext, Journal.class);
                                intJournal.putExtra(Journal.ARG_DATE_PARAM, strDate);
                                intJournal.putExtra(Journal.ARG_TOTAL, tvRowHistoireTotal.getText().toString().trim());
                                _mContext.startActivity(intJournal);
                            }
                        };

                        tvRowHistoireMonth.setOnClickListener(ocl);
                        tvRowHistoireTotal.setOnClickListener(ocl);

                    } catch (Exception e) {
                        strMonth = "";
                        dAmount = 0.0;
                    }
                }
            }
            return convertView;
        }

        final String[] _mMonths = {"Janvier", "F�vrier", "Mars", "Avril", "Mai", "Juin", "Juillet", "Ao�t", "Septembre", "Octobre", "Novembre", "D�cembre"};

        String getMonthByNumber(final String strNumber) {
            int nMonth = 0;

            try {
                nMonth = Integer.parseInt(strNumber);
                nMonth = (nMonth > 0) ? nMonth - 1 : nMonth;

            } catch (Exception e) {
                nMonth = 0;
            }

            if (nMonth < _mMonths.length) {
                return _mMonths[nMonth];
            }
            return "Janvier";
        }

        /* (non-Javadoc)
         * @see android.widget.ArrayAdapter#clear()
         */
        @Override
        public void clear() {
            // TODO Auto-generated method stub
            if (_mHistoireCursor != null) {
                if (!_mHistoireCursor.isClosed()) {
                    _mHistoireCursor.close();
                }
            }
            super.clear();
        }
    }


}
