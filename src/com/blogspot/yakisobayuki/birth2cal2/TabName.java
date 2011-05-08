package com.blogspot.yakisobayuki.birth2cal2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
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

public class TabName extends Activity implements OnClickListener {
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
	private CheckBox check_full;
	private Button button_import;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.list);
		gridView = (GridView) findViewById(R.id.list);

		ReadDialog rd = new ReadDialog();
		rd.execute();

		check_full = (CheckBox) findViewById(R.id.CheckBox_full);
		check_full.setOnClickListener(this);

		button_import = (Button) findViewById(R.id.button_import);
		button_import.setOnClickListener(this);
	}

	private class ReadDialog extends
			AsyncTask<Object, Void, List<ContactsStatus>> {
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			// バックグラウンドの処理前にUIスレッドでダイアログ表示
			progressDialog = new ProgressDialog(TabName.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("読込中…");
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected List<ContactsStatus> doInBackground(Object... params) {
			// 表示用にデータを収集
			fillData();
			return null;
		}

		@Override
		protected void onPostExecute(List<ContactsStatus> result) {
			// 処理中ダイアログをクローズ
			progressDialog.dismiss();

			// 取得したデータをviewにセット
			mAdapter = new ContactAdapter(TabName.this, R.layout.listview,
					mList);
			gridView.setAdapter(mAdapter);

		}
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
		String[] projection = new String[] { StructuredName.CONTACT_ID,
				StructuredName.PHONETIC_FAMILY_NAME,
				StructuredName.PHONETIC_GIVEN_NAME };
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

					selectionArgs = new String[] {
							c1.getString(c1
									.getColumnIndex(StructuredName.CONTACT_ID)),
							Event.CONTENT_ITEM_TYPE,
							String.valueOf(Event.TYPE_ANNIVERSARY),
							String.valueOf(Event.TYPE_BIRTHDAY) };

					// ふりがなソートしたデータを利用して誕生日と記念日のデータを出力する
					Cursor c3 = managedQuery(
							uri,
							new String[] { Event.CONTACT_ID,
									Event.DISPLAY_NAME, Event.TYPE, Event.DATA,
									Event.DATA_VERSION },
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

								// 誕生日情報をフォーマット変換
								String date_tmp;
								try {
									date_tmp = new BirthdayFormat()
											.DateCheck(date);
								} catch (Exception e) {
									date_tmp = null;
								}

								if (date_tmp != null) {

									String daykind = c3.getString(c3
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
									yyyy = Integer.parseInt(date_tmp.substring(
											0, 4));
									mm = Integer.parseInt(date_tmp.substring(5,
											7));
									dd = Integer.parseInt(date_tmp.substring(8,
											10));

									if ((mMonth + 1 < mm)
											|| ((mMonth + 1 == mm) && (mDay < dd))) { // 過ぎてない
										age = Integer
												.toString(mYear - yyyy - 1);
									} else if ((mMonth + 1 > mm)
											|| ((mMonth + 1 == mm) && (mDay >= dd))) { // 過ぎてる
										age = Integer.toString(mYear - yyyy);
									}

									item.setParam(displayName, daykind,
											date_tmp, age);
									mList.add(item);
								}
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

			if (logstatus == true) {
				Log.d("birth2cal", Integer.toString(calId));
			}

			if (calId == 0) {
				// カレンダー選択を行っていなかったときアラームダイアログを表示する
				ViewGroup alert = (ViewGroup) findViewById(R.id.dialog);
				View layout = getLayoutInflater().inflate(R.layout.dialog,
						alert);
				TextView tv1 = (TextView) layout.findViewById(R.id.dialog_text);
				tv1.setText("カレンダーが登録されていません。");
				TextView tv2 = (TextView) layout
						.findViewById(R.id.dialog_text2);
				tv2.setText("メニューから選択してください。");

				// layoutで記載したviewをダイアログに設定する
				AlertDialog.Builder dlg;
				dlg = new AlertDialog.Builder(TabName.this);
				dlg.setTitle("error!!");
				dlg.setView(layout);
				dlg.setPositiveButton("OK", null);
				dlg.show();
			} else {
				// カレンダーが選択されていたので、カレンダーに登録を開始する
				int Chk_count = 0; // CheckBoxがONのカウント
				for (ContactsStatus status : mList) {
					if (status.getCheckFlag() == true) {
						Chk_count++;
					}
				}

				if (Chk_count == 0) {
					// チェックボックスのチェックが0個だったらエラー
					ViewGroup alert = (ViewGroup) findViewById(R.id.dialog);
					View layout = getLayoutInflater().inflate(R.layout.dialog,
							alert);
					TextView tv1 = (TextView) layout
							.findViewById(R.id.dialog_text);
					tv1.setText("何もチェックされていません。");

					AlertDialog.Builder dlg;
					dlg = new AlertDialog.Builder(TabName.this);
					dlg.setPositiveButton("OK", null);
					dlg.setTitle("error!!");
					dlg.setView(layout);
					dlg.show();

				} else {
					// 問題がなければプログレスダイアログを表示し、別スレッドで処理
					CreateCalendar createCalendar = new CreateCalendar(this,
							Chk_count, getApplicationContext());
					createCalendar.CreateStart(mList);
				}
			}
		}
	}
}
