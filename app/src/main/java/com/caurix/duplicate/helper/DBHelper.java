package com.caurix.duplicate.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "MySmsDatabase";
	public static final String TABLE_NAME = "sms_log";
	public static final String ID = "_id";
	public static final String SENDER = "sender";
	public static final String CLIENT_NUMBER = "client_number";
	public static final String AMOUNT = "amount";
	public static final String TRX_TYPE = "trx_type";
	public static final String RECEIVED_TIME = "received_time";
	public static final String SENT_TIME = "sent_time";
	public static final String IS_DUPLICATE = "is_duplicate";
	
	public static final int DB_VERSION = 2;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION);
		this.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ID + " INTEGER primary key AUTOINCREMENT, " + SENDER
				+ " VARCHAR NOT NULL  DEFAULT 0, " + CLIENT_NUMBER + " VARCHAR NOT NULL  DEFAULT 0, " + AMOUNT + " VARCHAR NOT NULL  DEFAULT 0, "+ TRX_TYPE +" VARCHAR NOT NULL  DEFAULT CASHIN, "+ RECEIVED_TIME +" VARCHAR NOT NULL  DEFAULT 0, "+ SENT_TIME +" VARCHAR NOT NULL  DEFAULT 0, "+ IS_DUPLICATE +" VARCHAR NOT NULL  DEFAULT 0)";
		db.execSQL(query);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
	
	
	
	

	public boolean insertSmsData(String sender, String client_number, String amount,String trx_type, String received_time, String sent_time,String is_duplicate ) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(SENDER, sender);
		contentValues.put(CLIENT_NUMBER, client_number);
		contentValues.put(AMOUNT, amount);
		contentValues.put(TRX_TYPE, trx_type);
		contentValues.put(RECEIVED_TIME, received_time);
		contentValues.put(SENT_TIME, sent_time);
		contentValues.put(IS_DUPLICATE, is_duplicate);
		db.insert(TABLE_NAME, null, contentValues);
		return true;
	}

	public Cursor getData(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor res = db.rawQuery("select * from " + TABLE_NAME + " where " + ID + "=" + id + "", null);
		return res;
	}

	public int numberOfRows() {
		SQLiteDatabase db = this.getWritableDatabase();
		int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
		return numRows;
	}

	public int deleteSmsData(String sent_time) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_NAME, SENT_TIME + " <= ? ", new String[] { sent_time });

	}

	public Cursor getAllSmsData(String sender, String client_number, String amount,String trx_type ,String received_time, String sent_time, String bottom_date ) {
		String query = "select * from " + TABLE_NAME + " where " 
	   + SENDER + " ='" + sender + "' AND " 
	   + CLIENT_NUMBER + " = '"+ client_number + "' AND "
	   + AMOUNT + " = '"+ amount + "' AND "
	   + TRX_TYPE + " = '"+ trx_type + "' AND "
	   + RECEIVED_TIME + " <> '"+ received_time + "' AND ("
	   + RECEIVED_TIME + " BETWEEN '"+ received_time + "' AND '"+ bottom_date + "') AND "
	   + SENT_TIME + " = '"+ sent_time + "' ";
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery(query, null);

		return c;
	}

	public Cursor getAllSmsData() {
		String query = "select * from " + TABLE_NAME;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery(query, null);

		return c;
	}
	public boolean importDatabase(String dbPath ,String toPath) throws IOException {

		// Close the SQLiteOpenHelper so it will commit the created empty
		// database to internal storage.
		close();
		File newDb = new File(dbPath);
		File oldDb = new File(toPath);

		if (newDb.exists()) {
			FileUtils.copyFile(new FileInputStream(newDb), new FileOutputStream(oldDb));
			// Access the copied database so SQLiteHelper will cache it and mark
			// it as created.
			getWritableDatabase().close();
			return true;
		}
		return false;
	}

}
class FileUtils {
	/**
	 * Creates the specified <code>toFile</code> as a byte for byte copy of the
	 * <code>fromFile</code>. If <code>toFile</code> already exists, then it
	 * will be replaced with a copy of <code>fromFile</code>. The name and path
	 * of <code>toFile</code> will be that of <code>toFile</code>.<br/>
	 * <br/>
	 * <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by
	 * this function.</i>
	 *
	 * @param fromFile
	 *            - FileInputStream for the file to copy from.
	 * @param toFile
	 *            - FileInputStream for the file to copy to.
	 */
	public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
		FileChannel fromChannel = null;
		FileChannel toChannel = null;
		try {
			fromChannel = fromFile.getChannel();
			toChannel = toFile.getChannel();
			fromChannel.transferTo(0, fromChannel.size(), toChannel);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				if (fromChannel != null) {
					fromChannel.close();
				}
			} finally {
				if (toChannel != null) {
					toChannel.close();
				}
			}
		}
	}
}
