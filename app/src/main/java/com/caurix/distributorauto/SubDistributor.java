package com.caurix.distributorauto;

import java.util.HashMap;

import com.caurix.distributor.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SubDistributor extends Activity {

    private static final String AUCUNE_DONN = "Aucune donnÈe trouvÈe.";
    public static final String SELECTED_PHONE = "SELECTED_PHONE";
    public static final String ADD_MODE = "ADD_MODE";

    private Cursor mSDCursor;
    private ListView mLVSubDist;
    private MenuItem editContactView;
    private MenuItem deleteContactView;
    private String selectedPhone = "";

    private HashMap<View, String> listItemMapping = new HashMap<View, String>();

    public static final String ARG_DATE_PARAM = "arg_date_param", ARG_TOTAL = "total", ARG_DATE = "date";

    private String mstrDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_subdistributors);

        mLVSubDist = (ListView) findViewById(R.id.lvSubDistributors);

        View vHeader = View.inflate(this, R.layout.header_subdist, null);
        mLVSubDist.addHeaderView(vHeader);
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
        getMenuInflater().inflate(R.menu.menusubdist, menu);
        editContactView = (MenuItem) menu.findItem(R.id.itmEdit);
        deleteContactView = (MenuItem) menu.findItem(R.id.itmDelete);

        return true;
    }

    private class SubDistributorAdapter extends ArrayAdapter {
        public SubDistributorAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            if (mSDCursor == null)
                return 0;
            return mSDCursor.getCount();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.row_subdist, null);

            if (mSDCursor == null)
                return convertView;
            if (!mSDCursor.moveToPosition(position))
                return convertView;

            TextView tvSDName = (TextView) convertView.findViewById(R.id.colSDName);
            TextView tvSDPhone = (TextView) convertView.findViewById(R.id.colMobile);
            TextView tvSDBalance = (TextView) convertView.findViewById(R.id.colBalance);

            int nColIdx = -1;
            String strCode = "";

            if ((nColIdx = mSDCursor.getColumnIndex(DistributorDB.COLUMN_SUBD_SDNAME)) != -1) {
                tvSDName.setText(mSDCursor.getString(nColIdx));
            }
            if ((nColIdx = mSDCursor.getColumnIndex(DistributorDB.COLUMN_SUBD_TRANSACTIONPHONE)) != -1) {
                tvSDPhone.setText(mSDCursor.getString(nColIdx));
            }
            if ((nColIdx = mSDCursor.getColumnIndex(DistributorDB.COLUMN_SUBD_CURRENTBALANCE)) != -1) {
                tvSDBalance.setText(mSDCursor.getString(nColIdx));
            }
            if ((nColIdx = mSDCursor.getColumnIndex(DistributorDB.COLUMN_SUBD_SDID)) != -1) {
                strCode = "Sub-Dist. ID: " + mSDCursor.getString(nColIdx) + " - Secret: ";
            }

            if ((nColIdx = mSDCursor.getColumnIndex(DistributorDB.COLUMN_SUBD_CODE_SCRET)) != -1) {
                strCode = strCode + mSDCursor.getString(nColIdx);
            }

            listItemMapping.put(convertView, tvSDPhone.getText().toString());
            final String sx = strCode;

            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View selectedView = v;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Toast.makeText(SubDistributor.this, sx,
                            // Toast.LENGTH_LONG).show();
                            boolean selStatus = !selectedView.isSelected();
                            selectedView.setSelected(selStatus);
                            editContactView.setEnabled(selStatus);
                            deleteContactView.setEnabled(selStatus);

                            if (selStatus) {
                                selectedPhone = listItemMapping.get(selectedView);
                            } else {
                                selectedPhone = "";
                            }

                            ListView parent = (ListView) selectedView.getParent();
                            int size = parent.getChildCount();
                            for (int i = 0; i < size; i++) {
                                View child = parent.getChildAt(i);
                                if (child != selectedView) {
                                    child.setSelected(false);
                                }
                            }
                        }
                    });
                }
            });

            return convertView;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        showList();

        super.onResume();
    }

    private void showList() {
        if (editContactView != null) {
            editContactView.setEnabled(false);
        }
        if (deleteContactView != null) {
            deleteContactView.setEnabled(false);
        }
        selectedPhone = "";

        mSDCursor = SharedSingleton.getInstance().getDB(this).getSubDistributors();
        mLVSubDist.setAdapter(new SubDistributorAdapter(this, -1));

        if (mSDCursor == null) {
            showNoDataFoundMessage();
        } else if (mSDCursor.getCount() <= 0) {
            showNoDataFoundMessage();
        } else {
            hideNoDataFoundMessage();
        }
    }

    private void hideNoDataFoundMessage() {
        TextView tvNoData = (TextView) findViewById(R.id.tvNoDataFound);
        tvNoData.setVisibility(TextView.GONE);
    }

    public void showNoDataFoundMessage() {
        Toast.makeText(this, AUCUNE_DONN, Toast.LENGTH_LONG).show();
        setTitle(AUCUNE_DONN);
        TextView tvNoData = (TextView) findViewById(R.id.tvNoDataFound);
        tvNoData.setVisibility(TextView.VISIBLE);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        if (mSDCursor != null)
            mSDCursor.close();
        super.onPause();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.itmAddNew:
                Intent intEditSD = new Intent(this, AddSDActivity.class);
                intEditSD.putExtra(ADD_MODE, true);
                startActivity(intEditSD);
                break;
            case R.id.itmEdit:
                intEditSD = new Intent(this, AddSDActivity.class);
                intEditSD.putExtra(SELECTED_PHONE, selectedPhone);
                startActivity(intEditSD);
                break;
            case R.id.itmDelete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.strDeleteSDConfirmation)
                        .setPositiveButton(R.string.menuDeleteSD, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (selectedPhone != null && !selectedPhone.equals("")) {
                                    DistributorDB tdb = SharedSingleton.getInstance().getDB(SubDistributor.this);
                                    tdb.deleteSubDistributor(selectedPhone);

                                    showList();
                                }
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                // Create the AlertDialog object and return it
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
        return true;
    }

}