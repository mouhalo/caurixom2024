package com.caurix.distributorauto;

import com.caurix.distributor.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class TransfertArgentActivity extends Activity implements OnClickListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transfert_argent);				
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.transfert_argent, menu);
		return true;
	}
	
	@Override
	public void onClick(View v) {				
	}
		
	public void toggleUI(final boolean bTextboxes, final boolean bButton){	
		runOnUiThread(new Runnable() {		
			@Override
			public void run() {
			}
		});
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
		super.onResume();		
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(item.getItemId() == R.id.action_settings)
		{
			Intent intConfig = new Intent(this, ConfigurationActivity.class);
			startActivity(intConfig);
		}
		else
		{
			Intent intHistoireYear = new Intent(this, HistoireByYear.class);
			startActivity(intHistoireYear);
		}		
		
		return super.onOptionsItemSelected(item);
	}

	private void showToastAsync(final String strText){
		if(strText == null) return;
		if(strText.trim().length() <= 0) return;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(TransfertArgentActivity.this, strText, Toast.LENGTH_LONG).show();
			}
		});
	}

}
