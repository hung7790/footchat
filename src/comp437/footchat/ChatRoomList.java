package comp437.footchat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

public class ChatRoomList extends Activity implements OnClickListener {

	/**
	 * @param args
	 */

	private String Username;

	private ListView m_lv_roomlist;
	private List<String> roomStatus;
	private Spinner m_sp_filter;
	private List<ChatRoomItem> roomlistfromdb = new ArrayList<ChatRoomItem>();;
	private Button m_btn_refresh;

	private chatroomListThread m_chatroomListThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roomlist);
		getWidget();
		getAccountData();
		addListener();
		setField(getFilterList());
		getDataBase();
	}

	public void onBackPressed() {
		return;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void getAccountData() {
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		Username = bundle.getString("Username");
	}

	private void addListener() {
		m_btn_refresh.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_refresh) {
			getDataBase();
		}
	}

	public void updateChatRoomList(int filter) {

		List<ChatRoomItem> filtered = new ArrayList<ChatRoomItem>();
		String filter_string = null;
		if (filter == 0)
			filter_string = "All";
		else if (filter == 1)
			filter_string = "future";
		else if (filter == 2)
			filter_string = "live";
		else if (filter == 3)
			filter_string = "nolive";
		for (int i = 0; i < roomlistfromdb.size(); i++) {
			if (filter_string.equals("All"))
				filtered.add(roomlistfromdb.get(i));
			if (roomlistfromdb.get(i).status.equals(filter_string)) {
				filtered.add(roomlistfromdb.get(i));
			}
		}
		RoomAdapter roomada = new RoomAdapter(this, filtered);
		m_lv_roomlist.setAdapter(roomada);
		m_lv_roomlist.setOnItemClickListener(new RoomOnClickListener());

	}

	private void setField(List<String> filterList) {
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, filterList);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_sp_filter.setAdapter(dataAdapter);
		m_sp_filter.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				updateChatRoomList(arg0.getSelectedItemPosition());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});

	}

	private List<String> getFilterList() {
		List<String> filter = new ArrayList<String>();
		filter.add("All");
		filter.add("future");
		filter.add("live");
		filter.add("not live");
		return filter;
	}

	private void getWidget() {
		m_lv_roomlist = (ListView) findViewById(R.id.list_roomlist);
		m_sp_filter = (Spinner) findViewById(R.id.sp_roomfilter);
		m_btn_refresh = (Button) findViewById(R.id.btn_refresh);
	}

	public class RoomOnClickListener implements OnItemClickListener {
		public void onClick(View view) {

		}

		public boolean checkPassworddb(String pw) {
			return true;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			final Context context = view.getContext();
			TextView text = (TextView) view.findViewById(R.id.txt_roomname);
			String listItemId = text.getTag().toString();
			for (int i = 0; i < roomlistfromdb.size(); i++) {
				if (roomlistfromdb.get(i).chatroomId == Integer
						.parseInt(listItemId)) {

					enterChatroom(listItemId);
				}

			}

		}

	}

	// ==================================================================

	private void enterChatroom(String chatroomID) {
		Intent intent = new Intent();
		intent.setClass(this, ChatroomPage.class);

		Bundle bundle = new Bundle();
		bundle.putString("Username", Username);
		bundle.putString("ChatroomID", chatroomID);
		intent.putExtras(bundle);

		startActivity(intent);
		this.finish();
	}

	// database
	// ===================================================================

	private void getDataBase() {
		if (m_chatroomListThread != null) {
			if (!m_chatroomListThread.isInterrupted()) {
				m_chatroomListThread.interrupt();
			}
		}
		m_chatroomListThread = new chatroomListThread();
		m_chatroomListThread.start();
	}

	private class chatroomListThread extends Thread {
		public void run() {

			PostRequest pr = new PostRequest();
			XMLParser xp = new XMLParser();
			Object[] resultList;

			pr.setUrl(getString(R.string.post_url));
			pr.addPost(getString(R.string.post_requireType),
					getString(R.string.post_rt_chatroomList));
			pr.addPost(getString(R.string.post_sql_Username), Username);

			InputStream in;
			try {
				in = pr.post();
				String[] tagArray = { "id", "desc", "home", "away", "status",
						"favour" };

				resultList = xp.get2DReasult(in, "chatroom", tagArray, 6);
				Message msg = new Message();
				msg.obj = resultList;
				chatlistHandler.sendMessage(msg);
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}

		}

	}

	public Handler chatlistHandler = new Handler() {
		public void handleMessage(Message msg) {
			Object[] chatlistObj = (Object[]) msg.obj;

			List<ChatRoomItem> list = new ArrayList<ChatRoomItem>();

			ArrayList<String> tempAL0 = (ArrayList<String>) chatlistObj[0];
			int numTemp = tempAL0.size();

			for (int i = 0; i < numTemp; i++) {
				ArrayList<String> tempAL1;
				ArrayList<String> tempAL2;
				ArrayList<String> tempAL3;

				tempAL1 = (ArrayList<String>) chatlistObj[0];
				int roomId = Integer.parseInt(tempAL1.get(i));

				tempAL1 = (ArrayList<String>) chatlistObj[1];
				tempAL2 = (ArrayList<String>) chatlistObj[2];
				tempAL3 = (ArrayList<String>) chatlistObj[3];
				String roomDescription = tempAL1.get(i) + "\n" + tempAL2.get(i)
						+ " Vs " + tempAL3.get(i);

				tempAL1 = (ArrayList<String>) chatlistObj[4];
				String status = tempAL1.get(i);

				tempAL1 = (ArrayList<String>) chatlistObj[5];
				boolean favourBl = false;
				if (tempAL1.get(i).equals("yes")) {
					favourBl = true;
				}
				ChatRoomItem room = new ChatRoomItem(roomId, roomDescription,
						status, 011, favourBl);
				list.add(room);
			}
			roomlistfromdb = list;

			updateChatRoomList(m_sp_filter.getSelectedItemPosition());

			if (m_chatroomListThread != null) {
				if (!m_chatroomListThread.isInterrupted()) {
					m_chatroomListThread.interrupt();
				}
			}
		}
	};

	// add menu
	// ==================================================================================
	private int group1Id = 1;

	int SETTINGPAGEID = Menu.FIRST;
	int LOGOUTID = Menu.FIRST + 1;

	// @Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(group1Id, SETTINGPAGEID, SETTINGPAGEID, "Setting Page");
		menu.add(group1Id, LOGOUTID, LOGOUTID, "Log out");

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 1:
			Intent intent = new Intent();
			intent.setClass(this, Setting.class);

			Bundle bundle = new Bundle();
			bundle.putString("Username", Username);
			intent.putExtras(bundle);

			startActivity(intent);
			this.finish();

			return true;

		case 2:
			Intent intent1 = new Intent();
			intent1.setClass(this, LogInPage.class);
			startActivity(intent1);
			this.finish();

			return true;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
