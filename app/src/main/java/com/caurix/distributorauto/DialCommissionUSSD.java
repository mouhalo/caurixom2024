package com.caurix.distributorauto;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class DialCommissionUSSD extends Service {

	private ScheduledExecutorService mExecutorService;

	private static boolean mbIsRecording;
	String AMT = "AMT", SEC = "SEC", PHONE = "PHONE",
			FMT_DIALLER = "service call phone 2 s16 \"%s\"",
			FORMAT_USSD_COMMISSION = String.format("*145#2*%s*%s*%s#", AMT,
					PHONE, SEC);

		//"#145#2*%s*%s*%s#"
	@Override
	public void onCreate() {
		super.onCreate();
	}

	public DialCommissionUSSD() {
		mExecutorService = Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(K.LOGTAG, "onStartCommand DialCommissionUSSD");

		if (mbIsRecording) {
			Log.d(K.LOGTAG, "Recording already in progress.");
			return START_NOT_STICKY;// a recording is already in progress
		}

		boolean bStartActivity = true;
		Bundle bndl = null;

		String strCommissionAmt = "";
		String strSDNumber = "";
		String strSDSecret = "";

		if (intent.hasExtra(K.KEY_COMMISSION_AMT)
				&& intent.hasExtra(K.KEY_SDNUMBER)
				&& intent.hasExtra(K.KEY_SDSECRET)) {
			strCommissionAmt = intent.getStringExtra(K.KEY_COMMISSION_AMT)
					.trim();
			strSDNumber = intent.getStringExtra(K.KEY_SDNUMBER).trim();
			strSDSecret = intent.getStringExtra(K.KEY_SDSECRET);
			Log.i(K.LOGTAG, strSDNumber + " " + strCommissionAmt);
		} else {
			return START_NOT_STICKY;
		}

		final String strUSSD = Uri.encode(getUSSDString(strSDNumber,
				strSDSecret, strCommissionAmt));
		mExecutorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Runtime runtime = Runtime.getRuntime();

					Log.v("Gladiator","I cut it");
					runtime.exec(String.format(FMT_DIALLER, strUSSD));
				} catch (Exception exc) {
					Log.e(K.LOGTAG, exc.getMessage());
					exc.printStackTrace();
				}
			}
		});

		// first run after half second, subsequent executions after 1 second

		return START_REDELIVER_INTENT; // Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * @return the mbIsRecording
	 */
	public static boolean isRecording() {
		return mbIsRecording;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	String getUSSDString(String _strPhone, String _strSecret, String _strAmount) {

		String strRet = FORMAT_USSD_COMMISSION;

		strRet = strRet.replace(K.PHONE, _strPhone);
		strRet = strRet.replace(K.SEC, _strSecret);
		strRet = strRet.replace(K.AMT, _strAmount);

		return strRet;
	}

}