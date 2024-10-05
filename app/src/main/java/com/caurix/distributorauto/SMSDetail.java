package com.caurix.distributorauto;

import java.io.Serializable;

import android.database.Cursor;
import android.util.Log;

public class SMSDetail implements Serializable {
	public TRX_TYPE mTrxType;
	public String smsDateTime, // 1
			smsSDPhoneNumber, // 2
			smsTargetPhoneNumber, // 3
			smsSecret, // 4
			smsNotes, // 5 NOT INCLUDED IN WHERE
			smsTrxType, // 6
			smsSDID, // 7
			smsStatus; // 8
	public long smsAmount; // 9

	public String subDistributorName, subDistributorBalance;

	public SMSDetail() {
		mTrxType = TRX_TYPE.NA;
		smsStatus = smsSDID = smsDateTime = smsSDPhoneNumber = smsTargetPhoneNumber = smsSecret = smsNotes = smsTrxType = "";
		smsAmount = 0;
	}

	public SMSDetail(SMSDetail _d) {
		smsAmount = _d.smsAmount;
		smsDateTime = _d.smsDateTime;
		smsSDPhoneNumber = _d.smsSDPhoneNumber;
		smsSecret = _d.smsSecret;
		smsTargetPhoneNumber = _d.smsTargetPhoneNumber;
		smsTrxType = _d.smsTrxType;
		smsSDID = _d.smsSDID;
		smsStatus = _d.smsStatus;
	}

//	public final static String FORMAT_WHERE = "%s='%s' AND %s='%s' AND %s='%s' AND %s='%s' AND %s='%s' AND %s='%s' AND %s='%s' AND %s=%s"; 
	public final static String FORMAT_WHERE = "%s='%s' AND %s='%s' AND %s='%s' AND %s='%s' AND %s='%s' AND %s='%s'";// last
																																			// %s
																																			// for
																																			// double
																																			// value
																																			// amount

	public String getWhere() {
		return String.format(FORMAT_WHERE, DistributorDB.COLUMN_SMS_SDPHONENUMBER,
				smsSDPhoneNumber, DistributorDB.COLUMN_SMS_TARGET_PHONE,
				smsTargetPhoneNumber, DistributorDB.COLUMN_SMS_SECRET,
				smsSecret, DistributorDB.COLUMN_SMS_TRX_TYPE, smsTrxType,
				DistributorDB.COLUMN_SMS_SDID, smsSDID,
				DistributorDB.COLUMN_SMS_AMOUNT, smsAmount + "");
	}

	public boolean populateFromCursor(Cursor c) {
		if (c == null)
			return false;
		if (c.isClosed())
			return false;
		if (c.getCount() <= 0)
			return false;

		if (c.getCount() > 0) {
			if (c.isAfterLast() || c.isBeforeFirst())
				c.moveToFirst();
		}

		try {
			smsAmount = Long.parseLong(c.getString(c
					.getColumnIndex(DistributorDB.COLUMN_SMS_AMOUNT)));
			smsDateTime = c.getString(c
					.getColumnIndex(DistributorDB.COLUMN_SMS_DATETIME));
			smsSDPhoneNumber = c.getString(c
					.getColumnIndex(DistributorDB.COLUMN_SMS_SDPHONENUMBER));
			smsSecret = c.getString(c
					.getColumnIndex(DistributorDB.COLUMN_SMS_SECRET));
			smsTargetPhoneNumber = c.getString(c
					.getColumnIndex(DistributorDB.COLUMN_SMS_TARGET_PHONE));
			smsTrxType = c.getString(c
					.getColumnIndex(DistributorDB.COLUMN_SMS_TRX_TYPE));
			smsSDID = c.getString(c
					.getColumnIndex(DistributorDB.COLUMN_SMS_SDID));
			smsStatus = c.getString(c
					.getColumnIndex(DistributorDB.COLUMN_SMS_STATUS));

			try {
				subDistributorName = c.getString(c
						.getColumnIndex(DistributorDB.COLUMN_SUBD_SDNAME));
				subDistributorBalance = c
						.getString(c
								.getColumnIndex(DistributorDB.COLUMN_SUBD_CURRENTBALANCE));
			} catch (Exception e) {
				subDistributorName = "";
				subDistributorBalance = "0";
			}
		} catch (Exception excp) {
			Log.e(K.LOGTAG, excp.getMessage());
			return false;
		}
		return true;
	}

}
