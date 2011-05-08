package com.blogspot.yakisobayuki.birth2cal2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CreateCalendar extends Activity {
	final Boolean logstatus = false;
	private List<ContactsStatus> mList = null;

	private ProgressDialog mProgressDialog = null;
	private Activity mActivity;
	private int mCount = 0;
	private Context mContext;

	// 今日の日付取得
	final Calendar mCalendar = Calendar.getInstance();
	final int mYear = mCalendar.get(Calendar.YEAR);
	final int mMonth = mCalendar.get(Calendar.MONTH);
	final int mDay = mCalendar.get(Calendar.DAY_OF_MONTH);

	// チェックされた数を取得
	public CreateCalendar(Activity activity, int Chk_count, Context context) {
		this.mActivity = activity;
		this.mCount = Chk_count;
		this.mContext = context; // Prefarence用にcontextが必要
	}

	public void CreateStart(List<ContactsStatus> list) {
		this.mList = list;
		
		// バックグラウンドの処理前にUIスレッドでダイアログ表示
		mProgressDialog = new ProgressDialog(mActivity);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMessage("カレンダーに登録中です");
		mProgressDialog.setMax(mCount);
		mProgressDialog.setCancelable(true);
		mProgressDialog.show();

		(new Thread(runnable)).start();
	}

	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			int count = 0;

			SharedPreferences pref = mContext.getSharedPreferences("cal_list",
					0);
			int calId = Integer.parseInt(pref
					.getString("calendar_list_id", "0"));

			// 全ContactsStatusからフラグがONのものだけ処理
			for (ContactsStatus List : mList) {
				if (List.getCheckFlag() == true) {

					CreateEvent(List.getBirth(), List.getDisplayName(),
							List.getDayKind(), calId);

					// 処理完了数を通知
					count++;
					mProgressDialog.setProgress(count);
				}
			}

			Message message = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("complete", Integer.toString(count));
			message.setData(bundle);
			handler.sendMessage(message);
			
			mProgressDialog.dismiss();
		}
	};

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 終わったら完了のダイアログを表示
			ViewGroup alert = (ViewGroup) mActivity
					.findViewById(R.id.dialog);
			View layout = mActivity.getLayoutInflater().inflate(
					R.layout.dialog, alert);

			TextView tv = (TextView) layout.findViewById(R.id.dialog_text);
			tv.setText("カレンダー登録完了！");
			
			AlertDialog.Builder dlg;
			dlg = new AlertDialog.Builder(mActivity);
			dlg.setPositiveButton("OK", null);
			dlg.setTitle("complete!!");
			dlg.setView(layout);
			dlg.show();
		}
	};

	// カレンダー登録
	public void CreateEvent(String birthday, String ContactName,
			String daykind, int calId) {
		String str = null;
		String rrule;
		String titl = null, dtst = null, id = null;
		long startLongDay;
		int eventcheck = 0;

		// dtstart に使用する開始時間 Long型
		str = getStartDay(birthday);

		if (logstatus == true) {
			Log.d("birth2cal", str);
		}
		startLongDay = Long.valueOf(str);

		rrule = "FREQ=YEARLY";
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
		Cursor cevent = mContext.getContentResolver().query(events, projection,
				"calendar_id =" + calId + " AND dtstart =" + day_check, null,
				null);

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
		ContentResolver cr = mContext.getContentResolver();

		if (eventcheck == 0) {
			// 既存の登録がない場合
			values.put("calendar_id", calId);
			values.put("title", ContactName + " " + daykind);
			values.put("allDay", 1);
			values.put("dtstart", startLongDay);
			values.put("eventTimezone",
					TimeZone.getDefault().getDisplayName(Locale.ENGLISH));
			values.put("rrule", rrule);
			values.put("duration", "P1D");

			// カレンダーへ登録
			cr.insert(events, values);

			 // 既に登録されていた場合
		} else if (eventcheck == 1) {
			 // 該当のイベントIDをURIに付加して処理
			 Uri pevents = Uri.withAppendedPath(events, id);
			 values.put("calendar_id", calId);
			 values.put("title", ContactName + " " + daykind);
			 values.put("allDay", 1);
			 values.put("dtstart", startLongDay);
			 values.put("eventTimezone",
			 TimeZone.getDefault().getDisplayName(Locale.ENGLISH));
			 values.put("rrule", rrule);
			 values.put("duration", "P1D");
			
			 // カレンダーを更新
			 cr.update(pevents, values, null, null);
		}
	}

	public String getStartDay(String str) {
		String setDay = null;
		String birth = null;

		long time = 0;
		int yyyy = 0, mm = 0, dd = 0;

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
			return null;
		}

		// 開始時間登録
		// 誕生日のフォーマットを、yyyy-MM-dd HH:mm:ss に変換するために変更
		// 日にちには、開始時間で+1日分、終了時間で+2日分のオフセットが必要
		birth = Integer.toString(yyyy) + "-" + Integer.toString(mm) + "-"
				+ Integer.toString(dd) + " 09:00:00";

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

		setDay = Long.toString(time);
		return setDay;
	}
}