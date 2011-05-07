package com.blogspot.yakisobayuki.birth2cal2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Setting extends Activity implements OnItemClickListener {
	/** リストビュー */
	final Boolean logstatus = true;
	private ListView listView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);

		listView = (ListView) findViewById(R.id.setting);
		List<Map<String, String>> dataList = createData();

		// リストビューに渡すアダプタを生成
		SimpleAdapter adapter = new SimpleAdapter(this, dataList,
				android.R.layout.simple_list_item_2, new String[] { "title",
						"comment" }, new int[] { android.R.id.text1,
						android.R.id.text2 });

		// アダプタを設定
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {

		// 表示カレンダー名を変更(今んところできてない)
		SharedPreferences pref = getSharedPreferences("cal_list", 0);
		String cal_name = pref.getString("calendar_list_name", "未選択");

		switch (pos) {
		case 0:
			// カレンダーリスト選択処理
			SelectCal selectCal = new SelectCal(this, getApplicationContext());
			selectCal.CalendarListView();

			break;
		case 1:
			// カレンダーの消去処理
			// ダイアログの表示
			new AlertDialog.Builder(Setting.this)
					.setTitle("登録済み情報の消去")
					.setMessage("カレンダー：" + cal_name + "から誕生日/記念日情報を消去しますか？")
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

			break;
		default:
			break;
		}
	}

	private List<Map<String, String>> createData() {
		List<Map<String, String>> retDataList = new ArrayList<Map<String, String>>();

		// プリファレンスでデータを取得
		SharedPreferences pref = getSharedPreferences("cal_list", MODE_PRIVATE);
		String cal_name = pref.getString("calendar_list_name", "未選択");

		Map<String, String> SelectCal = new HashMap<String, String>();
		SelectCal.put("title", "選択中のカレンダー");
		SelectCal.put("comment", "カレンダー：" + cal_name);
		retDataList.add(SelectCal);

		Map<String, String> DeleteCal = new HashMap<String, String>();
		DeleteCal.put("title", "誕生日/記念日の完全消去");
		DeleteCal.put("comment", "選択中のカレンダーの登録済情報を削除");
		retDataList.add(DeleteCal);

		return retDataList;
	}

	private void EraseCal() {
		// プリファレンスでデータを取得
		SharedPreferences pref = getSharedPreferences("cal_list", MODE_PRIVATE);
		int calId = Integer.parseInt(pref.getString("calendar_list_id", "0"));

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
			String titl = cevent.getString(cevent.getColumnIndex("title"));

			if (logstatus == true) {
				Log.d("birth2cal", "ti:" + titl);
			}

			// 開始時間とタイトルが一致するイベントがあればidを取得
			if (titl.indexOf("誕生日") != -1 || titl.indexOf("記念日") != -1) {
				String id = cevent.getString(cevent.getColumnIndex("_id"));
				ContentResolver cr = getContentResolver();

				// 該当のイベントIDをURIに付加して削除処理
				Uri pevents = Uri.withAppendedPath(events, id);
				cr.delete(pevents, null, null);

			}
		}

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
			SharedPreferences pref = getSharedPreferences("cal_list",
					MODE_PRIVATE);
			int calId = Integer.parseInt(pref
					.getString("calendar_list_id", "0"));

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
				String titl = cevent.getString(cevent.getColumnIndex("title"));

				if (logstatus == true) {
					Log.d("birth2cal", "ti:" + titl);
				}

				// 開始時間とタイトルが一致するイベントがあればidを取得
				if (titl.indexOf("誕生日") != -1 || titl.indexOf("記念日") != -1) {
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
			ViewGroup alert = (ViewGroup) findViewById(R.id.alert_complete);
			View layout = getLayoutInflater().inflate(R.layout.com_calendar,
					alert);

			AlertDialog.Builder dlg;
			dlg = new AlertDialog.Builder(Setting.this);
			dlg.setPositiveButton("OK", null);
			dlg.setTitle("complete!!");
			dlg.setView(layout);
			dlg.show();

		}

	}
}
