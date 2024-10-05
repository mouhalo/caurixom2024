package com.caurix.distributorauto;

import java.util.Random;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

public class SharedSingleton {

	private DistributorDB m_DB;
	private static String mstrIMEI;

	private static SharedSingleton _instance;

	private SharedSingleton() {
		mstrIMEI = "";
	}

	private static class SingletonHolder {
		public static final SharedSingleton instance = new SharedSingleton();
	}

	public static synchronized SharedSingleton getInstance() {
		return SingletonHolder.instance;
	}

//	public static SharedSingleton getInstance(){
//		if(_instance == null){
//			_instance = new SharedSingleton();
//		}		
//		return _instance;
//	}

	public DistributorDB getDB(Context ctx) {
		if (m_DB == null && ctx != null) {
			m_DB = new DistributorDB(ctx);
			m_DB.open();
		}
		m_DB.openIfClosed();
		return m_DB;
	}

	public String getIMEI(Context ctx) {
		if (mstrIMEI.length() > 0) return mstrIMEI;
		try {
			if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.

			}
			mstrIMEI = ((TelephonyManager) (ctx.getSystemService(Context.TELEPHONY_SERVICE))).getDeviceId();
		}catch(Exception e){
			e.printStackTrace();
		}	
		return mstrIMEI;
	}

	public String getFooter(Context ctx){
		if(ctx == null) return "";
		
		String strResp = "";
		String strSetting = "";
		
		DistributorDB db = getDB(ctx);
		
		if(db.getAsBoolean(DistributorDB.SETTING_INCLURE_NOM_DU_MARCHAND)){
			strSetting =  db.getGeneralSetting(DistributorDB.SETTING_NOM_DU_MARCHAND);
			if(!strSetting.trim().equalsIgnoreCase(DistributorDB.STR_NF) && strSetting.trim().length() > 0){
				strResp += strSetting + "\n";
			}
		}
		if(db.getAsBoolean(DistributorDB.SETTING_INCLURE_NUMERO_DE_TELEPHONE)){
			strSetting =  db.getGeneralSetting(DistributorDB.SETTING_NUMERO_DE_TELEPHONE);
			if(!strSetting.trim().equalsIgnoreCase(DistributorDB.STR_NF) && strSetting.trim().length() > 0){
				strResp += strSetting + "\n";
			}
		}
		return strResp ;
	}
	
	public int getDelay(Context ctx){
		String strDelay = getDB(ctx).getGeneralSetting(DistributorDB.SETTING_DELAI_EXPIRATION).trim();
		int nDelay = 1000;
		try{
			nDelay = Integer.parseInt(strDelay);
			nDelay = (nDelay <= 5 && nDelay > 15)? 6: nDelay;// don't allow delays more than 15 seconds and less than 5
			
		}catch(Exception e){
			nDelay = 1000;
		}
		return nDelay;
	}
	public double getLastTrxAmount(Context ctx){
		String strLastTrx = "";
		double dLastAmount = 0.0;
		strLastTrx = getDB(ctx).getGeneralSetting(DistributorDB.SETTING_LAST_AMOUNT);
		
		try{
			dLastAmount = Double.parseDouble(strLastTrx);
		}catch(Exception e){
			dLastAmount = 0.0;
		}
		
		return dLastAmount;
	}
	
//	public int getTrxCounterFake(Context ctx){
//		int nRet = 0;
//		String strRet = "";
//		try{
//			strRet = getDB(ctx).getGeneralSetting(DistributorDB.SETTING_TRX_COUNTER_FAKE);
//			nRet = Integer.parseInt(strRet);
//		}catch(Exception e){
//			nRet = 0;
//			e.printStackTrace();
//		}
//		return nRet;
//	}
//	public void putTrxCounter(int nCounter, Context ctx){
//		if(nCounter < 0) nCounter = 0;
//		try{
//			getDB(ctx).updateGeneralSetting(DistributorDB.SETTING_TRX_COUNTER_FAKE, nCounter + "");
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}	

	public String getSDIDNew(){	
		int nPIN = 0;
		String strPINDB = "";
		
		nPIN = new Random().nextInt(9999);
		strPINDB = nPIN + "";

		if(strPINDB.length() == 1){
			strPINDB = "000" + strPINDB;
		}else if(strPINDB.length() == 2){
			strPINDB = "00" + strPINDB;
		}
		else if(strPINDB.length() == 3){
			strPINDB = "0" + strPINDB;
		}
		return strPINDB;
	}
	
}
