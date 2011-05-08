package com.blogspot.yakisobayuki.birth2cal2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Setting extends PreferenceActivity {

	private PreferenceScreen mScreenPref1;
	private PreferenceScreen mScreenPref2;
	private String mCalName = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 表示カレンダー名を変更(今んところできてない)
		SharedPreferences pref = getSharedPreferences("cal_list", 0);
		mCalName = pref.getString("calendar_list_name", "未選択");

		// xmlレイアウトの読み込み
		this.addPreferencesFromResource(R.layout.setting);
		// 親のPreferenceScreen
		CharSequence SP_select = "select_calendar";
		mScreenPref1 = (PreferenceScreen) findPreference(SP_select);
		mScreenPref1.setSummary("カレンダー：" + mCalName);
		mScreenPref1
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference pref) {
						return screenPref1_onPreferenceClick(pref);
					}
				});

		// 親のPreferenceScreen
		CharSequence SP_delete = "delete_calendar";
		mScreenPref2 = (PreferenceScreen) findPreference(SP_delete);
		mScreenPref2
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference pref) {
						return screenPref2_onPreferenceClick(pref);
					}
				});
	}

	private boolean screenPref1_onPreferenceClick(Preference pref) {
		SelectCal selectCal = new SelectCal(this, getApplicationContext());
		selectCal.CalendarListView();

		selectCal.mAlertDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {

				SharedPreferences pref = getSharedPreferences("cal_list", 0);
				mCalName = pref.getString("calendar_list_name", "未選択");

				mScreenPref1.setSummary("カレンダー：" + mCalName);
			}
		});
		return true;
	}

	private boolean screenPref2_onPreferenceClick(Preference pref) {
		if (mCalName.equals("未選択")) {
						ViewGroup alert = (ViewGroup) findViewById(R.id.dialog);
			View layout = getLayoutInflater().inflate(R.layout.dialog,
					alert);
			TextView tv1 = (TextView) layout.findViewById(R.id.dialog_text);
			tv1.setText("カレンダーが登録されていません。");
			TextView tv2 = (TextView) layout.findViewById(R.id.dialog_text2);
			tv2.setText("メニューから選択してください。");

			// layoutで記載したviewをダイアログに設定する
			AlertDialog.Builder dlg;
			dlg = new AlertDialog.Builder(Setting.this);
			dlg.setTitle("error!!");
			dlg.setView(layout);
			dlg.setPositiveButton("OK", null);
			dlg.show();

		} else {
			ViewGroup alert = (ViewGroup) findViewById(R.id.dialog);
			View layout = getLayoutInflater().inflate(R.layout.dialog,
					alert);
			TextView tv = (TextView) layout.findViewById(R.id.dialog_text);
			tv.setText("カレンダー：" + mCalName + "　から");
			TextView tv2 = (TextView) layout.findViewById(R.id.dialog_text2);
			tv2.setText("誕生日/記念日情報を消去しますか？");

			new AlertDialog.Builder(Setting.this)
					.setTitle("登録済み情報の消去")
					.setView(layout)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									// OKボタンが押されたら消去処理
									EraceCal eraceCal = new EraceCal();
									eraceCal.execute();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// canselボタンが押されたらなにもしない
								}
							}).show();
		}

		return true;
	}

	public class EraceCal extends AsyncTask<Object, Void, Void> {
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			// バックグラウンドの処理前にUIスレッドでダイアログ表示
			progressDialog = new ProgressDialog(Setting.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("読込中…");
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Object... params) {

			// プリファレンスでデータを取得
			SharedPreferences pref = getSharedPreferences("cal_list", 0);
			int calId = Integer.parseInt(pref.getString("calendar_list_id",
					"-1"));

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
			String[] projection = new String[] { "title", "_id" };
			Cursor cevent = managedQuery(events, projection, "calendar_id ="
					+ calId, null, null);

			while (cevent.moveToNext()) {
				// タイトルと開始時間を格納
				String title = cevent.getString(cevent.getColumnIndex("title"));

				// 開始時間とタイトルが一致するイベントがあればidを取得
				if (title.indexOf("誕生日") != -1 || title.indexOf("記念日") != -1) {
					String id = cevent.getString(cevent.getColumnIndex("_id"));
					ContentResolver cr = getContentResolver();

					// 該当のイベントIDをURIに付加して削除処理
					Uri pevents = Uri.withAppendedPath(events, id);
					cr.delete(pevents, null, null);

				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// 処理中ダイアログをクローズ
			progressDialog.dismiss();

			// 終わったら完了のダイアログを表示
			ViewGroup alert = (ViewGroup) findViewById(R.id.dialog);
			View layout = getLayoutInflater().inflate(R.layout.dialog,
					alert);
			TextView tv = (TextView) layout.findViewById(R.id.dialog_text);
			tv.setText("カレンダー削除完了！");

			AlertDialog.Builder dlg;
			dlg = new AlertDialog.Builder(Setting.this);
			dlg.setPositiveButton("OK", null);
			dlg.setTitle("complete!!");
			dlg.setView(layout);
			dlg.show();
		}
	}
}
