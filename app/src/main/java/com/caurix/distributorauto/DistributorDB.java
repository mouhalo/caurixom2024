package com.caurix.distributorauto;

//import android.app.DownloadManager.Query;
//import static android.support.v4.app.NotificationCompatJellybean.TAG;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//TODO: The class shall be renamed to iSymphonyDBAdapter, since the DB has more than area settings now.
public class DistributorDB {

	//
	private static final String CREATE_GENERAL_SETTINGS = "CREATE TABLE IF NOT EXISTS general_settings(_id integer"
			+ " primary key autoincrement,SettingKey Text not null, SettingValue text);",

			CREATE_SMS_LOG = "CREATE TABLE IF NOT EXISTS SMS_LOG (_id INTEGER primary key autoincrement, "
					+ "smsDateTime TEXT, smsSDID TEXT not null, "
					+ "smsTargetPhoneNumber TEXT not null, "
					+ "smsSDPhoneNumber TEXT not null, "
					+ "smsAmount NUMERIC, smsStatus Text not null, "
					+ "smsSecret Text not null, "
					+ "smsTrxType Text not null,"
					+ "smsNotes Text);",

			CREATE_TRANSACTION_LOG = "CREATE TABLE IF NOT EXISTS trx_log (_id INTEGER primary key autoincrement, "
					+ "trxDateTime TEXT, trxTargetNumber TEXT not null, "
					+ "trxAmount NUMERIC, trxStatus Text not null, "
					+ "trxSDID Text, trxNotes Text, trxSyncStatus TINYINT,"
					+ "trxSDNumber Text not null, trxType Text not null,"
					+ "distributor_commission FLOAT, sub_distributor_commission FLOAT, total_commission FLOAT"
					+ ");",

			CREATE_SUBDISTRIBUTORS = "CREATE TABLE IF NOT EXISTS sub_dist("
					+ "_id INTEGER primary key autoincrement, "
					+ "SDID TEXT not null, "
					+ "regDateTime TEXT, Address1 TEXT, Address2 TEXT, contactPhone1 TEXT, contactPhone2 TEXT,"
					+ "email1 TEXT, email2 TEXT, transactionPhone TEXT not null UNIQUE, currentBalance NUMERIC not null, "
					+ "commPercentCashIn NUMERIC not null, commPercentCashOut NUMERIC not null, "
					+ "codeSecret TEXT not null, enabled NUMERIC not null, sdName TEXT not null, sdContactPersonName TEXT, sdPendingCommission NUMERIC"
					+ ");",

			CREATE_COMMISSION = "CREATE TABLE IF NOT EXISTS trx_commission ("
					+ "_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL, "
					+ "transaction_id TEXT, sub_distributor_phone TEXT, distributor_commission FLOAT, "
					+ "sub_distributor_commission FLOAT, total_commission FLOAT, summary_sent INTEGER DEFAULT 0, ussd_dialed INTEGER DEFAULT 0,"
					+ "created_at DATETIME,  updated_at DATETIME);";

	private SQLiteDatabase mDb;
	private static final String DATABASE_NAME = "distributor_db";
	private static final int DATABASE_VERSION = 2;

	public static final String TABLE_SETTINGS = "general_settings",
			TABLE_SMS_LOG = "SMS_LOG",
			TABLE_SUB_DIST = "sub_dist",
			SETTING_NOM_DU_MARCHAND = "nom_du_marchand",
			SETTING_NUMERO_DE_TELEPHONE = "numer_de_tel",
			SETTING_CODE_SECRET = "code_secret",
			SETTING_DELAI_EXPIRATION = "delai_expiration", // seconds
			SETTING_COMMISSION_THRESHOLD = "commission_threshold",
			SETTING_AMOUNT_THRESHOLD_FOR_UNATTENDED_MODE = "amount_threshold_for_unattended_mode",
			SETTING_UNATTENDED_MODE = "unattended_mode",

			SETTING_INCLURE_NOM_DU_MARCHAND = "incl_nom_du_marchand",
			SETTING_INCLURE_NUMERO_DE_TELEPHONE = "incl_numer_de_tel",
			SETTING_DEALER_DEALER_PHONE = "dealer_phone",
			SETTING_MY_UNIQUE_ID = "my_uid",
			SETTING_DIALED_USSD = "dialled_uusd",
			SETTING_LAST_TRX_TARGET_NUMBER = "trx_last_target",
			SETTING_LAST_TRX_STATUS = "trx_last_status",
			SETTING_LAST_AMOUNT = "trx_last_amount",
			SETTING_TRX_COUNTER_FAKE = "trx_counter_fake",
			COLUMN_GENERAL_SETTINGS_ID = "_id",
			COLUMN_GENERAL_SETTINGS_KEY = "SettingKey",
			COLUMN_GENERAL_SETTINGS_VALUE = "SettingValue",
			COLUMN_DYN_VALYEAR = "valYear",
			COLUMN_DYN_VALSUMYEAR = "valSumYear",
			COLUMN_DYN_VALMONTH = "valMonth",
			COLUMN_DYN_VALTOTALMONTH = "valTotalMonth",
			COLUMN_DYN_VALDAY = "valDay",
			COLUMN_DYN_VALTOTALDAY = "valTotalDay",
			STR_NF = "0",

			TABLE_TRANSACTION_LOG = "trx_log",
			COLUMN_TRX_LOG_ID = "_id",
			COLUMN_TRX_LOG_DATE_TIME = "trxDateTime",
			COLUMN_TRX_LOG_TARGET_NUMBER = "trxTargetNumber",
			COLUMN_TRX_LOG_AMOUNT = "trxAmount",
			COLUMN_TRX_LOG_STATUS = "trxStatus",
			COLUMN_TRX_LOG_SDID = "trxSDID",
			COLUMN_TRX_LOG_SDNUMBER = "trxSDNumber",
			COLUMN_TRX_LOG_TRXTYPE = "trxType",
			COLUMN_TRX_LOG_NOTES = "trxNotes",
			COLUMN_TRX_SYNC_STATUS = "trxSyncStatus",

			TABLE_COMMISSION = "trx_commission",
			COLUMN_SMS_DATETIME = "smsDateTime",
			COLUMN_SMS_SDPHONENUMBER = "smsSDPhoneNumber",
			COLUMN_SMS_TARGET_PHONE = "smsTargetPhoneNumber",
			COLUMN_SMS_AMOUNT = "smsAmount",
			COLUMN_SMS_SECRET = "smsSecret",
			COLUMN_SMS_NOTES = "smsNotes",
			COLUMN_SMS_TRX_TYPE = "smsTrxType",
			COLUMN_SMS_SDID = "smsSDID",
			COLUMN_SMS_STATUS = "smsStatus",

