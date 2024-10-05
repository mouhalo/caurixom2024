package com.caurix.duplicate.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DuplicateMessageChecker {

	//private static final int NO_OF_DAYS_SMS_SAVED = 3;

	Context mContext;
	DBHelper mDbHelper;

	public DuplicateMessageChecker(Context ctx) {
		mContext = ctx;
		mDbHelper = new DBHelper(mContext);
	}

	public void saveToDataBase(String sender, String client_number, String amount,String trx_type, String received_time, String sent_time,String is_duplicate) {

		mDbHelper.insertSmsData(sender, client_number, amount,trx_type,received_time,sent_time,is_duplicate);
	}

//	public boolean isDuplicateMessageMyDb(String sender, String client_number, String amount,String trx_type, String received_time, String sent_time) {
//
//		// Fetch Inbox SMS Message from App Local Database
////		int count = 0;
////		ArrayList<String> list = new ArrayList<String>();
////		Cursor cur = mDbHelper.getAllSmsData();
////		if (cur != null) {
////			if (cur.moveToFirst()) {
////				do {
////					list.add(cur.getString(1) + " " + cur.getString(2) + " " + cur.getString(3));
////				} while (cur.moveToNext());
////
////			}
////		}
//
//		ArrayList<String> list2 = new ArrayList<String>();
//		Cursor c = mDbHelper.getAllSmsData(sender, client_number, amount,trx_type,received_time,sent_time);
//		if (c != null) {
//			if (c.moveToFirst() && c.getCount() != 0) {
//				String v1 = c.getString(1);
//				String v2 = c.getString(2);
//				String v3 = c.getString(3);
//				Log.e("print_v1","->"+v1);
//				Log.e("print_v2","->"+v2);
//				Log.e("print_v3","->"+v3);
//				list2.add(c.getString(1) + " " + c.getString(2) + " " + c.getString(3) + " " + c.getString(4)+ " " + c.getString(5)+ " " + c.getString(6)+ " " + c.getString(7));
//				Log.e("print_list","->"+list2);
//				c.close();
//				return true;
//			}
//		}
//		return false;
//	}

	public boolean isDuplicateMessageSystem(String msgNew, String senderNew, String dateTimeMilliesNew) {

		// Create Inbox box URI
		Uri inboxURI = Uri.parse("content://sms/inbox");

		// List required columns
		String[] reqCols = new String[] { "date_sent", "address", "body" };

		// Get Content Resolver object, which will deal with Content Provider
		ContentResolver cr = mContext.getContentResolver();

		// Fetch Inbox SMS Message from Built-in Content Provider
		Cursor c = cr.query(inboxURI, reqCols, "date_sent = ? AND address = ? AND body = ?",
				new String[] { dateTimeMilliesNew, senderNew, dateTimeMilliesNew }, null);
		if (c != null) {
			if (c.moveToFirst()) {
				c.close();
				return true;
			}
		}
		return false;
	}

//	public void setDeleteDBAlarm() {
//
//		AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//		Intent intent = new Intent(mContext, AlarmReceiver.class);
//		PendingIntent alarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//		// Set the alarm to start at approximately 2:00 p.m.
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTimeInMillis(System.currentTimeMillis());
//		calendar.set(Calendar.HOUR_OF_DAY, 0);
//		calendar.set(Calendar.MINUTE, 5);
//
//		// With setInexactRepeating(), you have to use one of the AlarmManager
//		// interval
//		// constants--in this case, AlarmManager.INTERVAL_DAY.
//		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
//				alarmIntent);
//	}

//	public void deleteOldSMS() {
//
//		ArrayList<String> list = new ArrayList<String>();
//		Cursor cur = mDbHelper.getAllSmsData();
//		if (cur != null) {
//			if (cur.moveToFirst()) {
//				do {
//					list.add(cur.getString(1) + " " + cur.getString(2) + " " + cur.getString(3));
//				} while (cur.moveToNext());
//
//			}
//		}
//
//		// Current Time in Millies
//		long time = System.currentTimeMillis();
//
//		// Previous No of Days that SMS to be removed from database
//		long daysToRemoveMillis = 24 * 60 * 60 * 1000 * NO_OF_DAYS_SMS_SAVED;
//
//		long remainingTime = time - daysToRemoveMillis;
//
//		int prevC = mDbHelper.numberOfRows();
//
//		int d = mDbHelper.deleteSmsData(String.valueOf(remainingTime));
//
//		int C = mDbHelper.numberOfRows();
//	}

	

	public boolean isDuplicateMessage(String sender, String client_number, String amount,String trx_type, String received_time, String sent_time) {

		// Get date to compare and verify if the received time is in between last 3 days.
		Date newDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(newDate);
		cal.add(Calendar.DATE, -3);
		Date dateBefore3Days = cal.getTime();
		String bottomDate = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SS")
				.format(dateBefore3Days);

		ArrayList<String> list2 = new ArrayList<String>();
		Cursor c = mDbHelper.getAllSmsData(sender, client_number, amount,trx_type,received_time,sent_time, bottomDate);
		if (c != null) {
			if (c.moveToFirst() && c.getCount() != 0) {
				String v1 = c.getString(1);
				String v2 = c.getString(2);
				String v3 = c.getString(3);
				Log.e("print_v1","->"+v1);
				Log.e("print_v2","->"+v2);
				Log.e("print_v3","->"+v3);
				list2.add(c.getString(1) + " " + c.getString(2) + " " + c.getString(3) + " " + c.getString(4)+ " " + c.getString(5)+ " " + c.getString(6));
				Log.e("print_list","->"+list2);
				c.close();
				return true;
			}
		}
		return false;
	}

}
