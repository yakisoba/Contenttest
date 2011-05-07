package com.blogspot.yakisobayuki.birth2cal2;

import java.util.Calendar;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
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
		menu.add(0, Menu.FIRST + 1, Menu.NONE, "設定");

		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;

		switch (item.getItemId()) {
		case 1:
			// カレンダーのリスト作成および表示
			SelectCal selectCal = new SelectCal(this, getApplicationContext());
			selectCal.CalendarListView();

			break;

		case 2:
			// メニューの表示
			Intent intent = new Intent(Birth2Cal.this, Setting.class);
			intent.setAction(Intent.ACTION_VIEW);
			startActivity(intent);

			break;
		}

		return ret;
	}
}