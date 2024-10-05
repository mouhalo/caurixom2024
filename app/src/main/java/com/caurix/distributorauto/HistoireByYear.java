package com.caurix.distributorauto;

import com.caurix.distributor.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
//import android.app.ActionBar;

public class HistoireByYear extends AppCompatActivity implements ActionBar.OnNavigationListener {
//{
    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private String[] m_aryYears;
//	Cursor mHistoireCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histoire_by_year);

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar(); //getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        m_aryYears = SharedSingleton.getInstance().getDB(this).getYears();
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(getActionBarThemedContextCompat(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1, m_aryYears), this);
    }

    /**
     * Backward-compatible version of {@link ActionBar#getThemedContext()} that
     * simply returns the {@link android.app.Activity} if
     * <code>getThemedContext</code> is unavailable.
     */

    @TargetApi(14) //Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Context getActionBarThemedContextCompat() {
        if (Build.VERSION.SDK_INT >= 14) {//Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return getSupportActionBar().getThemedContext();
        } else {
            return this;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getSupportActionBar()
                .getSelectedNavigationIndex());
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        Fragment fragment = new DummySectionFragment();
        Bundle args = new Bundle();

        args.putString(DummySectionFragment.ARG_YEAR, m_aryYears[position]);
        args.putString(DummySectionFragment.ARG_TOTAL, SharedSingleton.getInstance().getDB(this).getTotalByYear(m_aryYears[position]) + "");

        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();
        return true;
    }

    public static class HistoireByYearAdapter extends ArrayAdapter {

        Cursor _mHistoireCursor;
        String _strYear;
        Context _mContext;

        public HistoireByYearAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return (_mHistoireCursor == null) ? 0 : _mHistoireCursor.getCount();
        }

        public void populateData(String strYear, Context ctx) {
            try {
                _mContext = ctx;
                _strYear = strYear;
                if (_mHistoireCursor != null) {
                    if (!_mHistoireCursor.isClosed()) {
                        _mHistoireCursor.close();
                    }
                }
                _mHistoireCursor = null;
                _mHistoireCursor = SharedSingleton.getInstance().getDB(ctx).getMonthsTotalByYear(strYear);
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
                        strMonth = _mHistoireCursor.getString(_mHistoireCursor.getColumnIndex(DistributorDB.COLUMN_DYN_VALMONTH));
                        dAmount = _mHistoireCursor.getDouble(_mHistoireCursor.getColumnIndex(DistributorDB.COLUMN_DYN_VALTOTALMONTH));

                        final TextView tvRowHistoireMonth = (TextView) convertView.findViewById(R.id.tvRowHistoireMonth);
                        final TextView tvRowHistoireTotal = (TextView) convertView.findViewById(R.id.tvRowHistoireTotalMonth);

                        strMonth = getMonthByNumber(strMonth);
                        tvRowHistoireMonth.setText(strMonth);
                        tvRowHistoireTotal.setText(dAmount + "");

                        OnClickListener ocl = new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                Intent intMonthly = new Intent(_mContext, HistoireMonthly.class);
                                intMonthly.putExtra(K.ARG_MONTH, tvRowHistoireMonth.getText());
                                intMonthly.putExtra(K.ARG_YEAR, _strYear);
                                intMonthly.putExtra(K.ARG_TOTAL_MONTH, tvRowHistoireTotal.getText().toString());
                                _mContext.startActivity(intMonthly);
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

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number",
                ARG_YEAR = "arg_year",
                ARG_TOTAL = "arg_total";


        ListView lvYears;

        public DummySectionFragment() {
            _mHBYA = null;
        }

        HistoireByYearAdapter _mHBYA;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(
                    R.layout.fragment_histoire_by_year, container, false);
            Log.d(K.LOGTAG, getArguments().getString(ARG_YEAR));

            ListView lvMonths = (ListView) rootView.findViewById(R.id.lvHistoireByYear);
            if (lvMonths != null) {

                if (_mHBYA == null) {
                    _mHBYA = new HistoireByYearAdapter(getActivity(), -1);
                }

                _mHBYA.populateData(getArguments().getString(ARG_YEAR), getActivity());
                lvMonths.setAdapter(_mHBYA);

                if (_mHBYA.getCount() == 0) showNoDataFoundMessage(rootView);

            }

            TextView tvTotal = (TextView) rootView.findViewById(R.id.tvAmtTotalByYear);
            tvTotal.setText(getArguments().getString(ARG_TOTAL));

            return rootView;
        }

        public void showNoDataFoundMessage(View rootView) {
            try {
                TextView tvNoData = (TextView) rootView.findViewById(R.id.tvNoDataFoundFrag);
                tvNoData.setVisibility(TextView.VISIBLE);
            } catch (Exception e) {
                Log.e(K.LOGTAG, e.getMessage());
            }
        }

    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    protected void onResume() {
        m_aryYears = SharedSingleton.getInstance().getDB(this).getYears();
        super.onResume();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#finish()
     */
    @Override
    public void finish() {
        // TODO Auto-generated method stub
        super.finish();
    }

}
