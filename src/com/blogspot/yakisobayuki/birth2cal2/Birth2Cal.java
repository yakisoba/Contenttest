package com.blogspot.yakisobayuki.birth2cal2;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class Birth2Cal extends TabActivity implements TabHost.TabContentFactory {
	// カレンダー表示用
	String[] mCalendar_list;
	String[] mCalendar_ID;
	String result;
	int mButton;
	int mButton_y;

	// 今日の日付取得
	final Calendar mCalendar = Calendar.getInstance();
	final int mYear = mCalendar.get(Calendar.YEAR);
	final int mMonth = mCalendar.get(Calendar.MONTH);
	final int mDay = mCalendar.get(Calendar.DAY_OF_MONTH);

	public class CalendarList {
		private String[] calendar;
		private String[] calId;

		public void setCalendarList(int num) {
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		tabHost.setCurrentTab(0);

		Intent intent;

		intent = new Intent().setClass(this, TabBirthday.class);
		View childview1 = new CustomTabContentView(this, "誕生日順",
				android.R.drawable.ic_menu_my_calendar);
		spec = tabHost.newTabSpec("tab1").setIndicator(childview1)
				.setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, TabAge.class);
		View childview2 = new CustomTabContentView(this, "年齢順",
				android.R.drawable.ic_menu_sort_by_size);
		spec = tabHost.newTabSpec("tab2").setIndicator(childview2)
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, TabName.class);
		View childview3 = new CustomTabContentView(this, "名前順",
				android.R.drawable.ic_menu_sort_alphabetically);
		spec = tabHost.newTabSpec("tab3").setIndicator(childview3)
				.setContent(intent);
		tabHost.addTab(spec);

	}

	// タブ表示を変更する
	public class CustomTabContentView extends FrameLayout {
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		public CustomTabContentView(Context context) {
			super(context);
		}

		public CustomTabContentView(Context context, String title, int icon) {
			this(context);
			View childview = inflater.inflate(R.layout.tabwidget, null);
			TextView tv = (TextView) childview.findViewById(R.id.textview);
			ImageView iv = (ImageView) childview.findViewById(R.id.imageview);
			tv.setText(title);
			iv.setImageResource(icon);
			addView(childview);
		}
	}

	@Override
	public View createTabContent(String tag) {
		return null;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);

		// menuの追加
		menu.add(0, Menu.FIRST, Menu.NONE, "カレンダー選択");
		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;

		switch (item.getItemId()) {
		case 1:
			// カレンダーのリストを作成する
			CalendarList();

			// プリファレンスでデータを取得
			SharedPreferences pref = getSharedPreferences("cal_list",
					MODE_PRIVATE);
			int button_id = pref.getInt("calendar_list_num_v112", -1);
			String cal_name = pref.getString("calendar_list_name", "");

			// 以前のボタン名とボタン番号の整合性確認。合っていない場合は未選択状態にする。
			if (button_id >= 0) {
				if (cal_name.equals(mCalendar_list[button_id])) {
				} else {
					button_id = -1;
				}
			}

			// 選択するカレンダーを明示的に初期化
			mButton = button_id;

			// ダイアログの表示
			new AlertDialog.Builder(this)
					.setTitle("カレンダー選択")
					.setSingleChoiceItems(mCalendar_list, button_id,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mButton = which;
								}
							})
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									new AlertDialog.Builder(Birth2Cal.this);

									if (mButton != -1) {
										// 選択したボタンの名称と番号をプリファレンスで保存。
										result = mCalendar_ID[mButton];
										SharedPreferences pref = getSharedPreferences(
												"cal_list", MODE_PRIVATE);
										Editor e = pref.edit();
										e.putString("calendar_list_name",
												mCalendar_list[mButton]);
										e.putString("calendar_list_id", result);
										e.putInt("calendar_list_num_v112",
												mButton);
										e.commit();
									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									new AlertDialog.Builder(Birth2Cal.this);
								}
							}).show();
			break;

		}

		return ret;
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
		final Cursor clist = managedQuery(calendars, projection,
				"access_level = 700", null, null);

		// 生成して、リストの数分String型の要素数を持つように指示
		CalendarList item = new CalendarList();
		item.setCalendarList(clist.getCount());

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
}