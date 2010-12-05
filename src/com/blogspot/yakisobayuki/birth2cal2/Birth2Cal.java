package com.blogspot.yakisobayuki.birth2cal2;

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
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Birth2Cal extends Activity implements Runnable, OnClickListener {
	/** Called when the activity is first created. */
	private List<ContactsStatus> mList = null;
	private ContactAdapter mAdapter = null;
	ListView listView;

	String[] mCalendar_list;
	String[] mCalendar_ID;
	String result;
	int mButton;
	int mButton_y;

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
		listView = (ListView) findViewById(R.id.list);

		readDialog();

		check_full = (CheckBox) findViewById(R.id.CheckBox_full);
		check_full.setOnClickListener(this);

		button_import = (Button) findViewById(R.id.button_import);
		button_import.setOnClickListener(this);
	}

	private void readDialog() {
		prg = new ProgressDialog(this);
		prg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		prg.setMessage("読込中…");
		prg.setCancelable(false);
		prg.show();

		Thread thread = new Thread(this);
		thread.start();
	}

	public void run() {
		fillData();

		Message message = new Message();
		Bundle bundle = new Bundle();
		message.setData(bundle);
		handler1.sendEmptyMessage(0);

		prg.dismiss();

	}

	private final Handler handler1 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mAdapter = new ContactAdapter(Birth2Cal.this, R.layout.listview,
					mList);
			listView.setAdapter(mAdapter);
		}
	};

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);

		// 今日の日付取得
		final Calendar calendar = Calendar.getInstance();
		final int month = calendar.get(Calendar.MONTH);
		final int day = calendar.get(Calendar.DAY_OF_MONTH);

		menu.add(0, Menu.FIRST, Menu.NONE, "カレンダー選択");
		menu.add(0, Menu.FIRST + 1, Menu.NONE, "繰り返し年数設定");

		// おちゃめ機能
		if (month == 11 && day == 1) {
			menu.add(0, Menu.FIRST + 2, Menu.NONE, "about");
		}
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
			int button_id = pref.getInt("calendar_list_num", -1);
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
									result = mCalendar_ID[mButton];

									// 選択したボタンの名称と番号をプリファレンスで保存。
									SharedPreferences pref = getSharedPreferences(
											"cal_list", MODE_PRIVATE);
									Editor e = pref.edit();
									e.putString("calendar_list_name",
											mCalendar_list[mButton]);
									e.putString("calendar_list_id", result);
									e.putInt("calendar_list_num", mButton);
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
			break;

		case 2:
			// 登録年数のリスト作成
			final String[] check_year = { "1", "2", "3", "4", "5", "6", "7",
					"8", "9", "10", "11", "12", "13", "14", "15", "期間なし" };

			// プリファレンスでデータを取得
			SharedPreferences prefr = getSharedPreferences("cal_list",
					MODE_PRIVATE);
			int year_id = prefr.getInt("calendar_year", 1);

			// 選択するカレンダーを明示的に初期化
			mButton_y = year_id - 1;

			// ダイアログの表示
			new AlertDialog.Builder(this)
					.setTitle("登録年数選択")
					.setSingleChoiceItems(check_year, year_id - 1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mButton_y = which;
								}
							})
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									new AlertDialog.Builder(Birth2Cal.this);

									// 選択したボタンの名称をプリファレンスで保存
									SharedPreferences pref = getSharedPreferences(
											"cal_list", MODE_PRIVATE);
									Editor e = pref.edit();
									e.putInt("calendar_year", mButton_y + 1);
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
			break;

		// おちゃめ機能　１２月１日だけ表示される
		case 3:
			// カレンダー選択を行っていなかったときアラームダイアログを表示する
			ViewGroup alert = (ViewGroup) findViewById(R.id.yaki_birthday);
			View layout = getLayoutInflater().inflate(R.layout.yaki, alert);

			// layoutで記載したviewをダイアログに設定する
			AlertDialog.Builder dlg;
			dlg = new AlertDialog.Builder(Birth2Cal.this);
			dlg.setView(layout);
			dlg.setPositiveButton("OK", null);
			dlg.show();
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

				TextView displayName = (TextView) view
						.findViewById(R.id.ContactsName1);
				TextView daykind = (TextView) view.findViewById(R.id.DayKind1);
				TextView birthday = (TextView) view
						.findViewById(R.id.Birthday1);
				final CheckBox chk01 = (CheckBox) view
						.findViewById(R.id.CheckBox1);
				ImageView image = (ImageView) view.findViewById(R.id.Image1);

				displayName.setText(item.getDisplayName());
				daykind.setText(item.getDayKind());
				birthday.setText(item.getBirth());
				chk01.setChecked(item.getCheckFlag());
				if (item.getDayKind().equals("誕生日")) {
					image.setImageResource(R.drawable.heart);
				} else {
					image.setImageResource(R.drawable.star);
				}

				// CheckBoxをチェックしたときの動作
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
				StructuredName.PHONETIC_GIVEN_NAME};
		String selection = Data.MIMETYPE + "=?";
		String[] selectionArgs = new String[] { StructuredName.CONTENT_ITEM_TYPE };

		// まずStructuredNameからユーザ名のふりがなを取得しソート
		Cursor c1 = managedQuery(uri, projection, selection, selectionArgs,
				StructuredName.PHONETIC_FAMILY_NAME + " ASC ,"
						+ StructuredName.PHONETIC_GIVEN_NAME + " ASC");

		projection = new String[] { Event.DISPLAY_NAME, Event.DATA, Event.TYPE };
		selection = Data.CONTACT_ID + "=? AND " + Data.MIMETYPE + "=? AND ("
				+ Event.TYPE + "=? OR " + Event.TYPE + "=?) ";

		if (c1 != null) {
			try {
				while (c1.moveToNext()) {

					Log.d("Testoutput",
							c1.getString(c1.getColumnIndex(StructuredName.CONTACT_ID)) + " "
									+ c1.getString(c1.getColumnIndex(StructuredName.PHONETIC_FAMILY_NAME)) + " "
									+ c1.getString(c1.getColumnIndex(StructuredName.PHONETIC_GIVEN_NAME)));

					selectionArgs = new String[] { c1.getString(0),
							Event.CONTENT_ITEM_TYPE,
							String.valueOf(Event.TYPE_ANNIVERSARY),
							String.valueOf(Event.TYPE_BIRTHDAY) };

					// ふりがなソートしたデータを利用して誕生日と記念日のデータを出力する
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
								// コンタクトユーザのリストを作成
								ContactsStatus item = new ContactsStatus();

								String displayName = c3.getString(c3
										.getColumnIndex(Event.DISPLAY_NAME));
								String date = c3.getString(c3
										.getColumnIndex(Event.DATA));
								
								// 誕生日に”-”を含まない場合異常になるのでフォーマット変更
								if (date.indexOf("-") == -1) {

									String date_tmp;
									date_tmp = date.substring(0, 4) + "-"
											+ date.substring(4, 6) + "-"
											+ date.substring(6, 8);
									date = date_tmp;
								}

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

		// 全てチェックのCheckBoxを叩いた処理
		if (v == check_full) {
			if (check_full.isChecked() == true) {
				// リストの分だけCheckBoxをONに
				for (ContactsStatus status : mList) {
					status.setCheckFlag(true);
				}
				// CheckBoxのテキストを変更
				CheckBox b = (CheckBox) v;
				b.setText("チェック解除");

			} else if (check_full.isChecked() == false) {
				// リストの分だけCheckBoxをOFFに
				for (ContactsStatus status : mList) {
					status.setCheckFlag(false);
				}

				// CheckBoxのテキストを変更
				CheckBox b = (CheckBox) v;
				b.setText("全てチェック");
			}
			mAdapter.notifyDataSetChanged();

			// カレンダーに登録のボタンを押した処理
		} else if (v == button_import) {

			// プリファレンスから登録するカレンダーを取得する
			SharedPreferences pref = getSharedPreferences("cal_list",
					MODE_PRIVATE);
			int calId = Integer.parseInt(pref
					.getString("calendar_list_id", "0"));

			Log.d("Testoutput", Integer.toString(calId));

			if (calId == 0) {
				// カレンダー選択を行っていなかったときアラームダイアログを表示する
				ViewGroup alert = (ViewGroup) findViewById(R.id.alert_nocalendar);
				View layout = getLayoutInflater().inflate(R.layout.nocalendar,
						alert);

				// layoutで記載したviewをダイアログに設定する
				AlertDialog.Builder dlg;
				dlg = new AlertDialog.Builder(Birth2Cal.this);
				dlg.setTitle("error!!");
				dlg.setView(layout);
				dlg.setPositiveButton("OK", null);
				dlg.show();
			} else {
				// 問題がなければプログレスダイアログを表示し、別スレッドで処理
				prg = new ProgressDialog(this);
				prg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				prg.setMessage("カレンダーに登録中です...");
				prg.setCancelable(true);
				prg.show();

				(new Thread(runnable)).start();
			}
		} else {

		}
	}

	// カレンダー登録中の処理
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {

			int Chk_count = 0; // CheckBoxがONのカウント

			// プリファレンスからカレンダーIDを取得
			SharedPreferences pref = getSharedPreferences("cal_list",
					MODE_PRIVATE);
			int calId = Integer.parseInt(pref
					.getString("calendar_list_id", "0"));

			if (calId != 0) {

				// チェックが付いている分だけカレンダー登録実施
				for (ContactsStatus status : mList) {
					if (status.getCheckFlag() == true) {

						CreateEvent(status.getBirth(), status.getDisplayName(),
								status.getDayKind(), calId);

						Chk_count++;
					}
				}
			}

			// 他の処理を呼出し
			Message message = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("complete", Integer.toString(Chk_count));
			message.setData(bundle);
			handler.sendMessage(message);

			// プログレスダイアログ終了
			prg.dismiss();
		}
	};

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int Chk_count = Integer.parseInt(msg.getData().get("complete")
					.toString());

			AlertDialog.Builder dlg;
			dlg = new AlertDialog.Builder(Birth2Cal.this);
			dlg.setPositiveButton("OK", null);

			if (Chk_count == 0) {
				// CheckBoxにチェックがなければアラートダイアログ表示
				ViewGroup alert = (ViewGroup) findViewById(R.id.alert_nochek);
				View layout = getLayoutInflater().inflate(R.layout.nocheck,
						alert);

				dlg.setTitle("error!!");
				dlg.setView(layout);
			} else {
				// アラーとダイアログでカレンダー登録完了の表示
				ViewGroup alert = (ViewGroup) findViewById(R.id.alert_complete);
				View layout = getLayoutInflater().inflate(
						R.layout.com_calendar, alert);

				dlg.setTitle("complete!!");
				dlg.setView(layout);
			}
			dlg.show();
		}
	};

	// カレンダー登録
	private void CreateEvent(String birthday, String ContactName,
			String daykind, int calId) {
		long startLongDay, endLongDay;
		String rrule;
		int eventcheck = 0;
		String titl, dtst, id = null;

		// プリファレンスから繰り返し年数を取得
		SharedPreferences prefr = getSharedPreferences("cal_list", MODE_PRIVATE);
		int year_id = prefr.getInt("calendar_year", 1);

		// [0]：rrule に使用する繰返し年数の登録最終年
		// [1]：dtstart に使用する開始時間 Long型
		// [2]：dtend に使用する終了時間 Long型
		String[] str = { "", "", "" };
		str = getLongDay(birthday, year_id);

		// Log.d("Testoutput", str[0] + " " + str[1] + " " + str[2]);
		startLongDay = Long.valueOf(str[1]);
		endLongDay = Long.valueOf(str[2]);

		if (year_id == 16) {
			rrule = "FREQ=YEARLY";
		} else {
			rrule = "FREQ=YEARLY;UNTIL=" + str[0] + "T010000Z";
		}

		TimeZone tz = TimeZone.getDefault();
		long day_check = startLongDay - (tz.getRawOffset() / 60) * 100;
		// long day_check = startLongDay - tz.getRawOffset();

		String AUTHORITY = null;

		// SDKバージョンでURIの記述を変更
		final int sdkVersion = Build.VERSION.SDK_INT;
		if (sdkVersion < 8) {
			AUTHORITY = "calendar";
		} else {
			AUTHORITY = "com.android.calendar";
		}

		// 既に同じ情報を登録してあるか確認
		Uri events = Uri.parse("content://" + AUTHORITY + "/events");
		String[] projection = new String[] { "title", "dtstart", "_id" };
		Cursor cevent = managedQuery(events, projection, "calendar_id ="
				+ calId + " AND dtstart =" + day_check, null, null);

		while (cevent.moveToNext()) {
			// タイトルと開始時間を格納
			titl = cevent.getString(cevent.getColumnIndex("title"));
			dtst = cevent.getString(cevent.getColumnIndex("dtstart"));

			 Log.d("Testoutput", "dt:" + dtst + " ch:" + day_check);
			Log.d("Testoutput", "ti:" + titl + " cd:" + ContactName + " "+daykind);

			// 開始時間とタイトルが一致するイベントがあればidを取得
			if (dtst.equals(Long.toString(day_check))
					&& titl.equals(ContactName + " " + daykind)) {
				id = cevent.getString(cevent.getColumnIndex("_id"));
				// Log.d("Testoutput", ContactName + "の誕生日は登録済み" + id);
				eventcheck = 1;
			}
		}

		ContentValues values = new ContentValues();
		ContentResolver cr = getContentResolver();

		if (eventcheck == 0) {
			// 既存の登録がない場合

			// 繰返し年数1年 = 繰返しなしの場合
			if (year_id == 1) {
				values.put("calendar_id", calId);
				values.put("title", ContactName + " " + daykind);
				values.put("allDay", 1);
				values.put("dtstart", startLongDay);
				values.put("dtend", endLongDay);
				values.put("eventTimezone",TimeZone.getDefault().getDisplayName(Locale.ENGLISH)); 

				// カレンダーへ登録
				cr.insert(events, values);

				// 繰返し年数複数年の場合
			} else {
				values.put("calendar_id", calId);
				values.put("title", ContactName + " " + daykind);
				values.put("allDay", 1);
				values.put("dtstart", startLongDay);
				values.put("dtend", endLongDay);
				values.put("eventTimezone", TimeZone.getDefault()
						.getDisplayName(Locale.ENGLISH));
				values.put("rrule", rrule);
				values.put("duration", "P1D");

				// カレンダーへ登録
				cr.insert(events, values);
			    cr.update(events, values, null, null); 
			}
			// Log.d("Testoutput", "cal insert");

			// 既に登録されていた場合
		} else if (eventcheck == 1) {

			// 該当のイベントIDをURIに付加して処理
			Uri pevents = Uri.withAppendedPath(events, id);

			// 繰返し年数1年 = 繰返しなしの場合
			if (year_id == 1) {
				values.put("calendar_id", calId);
				values.put("title", ContactName + " " + daykind);
				values.put("allDay", 1);
				values.put("dtstart", startLongDay);
				values.put("dtend", endLongDay);
				values.put("eventTimezone", TimeZone.getDefault()
						.getDisplayName(Locale.ENGLISH));

				// 既に登録されていた rrule と duration は削除
				values.putNull("rrule");
				values.putNull("duration");

				// カレンダーを更新
				cr.update(pevents, values, null, null);

				// 繰返し年数複数年の場合
			} else {
				Log.d("Testoutput", Integer.toString(calId));
				values.put("calendar_id", calId);
				values.put("title", ContactName + " " + daykind);
				values.put("allDay", 1);
				values.put("dtstart", startLongDay);
				values.put("dtend", endLongDay);
				values.put("eventTimezone", TimeZone.getDefault()
						.getDisplayName(Locale.ENGLISH));
				values.put("rrule", rrule);
				values.put("duration", "P1D");

				// カレンダーを更新
				cr.update(pevents, values, null, null);
			}
		}
	}

	// rrule、dtstar、dtendパラメータ作成
	private String[] getLongDay(String str, int year_id) {
		long time = 0;
		String birth = null;
		int yyyy = 0, mm = 0, dd = 0;

		// [0]：rrule に使用する繰返し年数の登録最終年
		// [1]：dtstart に使用する開始時間 Long型
		// [2]：dtend に使用する終了時間 Long型
		String[] setDay = { "", "", "" };

		// 今日の日付取得
		final Calendar calendar = Calendar.getInstance();
		final int year = calendar.get(Calendar.YEAR);
		final int month = calendar.get(Calendar.MONTH);
		final int day = calendar.get(Calendar.DAY_OF_MONTH);

		try {
			// 連絡帳の誕生日のフォーマットを、yyyy-MM-dd に変換
			// しなくても上記の方だとは思うが念のため。
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date = format.parse(str);
			birth = format.format(date);

			// 年、月、日に分けInt型へキャスト
			yyyy = Integer.parseInt(birth.substring(0, 4));
			mm = Integer.parseInt(birth.substring(5, 7));
			dd = Integer.parseInt(birth.substring(8, 10));

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// 今年の誕生日が過ぎたかどうか判定
		if ((month + 1 < mm) || ((month + 1 == mm) && (day <= dd))) { // 過ぎてない
			yyyy = year;
		} else if ((month + 1 > mm) || ((month + 1 == mm) && (day > dd))) { // 過ぎてる
			yyyy = year + 1;
		}

		// 繰返し年数はを加算すると1年分多くなるため-1する
		year_id = yyyy + year_id - 1;

		// [0]：rrule用に yyyymmddのフォーマットで保存
		// String型に変更する際に、桁を2桁に合わせる
		setDay[0] = String.format("%04d", year_id) + String.format("%02d", mm)
				+ String.format("%02d", dd);

		// 開始時間と、終了時間の登録
		for (int i = 1; i <= 2; i++) {
			// 誕生日のフォーマットを、yyyy-MM-dd HH:mm:ss に変換するために変更
			// 日にちには、開始時間で+1日分、終了時間で+2日分のオフセットが必要
			birth = Integer.toString(yyyy) + "-" + Integer.toString(mm) + "-"
					+ Integer.toString(dd + i) + " 00:00:00";

			try {
				// 誕生日のフォーマットを、yyyy-MM-dd HH:mm:ss に変換
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date date = format.parse(birth);

				// タイムゾーンを考慮して、上記のフォーマットをLong型に変換する
				Time times = new Time();
				times.timezone = TimeZone.getDefault().getDisplayName(Locale.ENGLISH);
				times.set(date.getTime());
				time = times.normalize(true);

			} catch (Exception e) {
				e.printStackTrace();
			}

			// [1]：dtstart に使用する開始時間 Long型をStringに変換し格納
			// [2]：dtend に使用する終了時間 Long型をStringに変換し格納
			setDay[i] = Long.toString(time);
		}

		return setDay;
	}
}
