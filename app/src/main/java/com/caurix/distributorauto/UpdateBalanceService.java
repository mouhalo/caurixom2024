package com.caurix.distributorauto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class UpdateBalanceService extends Service {

	ProgressDialog pd;
	JSONArray respMsg;
	public static Timer mTimer;
	PowerManager pm;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {

		// pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// PowerManager.WakeLock wl = pm.newWakeLock(
		// PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
		// wl.acquire();
		super.onCreate();
		// Toast.makeText(this, "Service net Created", 300).show();

	}

	@Override
	public void onDestroy() {
		// mTimer.cancel();

		super.onDestroy();

	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();

	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//Toast.makeText(this, "task perform in service", 300).show();

		if (mTimer != null) {

			mTimer.cancel();
			mTimer = new Timer();

			useTimer();

		} else {

			mTimer = new Timer();
			useTimer();

		}

		// td.start();
		return START_STICKY;

	}

	public void useTimer() {

		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				Log.e("CHECKING CONNECTION",
						"CALLING WS FOR CHECKING CONNECTION");

				handler.sendEmptyMessage(0);
			}
		}, 0, 360000);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 0:
				getsimply();

				// Toast.makeText(getApplicationContext(),
				// "Checkiŋ", 300).show();
				break;
			}
			super.handleMessage(msg);
			// showAppNotification();
		}
	};

	void getsimply() {

		new Thread() {
			private String requestURL = "http://www.caurix.net/controller/DistributorTestController.php?cmd=getSubDistributorBalance";
			private URL url;
			private String response = "";

			public void run() {

				try {

					System.out.println("CELLING BACKGROUD");

					try {
						url = new URL(requestURL);

						HttpURLConnection conn = (HttpURLConnection) url
								.openConnection();
						conn.setReadTimeout(15000);
						conn.setConnectTimeout(15000);
						conn.setRequestMethod("GET");
						conn.setDoInput(true);
						conn.setDoOutput(true);

						OutputStream os = conn.getOutputStream();
						BufferedWriter writer = new BufferedWriter(
								new OutputStreamWriter(os, "UTF-8"));
						// writer.write(getPostDataString(postDataParams));

						writer.flush();
						writer.close();
						os.close();
						int responseCode = conn.getResponseCode();

						if (responseCode == HttpsURLConnection.HTTP_OK) {
							String line;
							BufferedReader br = new BufferedReader(
									new InputStreamReader(conn.getInputStream()));
							while ((line = br.readLine()) != null) {
								response += line;
							}

							JSONObject obj = new JSONObject(response);
							respMsg = obj.getJSONArray("responseBody");

							for (int i = 0; i < respMsg.length(); i++) {

								String Number = respMsg.getJSONObject(i)
										.getString("sd_number");
								String balance = respMsg.getJSONObject(i)
										.getString("sd_balance");

								SharedSingleton.getInstance().getDB(UpdateBalanceService.this)
										.updateBalanceFromService(Number,balance);
							}

						} else {
							response = "";

						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println("RESPONSE :" + response);
			};
		}.start();
	}

	Handler handNWerror = new Handler() {

		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// pd.dismiss();
			final AlertDialog.Builder builder = new AlertDialog.Builder(
					getBaseContext());

			if (msg.what == 0)
			// builder.setMessage("" + Constant.MSG_NETWORK_ERROR);
			{
				// Toast.makeText(getApplicationContext(), "network error", 300)
				// .show();
			}

			else if (msg.what == 1) {
				// builder.setMessage("" + Constant.MSG_NETWORK_NO_INTERNET);
				// builder.create().show();
				// Toast.makeText(getApplicationContext(),
				// "network  internt error", 300).show();

			} else if (msg.what == 2) {
				// builder.setMessage("" + Constant.MSG_SERVER_ERROR);
				// Toast.makeText(getApplicationContext(),
				// "network  server error", 300).show();

			}
			// builder.create().show();
		}

	};

	Handler handGetRoom = new Handler() {
		public void handleMessage(Message msg) {
			// pd.dismiss();
			switch (msg.what) {
			case 0:

			case 1:

			case 5:

				break;
			case 6:

				break;

			case 7:

				break;
			default:
				break;
			}

		};
	};

}