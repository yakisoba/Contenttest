package com.blogspot.yakisobayuki.birth2cal2;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.Data;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class TabBirthday extends Activity implements Runnable, OnClickListener {
	/** Called when the activity is first created. */
	final Boolean logstatus = false;

	// List表示用
	private List<ContactsStatus> mList = null;
	private ContactAdapter mAdapter = null;
	GridView gridView;

	// 今日の日付取得
	final Calendar mCalendar = Calendar.getInstance();
	final int mYear = mCalendar.get(Calendar.YEAR);
	final int mMonth = mCalendar.get(Calendar.MONTH);
	final int mDay = mCalendar.get(Calendar.DAY_OF_MONTH);

	// 表示のアイテム
	private ProgressDialog prg;
	private CheckBox check_full;
	private Button button_import;

	public class ContactsStatus {
		private String displayName;
		private String daykind;
		private String birthday;
		private String age;
		private boolean checkflg;

		public String getDisplayName() {
			return displayName;
		}

		public String getDayKind() {
			return daykind;
		}

		public String getBirth() {
			return birthday;
		}

		public String getAge() {
			return age;
		}

		public boolean getCheckFlag() {
			return checkflg;
		}

		public void setCheckFlag(boolean checkflg) {
			this.checkflg = checkflg;
		}

		public void setParam(String displayName, String daykind,
				String birthday, String age) {
			this.displayName = displayName;
			this.daykind = daykind;
			this.birthday = birthday;
			this.age = age;
		}
	}

	public class SortObj {
		private String displayName;
		private String daykind;
		private String birthday;
		private String age;
		private int month_day;

		public SortObj(String sort1, String sort2, String sort3, String sort4,
				int sort5) {
			this.displayName = sort1;
			this.birthday = sort2;
			this.daykind = sort3;
			this.age = sort4;
			this.month_day = sort5;
		}

		public String getNum1() {
			return displayName;
		}

		public String getNum2() {
			return birthday;
		}

		public String getNum3() {
			return daykind;
		}

		public String getNum4() {
			return age;
		}

		public int getNum5() {
			return month_day;
		}
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.list);
		gridView = (GridView) findViewById(R.id.list);

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
			mAdapter = new ContactAdapter(TabBirthday.this, R.layout.listview,
					mList);
			gridView.setAdapter(mAdapter);
		}
	};

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
				TextView age = (TextView) view.findViewById(R.id.Age1);
				final CheckBox chk01 = (CheckBox) view
						.findViewById(R.id.CheckBox1);
				ImageView image = (ImageView) view.findViewById(R.id.Image1);
				TextView selkind = (TextView) view.findViewById(R.id.Age2);

				displayName.setText(item.getDisplayName());
				daykind.setText(item.getDayKind());
				birthday.setText(item.getBirth());
				chk01.setChecked(item.getCheckFlag());

				if (item.getDayKind().equals("誕生日")) {
					image.setImageResource(R.drawable.heart);
					selkind.setText("歳");
				} else {
					image.setImageResource(R.drawable.star);
					selkind.setText("周年");
				}
				age.setText(item.getAge());

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
		String[] projection = new String[] { Event.CONTACT_ID,
				Event.DISPLAY_NAME, Event.DATA, Event.TYPE };
		String selection = Data.MIMETYPE + "=? AND (" + Event.TYPE + "=? OR "
				+ Event.TYPE + "=?) ";
		String[] selectionArgs = new String[] { Event.CONTENT_ITEM_TYPE,
				String.valueOf(Event.TYPE_ANNIVERSARY),
				String.valueOf(Event.TYPE_BIRTHDAY) };

		Cursor c1 = managedQuery(uri, projection, selection, selectionArgs,
				null);

		List<SortObj> sortList = new ArrayList<SortObj>();

		if (c1 != null) {
			try {
				while (c1.moveToNext()) {
					// コンタクトユーザのリストを作成
					String displayName = c1.getString(c1
							.getColumnIndex(Event.DISPLAY_NAME));
					String date = c1.getString(c1.getColumnIndex(Event.DATA));

					String date_tmp = DateCheck(date);
					if (date_tmp != null) {

						String daykind = c1.getString(c1
								.getColumnIndex(Event.TYPE));
						if (Integer.parseInt(daykind) == 1) {
							daykind = "記念日";
						} else if (Integer.parseInt(daykind) == 3) {
							daykind = "誕生日";
						}

						// 今年の誕生日が過ぎたかどうか判定
						String age = null;
						int yyyy, mm, dd;
						// 年、月、日に分けint型へキャスト
						yyyy = Integer.parseInt(date.substring(0, 4));
						mm = Integer.parseInt(date.substring(5, 7));
						dd = Integer.parseInt(date.substring(8, 10));

						int month_day = 0;

						if ((mMonth + 1 < mm)
								|| ((mMonth + 1 == mm) && (mDay < dd))) { // 過ぎてない
							age = Integer.toString(mYear - yyyy - 1);
							month_day = Integer.parseInt(date.substring(5, 7))
									* 100
									+ Integer.parseInt(date.substring(8, 10));
						} else if ((mMonth + 1 == mm) && (mDay == dd)) { // 今日
							age = Integer.toString(mYear - yyyy);
							month_day = Integer.parseInt(date.substring(5, 7))
									* 100
									+ Integer.parseInt(date.substring(8, 10));
						} else if ((mMonth + 1 > mm)
								|| ((mMonth + 1 == mm) && (mDay > dd))) { // 過ぎてる
							age = Integer.toString(mYear - yyyy);
							month_day = Integer.parseInt(date.substring(5, 7))
									* 100
									+ Integer.parseInt(date.substring(8, 10))
									+ 10000;
						}

						sortList.add(new SortObj(displayName, date, daykind,
								age, month_day));

						Collections.sort(sortList, new Comparator<SortObj>() {
							public int compare(SortObj t1, SortObj t2) {
								// Log.d("birth2cal",
								// Integer.toString(t1.getNum5()));
								return t1.getNum5() - t2.getNum5();
							}
						});
					}
				}
			} finally {
				c1.close();
			}

			for (SortObj obj : sortList) {
				Log.d("birth2cal", "[" + obj.getNum1() + "][" + obj.getNum2()
						+ "][" + obj.getNum3() + "][" + obj.getNum4() + "]");

				ContactsStatus item = new ContactsStatus();
				item.setParam(obj.getNum1(), obj.getNum3(), obj.getNum2(),
						obj.getNum4());
				mList.add(item);
			}
		}
	}

	/**
	 * 誕生日フォーマットのチェック
	 * 
	 * @param date
	 *            　誕生日情報
	 * @return date 修正した情報
	 */
	public String DateCheck(String date) {
		if (date.length() == 10) { // 文字数は正しい
			if (date.indexOf("-") != -1 && date.substring(4, 5).equals("-")
					&& date.substring(7, 8).equals("-")) {
				return date; // 正しいのでそのまま帰す

			} else { // 10文字だけど”-”がない
				try {
					int temp = 0;
					temp = Integer.parseInt(date.substring(0, 4));
					temp = Integer.parseInt(date.substring(5, 7));
					temp = Integer.parseInt(date.substring(8, 10));
					String date_tmp = date.substring(0, 4) + "-"
							+ date.substring(5, 7) + "-"
							+ date.substring(8, 10);
					return date_tmp;
				} catch (Exception e) {
					return null;
				}
			}

		} else if (date.length() < 10) { // 文字数が正しくない
			String date_tmp = null;
			int hit[] = { 0, 0 };
			int hit_point = 0;
			int temp = 0;

			for (int i = 0; i < date.length(); i++) {
				try {
					temp = Integer.parseInt(date.substring(i, i + 1));
					if (logstatus == true) {
						Log.d("Birth2Cal", Integer.toString(temp));
					}
				} catch (Exception e) {
					hit[hit_point] = i;
					hit_point++;
				}
			}

			if (hit_point >= 2) {
				try {
					int temp1, temp2, temp3 = 0;
					if (hit[0] < 4 || (hit[1] - hit[0]) < 2 || hit_point >= 3) {
						return null; // 年が4桁以下
					} else {
						NumberFormat nf = new DecimalFormat("00");
						temp1 = Integer.parseInt(date.substring(0, hit[0]));
						temp2 = Integer.parseInt(date.substring(hit[0] + 1,
								hit[1]));
						temp3 = Integer.parseInt(date.substring(hit[1] + 1,
								date.length()));
						if (temp3 > 31 || temp2 > 12)
							return null;
						date_tmp = Integer.toString(temp1) + "-"
								+ nf.format(temp2) + "-" + nf.format(temp3);
						return date_tmp;
					}
				} catch (Exception e) {
					return null;
				}

			} else if (hit_point == 0 && date.length() == 8) { // 区切り文字がないけど20000101なので判定できる
				int temp1, temp2, temp3 = 0;
				try {
					temp1 = Integer.parseInt(date.substring(0, 4));
					temp2 = Integer.parseInt(date.substring(4, 6));
					temp3 = Integer.parseInt(date.substring(6, 8));
					date_tmp = date.substring(0, 4) + "-"
							+ date.substring(4, 6) + "-" + date.substring(6, 8);
					return date_tmp;
				} catch (Exception e) {
					return null;
				}
			} else { // 2000111みたいな1月11日なのか11月1日なのか判定不可
				return null;
			}
		} else {
			return null;
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

			if (logstatus == true) {
				Log.d("birth2cal", Integer.toString(calId));
			}
			if (calId == 0) {
				// カレンダー選択を行っていなかったときアラームダイアログを表示する
				ViewGroup alert = (ViewGroup) findViewById(R.id.alert_nocalendar);
				View layout = getLayoutInflater().inflate(R.layout.nocalendar,
						alert);

				// layoutで記載したviewをダイアログに設定する
				AlertDialog.Builder dlg;
				dlg = new AlertDialog.Builder(TabBirthday.this);
				dlg.setTitle("error!!");
				dlg.setView(layout);
				dlg.setPositiveButton("OK", null);
				dlg.show();
			} else {
				int Chk_count = 0; // CheckBoxがONのカウント

				for (ContactsStatus status : mList) {
					if (status.getCheckFlag() == true) {
						Chk_count++;
					}
				}

				// 問題がなければプログレスダイアログを表示し、別スレッドで処理
				prg = new ProgressDialog(this);
				prg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				prg.setMax(Chk_count);
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
						prg.setProgress(Chk_count);
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
			dlg = new AlertDialog.Builder(TabBirthday.this);
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
		int year_id = prefr.getInt("calendar_year_v112", 1);

		// [0]：rrule に使用する繰返し年数の登録最終年
		// [1]：dtstart に使用する開始時間 Long型
		// [2]：dtend に使用する終了時間 Long型
		String[] str = { "", "", "" };
		str = getLongDay(birthday, year_id);

		if (logstatus == true) {
			Log.d("birth2cal", str[0] + " " + str[1] + " " + str[2]);
		}
		startLongDay = Long.valueOf(str[1]);
		endLongDay = Long.valueOf(str[2]);

		if (year_id == 16) {
			rrule = "FREQ=YEARLY";
		} else {
			rrule = "FREQ=YEARLY;UNTIL=" + str[0] + "T010000Z";
		}

		// TimeZone tz = TimeZone.getDefault();
		long day_check = startLongDay;

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

			if (logstatus == true) {
				Log.d("birth2cal", "dt:" + dtst + " ch:" + day_check);
				Log.d("birth2cal", "ti:" + titl + " cd:" + ContactName + " "
						+ daykind);
			}

			// 開始時間とタイトルが一致するイベントがあればidを取得
			if (dtst.equals(Long.toString(day_check))
					&& titl.equals(ContactName + " " + daykind)) {
				id = cevent.getString(cevent.getColumnIndex("_id"));
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
				values.put("eventTimezone", TimeZone.getDefault()
						.getDisplayName(Locale.ENGLISH));

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
				// cr.update(events, values, null, null);
			}

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
		if ((mMonth + 1 < mm) || ((mMonth + 1 == mm) && (mDay <= dd))) { // 過ぎてない
			yyyy = mYear;
		} else if ((mMonth + 1 > mm) || ((mMonth + 1 == mm) && (mDay > dd))) { // 過ぎてる
			yyyy = mYear + 1;
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
					+ Integer.toString(dd + i - 1) + " 09:00:00";

			try {
				// 誕生日のフォーマットを、yyyy-MM-dd HH:mm:ss に変換
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date date = format.parse(birth);

				// タイムゾーンを考慮して、上記のフォーマットをLong型に変換する
				Time times = new Time();
				times.timezone = TimeZone.getDefault().getDisplayName(
						Locale.ENGLISH);
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
