package test.com;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class ContactPickerActivity extends Activity/* implements OnClickListener*/{
	/** Called when the activity is first created. */
	private List<ContactsStatus> mList = null;
	private ContactAdapter mAdapter = null;
	
	CheckBox chk01; 

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
		
		public boolean getCheckFlag(){
			return checkflg;
		}
		public void setCheckFlag(boolean checkflg){
			this.checkflg = checkflg;
		}
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main);

		Log.d("Testoutput", "------------------アプリ開始------------------");
		
		ListView listView = (ListView) findViewById(R.id.list);
		fillData();

		mAdapter = new ContactAdapter(this, R.layout.contactsname, mList);

		listView.setAdapter(mAdapter);
	}

	public class ContactAdapter extends ArrayAdapter<ContactsStatus>/* implements OnClickListener*/{
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
				TextView displayName = (TextView) view.findViewById(R.id.ContactsName);
				displayName.setTypeface(Typeface.DEFAULT_BOLD);
				displayName.setText(item.getDisplayName());
				// ビューに誕生日をセット
				TextView birthday = (TextView) view.findViewById(R.id.Birthday);
				birthday.setText(item.getBirth());
				// ビューにチェックボックスのON/OFFをセット
				final CheckBox chk01 = (CheckBox) view.findViewById(R.id.CheckBox);
	    		chk01.setChecked(item.getCheckFlag());
	    		
	    		Log.d("Testoutput", "List1件表示");
	    		//chk01.setOnClickListener(this);
	            
	    		chk01.setOnClickListener(new OnClickListener() {
	                public void onClick(View v) {
	                	if(chk01.isChecked() == true) {
	                		item.setCheckFlag(true);
	                	}else{
	                		item.setCheckFlag(false);
		                }
	            		Log.d("Testoutput", "viewが違う");
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

					String displayName = c.getString(nameIndex); // ユーザ名
					String date = c.getString(birthIndex); // 誕生日
					
					//Log.d("Testoutput", displayName);
					//Log.d("Testoutput", date);

					item.setDisplayName(displayName);
					item.setBirth(date);
					mList.add(item);
				}
			} finally {
				c.close();
			}
		}
	}
}
