package com.test.Contenttest;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class ContactPickerActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	private List<ContactsStatus> mList = null;
	private ContactAdapter mAdapter = null;

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

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main);

		Log.d("Testoutput", "------------------アプリ開始------------------");

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

			try {
				/* 該当カレンダーがあるか捜索＋カレンダー作成 */
				String calId = SearchCalendar();
				Log.d("Testoutput", "該当のカレンダーID：" + Integer.parseInt(calId));

				/* リストから情報取得 */
				for (ContactsStatus status : mList) {
					if (status.getCheckFlag() == true) {
						Log.d("Testoutput",
								status.getBirth() + "\t"
										+ status.getDisplayName());
					}
				}

			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		} else {
			Log.d("Testoutput", "何か押された！");
		}
	}

	private String SearchCalendar() throws InterruptedException {
		String calName, calId = null;

		/* GoogleCalendarのリスト取得 */
		String[] projection = new String[] { "_id", "name" };
		Uri calendars = Uri.parse("content://com.android.calendar/calendars");

		Cursor c2 = managedQuery(calendars, projection, "selected", null, null);

		Log.d("Testoutput", "---------------カレンダー捜索開始---------------");

		if (c2 != null) {
			try {
				while (c2.moveToNext()) {
					calName = c2.getString(c2.getColumnIndex("name"));
					calId = c2.getString(c2.getColumnIndex("_id"));

					Log.d("Testoutput", calId + " : " + calName);

					/* 【重要】ここは以降拡張予定　今回は固定で「アプリテスト用」とする */
					if (calName.equals("アプリテスト用")) {
						Log.d("Testoutput", calName + "は存在した！");
						break;
					} else {
						calId = "0";
					}
				}
			} finally {
				c2.close();
				Log.d("Testoutput", "---------------カレンダー捜索完了---------------");
			}
		} else {
			Log.d("Testoutput", "NULL");
		}
		return calId;
	}
}
