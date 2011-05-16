package com.blogspot.yakisobayuki.birth2cal2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

public class SelectCal extends Activity {
	// カレンダー表示用
	Activity mActivity;
	Context mContext;
	String[] mCalendar_list;
	String[] mCalendar_ID;
	String result;
	int mButton;
	int mButton_y;
	AlertDialog mAlertDialog;

	public SelectCal(Activity activity, Context context) {
		this.mActivity = activity;
		this.mContext = context;
	}

	public class CalendarList {
		private String[] calendar;
		private String[] calId;

		public CalendarList(int num) {
			this.calendar = new String[num];
			this.calId = new String[num];
		}

		public String[] getCalendarName() {
			return calendar;
		}

		public void setCalendarName(String calendar, int num) {
			this.calendar[num] = calendar;
		}

		public String[] getCalendarId() {
			return calId;
		}

		public void setCalendarId(String calId, int num) {
			this.calId[num] = calId;
		}
	}

	// カレンダーのリストの作成
	public void CalendarList() {
		mCalendar_list = null;
		mCalendar_ID = null;

		String AUTHORITY = null;

		// SDKバージョンでURIの記述を変更
		final int sdkVersion = Build.VERSION.SDK_INT;
		if (sdkVersion < 8) {
			AUTHORITY = "calendar";
		} else {
			AUTHORITY = "com.android.calendar";
		}

		final Uri calendars = Uri
				.parse("content://" + AUTHORITY + "/calendars");

		final String[] projection = new String[] { "_id", "displayName" };
		// access_level=700はオーナー権限、writeが出来るカレンダーを取得。
		final Cursor clist = mContext.getContentResolver().query(calendars,
				projection, "access_level = 700", null, null);

		// 生成して、リストの数分String型の要素数を持つように指示
		CalendarList item = new CalendarList(clist.getCount());

		if (clist != null) {
			try {
				int count = 0;

				while (clist.moveToNext()) {

					// カレンダー名とIDを格納
					String calendar = clist.getString(clist
							.getColumnIndex("displayName"));
					String calendarId = clist.getString(clist
							.getColumnIndex("_id"));
					item.setCalendarName(calendar, count);
					item.setCalendarId(calendarId, count);

					count++;
				}
			} finally {
				clist.close();
			}
		}
		mCalendar_list = item.getCalendarName();
		mCalendar_ID = item.getCalendarId();
	}

	public void CalendarListView() {
		// カレンダーのリストを作成する
		CalendarList();

		// プリファレンスでデータを取得
		SharedPreferences pref = mContext.getSharedPreferences("cal_list", 0);
		int button_id = pref.getInt("calendar_list_num_v112", -1);
		String cal_name = pref.getString("calendar_list_name", "");

		// 以前のボタン名とボタン番号の整合性確認。合っていない場合は未選択状態にする。
		if (button_id >= 0) {
			try {
				if (cal_name.equals(mCalendar_list[button_id])) {
				} else {
					button_id = -1;
				}
			} catch (Exception e) {
				button_id = -1;
			}
		}

		// 選択するカレンダーを明示的に初期化
		mButton = button_id;

		// ダイアログの表示
		mAlertDialog = new AlertDialog.Builder(mActivity)
				.setTitle(mActivity.getString(R.string.select_cal))
				.setSingleChoiceItems(mCalendar_list, button_id,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								mButton = which;
							}
						})
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						new AlertDialog.Builder(mActivity);

						if (mButton != -1) {
							// 選択したボタンの名称と番号をプリファレンスで保存。
							result = mCalendar_ID[mButton];
							SharedPreferences pref = mContext
									.getSharedPreferences("cal_list", 0);
							Editor e = pref.edit();
							e.putString("calendar_list_name",
									mCalendar_list[mButton]);
							e.putString("calendar_list_id", result);
							e.putInt("calendar_list_num_v112", mButton);
							e.commit();
						}
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								new AlertDialog.Builder(mActivity);
							}
						}).show();
	}
}
