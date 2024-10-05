package com.caurix.distributorauto;
import java.io.Serializable;

import android.util.Log;

public class TrxDetail implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public TRX_TYPE mTrxType;
	public String
			id,
			trxDateTime,			//1
			trxSDPhoneNumber,		//2
			trxTargetPhoneNumber,	//3		
			trxNotes,				//5 NOT INCLUDED IN WHERE clause
			smsTrxType,				//6
			trxSDID, 				//7
			trxSDName,
			trxStatus;				//8
	public	double trxAmount;		//9

	public TrxDetail(){
		mTrxType = TRX_TYPE.NA;
		trxDateTime = trxSDPhoneNumber = trxTargetPhoneNumber =
				trxNotes = smsTrxType = trxSDID = trxStatus = "";
		trxAmount = 0.0;
	}
	
	public String genSubDistributorSMS(){		
		String strRet = null;
		try{
			strRet = String.format("%s\n%s\n%s\n%s\n%s\n%s\n", smsTrxType, trxDateTime, trxTargetPhoneNumber, trxStatus, trxSDPhoneNumber, trxAmount);
		}catch(Exception e){
			Log.e(K.LOGTAG, e.getMessage());
		}		
		return strRet; //String.format("%s\n%s\n%s\n%s\n%s\n%s", trxDateTime, trxTargetPhoneNumber, smsTrxType, trxStatus, trxSDPhoneNumber, trxAmount);
	}

	public String getContentForOKSMS(){
		String strRet = null;
		try{
			strRet = "Transaction reussie pour Client " + String.format("%s\n%s\n", trxTargetPhoneNumber, trxAmount) +" Confirmation: " + trxNotes ;
		}catch(Exception e){
			Log.e(K.LOGTAG, e.getMessage());
		}
		return strRet; //String.format("%s\n%s\n%s\n%s\n%s\n%s", trxDateTime, trxTargetPhoneNumber, smsTrxType, trxStatus, trxSDPhoneNumber, trxAmount);
	}
}
