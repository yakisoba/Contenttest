package com.blogspot.yakisobayuki.birth2cal2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
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

public class Birth2Cal extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	private List<ContactsStatus> mList = null;
	private ContactAdapter mAdapter = null;

	String[] mCalendar_list;
	String[] mCalendar_ID;
	String result;
	int mButton;

	ProgressDialog prg;
	CheckBox chk01;
	private CheckBox check_full;
	private Button button_import;

	public class ContactsStatus {
		private String displayName;
		private String daykind;
		private String birthday;
		private boolean checkflg;

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getDayKind() {
			return daykind;
		}

		public void setDayKind(String daykind) {
			this.daykind = daykind;
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

		ListView listView = (ListView) findViewById(R.id.list);
		fillData();

		mAdapter = new ContactAdapter(this, R.layout.listview, mList);
		listView.setAdapter(mAdapter);

		check_full = (CheckBox) findViewById(R.id.CheckBox_full);
		check_full.setOnClickListener(this);

		button_import = (Button) findViewById(R.id.button_import);
		button_import.setOnClickListener(this);

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, Menu.NONE, "カレンダー選択");
		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;

		switch (item.getItemId()) {
		case 1:
			/* カレンダーのリストを作成する */
			CalendarList();

			/* プリファレンスでデータを取得 */
			SharedPreferences pref = getSharedPreferences("cal_list",
					MODE_PRIVATE);
			int button_id = Integer.parseInt(pref.getString(
					"calendar_list_num", "-1"));
			String cal_name = pref.getString("calendar_list_name", "");

			if (button_id >= 0) {
				if (cal_name.equals(mCalendar_list[button_id])) {
				} else {
					button_id = -1;
				}
			}

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
									result = mCalendar_ID[mButton];

									SharedPreferences pref = getSharedPreferences(
											"cal_list", MODE_PRIVATE);
									Editor e = pref.edit();
									e.putString("calendar_list_name",
											mCalendar_list[mButton]);
									e.putString("calendar_list_num",
											Integer.toString(mButton));
									e.putString("calendar_list_id", result);
									e.commit();

								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									new AlertDialog.Builder(Birth2Cal.this);
								}
							}).show();
		}
		return ret;
	}

	/* カレンダーのリストの作成 */
	public void CalendarList() {
		mCalendar_list = null;
		mCalendar_ID = null;

		final String[] projection = new String[] { "_id", "displayName" };
		final Uri calendars = Uri
				.parse("content://com.android.calendar/calendars");

		final Cursor clist = managedQuery(calendars, projection,
				"access_level = 700", null, null);

		CalendarList item = new CalendarList();
		item.setCalendarList(clist.getCount());

		if (clist != null) {
			try {
				int count = 0;

				while (clist.moveToNext()) {

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
			View view = inflater.inflate(R.layout.listview, null);

			// 表示すべきデータの取得
			final ContactsStatus item = getItem(position);

			if (item != null) {
				// ビューにユーザ名をセット
				TextView displayName = (TextView) view
						.findViewById(R.id.ContactsName);
				displayName.setTypeface(Typeface.DEFAULT_BOLD);
				displayName.setText(item.getDisplayName());

				TextView daykind = (TextView) view.findViewById(R.id.DayKind);
				daykind.setText(item.getDayKind());

				TextView birthday = (TextView) view.findViewById(R.id.Birthday);
				birthday.setText(item.getBirth());

				final CheckBox chk01 = (CheckBox) view
						.findViewById(R.id.CheckBox);
				chk01.setChecked(item.getCheckFlag());

				chk01.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (chk01.isChecked() == true) {
							item.setCheckFlag(true);
						} else {
							item.setCheckFlag(false);
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

		Uri uri = Data.CONTENT_URI;
		String[] projection = new String[] { StructuredName.CONTACT_ID,
				StructuredName.PHONETIC_FAMILY_NAME,
				StructuredName.PHONETIC_GIVEN_NAME };
		String selection = Data.MIMETYPE + "=?";
		String[] selectionArgs = new String[] { StructuredName.CONTENT_ITEM_TYPE };

		Cursor c1 = managedQuery(uri, projection, selection, selectionArgs,
				StructuredName.PHONETIC_FAMILY_NAME + " ASC ,"
						+ StructuredName.PHONETIC_GIVEN_NAME + " ASC");

		projection = new String[] { Event.DISPLAY_NAME, Event.DATA, Event.TYPE };
		selection = Data.CONTACT_ID + "=? AND " + Data.MIMETYPE + "=? AND ("
				+ Event.TYPE + "=? OR " + Event.TYPE + "=?) ";

		if (c1 != null) {
			try {
				while (c1.moveToNext()) {

					selectionArgs = new String[] { c1.getString(0),
							Event.CONTENT_ITEM_TYPE,
							String.valueOf(Event.TYPE_ANNIVERSARY),
							String.valueOf(Event.TYPE_BIRTHDAY) };

					Cursor c3 = managedQuery(
							uri,
							new String[] { Event.CONTACT_ID,
									Event.DISPLAY_NAME, Event.TYPE, Event.DATA },
							Data.CONTACT_ID + "=? AND " + Data.MIMETYPE
									+ "=? AND (" + Event.TYPE + "=? OR "
									+ Event.TYPE + "=? )",
							new String[] { c1.getString(0),
									Event.CONTENT_ITEM_TYPE,
									String.valueOf(Event.TYPE_ANNIVERSARY),
									String.valueOf(Event.TYPE_BIRTHDAY) }, null);

					if (c3 != null) {
						try {
							while (c3.moveToNext()) {
								Log.d("Testoutput",
										c3.getString(c3
												.getColumnIndex(Event.CONTACT_ID))
												+ " "
												+ c3.getString(c3
														.getColumnIndex(Event.DATA)));

								ContactsStatus item = new ContactsStatus();

								String displayName = c3.getString(c3
										.getColumnIndex(Event.DISPLAY_NAME));
								String date = c3.getString(c3
										.getColumnIndex(Event.DATA));

								String daykind = c3.getString(c3
										.getColumnIndex(Event.TYPE));

								if (Integer.parseInt(daykind) == 1) {
									daykind = "記念日";
								} else if (Integer.parseInt(daykind) == 3) {
									daykind = "誕生日";
								}

								item.setDisplayName(displayName);
								item.setDayKind(daykind);
								item.setBirth(date);
								mList.add(item);
							}
						} finally {
							c3.close();
						}
					}
				}
			} finally {
				c1.close();
			}
		}

	}

	@Override
	public void onClick(View v) {
		if (v == check_full) {
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

			SharedPreferences pref = getSharedPreferences("cal_list",
					MODE_PRIVATE);
			String calId = pref.getString("calendar_list_id", "");

			if (calId.equals("")) {
				AlertDialog.Builder dlg;
				dlg = new AlertDialog.Builder(Birth2Cal.this);
				dlg.setTitle("error!!");
				dlg.setMessage("カレンダーが登録されていません。メニューから選択してください。");
				dlg.show();
			} else {
				prg = new ProgressDialog(this);
				prg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				prg.setMessage("処理を実行中です...");
				prg.setCancelable(true);
				prg.show();

				(new Thread(runnable)).start();
			}
		} else {
			// Log.d("Testoutput", "何か押された！");
		}
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {

			int Completion_count = 0;

			SharedPreferences pref = getSharedPreferences("cal_list",
					MODE_PRIVATE);
			String calId = pref.getString("calendar_list_id", "");

			// Log.d("Testoutput", "該当のカレンダーID：" + Integer.parseInt(calId));

			if (Integer.parseInt(calId) != 0) {

				for (ContactsStatus status : mList) {
					if (status.getCheckFlag() == true) {

						CreateEvent(status.getBirth(), status.getDisplayName(),
								status.getDayKind(), Integer.parseInt(calId));

						Completion_count++;
					}
				}
			}

			Message message = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("complete", Integer.toString(Completion_count));
			message.setData(bundle);
			handler.sendMessage(message);

			prg.dismiss();
		}
	};

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String complete = msg.getData().get("complete").toString();

			if (Integer.parseInt(complete) == 0) {
				AlertDialog.Builder dlg;
				dlg = new AlertDialog.Builder(Birth2Cal.this);
				dlg.setTitle("error!!");
				dlg.setMessage("何もチェックされていません");
				dlg.show();
			} else {
				AlertDialog.Builder dlg;
				dlg = new AlertDialog.Builder(Birth2Cal.this);
				dlg.setTitle("complete!!");
				dlg.setMessage("カレンダーへ登録完了！");
				dlg.show();
			}
		}
	};

	private void CreateEvent(String birthday, String ContactName,
			String daykind, int calId) {
		long startLongDay;
		long endLongDay;
		int eventcheck = 0;

		Uri events = Uri.parse("content://com.android.calendar/events");
		ContentValues values = new ContentValues();
		ContentResolver cr = this.getContentResolver();

		startLongDay = this.getLongDay(birthday, 0);
		endLongDay = this.getLongDay(birthday, 1);

		/* システム時間から誤差を算出する */
		TimeZone tz = TimeZone.getDefault();
		long day_check = startLongDay - (tz.getRawOffset() / 60) * 100;
		// long day_check = startLongDay - tz.getRawOffset();

		/* すでに登録されているときは登録しない */
		String[] projection = new String[] { "title", "dtstart" };
		Cursor cevent = managedQuery(events, projection, "calendar_id ="
				+ calId + " AND dtstart =" + day_check, null, null);

		while (cevent.moveToNext()) {
			String titl = cevent.getString(cevent.getColumnIndex("title"));
			String dtst = cevent.getString(cevent.getColumnIndex("dtstart"));

			if (dtst.equals(Long.toString(day_check))
					&& titl.equals(ContactName + " " + daykind)) {
				// Log.d("Testoutput", ContactName + " 登録済み");
				eventcheck = 1;
			}
		}

		if (eventcheck == 0) {
			values.put("calendar_id", calId);
			values.put("title", ContactName + " " + daykind);
			values.put("allDay", 1);
			values.put("dtstart", startLongDay);
			values.put("dtend", endLongDay);
			values.put("eventTimezone", TimeZone.getDefault().getDisplayName());

			cr.insert(events, values);
			// Log.d("Testoutput", ContactName + " 登録");
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
		if ((month + 1 < mm) || ((month + 1 == mm) && (day <= dd))) { // 過ぎてない
			yyyy = year;
		} else if ((month + 1 > mm) || ((month + 1 == mm) && (day > dd))) { // 過ぎてる
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
			times.timezone = TimeZone.getDefault().getDisplayName();

			times.set(date.getTime());
			time = times.normalize(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return time;
	}
}
