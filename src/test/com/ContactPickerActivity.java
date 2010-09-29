package test.com;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.Log;

import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ContactPickerActivity extends Activity {
	/** Called when the activity is first created. */
	private ListView listView = null;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main);
		
		try {
			fillData();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	// コンタクトデータを取得して表示
	private void fillData() throws InterruptedException {
		listView = (ListView) findViewById(R.id.list);

        // ListViewに表示する要素を保持するアダプタを生成
		// 【重要】これだと1つ目のテキストと2つ目のテキストが同じレイアウトになってしまう。
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                                R.layout.contactsname);
		
		Cursor c = managedQuery( Data.CONTENT_URI, 
			    new String[]{Event.DISPLAY_NAME, Event.DATA}, 
			    Data.MIMETYPE + "=? AND " + Event.TYPE  + "=?", 
			    new String[]{Event.CONTENT_ITEM_TYPE , 
				String.valueOf(Event.TYPE_BIRTHDAY)}, 
			    Data.DISPLAY_NAME ); 

		// 名前と誕生日のindexを取得して、出力の際に参照する
		int nameIndex = c.getColumnIndex(Event.DISPLAY_NAME);
		int birthIndex = c.getColumnIndex(Event.DATA);
		
		if( c != null ) { 
			try { 
				while( c.moveToNext() ) { 
					//TextView nameView = (TextView) findViewById(R.id.ContactsName);
					//TextView birthView = (TextView) findViewById(R.id.Birthday);

					String displayName 	= c.getString(nameIndex); //ユーザ名
			        String date			= c.getString(birthIndex); //誕生日
			        
			        Log.d("Testoutput",displayName);
			        Log.d("Testoutput",date);
			        
			        arrayAdapter.add(displayName);
			        arrayAdapter.add(date);
			        
			        // アダプタを設定
			        listView.setAdapter(arrayAdapter);
			    } 
			} finally { 
				c.close(); 
			}
		}
	}
}