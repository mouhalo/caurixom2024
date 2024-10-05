package com.caurix.distributorauto.web;


import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.caurix.distributorauto.K;
import com.caurix.distributorauto.SharedSingleton;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebTask extends AsyncTask<Void, String, String> {

	private String url;
	private Map<String, String> postParameters;;
	private Exception ex;
	private WebTaskDelegate delegate;
	private OkHttpClient client = new OkHttpClient();

	public WebTask(String url) {
		this(url, null, new WebTaskDelegate());
	}

	public WebTask(String url, WebTaskDelegate delegate) {
		this(url, null, delegate);
	}

	public WebTask(String url, Map<String, String> postParameters) {
		this(url, postParameters, new WebTaskDelegate());
	}

	public WebTask(String url, Map<String, String> postParameters,
			WebTaskDelegate delegate) {
		super();
		this.url = url;
		this.postParameters = postParameters;
		this.delegate = delegate;
	}

	
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (delegate != null) {
			delegate.synchronousPreExecute();
		}
	}
	

	@Override
	protected String doInBackground(Void... params) {

		FormBody.Builder builder = new FormBody.Builder();
		for ( Map.Entry<String, String> entry : postParameters.entrySet() ) {
			builder.add( entry.getKey(), entry.getValue() );
		}
		RequestBody formBody = builder.build();

		Request request = new Request.Builder()
				.url( url.replaceAll(" ", "%20") )
				.post( formBody )
				.build();

		try {
			Response response = client.newCall(request).execute();
			return response.body().string();
		}catch (Exception e){
			Log.d(K.LOGTAG, e.getMessage());
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		Log.d(K.LOGTAG, "onPostExecute");
		//String strurl = K.DISTRIBUTOR_SERVICE_URL + "saveTransaction";
		String strurl = "http://154.12.224.173/caurix/API/Distributor/saveTransaction.php";
		Log.d(K.LOGTAG, "URL :" + strurl);
		if(url.equals(strurl))
		{
			String trx_id = "0";
			String status = null;
			int postedOnServer = -1;
			int postedConfirmationOnServer = -1;
			if (result != null) {
				if (!result.equals("")) {
					Log.d(K.LOGTAG, result);
					postedOnServer = 1;
					String[] result1 =	result.split(",\"id\":\"");
					if (result1.length == 2) {
						trx_id = result1[1].split("\",\"")[0];
					}
					SharedSingleton.getInstance()
							.getDB(null).setPostedOnServer(postedOnServer, trx_id);
					Log.e(K.LOGTAG, "postedOnServer ->"+postedOnServer+" ID:"+trx_id);

					String[] result2 =	result.split(",\"status\":\"");
					if (result2.length == 2) {
						status = result2[1].split("\",\"")[0];
						if (status.equals("OK")) {
							postedConfirmationOnServer = 1;
							SharedSingleton.getInstance()
									.getDB(null).setPostedConfirmationOnServer(postedConfirmationOnServer, trx_id);
							Log.e(K.LOGTAG, "postedConfirmationOnServer ->"+postedConfirmationOnServer+":ID:"+trx_id);
						}
					}
				}
			}
		}
		if (ex != null) {
			if (delegate != null) {
				delegate.onError(ex);
				return;
			}
		}

		if (delegate != null) {
			delegate.asynchronousPostExecute(result);
		}
	}
}