			COLUMN_SUBD_SDID = "SDID",
			COLUMN_SUBD_REGDATETIME = "regDateTime",
			COLUMN_SUBD_ADDRESS1 = "Address1",
			COLUMN_SUBD_ADDRESS2 = "Address2",
			COLUMN_SUBD_CONTACTPHONE1 = "contactPhone1",
			COLUMN_SUBD_CONTACTPHONE2 = "contactPhone2",
			COLUMN_SUBD_EMAIL1 = "email1",
			COLUMN_SUBD_EMAIL2 = "email2",
			COLUMN_SUBD_TRANSACTIONPHONE = "transactionPhone",
			COLUMN_SUBD_CURRENTBALANCE = "currentBalance",
			COLUMN_SUBD_COMMPERCENTCASHIN = "commPercentCashIn",
			COLUMN_SUBD_COMMPERCENTCASHOUT = "commPercentCashOut",
			COLUMN_SUBD_CODE_SCRET = "codeSecret",
			COLUMN_SUBD_ENABLED = "enabled",
			COLUMN_SUBD_SDNAME = "sdName",
			COLUMN_SUBD_CONTACTPERSON = "sdContactPersonName",
			COLUMN_SUBD_PENDINGCOMMISSION = "sdPendingCommission",

			COLUMN_COMMISSION_ID = "_id",
			COLUMN_COMMISSION_TRANSACTION_ID = "transaction_id",
			COLUMN_COMMISSION_SUB_DISTRIBUTOR_PHONE = "sub_distributor_phone",
			COLUMN_COMMISSION_DISTRIBUTOR_COMMISSION = "distributor_commission",
			COLUMN_COMMISSION_SUB_DISTRIBUTOR_COMMISSION = "sub_distributor_commission",
			COLUMN_COMMISSION_TOTAL_COMMISSION = "total_commission",
			COLUMN_COMMISSION_SUMMARY_SENT = "summary_sent",
			COLUMN_COMMISSION_USSD_DIALED = "ussd_dialed",
			COLUMN_COMMISSION_CREATED_AT = "created_at",
			COLUMN_COMMISSION_UPDATED_AT = "updated_at";
	/*
	 * 
	 * "smsDateTime TEXT, smsSDID TEXT not null, " +
	 * "smsSDPhoneNumber TEXT not null, " +
	 * "smsAmount REAL, smsStatus Text not null, " + "smsSecret Text not null, "
	 * + "smsNotes
	 */

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(K.LOGTAG, "Creating DB");
			db.execSQL(CREATE_GENERAL_SETTINGS);
			db.execSQL(CREATE_SMS_LOG);
			db.execSQL(CREATE_TRANSACTION_LOG);
			db.execSQL(CREATE_SUBDISTRIBUTORS);
			db.execSQL(CREATE_COMMISSION);
		}

		 @Override
		 public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		     // If you need to add a column
		     if (newVersion > oldVersion) {
		     }
		 }
		 
		/*@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(K.LOGTAG, "Updating DB");
			db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_SETTINGS));
			onCreate(db);
		}*/
	}// DatabaseHelper

	private DatabaseHelper mDbHelper;
	public void checkPostedOnServerColumn() {
		if(isFieldExist("trx_log","postedOnServer") == false)
		{
		mDb.execSQL("ALTER TABLE 'trx_log' ADD COLUMN 'postedOnServer' INTEGER NOT NULL  DEFAULT -1");
   	 }
	}

	public void checkPostedConfirmationOnServerColumn() {
		if(isFieldExist("trx_log","postedConfirmationOnServer") == false)
		{
			mDb.execSQL("ALTER TABLE 'trx_log' ADD COLUMN 'postedConfirmationOnServer' INTEGER NOT NULL  DEFAULT -1");
		}
	}

	public boolean isFieldExist(String tableName, String fieldName)
	{
	    boolean isExist = false;
	    if(this.mDbHelper == null)
	    {
	    	this.mDbHelper = new DatabaseHelper(mCtx);
			mDb = mDbHelper.getWritableDatabase();
	    }
	    
	   //SQLiteDatabase db = this.getWritableDatabase();
	    Cursor res = mDb.rawQuery("PRAGMA table_info(" + tableName + ")", null);


	    if (res.moveToFirst()) {
	        do {
	            int value = res.getColumnIndex("name");
	            if(value != -1 && res.getString(value).equals(fieldName))
	            {
	                isExist = true;
	            }
	            // Add book to books

	        } while (res.moveToNext());
	    }

	    return isExist;
	}
	public DistributorDB(Context ctx) {
		this.mCtx = ctx;
	}

	public void close() {
		mDb.close();
	}

	public DistributorDB open() throws SQLException {
		this.mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		if (getGeneralSetting(SETTING_NOM_DU_MARCHAND).equals(STR_NF)) {
			createFirstTimeData();
		}
		checkPostedOnServerColumn();
		checkPostedConfirmationOnServerColumn();
		return this;
	}

	private void createFirstTimeData() {
		/*
		 * General settings table The table will be created on install time, and
		 * specific keys will be created on first run. Later on, we'll only
		 * update these keys with new values.
		 */
		putGeneralSetting(SETTING_NOM_DU_MARCHAND, "Supermarche");
		putGeneralSetting(SETTING_NUMERO_DE_TELEPHONE, "1234");
		putGeneralSetting(SETTING_DELAI_EXPIRATION, "25");
		putGeneralSetting(SETTING_CODE_SECRET, "1217");
		putGeneralSetting(SETTING_COMMISSION_THRESHOLD, "100");
		putGeneralSetting(SETTING_AMOUNT_THRESHOLD_FOR_UNATTENDED_MODE, "100");
		putGeneralSetting(SETTING_UNATTENDED_MODE, "0");

		putGeneralSetting(SETTING_DEALER_DEALER_PHONE, "03419797120");
		putGeneralSetting(SETTING_MY_UNIQUE_ID, "11111");

		putGeneralSetting(SETTING_INCLURE_NOM_DU_MARCHAND, "1");
		putGeneralSetting(SETTING_INCLURE_NUMERO_DE_TELEPHONE, "1");

		putGeneralSetting(SETTING_DIALED_USSD, "0");

		putGeneralSetting(SETTING_LAST_TRX_TARGET_NUMBER, STR_NF);
		putGeneralSetting(SETTING_LAST_TRX_STATUS, STR_NF);
		putGeneralSetting(SETTING_LAST_AMOUNT, STR_NF);
		putGeneralSetting(SETTING_TRX_COUNTER_FAKE, "0");
	}

	private void putGeneralSetting(String strKey, String strValue) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_GENERAL_SETTINGS_KEY, strKey);
		initialValues.put(COLUMN_GENERAL_SETTINGS_VALUE, strValue);
		mDb.insert(TABLE_SETTINGS, null, initialValues);
	}

	public boolean updateGeneralSetting(String settingKey, String settingValue) {
		if (settingKey == null || settingValue == null)
			return false;
		if (settingKey.trim().length() <= 0)
			return false;
		if (settingValue.trim().length() <= 0)
			return false;

		ContentValues cv = new ContentValues();
		cv.put(COLUMN_GENERAL_SETTINGS_VALUE, settingValue);
		Log.i("GSDB",
				settingKey
						+ " - "
						+ settingValue
						+ " - "
						+ mDb.update(TABLE_SETTINGS, cv, String.format(
								"%s='%s'", COLUMN_GENERAL_SETTINGS_KEY,
								settingKey), null) + "");
		return false;
	}

	/**
	 * 
	 * Read string value of a general setting by key, returns empty string if
	 * key not found.
	 * 
	 * @param strKey
	 * @return
	 */
	public String getGeneralSetting(String strKey) {
		String strRet = DistributorDB.STR_NF;
		Cursor mCursor = mDb.query(TABLE_SETTINGS, new String[] {
				DistributorDB.COLUMN_GENERAL_SETTINGS_KEY,
				DistributorDB.COLUMN_GENERAL_SETTINGS_VALUE },
				COLUMN_GENERAL_SETTINGS_KEY + "='" + strKey + "'", null, null,
				null, null);

		if (mCursor != null) {
			if (!mCursor.moveToFirst()) {
				mCursor.close();
				return strRet;
			}
		} else {
			return strRet;
		}

		try {
			strRet = mCursor
					.getString(mCursor
							.getColumnIndexOrThrow(DistributorDB.COLUMN_GENERAL_SETTINGS_VALUE));
		} catch (Exception ex) {
			strRet = DistributorDB.STR_NF;
		}

		mCursor.close();
		return strRet;
	}

	public boolean getAsBoolean(String strGeneralSetting) {
		return (getGeneralSetting(strGeneralSetting).trim()
				.equalsIgnoreCase("1"));
	}

	public void updateFromBoolean(String strGeneralSetting, boolean bVal) {
		updateGeneralSetting(strGeneralSetting, (bVal) ? "1" : "0");
	}

	public boolean openIfClosed() {
		if (mDb != null) {
			if (!mDb.isOpen()) {
				open();
			}
		}
		return true;
	}

	public long putTrx(long trxDateTime, String trxTargetNumber,
			long trxAmount, TRX_STATUS trxStatus, String trxSDID,
			String trxSDNumber, String trxType, String trxNotes) {

		if (trxDateTime <= 0)
			return -1;
		if (trxTargetNumber == null)
			return -2;
		if (trxTargetNumber.length() <= 0)
			return -2;
		if (trxAmount <= 0)
			return -3;
		if (trxStatus == null)
			return -4;

		trxNotes = (trxNotes == null) ? "" : trxNotes;

		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_TRX_LOG_DATE_TIME, new SimpleDateFormat(
				K.DATEFORMAT).format(trxDateTime));
		initialValues.put(COLUMN_TRX_LOG_TARGET_NUMBER, trxTargetNumber);// don't
																			// need
																			// to
																			// trim
																			// this
																			// value,
																			// its
																			// an
																			// internal
																			// static
																			// constant.
		initialValues.put(COLUMN_TRX_LOG_AMOUNT, trxAmount);
		initialValues.put(COLUMN_TRX_LOG_SDID, trxSDID);
		initialValues.put(COLUMN_TRX_LOG_STATUS, trxStatus.toString());
		initialValues.put(COLUMN_TRX_LOG_SDNUMBER, trxSDNumber);
		initialValues.put(COLUMN_TRX_LOG_TRXTYPE, trxType);
		initialValues.put(COLUMN_TRX_LOG_NOTES, trxNotes);
		initialValues.put(COLUMN_TRX_SYNC_STATUS, 0);

		return mDb.insert(TABLE_TRANSACTION_LOG, null, initialValues);
	}

	public long putCommission(String subDistributorPhone,
			float distributorCommission, float subDistributorCommission,
			float totalCommission) {

		ContentValues initialValues = new ContentValues();

		initialValues.put(COLUMN_COMMISSION_TRANSACTION_ID, "");
		initialValues.put(COLUMN_COMMISSION_SUB_DISTRIBUTOR_PHONE,
				subDistributorPhone);// don't
		initialValues.put(COLUMN_COMMISSION_DISTRIBUTOR_COMMISSION,
				distributorCommission);
		initialValues.put(COLUMN_COMMISSION_SUB_DISTRIBUTOR_COMMISSION,
				subDistributorCommission);
		initialValues.put(COLUMN_COMMISSION_TOTAL_COMMISSION, totalCommission);
		initialValues.put(COLUMN_COMMISSION_SUMMARY_SENT, 0);
		initialValues.put(COLUMN_COMMISSION_USSD_DIALED, 0);
		initialValues.put(COLUMN_COMMISSION_CREATED_AT, new SimpleDateFormat(
				K.DATEFORMAT).format(new Date()));
		initialValues.put(COLUMN_COMMISSION_UPDATED_AT, new SimpleDateFormat(
				K.DATEFORMAT).format(new Date()));

		return mDb.insert(TABLE_COMMISSION, null, initialValues);
	}

	public Cursor getPastDayCommission() {
		String strQuery = "SELECT sub_distributor_phone,strftime('%Y-%m-%d',datetime('now','-1 day', 'localtime')) as for_date,count(*) as trx_count, sum(sub_distributor_commission) as commission_sum FROM trx_commission where strftime('%Y-%m-%d',created_at) =  strftime('%Y-%m-%d',datetime('now','-1 day', 'localtime'))  and summary_sent = 0 group by sub_distributor_phone";
		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public void setSummarySent() {
		String strQuery = "update trx_commission set summary_sent = 1 where strftime(\"%Y-%m-%d\",created_at) =  strftime(\"%Y-%m-%d\",datetime('now','-1 day', 'localtime'))  and summary_sent = 0";
		mDb.execSQL(strQuery);
	}

	public Cursor getTotalCommissionTally() {
		String strQuery = "select sum(total_commission) as commission_tally,* from trx_commission where ussd_dialed = 0";
		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public void setUSSDDialed() {
		String strQuery = "update trx_commission set ussd_dialed = 1";
		mDb.execSQL(strQuery);
	}


	public void setPostedOnServer(int postedOnServer, String trx_id) {
		String strQuery = "update trx_log set postedOnServer =" +postedOnServer + " WHERE _id = " + trx_id;
		mDb.execSQL(strQuery);
	}

	public void setPostedConfirmationOnServer(int postedConfirmationOnServer, String trx_id) {
		String strQuery = "update trx_log set postedConfirmationOnServer =" +postedConfirmationOnServer + " WHERE _id = " + trx_id;
		mDb.execSQL(strQuery);
	}
	
	public long putSMS(SMSDetail _smsIn) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_SMS_AMOUNT, _smsIn.smsAmount);
		initialValues.put(COLUMN_SMS_DATETIME, _smsIn.smsDateTime);
		initialValues.put(COLUMN_SMS_SDPHONENUMBER, _smsIn.smsSDPhoneNumber);
		initialValues.put(COLUMN_SMS_SECRET, _smsIn.smsSecret);
		initialValues.put(COLUMN_SMS_TARGET_PHONE, _smsIn.smsTargetPhoneNumber);
		initialValues.put(COLUMN_SMS_TRX_TYPE, _smsIn.smsTrxType);
		initialValues.put(COLUMN_SMS_SDID, _smsIn.smsSDID);
		initialValues.put(COLUMN_SMS_STATUS, _smsIn.smsStatus);

		return mDb.insert(TABLE_SMS_LOG, null, initialValues);
	}

	/**
	 * Update only transaction status, other parameters are used in WHERE clause
	 * only
	 * 
	 * @param trxTargetNumber
	 * @param trxPrevStatus
	 * @param trxNewStatus
	 * @param trxAmount
	 * @return
	 */
	public long updateTrx(String trxTargetNumberWhere,
			TRX_STATUS trxPrevStatusWhere, TRX_STATUS trxNewStatus,
			long trxAmountWhere, String strTrxID) {
		/*
		 * ContentValues contentvalue=new ContentValues();
		 * contentvalue.put(COLUMN_EVENT_QUESTION_QUESTION, question.trim());
		 * if(question == null || question.trim().length() <= 0) return false;
		 * return mDB.update(TABLE_QUESTION, contentvalue, "_id=" + questionid,
		 * null)>0;
		 */
		String strWhere = "";

		ContentValues contentvalue = new ContentValues();
		contentvalue.put(COLUMN_TRX_LOG_STATUS, trxNewStatus.toString());
		contentvalue.put(COLUMN_TRX_LOG_NOTES, strTrxID);
		if (trxAmountWhere != 0) {
			strWhere = COLUMN_TRX_LOG_TARGET_NUMBER + "='"
					+ trxTargetNumberWhere + "' AND " + COLUMN_TRX_LOG_AMOUNT
					+ " = " + trxAmountWhere + " AND " + COLUMN_TRX_LOG_STATUS
					+ " = '" + trxPrevStatusWhere + "'";
		} else {
			strWhere = COLUMN_TRX_LOG_TARGET_NUMBER + "='"
					+ trxTargetNumberWhere + "' AND " + COLUMN_TRX_LOG_STATUS
					+ " = '" + trxPrevStatusWhere + "'";
		}

		Log.i(K.LOGTAG, strWhere);
		return mDb.update(TABLE_TRANSACTION_LOG, contentvalue, strWhere, null);
		// COLUMN_TRX_LOG_TARGET_NUMBER + "='" + trxTargetNumberWhere + "' AND "
		// +
		// COLUMN_TRX_LOG_AMOUNT + " = " + trxAmountWhere + " AND " +
		// COLUMN_TRX_LOG_STATUS + " = '" + trxPrevStatusWhere + "'" , null);
	}


	public long updateTrxByID(String trxTargetNumberWhere,
						  TRX_STATUS trxPrevStatusWhere, TRX_STATUS trxNewStatus,
						  long trxAmountWhere, String strTrxID,int id) {
		/*
		 * ContentValues contentvalue=new ContentValues();
		 * contentvalue.put(COLUMN_EVENT_QUESTION_QUESTION, question.trim());
		 * if(question == null || question.trim().length() <= 0) return false;
		 * return mDB.update(TABLE_QUESTION, contentvalue, "_id=" + questionid,
		 * null)>0;
		 */
		String strWhere = "";

		ContentValues contentvalue = new ContentValues();
		contentvalue.put(COLUMN_TRX_LOG_STATUS, trxNewStatus.toString());
		contentvalue.put(COLUMN_TRX_LOG_NOTES, strTrxID);
		if (trxAmountWhere != 0) {
			if(id!=-1){

				strWhere = COLUMN_TRX_LOG_TARGET_NUMBER + "='" + trxTargetNumberWhere +
						"' AND " + COLUMN_TRX_LOG_AMOUNT + "='" + trxAmountWhere +
						"' AND " + COLUMN_TRX_LOG_ID + "='" + id +
						"' AND " + COLUMN_TRX_LOG_STATUS
						+ " = '" + trxPrevStatusWhere + "'";
			}else{
				strWhere = COLUMN_TRX_LOG_TARGET_NUMBER + "='"
						+ trxTargetNumberWhere + "' AND " + COLUMN_TRX_LOG_AMOUNT
						+ " = " + trxAmountWhere + " AND " + COLUMN_TRX_LOG_STATUS
						+ " = '" + trxPrevStatusWhere + "'";
			}

		} else {
			if(id!=-1){
				strWhere = COLUMN_TRX_LOG_TARGET_NUMBER + "='" + trxTargetNumberWhere+
				"' AND " + COLUMN_TRX_LOG_ID + " ='" + id +
						 "' AND " + COLUMN_TRX_LOG_STATUS + " = '" + trxPrevStatusWhere + "'";
			}else{
				strWhere = COLUMN_TRX_LOG_TARGET_NUMBER + "='"
						+ trxTargetNumberWhere + "' AND " + COLUMN_TRX_LOG_STATUS
						+ " = '" + trxPrevStatusWhere + "'";
			}

		}

		Log.i(K.LOGTAG, strWhere);
		return mDb.update(TABLE_TRANSACTION_LOG, contentvalue, strWhere, null);
		// COLUMN_TRX_LOG_TARGET_NUMBER + "='" + trxTargetNumberWhere + "' AND "
		// +
		// COLUMN_TRX_LOG_AMOUNT + " = " + trxAmountWhere + " AND " +
		// COLUMN_TRX_LOG_STATUS + " = '" + trxPrevStatusWhere + "'" , null);
	}
	public long updateTrxStatus(String transactionId, TRX_STATUS status) {

		String strWhere = "";

		ContentValues contentvalue = new ContentValues();
		contentvalue.put(COLUMN_TRX_LOG_STATUS, status.toString());

		strWhere = " _id = " + transactionId;

		Log.i(K.LOGTAG, strWhere);
		return mDb.update(TABLE_TRANSACTION_LOG, contentvalue, strWhere, null);
	}

	public long updateTrxSyncStatus(String transactionId) {

		String strWhere = "";

		ContentValues contentvalue = new ContentValues();
		contentvalue.put(COLUMN_TRX_SYNC_STATUS, 1);

		strWhere = " _id = " + transactionId;

		Log.i(K.LOGTAG, strWhere);
		return mDb.update(TABLE_TRANSACTION_LOG, contentvalue, strWhere, null);
	}

	public Cursor getTransactionByStatus(String status) {
		String strQuery = "SELECT trx_log.*,sub_dist.sdname FROM trx_log inner join sub_dist on (trx_log.trxSDNumber = sub_dist.transactionPhone) where trx_log.trxStatus='"
				+ status + "' order by trx_log._id desc";
		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public Cursor getTransactionByID(String id) {
		String strQuery = "SELECT trx_log.*,sub_dist.sdname FROM trx_log inner join sub_dist on (trx_log.trxSDNumber = sub_dist.transactionPhone) where trx_log._id='"
				+ id + "' order by trx_log._id desc";
		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public Cursor getFinalizedTransactions() {
		String strQuery = "SELECT * FROM trx_log where trxSyncStatus != 1 AND trxStatus IN ('"
				+ TRX_STATUS.OK
				+ "', '"
				+ TRX_STATUS.REJET
				+ "', '"
				+ TRX_STATUS.ECHEC + "')";
		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}


	public Cursor getAllTransactions() {
		String strQuery = "SELECT trx_log.*, sub_dist.sdname FROM "
				+ "trx_log inner join sub_dist "
				+ "on (trx_log.trxSDNumber = sub_dist.transactionPhone) where trxStatus IN ('"
				+ TRX_STATUS.OK + "', '" + TRX_STATUS.REJET + "', '"
				+ TRX_STATUS.ECHEC + "')";

		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public Cursor getAllPendingTransactions(String clientNo, long lAmount) {
		String strQuery = "SELECT trx_log.*, sub_dist.sdname FROM "
				+ "trx_log inner join sub_dist "
				+ "on (trx_log.trxSDNumber = sub_dist.transactionPhone) where trxStatus IN ('"
				+ TRX_STATUS.PENDING + "', '" + TRX_STATUS.REJET + "', '"
				+ TRX_STATUS.ECHEC + "') AND trx_log.trxAmount = "+lAmount +" AND trx_log.trxTargetNumber = "+ clientNo;

		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public Cursor getAlltransactionsByType(String type) {

// 		String sql = "select * from trx_log where trxType = "+"CREDIT"  ;

 		String sql = "select * from " +  TABLE_TRANSACTION_LOG + " where "
				+  COLUMN_TRX_LOG_TRXTYPE + " = '" + type + "'"     ;

		Cursor cursor = mDb.rawQuery(sql, null);

		return cursor;
 	}

	public Cursor getAlltransactions() {

// 		String sql = "select * from trx_log where trxType = "+"CREDIT"  ;

		String sql = "select * from " +  TABLE_TRANSACTION_LOG;

		Cursor cursor = mDb.rawQuery(sql, null);

		return cursor;
	}

	public Cursor getPendingPostOnServerTransactions() {
		String strQuery = "SELECT trx_log.*, sub_dist.sdname FROM "
				+ "trx_log inner join sub_dist "
				+ "on (trx_log.trxSDNumber = sub_dist.transactionPhone) where postedOnServer = 0 OR postedOnServer = '0' ";
		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public Cursor getOKPostNotOnServer() {
		String strQuery = "SELECT trx_log.*, sub_dist.sdname, sub_dist.transactionPhone, sub_dist.currentBalance FROM "
				+ "trx_log inner join sub_dist "
				+ "on (trx_log.trxSDNumber = sub_dist.transactionPhone) where postedConfirmationOnServer = -1  AND trxStatus = 'OK'";
		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public long deleteTrx() {
		return 0;
	}

	final String mPendingSMSQuery = "SELECT distinct sl.*, sd.sdName, sd.currentBalance FROM SMS_LOG sl inner join sub_dist sd on (sl.smsSDPhoneNumber = sd.transactionPhone) WHERE sl.smsStatus='PENDING' ORDER BY sl.smsDateTime";

	public Cursor getPendingSMS() {
		Cursor cRet = null;

		try {
			cRet = mDb.rawQuery(mPendingSMSQuery, null);
		} catch (Exception exc) {
			Log.e(K.LOGTAG, exc.toString());
		}
		return cRet;
	}

	public Cursor getHistory(String strKey) {
		String strQuery = "SELECT trx_log.*, sub_dist.sdname FROM  trx_log inner join sub_dist on (trx_log.trxSDNumber = sub_dist.transactionPhone) ORDER BY trxDateTime DESC";
		if (strKey != null) {
			if (strKey.length() > 0) {
				strQuery = "SELECT trxDateTime, trxTargetNumber, trxAmount, trxStatus, trxNotes, sub_dist.sdname FROM  trx_log inner join sub_dist on (trx_log.trxSDNumber = sub_dist.transactionPhone)"
						+ " WHERE strftime('%Y-%m-%d',trxDateTime)='"
						+ strKey
						+ "' ORDER BY trxDateTime DESC";
			}
		}
		Log.d(K.LOGTAG, strQuery);
		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public Cursor getYearsHistory() {
		String strQuery = "SELECT strftime('%Y',trxDateTime ) as timeduration, sum(trxAmount) as amount FROM trx_log where trxStatus = 'OK' OR trxStatus = 'REJET' group by timeduration ORDER BY trxDateTime DESC";

		Log.d(K.LOGTAG, strQuery);
		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public Cursor getMonthHistory(String year) {
		String strQuery = "SELECT strftime('%m',trxDateTime ) as timeduration, sum(trxAmount) as amount FROM  trx_log where (trxStatus = 'OK' OR trxStatus = 'REJET') AND strftime('%Y',trxDateTime ) = '"
				+ year + "' group by timeduration ORDER BY trxDateTime DESC";

		Log.d(K.LOGTAG, strQuery);
		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public Cursor getDayHistory(String year, String month) {
		String strQuery = "SELECT strftime('%d',trxDateTime ) as timeduration, sum(trxAmount) as amount FROM  trx_log where (trxStatus = 'OK' OR trxStatus = 'REJET') AND strftime('%Y',trxDateTime ) = '"
				+ year
				+ "' and strftime('%m',trxDateTime ) = '"
				+ month
				+ "' group by timeduration ORDER BY trxDateTime DESC";

		Log.d(K.LOGTAG, strQuery);
		android.database.Cursor cursor = mDb.rawQuery(strQuery, null);
		return cursor;
	}

	public int updateSMSStatus(SMSDetail _smsDetail, TRX_STATUS trxS) {
		int nRet = 0;
		try {

			/*
			 * ContentValues cv = new ContentValues();
			 * cv.put(COLUMN_GENERAL_SETTINGS_VALUE, settingValue);
			 * Log.i("GSDB", settingKey + " - " + settingValue + " - " +
			 * mDb.update( TABLE_SETTINGS, cv, String.format("%s='%s'",
			 * COLUMN_GENERAL_SETTINGS_KEY, settingKey), null) + "");
			 */
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_SMS_STATUS, trxS.toString());
			nRet = mDb.update(TABLE_SMS_LOG, cv, _smsDetail.getWhere(), null);

		} catch (Exception e) {
			Log.e(K.LOGTAG, e.getMessage());
		}
		return nRet;
	}

	/**
	 * 
	 * @return unique years in database like 2013, 2014, 2015.
	 */
	public String[] getYears() {
		String[] strYearsDistinct = new String[] { "2014" };
		ArrayList<String> aryYears = new ArrayList<String>(1);
		String strQuery = "SELECT DISTINCT strftime('%Y',trxDateTime) as valYear from trx_log";

		android.database.Cursor cYears = mDb.rawQuery(strQuery, null);

		if (cYears != null) {
			if (!cYears.moveToFirst())// failed to move to first
			{
				cYears.close();
				return strYearsDistinct;
			}
		} else {
			return strYearsDistinct;
		}

		try {
			do {
				aryYears.add(cYears.getString(0));// we know there will be only
													// one column on index 0
			} while (cYears.moveToNext());

			strYearsDistinct = new String[aryYears.size()];
			strYearsDistinct = aryYears.toArray(strYearsDistinct);
		} catch (Exception ex) {
			Log.e(K.LOGTAG, ex.getMessage());
		}

		cYears.close();

		return strYearsDistinct;
	}

	public double getTotalByYear(String strYear) {

		double dRet = 0.0;
		if (strYear == null)
			return dRet;
		if (strYear.length() <= 0 || strYear.length() < 4)
			return dRet;
		if (strYear.length() > 4)
			return dRet;

		try {
			Integer.parseInt(strYear);
		} catch (Exception exc) {
			return dRet;
		}

		String strQuery = String
				.format("SELECT strftime('%%Y', trxDateTime) as valYear, SUM(trxAmount) as valSumYear FROM trx_log WHERE valYear = '%s'  AND trxStatus='SENT'  GROUP BY valYear",
						strYear);

		android.database.Cursor mCursor = mDb.rawQuery(strQuery, null);

		if (mCursor != null) {
			if (!mCursor.moveToFirst()) {
				mCursor.close();
				return dRet;
			}
		} else {
			return dRet;
		}

		try {
			dRet = mCursor
					.getDouble(mCursor
							.getColumnIndexOrThrow(DistributorDB.COLUMN_DYN_VALSUMYEAR));
		} catch (Exception ex) {
			dRet = 0.0;
		}

		mCursor.close();

		return dRet;
	}

	public Cursor getMonthsTotalByYear(String strYear) {

		if (strYear == null)
			return null;
		if (strYear.length() <= 0 || strYear.length() < 4)
			return null;
		if (strYear.length() > 4)
			return null;

		try {
			Integer.parseInt(strYear);
		} catch (Exception exc) {
			return null;
		}

		String strQuery = String
				.format("SELECT strftime('%%m', trxDateTime) as valMonth, SUM(trxAmount) as valTotalMonth FROM trx_log WHERE strftime('%%Y', trxDateTime)='%s'  AND trxStatus='SENT' GROUP BY valMonth",
						strYear);

		android.database.Cursor mCursor = mDb.rawQuery(strQuery, null);

		if (mCursor != null) {
			if (!mCursor.moveToFirst()) {
				mCursor.close();
				return null;
			}
		} else {
			return null;
		}
		return mCursor;
	}

	/*
	 * SELECT strftime('%d', trxDateTime) as valDay, SUM(trxAmount) as
	 * valTotalDay FROM trx_log WHERE strftime('%Y', trxDateTime)='2014' AND
	 * strftime('%m', trxDateTime) ='02' GROUP BY valDay
	 */

	public Cursor getDaysTotalByMonthAndYear(String strYear, String strMonth) {

		if (strYear == null)
			return null;
		if (strMonth == null)
			return null;

		if (strYear.length() <= 0 || strYear.length() < 4)
			return null;
		if (strYear.length() > 4)
			return null;

		if (strMonth.length() <= 0)
			return null;

		try {
			Integer.parseInt(strYear);
		} catch (Exception exc) {
			return null;
		}
		try {
			Integer.parseInt(strMonth);
		} catch (Exception ex) {
			return null;
		}

		String strQuery = String
				.format("SELECT strftime('%%d', trxDateTime) as valDay, SUM(trxAmount) as valTotalDay FROM trx_log WHERE strftime('%%Y', trxDateTime)='%s' AND strftime('%%m', trxDateTime) ='%s' AND trxStatus='SENT' GROUP BY valDay",
						strYear, strMonth);

		android.database.Cursor mCursor = mDb.rawQuery(strQuery, null);

		if (mCursor != null) {
			if (!mCursor.moveToFirst()) {
				mCursor.close();
				return null;
			}
		} else {
			return null;
		}
		return mCursor;
	}

	public TrxDetail getTrx(String strTrxFrom, TRX_STATUS trx_status,
			double dAmount) {
		String strQuery = String
				.format("SELECT trx_log.*, sub_dist.sdname from trx_log "
						+ "inner join sub_dist on (trx_log.trxSDNumber = sub_dist.transactionPhone) "
						+ "WHERE trx_log.trxTargetNumber='%s' AND trx_log.trxStatus='%s' AND trx_log.trxAmount=%s order by trx_log._id desc",
						strTrxFrom, trx_status.toString(), dAmount);
		TrxDetail objTrxDetail = new TrxDetail();
		Cursor c = null;

		try {

			c = mDb.rawQuery(strQuery, null);
			if (c != null) {
				if (!c.moveToFirst()) {
					c.close();
					return objTrxDetail;
				}
			} else { // c== null
				return objTrxDetail;
			}

			objTrxDetail.id = c.getString(c.getColumnIndex(COLUMN_TRX_LOG_ID));
			objTrxDetail.trxDateTime = c.getString(c
					.getColumnIndex(COLUMN_TRX_LOG_DATE_TIME));
			objTrxDetail.trxAmount = c.getDouble(c
					.getColumnIndex(COLUMN_TRX_LOG_AMOUNT));
			objTrxDetail.trxSDID = c.getString(c
					.getColumnIndex(COLUMN_TRX_LOG_SDID));
			objTrxDetail.trxSDName = c.getString(c
					.getColumnIndex(COLUMN_SUBD_SDNAME));
			objTrxDetail.trxSDPhoneNumber = c.getString(c
					.getColumnIndex(COLUMN_TRX_LOG_SDNUMBER));
			objTrxDetail.trxStatus = c.getString(c
					.getColumnIndex(COLUMN_TRX_LOG_STATUS));
			objTrxDetail.trxTargetPhoneNumber = c.getString(c
					.getColumnIndex(COLUMN_TRX_LOG_TARGET_NUMBER));
			objTrxDetail.mTrxType = TRX_TYPE.valueOf(c.getString(c
					.getColumnIndex(COLUMN_TRX_LOG_TRXTYPE)));
			objTrxDetail.trxNotes = c.getString(c
					.getColumnIndex(COLUMN_TRX_LOG_NOTES));

		} catch (Exception e) {
			Log.e(K.LOGTAG, e.getMessage());
			e.printStackTrace();
		}
		return objTrxDetail;
	}

	public long putSubDistributor(String _sdid, String _regDateTime,
			String _address1, String _address2, String _contactPhone1,
			String _contactPhone2, String _email1, String _email2,
			String _transactionPhone, long _currentbalance,
			double _comPercentCashIn, double _comPercentCashOut,
			String _codeSecret, int _enabled, String _sdName,
			String _contactPerson) {
		Log.d("TAG", "putSubDistributorBalance: "+_currentbalance);

		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_SUBD_SDID, _sdid);
		initialValues.put(COLUMN_SUBD_REGDATETIME, _regDateTime);
		initialValues.put(COLUMN_SUBD_ADDRESS1, _address1);
		initialValues.put(COLUMN_SUBD_ADDRESS2, _address2);
		initialValues.put(COLUMN_SUBD_CONTACTPHONE1, _contactPhone1);
		initialValues.put(COLUMN_SUBD_CONTACTPHONE2, _contactPhone2);
		initialValues.put(COLUMN_SUBD_EMAIL1, _email1);
		initialValues.put(COLUMN_SUBD_EMAIL2, _email2);
		initialValues.put(COLUMN_SUBD_TRANSACTIONPHONE, _transactionPhone);
		initialValues.put(COLUMN_SUBD_CURRENTBALANCE, _currentbalance);
		initialValues.put(COLUMN_SUBD_COMMPERCENTCASHIN, _comPercentCashIn);
		initialValues.put(COLUMN_SUBD_COMMPERCENTCASHOUT, _comPercentCashOut);
		initialValues.put(COLUMN_SUBD_CODE_SCRET, _codeSecret);
		initialValues.put(COLUMN_SUBD_ENABLED, _enabled);
		initialValues.put(COLUMN_SUBD_SDNAME, _sdName);
		initialValues.put(COLUMN_SUBD_CONTACTPERSON, _contactPerson);

		return mDb.insert(TABLE_SUB_DIST, null, initialValues);
	}

	public int updateSubDistributor(String _selectedPhone,
			String _transactionPhone, String _address1, String _address2,
			String _contactPhone1, String _contactPhone2, String _email1,
			String _email2, long currentBalance, double _comPercentCashIn,
			double _comPercentCashOut, String _codeSecret, String _sdName,
			String _contactPerson) {
		Log.d("TAG", "setSubDistributorBalance: "+currentBalance);
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_SUBD_ADDRESS1, _address1);
		initialValues.put(COLUMN_SUBD_ADDRESS2, _address2);
		initialValues.put(COLUMN_SUBD_CONTACTPHONE1, _contactPhone1);
		initialValues.put(COLUMN_SUBD_CONTACTPHONE2, _contactPhone2);
		initialValues.put(COLUMN_SUBD_EMAIL1, _email1);
		initialValues.put(COLUMN_SUBD_EMAIL2, _email2);
		initialValues.put(COLUMN_SUBD_TRANSACTIONPHONE, _transactionPhone);
		initialValues.put(COLUMN_SUBD_CURRENTBALANCE, currentBalance);
		initialValues.put(COLUMN_SUBD_COMMPERCENTCASHIN, _comPercentCashIn);
		initialValues.put(COLUMN_SUBD_COMMPERCENTCASHOUT, _comPercentCashOut);
		initialValues.put(COLUMN_SUBD_CODE_SCRET, _codeSecret);
		initialValues.put(COLUMN_SUBD_SDNAME, _sdName);
		initialValues.put(COLUMN_SUBD_CONTACTPERSON, _contactPerson);

		return mDb.update(TABLE_SUB_DIST, initialValues,
				COLUMN_SUBD_TRANSACTIONPHONE + "='" + _selectedPhone + "'",
				null);
	}

	public void updateSubDistributorBalance(String subDistributorPhone,
			long transactionAmount, TRX_TYPE trxType) {
		String updateQuery = "update " + TABLE_SUB_DIST
				+ " set currentBalance=currentBalance";
		updateQuery += (trxType == TRX_TYPE.CASHIN) ? "-" : "+";
		updateQuery += transactionAmount;
		updateQuery += " where transactionPhone='" + subDistributorPhone + "'";

		mDb.execSQL(updateQuery);
	}

	public void addPendingCommission(String _sdid, double _add) {
		double dPendCommissionOld = getPendingCommission(_sdid);

		ContentValues cv = new ContentValues();

		cv.put(COLUMN_SUBD_PENDINGCOMMISSION, dPendCommissionOld + _add);

		Log.i(K.LOGTAG,
				_sdid
						+ " - "
						+ _add
						+ " - "
						+ mDb.update(TABLE_SUB_DIST, cv, String
								.format("%s='%s'",
										COLUMN_SUBD_PENDINGCOMMISSION, _sdid),
								null) + "");

	}

	public void deleteSubDistributor(String sdPhoneNo) {

		mDb.delete(TABLE_SUB_DIST, String.format("%s='%s'",
				COLUMN_SUBD_TRANSACTIONPHONE, sdPhoneNo), null);

	}

	public void lessPendingCommission(String _sdid, double _less) {
		double dPendCommissionOld = getPendingCommission(_sdid);

		ContentValues cv = new ContentValues();
		cv.put(COLUMN_SUBD_PENDINGCOMMISSION, dPendCommissionOld - _less);

		Log.i(K.LOGTAG,
				_sdid
						+ " - "
						+ _less
						+ " - "
						+ mDb.update(TABLE_SUB_DIST, cv, String
								.format("%s='%s'",
										COLUMN_SUBD_PENDINGCOMMISSION, _sdid),
								null) + "");

	}

	public double getPendingCommission(String _sdid) {
		double dRet = 0.0;
		Cursor mCursor = mDb.query(TABLE_SUB_DIST,
				new String[] { DistributorDB.COLUMN_SUBD_PENDINGCOMMISSION },
				COLUMN_SUBD_SDID + "='" + _sdid + "'", null, null, null, null);

		if (mCursor != null) {
			if (!mCursor.moveToFirst()) {
				mCursor.close();
				return dRet;
			}
		} else {
			return dRet;
		}

		try {
			dRet = mCursor
					.getDouble(mCursor
							.getColumnIndexOrThrow(DistributorDB.COLUMN_SUBD_PENDINGCOMMISSION));
		} catch (Exception ex) {
			Log.e(K.LOGTAG, ex.getMessage());
		}

		mCursor.close();
		return dRet;
	}

	public String getSDSecret(String _sdid) {
		String sRet = STR_NF;
		Cursor mCursor = mDb.query(TABLE_SUB_DIST,
				new String[] { DistributorDB.COLUMN_SUBD_CODE_SCRET },
				COLUMN_SUBD_SDID + "='" + _sdid + "'", null, null, null, null);

		if (mCursor != null) {
			if (!mCursor.moveToFirst()) {
				mCursor.close();
				return sRet;
			}
		} else {
			return sRet;
		}

		try {
			sRet = mCursor
					.getString(mCursor
							.getColumnIndexOrThrow(DistributorDB.COLUMN_SUBD_CODE_SCRET));
		} catch (Exception ex) {
			Log.e(K.LOGTAG, ex.getMessage());
		}

		mCursor.close();
		return sRet;
	}

	final String mSubDistributorsQuery = "SELECT sdName, currentBalance, transactionPhone, SDID, codeSecret FROM sub_dist ORDER BY sdName";

	public Cursor getSubDistributors() {
		Cursor cRet = null;
		try {
			cRet = mDb.rawQuery(mSubDistributorsQuery, null);
		} catch (Exception exc) {
			Log.e(K.LOGTAG, exc.toString());
		}
		return cRet;
	}

	public Cursor getSubDistributorByPhone(String phoneNum) {
		Cursor cRet = null;
		try {
			cRet = mDb.query(TABLE_SUB_DIST, new String[] { COLUMN_SUBD_SDID,
					COLUMN_SUBD_REGDATETIME, COLUMN_SUBD_ADDRESS1,
					COLUMN_SUBD_ADDRESS2, COLUMN_SUBD_CONTACTPHONE1,
					COLUMN_SUBD_CONTACTPHONE2, COLUMN_SUBD_EMAIL1,
					COLUMN_SUBD_EMAIL2, COLUMN_SUBD_TRANSACTIONPHONE,
					COLUMN_SUBD_CURRENTBALANCE, COLUMN_SUBD_COMMPERCENTCASHIN,
					COLUMN_SUBD_COMMPERCENTCASHOUT, COLUMN_SUBD_CODE_SCRET,
					COLUMN_SUBD_ENABLED, COLUMN_SUBD_SDNAME,
					COLUMN_SUBD_CONTACTPERSON, COLUMN_SUBD_PENDINGCOMMISSION },
					COLUMN_SUBD_TRANSACTIONPHONE + "='" + phoneNum + "'", null,
					null, null, null);
			Log.e(K.LOGTAG, "Counter : " + cRet.getCount());

		} catch (Exception exc) {
			Log.e(K.LOGTAG, exc.toString());
		}
		return cRet;
	}

	public String getSubDistributorBalance(String subDistributorPhone) {
		String currentBalance = "";
		String strQuery = "SELECT currentBalance From sub_dist "
				+ "where transactionPhone='" + subDistributorPhone + "'";
		Log.d(K.LOGTAG, strQuery);
		Cursor cData = mDb.rawQuery(strQuery, null);
		if (cData.moveToFirst()) {
			do {
				currentBalance = cData.getString(cData
						.getColumnIndex("currentBalance"));
				Log.e("currentBalance", ":" + currentBalance);
				// do what ever you want here
			} while (cData.moveToNext());
		}
		cData.close();
		return currentBalance;
	}

	public void updateSubDistributorBalanceOnly(String subDistributorPhone,
			long transactionAmount) {
		String updateQuery = "update " + TABLE_SUB_DIST
				+ " set currentBalance = ";
		updateQuery += "7250.00";
		updateQuery += " where transactionPhone='" + subDistributorPhone + "'";

		mDb.execSQL(updateQuery);
	}

	public void updateBalanceFromService(String number, String balance) {
		// TODO Auto-generated method stub

		String updateQuery = "update " + TABLE_SUB_DIST
				+ " set currentBalance = ";
		updateQuery += "7250.00";
		updateQuery += " where transactionPhone='" + number + "' OR contactPhone1 = '"
				+ number + "'";

		Log.d(K.LOGTAG,"qUERY FOR UPDATING BALANCE"+ updateQuery);
		
		mDb.execSQL(updateQuery);


	}
	
}