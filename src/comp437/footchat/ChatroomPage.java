package comp437.footchat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class ChatroomPage extends Activity implements OnClickListener {

	private final int NONINIT = -1;
	private final int matchTimeSubStringStart = 0;
	private final int matchTimeSubStringend = 16;

	private String username;
	private String ChatroomID;
	private int postNum = NONINIT;

	private TextView m_txt_matchDesc;
	private TextView m_txt_home_team_name;
	private TextView m_txt_away_team_name;
	private TextView m_txt_home_team_score;
	private TextView m_txt_away_team_score;
	private TextView m_txt_match_status;
	private EditText m_edt_message_post;
	private Button m_btn_message_post;
	private TabHost m_tabhost;
	private ListView m_message;
	private ListView m_event;
	private ListView m_guest;
	private SimpleAdapter m_message_adapter;
	private SimpleAdapter m_event_adapter;
	private SimpleAdapter m_guest_adapter;

	private ArrayList<HashMap<String, String>> message_list = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String, String>> event_list = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String, String>> guest_list = new ArrayList<HashMap<String, String>>();

	private chatroomThread m_chatroomThread;
	private PostMessageThread m_PostMessageThread;

	private String postTest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatroom);

		init();
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

	private void refreshAdapter() {
		m_message_adapter.notifyDataSetChanged();
		m_event_adapter.notifyDataSetChanged();
		m_guest_adapter.notifyDataSetChanged();
	}

	private void init() {
		getbundle();
		getWedgit();
		setUpTab();
		setAdapter();
		addListener();
		startThread();
	}

	private void sendNotification(String title, String content) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(title).setContentText(content);

		// Sets an ID for the notification
		int mNotificationId = 001;
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(mNotificationId, mBuilder.build());
	}

	// will be implemented later
	private void getbundle() {
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		username = bundle.getString("Username");
		ChatroomID = bundle.getString("ChatroomID");
	}

	private void setAdapter() {
		m_message_adapter = new SimpleAdapter(this, message_list,
				R.layout.message_post, new String[] {
						getString(R.string.msg_user),
						getString(R.string.msg_event),
						getString(R.string.msg_guest) }, new int[] {
						R.id.txt_message_nickname, R.id.txt_message_time,
						R.id.txt_message_content });
		m_event_adapter = new SimpleAdapter(this, event_list,
				R.layout.event_post, new String[] {
						getString(R.string.msg_event),
						getString(R.string.msg_guest) }, new int[] {
						R.id.txt_message_time, R.id.txt_message_content });
		m_guest_adapter = new SimpleAdapter(this, guest_list,
				R.layout.guest_post, new String[] {
						getString(R.string.msg_user),
						getString(R.string.msg_event),
						getString(R.string.msg_guest) }, new int[] {
						R.id.txt_message_nickname, R.id.txt_message_time,
						R.id.txt_message_content });

		m_message.setAdapter(m_message_adapter);
		m_event.setAdapter(m_event_adapter);
		m_guest.setAdapter(m_guest_adapter);
	}

	private void setUpTab() {
		m_tabhost.setup();
		m_tabhost.addTab(m_tabhost.newTabSpec("tab1")
				.setIndicator(getString(R.string.chatroom_tab_message))
				.setContent(R.id.tab1));
		m_tabhost.addTab(m_tabhost.newTabSpec("tab2")
				.setIndicator(getString(R.string.chatroom_tab_event))
				.setContent(R.id.tab2));
		m_tabhost.addTab(m_tabhost.newTabSpec("tab3")
				.setIndicator(getString(R.string.chatroom_tab_guest))
				.setContent(R.id.tab3));
	}

	private void getWedgit() {
		m_txt_matchDesc = (TextView) findViewById(R.id.txt_matchDesc);
		m_txt_home_team_name = (TextView) findViewById(R.id.txt_home_team_name);
		m_txt_away_team_name = (TextView) findViewById(R.id.txt_away_team_name);
		m_txt_home_team_score = (TextView) findViewById(R.id.txt_home_team_score);
		m_txt_away_team_score = (TextView) findViewById(R.id.txt_away_team_score);
		m_txt_match_status = (TextView) findViewById(R.id.txt_match_status);
		m_edt_message_post = (EditText) findViewById(R.id.edt_message_post);
		m_btn_message_post = (Button) findViewById(R.id.btn_message_post);
		m_tabhost = (TabHost) findViewById(R.id.tabhost);
		m_message = (ListView) findViewById(R.id.lv_message);
		m_event = (ListView) findViewById(R.id.lv_event);
		m_guest = (ListView) findViewById(R.id.lv_guest);
	}

	private void addListener() {
		m_btn_message_post.setOnClickListener(this);
	}

	private void startThread() {
		m_chatroomThread = new chatroomThread();
		m_chatroomThread.start();
	}

	private void stopThread() {
		if (m_chatroomThread != null) {
			if (!m_chatroomThread.isInterrupted()) {
				m_chatroomThread.interrupt();
			}
		}
	}

	public Handler chatroomHandler = new Handler() {

		public void handleMessage(Message msg) {

			Object[] receiveObject = (Object[]) msg.obj;

			if (receiveObject.length == 7) {
				String[] receiveMessage = (String[]) msg.obj;
				updateChatroomInfor(receiveMessage);
			} else {
				undateMessage(receiveObject);
			}
		}

		private void updateChatroomInfor(String[] chatroomInfor) {
			m_txt_matchDesc.setText(chatroomInfor[0]);
			m_txt_home_team_name.setText(getString(R.string.chatroom_home)
					+ chatroomInfor[1]);
			m_txt_away_team_name.setText(getString(R.string.chatroom_away)
					+ chatroomInfor[2]);
			m_txt_home_team_score.setText(chatroomInfor[3]);
			m_txt_away_team_score.setText(chatroomInfor[4]);

			if (chatroomInfor[6]
					.equals(getString(R.string.xml_value_status_live))) {
				m_txt_match_status
						.setText(getString(R.string.chatroom_live_match));
			} else if (chatroomInfor[6]
					.equals(getString(R.string.xml_value_status_future))) {
				m_txt_match_status
						.setText(getString(R.string.chatroom_future_match)
								+ chatroomInfor[5].substring(
										matchTimeSubStringStart,
										matchTimeSubStringend));
			} else if (chatroomInfor[6]
					.equals(getString(R.string.xml_value_status_nolive))) {
				m_txt_match_status
						.setText(getString(R.string.chatroom_nolive_match));
			}
		}

		private void undateMessage(Object[] messageObjArray) {
			stopThread();

			Object[] message = (Object[]) messageObjArray;
			ArrayList<String> msgUsernameList = (ArrayList<String>) message[0];
			ArrayList<String> msgTimeList = (ArrayList<String>) message[1];
			ArrayList<String> msgContentList = (ArrayList<String>) message[2];
			ArrayList<String> msgEventList = (ArrayList<String>) message[3];
			ArrayList<String> msgGuesttList = (ArrayList<String>) message[4];

			message_list.clear();
			event_list.clear();
			guest_list.clear();

			for (int i = 0; i < msgUsernameList.size(); i++) {

				if (msgEventList.get(i).equals(
						getString(R.string.xml_value_yes))) {
					HashMap<String, String> item = new HashMap<String, String>();
					item.put(getString(R.string.msg_event), msgTimeList.get(i));
					item.put(getString(R.string.msg_guest),
							msgContentList.get(i));
					event_list.add(item);
					if (i == msgUsernameList.size() - 1) {
						sendNotification(m_txt_matchDesc.getText().toString(),
								msgContentList.get(i));
					}
				} else if (msgGuesttList.get(i).equals(
						getString(R.string.xml_value_yes))) {
					HashMap<String, String> item = new HashMap<String, String>();
					item.put(getString(R.string.msg_user),
							msgUsernameList.get(i));
					item.put(getString(R.string.msg_event), msgTimeList.get(i));
					item.put(getString(R.string.msg_guest),
							msgContentList.get(i));
					guest_list.add(item);
				} else {
					HashMap<String, String> item = new HashMap<String, String>();
					item.put(getString(R.string.msg_user),
							msgUsernameList.get(i));
					item.put(getString(R.string.msg_event), msgTimeList.get(i));
					item.put(getString(R.string.msg_guest),
							msgContentList.get(i));
					message_list.add(item);
				}
			}

			refreshAdapter();

			startThread();
		}

	};

	private class chatroomThread extends Thread {

		private int sleepTime = 100;

		public void run() {
			int check_num = 0;
			while (true) {

				// check whether have new posts
				try {
					check_num = checkPostNum();
				} catch (ClientProtocolException e1) {

					e1.printStackTrace();
				} catch (IOException e1) {

					e1.printStackTrace();
				} catch (ParserConfigurationException e1) {

					e1.printStackTrace();
				} catch (SAXException e1) {

					e1.printStackTrace();
				}

				// if yes, update the chatroom information and posts
				if (check_num > postNum) {
					String[] chatroomInfor;

					// update the chatroom information
					try {
						chatroomInfor = getChatroomInfor();
						Message msg = new Message();
						msg.obj = chatroomInfor;
						chatroomHandler.sendMessage(msg);
					} catch (ClientProtocolException e) {

						e.printStackTrace();
					} catch (IOException e) {

						e.printStackTrace();
					} catch (ParserConfigurationException e) {

						e.printStackTrace();
					} catch (SAXException e) {

						e.printStackTrace();
					}

					// update posts

					Object[] chatroomNameList;
					try {
						chatroomNameList = getMessage();
						Message msg = new Message();
						msg.obj = chatroomNameList;
						chatroomHandler.sendMessage(msg);
					} catch (ClientProtocolException e) {

						e.printStackTrace();
					} catch (IOException e) {

						e.printStackTrace();
					} catch (ParserConfigurationException e) {

						e.printStackTrace();
					} catch (SAXException e) {

						e.printStackTrace();
					}

					postNum = check_num;
				}

				// sleep, then next round
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}

		}

		private Object[] getMessage() throws ClientProtocolException,
				IOException, ParserConfigurationException, SAXException {
			PostRequest pr = new PostRequest();
			XMLParser xp = new XMLParser();
			Object[] resultList;

			pr.setUrl(getString(R.string.post_url));
			pr.addPost(getString(R.string.post_requireType),
					getString(R.string.post_rt_view_Msg));
			pr.addPost(getString(R.string.post_sql_ChatroomID), ChatroomID);
			pr.addPost(getString(R.string.post_sql_Num),
					getString(R.string.chatroom_max_message));
			InputStream in = pr.post();

			String[] tagArray = { getString(R.string.xml_tag_message_nickname),
					getString(R.string.xml_tag_message_time),
					getString(R.string.xml_tag_message_content),
					getString(R.string.xml_tag_message_event),
					getString(R.string.xml_tag_message_guest) };

			resultList = xp.get2DReasult(in,
					getString(R.string.xml_tag_message), tagArray, 5);
			return resultList;
		}

		private String[] getChatroomInfor() throws ClientProtocolException,
				IOException, ParserConfigurationException, SAXException {
			PostRequest pr = new PostRequest();
			XMLParser xp = new XMLParser();
			Object[] resultList;

			pr.setUrl(getString(R.string.post_url));
			pr.addPost(getString(R.string.post_requireType),
					getString(R.string.post_rt_chatroomInfor));
			pr.addPost(getString(R.string.post_sql_ChatroomID), ChatroomID);
			InputStream in = pr.post();

			String[] tagArray = { getString(R.string.xml_tag_chatroomDesc),
					getString(R.string.xml_tag_homeTeam),
					getString(R.string.xml_tag_awayTeam),
					getString(R.string.xml_tag_homeTeamScore),
					getString(R.string.xml_tag_awayTeamScore),
					getString(R.string.xml_tag_time),
					getString(R.string.xml_tag_status) };

			resultList = xp.get2DReasult(in, getString(R.string.xml_tag_match),
					tagArray, 7);

			ArrayList<String> as1 = (ArrayList<String>) resultList[0];
			ArrayList<String> as2 = (ArrayList<String>) resultList[1];
			ArrayList<String> as3 = (ArrayList<String>) resultList[2];
			ArrayList<String> as4 = (ArrayList<String>) resultList[3];
			ArrayList<String> as5 = (ArrayList<String>) resultList[4];
			ArrayList<String> as6 = (ArrayList<String>) resultList[5];
			ArrayList<String> as7 = (ArrayList<String>) resultList[6];

			String[] returnArray = { as1.get(0), as2.get(0), as3.get(0),
					as4.get(0), as5.get(0), as6.get(0), as7.get(0) };

			return returnArray;
		}

		private int checkPostNum() throws ClientProtocolException, IOException,
				ParserConfigurationException, SAXException {
			int returnInt = NONINIT;

			PostRequest pr = new PostRequest();
			XMLParser xp = new XMLParser();

			pr.setUrl(getString(R.string.xml_url));

			InputStream in = pr.post();
			String[] tagArray = { getString(R.string.xml_tag_chatroomId),
					getString(R.string.xml_tag_chatroomPost) };
			Object[] resultArray = xp.get2DReasult(in,
					getString(R.string.xml_tag_chatroom), tagArray, 2);

			ArrayList<String> chatroomIDArrayList = (ArrayList<String>) resultArray[0];
			int tagertID = NONINIT;
			for (int i = 0; i < chatroomIDArrayList.size(); i++) {
				if (chatroomIDArrayList.get(i).equals(ChatroomID)) {
					tagertID = i;
					break;
				}
			}

			ArrayList<String> postNum = (ArrayList<String>) resultArray[1];
			if (tagertID != NONINIT) {
				returnInt = Integer.parseInt(postNum.get(tagertID));
			}

			return returnInt;
		}

	}

	private class PostMessageThread extends Thread {
		public void run() {

			PostRequest pr = new PostRequest();

			pr.setUrl(getString(R.string.post_url));
			pr.addPost(getString(R.string.post_requireType),
					getString(R.string.post_rt_post_Msg));
			pr.addPost(getString(R.string.post_sql_Username), username);
			pr.addPost(getString(R.string.post_sql_ChatroomID), ChatroomID);
			pr.addPost(getString(R.string.post_sql_Content), postTest);

			try {
				InputStream in = pr.post();
			} catch (ClientProtocolException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.btn_message_post:
			if (!m_edt_message_post.getText().toString().equals("")) {
				if (m_PostMessageThread != null) {
					if (!m_PostMessageThread.isInterrupted()) {
						m_PostMessageThread.interrupt();
					}
				}

				postTest = m_edt_message_post.getText().toString();
				m_edt_message_post.setText("");
				m_PostMessageThread = new PostMessageThread();
				m_PostMessageThread.start();

			}
			break;

		}

	}

	// add menu
	// ==================================================================================
	private int group1Id = 1;

	int SCORE = Menu.FIRST;
	int BACK = Menu.FIRST + 1;

	// @Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(group1Id, SCORE, SCORE,
				getString(R.string.chatroom_menu_score));
		menu.add(group1Id, BACK, BACK, getString(R.string.chatroom_menu_back));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		stopThread();
		switch (item.getItemId()) {
		case 1:

			if (m_txt_match_status.getText().toString()
					.equals(getString(R.string.chatroom_live_match))
					|| m_txt_match_status.getText().toString()
							.equals(getString(R.string.chatroom_nolive_match))) {
				stopThread();

				Intent intent2 = new Intent();
				intent2.setClass(this, PlayerRatingList.class);

				Bundle bundle2 = new Bundle();
				bundle2.putString("Username", username);
				bundle2.putString("ChatroomID", ChatroomID);
				intent2.putExtras(bundle2);

				startActivity(intent2);
				this.finish();
			}
			return true;

		case 2:

			stopThread();

			Intent intent = new Intent();
			intent.setClass(this, ChatRoomList.class);

			Bundle bundle = new Bundle();
			bundle.putString("Username", username);
			intent.putExtras(bundle);

			startActivity(intent);
			this.finish();

			return true;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
