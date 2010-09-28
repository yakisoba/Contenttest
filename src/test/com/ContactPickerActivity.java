package test.com;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.Log;
import android.widget.TextView;

public class ContactPickerActivity extends Activity {
	/** Called when the activity is first created. */
	
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
		// 試しにこの選択文がどのように出力するのかDebug
		//String d = Data.MIMETYPE + "=" + Event.CONTENT_ITEM_TYPE + " AND "+ Event.TYPE + "=" + Event.TYPE_BIRTHDAY;
        
		Cursor c = managedQuery( Data.CONTENT_URI, 
			    new String[]{Event.DISPLAY_NAME, Event.DATA}, 
			    Data.MIMETYPE + "=? AND " + Event.TYPE  + "=?", 
			    new String[]{Event.CONTENT_ITEM_TYPE , 
				String.valueOf(Event.TYPE_BIRTHDAY)}, 
			    Data.DISPLAY_NAME ); 
		
		if( c != null ) { 
			try { 
				while( c.moveToNext() ) { 
					TextView nameView = (TextView) findViewById(R.id.ContactsName);
					TextView birthView = (TextView) findViewById(R.id.Birthday);

					String displayName 	= c.getString(0); //ユーザ名
			        String date			= c.getString(1); //誕生日
			        
			        Log.d("Testoutput",displayName);
			        Log.d("Testoutput",date);
			        
					nameView.setText(displayName);
					birthView.setText(date);					 
			    } 
			} finally { 
				c.close(); 
			}
		}
	}
}