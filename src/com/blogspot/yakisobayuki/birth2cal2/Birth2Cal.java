package com.blogspot.yakisobayuki.birth2cal2;

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
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		tabHost.setCurrentTab(0);

		Intent intent;

		// 誕生日順
		intent = new Intent().setClass(this, TabBirthday.class);
		View childview1 = new CustomTabContentView(this,
				getString(R.string.birth_sort),
				android.R.drawable.ic_menu_my_calendar);
		spec = tabHost.newTabSpec("tab1").setIndicator(childview1)
				.setContent(intent);
		tabHost.addTab(spec);

		// 年齢順
		intent = new Intent().setClass(this, TabAge.class);
		View childview2 = new CustomTabContentView(this,
				getString(R.string.age_sort),
				android.R.drawable.ic_menu_sort_by_size);
		spec = tabHost.newTabSpec("tab2").setIndicator(childview2)
				.setContent(intent);
		tabHost.addTab(spec);

		// 名前順
		intent = new Intent().setClass(this, TabName.class);
		View childview3 = new CustomTabContentView(this,
				getString(R.string.name_sort),
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
		menu.add(0, Menu.FIRST, Menu.NONE, getString(R.string.op_cal));
		menu.add(0, Menu.FIRST + 1, Menu.NONE, getString(R.string.op_setting));

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