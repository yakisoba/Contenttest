package com.test.Contenttest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.Data;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactPickerActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	private List<ContactsStatus> mList = null;
	private ContactAdapter mAdapter = null;
	String[] Calendar_ID;
	String result;

	CheckBox chk01;
	private CheckBox check_full;
	private Button button_import;

	public class ContactsStatus {
		private String displayName;
		private String birthday;
		private boolean checkflg;

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getBirth() {
			return birthday;
		}

		public void setBirth(String birthday) {
			this.birthday = birthday;
		}

		public boolean getCheckFlag() {
			return checkflg;
		}

		public void setCheckFlag(boolean checkflg) {
			this.checkflg = checkflg;
		}
	}

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
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main);

		/* 連絡帳から名前と誕生日を取得して格納 */
		ListView listView = (ListView) findViewById(R.id.list);
		fillData();

		mAdapter = new ContactAdapter(this, R.layout.contactsname, mList);
		listView.setAdapter(mAdapter);

		/* 「全てチェック」ボタンが押された時の処理 */
		check_full = (CheckBox) findViewById(R.id.CheckBox_full);
		check_full.setOnClickListener(this);

		/* 「取り込み」ボタンが押された時の処理 */
		button_import = (Button) findViewById(R.id.button_import);
		button_import.setOnClickListener(this);

	}

	/* メニューを表示 */
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, Menu.NONE, "カレンダー選択");
		return ret;
	}

	// オプションメニューアイテムが選択された時に呼び出されます
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;

		switch (item.getItemId()) {
		case 1:

			String[] Calendar_list = null;
			Calendar_ID = null;

			final String[] projection = new String[] { "_id", "displayName" };
			final Uri calendars = Uri
					.parse("content://com.android.calendar/calendars");

			final Cursor clist = managedQuery(calendars, projection,
					"access_level = 700", null, null);

			CalendarList item1 = new CalendarList();
			item1.setCalendarList(clist.getCount());

			if (clist != null) {
				try {
					int count = 0;

					while (clist.moveToNext()) {

						String calendar = clist.getString(clist
								.getColumnIndex("displayName"));
						String calendarId = clist.getString(clist
								.getColumnIndex("_id"));

						item1.setCalendarName(calendar, count);
						item1.setCalendarId(calendarId, count);

						Log.d("Testoutput", calendarId + " : " + calendar);

						count++;
					}
				} finally {
					clist.close();
				}
			}
			Log.d("Testoutput", "カレンダーリスト作成完了");

			Calendar_list = item1.getCalendarName();
			Calendar_ID = item1.getCalendarId();

			new AlertDialog.Builder(this)
					.setTitle("カレンダー選択")
					.setSingleChoiceItems(Calendar_list, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Toast.makeText(
											ContactPickerActivity.this,
											Integer.toString(which)
													+ "を選択しました。",
											Toast.LENGTH_SHORT).show();
									result = Calendar_ID[which];
								}
							})
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* OKボタンをクリックした時の処理 */
									// new AlertDialog.Builder(
									// ContactPickerActivity.this);
									Log.d("Testoutput", result + "番が選択された");
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* Cancel ボタンをクリックした時の処理 */
									new AlertDialog.Builder(
											ContactPickerActivity.this);
								}
							}).show();
		}
		return ret;
	}

	public class ContactAdapter extends ArrayAdapter<ContactsStatus> {
		private LayoutInflater inflater;

		public ContactAdapter(Context context, int textViewResourceId,
				List<ContactsStatus> items) {
			super(context, textViewResourceId, items);

			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// ビューを受け取る
			View view = inflater.inflate(R.layout.contactsname, null);

			// 表示すべきデータの取得
			final ContactsStatus item = getItem(position);

			if (item != null) {
				// ビューにユーザ名をセット
				TextView displayName = (TextView) view
						.findViewById(R.id.ContactsName);
				displayName.setTypeface(Typeface.DEFAULT_BOLD);
				displayName.setText(item.getDisplayName());
				// ビューに誕生日をセット
				TextView birthday = (TextView) view.findViewById(R.id.Birthday);
				birthday.setText(item.getBirth());
				// ビューにチェックボックスのON/OFFをセット
				final CheckBox chk01 = (CheckBox) view
						.findViewById(R.id.CheckBox);
				chk01.setChecked(item.getCheckFlag());

				chk01.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (chk01.isChecked() == true) {
							item.setCheckFlag(true);
							Log.d("Testoutput", "1つチェックした");
						} else {
							item.setCheckFlag(false);
							Log.d("Testoutput", "1つチェック外した");
						}
					}
				});
			}
			return view;
		}
	}

	// コンタクトデータを取得して表示
	private void fillData() {
		this.mList = new ArrayList<ContactsStatus>();

		Cursor c = managedQuery(Data.CONTENT_URI, new String[] {
				Event.DISPLAY_NAME, Event.DATA }, Data.MIMETYPE + "=? AND "
				+ Event.TYPE + "=?", new String[] { Event.CONTENT_ITEM_TYPE,
				String.valueOf(Event.TYPE_BIRTHDAY) }, Data.DISPLAY_NAME);

		// 名前と誕生日のindexを取得して、出力の際に参照する
		int nameIndex = c.getColumnIndex(Event.DISPLAY_NAME);
		int birthIndex = c.getColumnIndex(Event.DATA);

		if (c != null) {
			try {

				while (c.moveToNext()) {

					ContactsStatus item = new ContactsStatus();

					String displayName = c.getString(nameIndex);
					String date = c.getString(birthIndex);

					item.setDisplayName(displayName);
					item.setBirth(date);
					mList.add(item);
				}
			} finally {
				c.close();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v == check_full) {
			Log.d("Testoutput", "チェックOn/OFFが押された！");

			// 全チェックボックのON/OFF切替＋ボタンおした時の表示名を変更
			if (check_full.isChecked() == true) {
				for (ContactsStatus status : mList) {
					status.setCheckFlag(true);
				}
				CheckBox b = (CheckBox) v;
				b.setText("チェック解除");
			} else if (check_full.isChecked() == false) {
				for (ContactsStatus status : mList) {
					status.setCheckFlag(false);
				}
				CheckBox b = (CheckBox) v;
				b.setText("全てチェック");
			}
			mAdapter.notifyDataSetChanged();

		} else if (v == button_import) {
			/* チェックされたものを取り込む処理を実施 */
			Log.d("Testoutput", "取り込みが押された！");

			int Completion_count = 0;

			/* 該当カレンダーがあるか捜索＋カレンダー作成 */
			String calId = result;
			Log.d("Testoutput", "該当のカレンダーID：" + Integer.parseInt(calId));

			if (Integer.parseInt(calId) != 0) {

				/* リストから情報取得 */
				for (ContactsStatus status : mList) {
					if (status.getCheckFlag() == true) {
						Log.d("Testoutput",
								status.getBirth() + "\t"
										+ status.getDisplayName());

						CreateEvent(status.getBirth(), status.getDisplayName(),
								Integer.parseInt(calId));

						Completion_count++;
					}
				}
				if (Completion_count == 0) {
					AlertDialog.Builder dlg;
					dlg = new AlertDialog.Builder(ContactPickerActivity.this);
					dlg.setTitle("error!!");
					dlg.setMessage("何もチェックされていません");
					dlg.show();
				} else {
					AlertDialog.Builder dlg;
					dlg = new AlertDialog.Builder(ContactPickerActivity.this);
					dlg.setTitle("complete!!");
					dlg.setMessage("カレンダーへ登録完了！");
					dlg.show();
				}
			} else {
				AlertDialog.Builder dlg;
				dlg = new AlertDialog.Builder(ContactPickerActivity.this);
				dlg.setTitle("error!!");
				dlg.setMessage("カレンダーが登録されてません");
				dlg.show();
			}
		} else {
			Log.d("Testoutput", "何か押された！");
		}
	}

	private void CreateEvent(String birthday, String ContactName, int calId) {
		long startLongDay;
		long endLongDay;
		int eventcheck = 0;

		Uri events = Uri.parse("content://com.android.calendar/events");
		ContentValues values = new ContentValues();
		ContentResolver cr = this.getContentResolver();

		startLongDay = this.getLongDay(birthday, 0);
		endLongDay = this.getLongDay(birthday, 1);

		/* 日本とアメリカで誤差15時間あるっぽいので */
		long day_check = startLongDay - 54000000;

		/* すでに登録されているときは登録しない */
		String[] projection = new String[] { "title", "dtstart" };
		Cursor c3 = managedQuery(events, projection, "calendar_id =" + calId
				+ " AND dtstart =" + day_check, null, null);

		while (c3.moveToNext()) {
			String titl = c3.getString(c3.getColumnIndex("title"));
			String dtst = c3.getString(c3.getColumnIndex("dtstart"));

			if (dtst.equals(Long.toString(day_check))
					&& titl.equals(ContactName + " 誕生日")) {
				Log.d("Testoutput", "すでに登録済み");
				eventcheck = 1;
			}
		}

		if (eventcheck == 0) {
			Log.d("Testoutput", "---------------イベント登録開始---------------");
			values.put("calendar_id", calId);
			values.put("title", ContactName + " 誕生日");
			values.put("allDay", 1);
			values.put("dtstart", startLongDay);
			values.put("dtend", endLongDay);

			cr.insert(events, values);
			Log.d("Testoutput", "---------------イベント登録終了---------------");
		}
	}

	private long getLongDay(String str, int st_end) {
		long time = 0;
		String birth = null;
		int yyyy = 0, mm = 0, dd = 0;

		/* 今日の日付取得 */
		final Calendar calendar = Calendar.getInstance();
		final int year = calendar.get(Calendar.YEAR);
		final int month = calendar.get(Calendar.MONTH);
		final int day = calendar.get(Calendar.DAY_OF_MONTH);

		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date = format.parse(str);
			birth = format.format(date);

			mm = Integer.parseInt(birth.substring(5, 7));
			dd = Integer.parseInt(birth.substring(8, 10));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		/* 今年の誕生日が過ぎたかどうか判定 */
		if (month + 1 < mm) { // 過ぎてない
			yyyy = year;
		} else if (month + 1 == mm) { // 日付も見ないとわからない
			if (day <= dd) { // 過ぎてない
				yyyy = year;
			} else { // 過ぎてる
				yyyy = year + 1;
			}
		} else if (month + 1 > mm) { // 過ぎてる
			yyyy = year + 1;
		}

		/* なぜか1日前に登録されるので下のような対処とする */
		if (st_end == 0) {
			dd = dd + 1;
		} else if (st_end == 1) {
			dd = dd + 2;
		}

		birth = Integer.toString(yyyy) + "-" + Integer.toString(mm) + "-"
				+ Integer.toString(dd) + " 00:00:00";

		try {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date date = format.parse(birth);

			Time times = new Time();
			times.timezone = TimeZone.getDefault().getDisplayName(
					Locale.JAPANESE);

			times.set(date.getTime());
			time = times.normalize(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return time;
	}
}
