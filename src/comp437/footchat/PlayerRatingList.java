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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerRatingList extends Activity implements
		OnRatingBarChangeListener {
	private List<PlayerRatingItem> playerRatingListFromDB;
	private List<PlayerRatingItem> playerRatingListFromDB2;

	private viewRatingThread m_viewRatingThread;
	private postRatingThread m_postRatingThread;

	private TabHost m_tabhost;

	ListView listViewItems;
	ListView listViewItems2;

	ArrayAdapterItem adapter;
	ArrayAdapterItem adapter2;

	private String ChatroomID;
	private String Username;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_rating);
		getbundle();
		setTag();
		setupListView();
		m_viewRatingThread = new viewRatingThread();
		m_viewRatingThread.start();
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

	private void getbundle() {
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		Username = bundle.getString("Username");
		ChatroomID = bundle.getString("ChatroomID");
	}

	private void setTag() {
		m_tabhost = (TabHost) findViewById(R.id.tabhost);
		m_tabhost.setup();
		m_tabhost.addTab(m_tabhost.newTabSpec("tab1").setIndicator("Home")
				.setContent(R.id.tab1));
		m_tabhost.addTab(m_tabhost.newTabSpec("tab2").setIndicator("Away")
				.setContent(R.id.tab2));
	}

	private void setupListView() {

		playerRatingListFromDB = new ArrayList<PlayerRatingItem>();
		playerRatingListFromDB2 = new ArrayList<PlayerRatingItem>();

		// our adapter instance
		adapter = new ArrayAdapterItem(this, R.layout.rating_item,
				playerRatingListFromDB, PlayerRatingList.this);
		adapter2 = new ArrayAdapterItem(this, R.layout.rating_item,
				playerRatingListFromDB2, PlayerRatingList.this);

		// create a new ListView, set the adapter and item click listener
		ListView listViewItems = (ListView) PlayerRatingList.this
				.findViewById(R.id.ratingListView);
		listViewItems.setAdapter(adapter);

		ListView listViewItems2 = (ListView) PlayerRatingList.this
				.findViewById(R.id.ratingListView2);
		listViewItems2.setAdapter(adapter2);
	}

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {
		// TODO Auto-generated method stub
	}

	public void changeRating(String team, int position, float rating) {

		if (rating > 0) {
			PlayerRatingItem pri;
			if (team.equals("h")) {
				pri = playerRatingListFromDB.get(position);
				pri.gamePlayerUserRating = (int) rating;
			} else {
				pri = playerRatingListFromDB2.get(position);
				pri.gamePlayerUserRating = (int) rating;
			}
			stopThread();
			m_postRatingThread = new postRatingThread();
			m_postRatingThread.setData(pri.gamePlayerId,
					pri.gamePlayerUserRating);
			m_postRatingThread.start();
		}
		refresh();
	}

	private void refresh() {
		adapter.notifyDataSetChanged();
		adapter2.notifyDataSetChanged();
	}

	// =======================================

	private void stopThread() {
		if (m_viewRatingThread != null) {
			if (!m_viewRatingThread.isInterrupted()) {
				m_viewRatingThread.interrupt();
			}
		}

		if (m_postRatingThread != null) {
			if (!m_postRatingThread.isInterrupted()) {
				m_postRatingThread.interrupt();
			}
		}
	}

	public Handler playerRatingHandler = new Handler() {
		public void handleMessage(Message msg) {

			Object[] receiveObject = (Object[]) msg.obj;

			if (receiveObject.length == 0) {
				stopThread();
				m_viewRatingThread = new viewRatingThread();
				m_viewRatingThread.start();
			} else {

				ArrayList<String> nameAL = (ArrayList<String>) receiveObject[0];
				ArrayList<String> noAL = (ArrayList<String>) receiveObject[1];
				ArrayList<String> teamAL = (ArrayList<String>) receiveObject[2];
				ArrayList<String> scoreAL = (ArrayList<String>) receiveObject[3];
				ArrayList<String> youAL = (ArrayList<String>) receiveObject[4];
				ArrayList<String> teamNameAL = (ArrayList<String>) receiveObject[5];
				ArrayList<String> idAL = (ArrayList<String>) receiveObject[6];

				playerRatingListFromDB.clear();
				playerRatingListFromDB2.clear();

				for (int i = 0; i < nameAL.size(); i++) {
					if (teamAL.get(i).equals("home")) {
						PlayerRatingItem rating = new PlayerRatingItem(1, 1,
								Integer.parseInt(idAL.get(i)),
								Integer.parseInt(noAL.get(i)),
								Double.parseDouble(scoreAL.get(i)),
								Integer.parseInt(youAL.get(i)),
								nameAL.get(i),
								"", teamNameAL.get(i), "h");
						playerRatingListFromDB.add(rating);
					} else {
						PlayerRatingItem rating = new PlayerRatingItem(1, 1,
								Integer.parseInt(idAL.get(i)),
								Integer.parseInt(noAL.get(i)),
								Double.parseDouble(scoreAL.get(i)), 
								Integer.parseInt(youAL.get(i)),
								nameAL.get(i), "", teamNameAL.get(i), "a");
						playerRatingListFromDB2.add(rating);
					}
				}

				refresh();
			}

		}

	};

	private class viewRatingThread extends Thread {
		public void run() {

			PostRequest pr = new PostRequest();
			XMLParser xp = new XMLParser();
			Object[] resultList;

			pr.setUrl(getString(R.string.post_url));
			pr.addPost(getString(R.string.post_requireType),
					getString(R.string.post_rt_viewRating));
			pr.addPost(getString(R.string.post_sql_Username), Username);
			pr.addPost(getString(R.string.post_sql_ChatroomID), ChatroomID);
			InputStream in;
			try {
				in = pr.post();
				String[] tagArray = { "name", "no", "team", "score", "you",
						"teamname", "playerId" };
				resultList = xp.get2DReasult(in, "player", tagArray, 7);

				Message msg = new Message();
				msg.obj = resultList;
				playerRatingHandler.sendMessage(msg);
			} catch (ClientProtocolException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			} catch (ParserConfigurationException e) {

				e.printStackTrace();
			} catch (SAXException e) {

				e.printStackTrace();
			}
		}
	}

	private class postRatingThread extends Thread {

		private String id;
		private String score;

		public void run() {
			PostRequest pr = new PostRequest();
			XMLParser xp = new XMLParser();

			pr.setUrl(getString(R.string.post_url));
			pr.addPost(getString(R.string.post_requireType),
					getString(R.string.post_rt_postRating));
			pr.addPost(getString(R.string.post_sql_Username), Username);
			pr.addPost(getString(R.string.post_sql_PlayerListID), id);
			pr.addPost(getString(R.string.post_sql_score), score);
			InputStream in;
			try {
				in = pr.post();
				Object[] resultList = {};
				Message msg = new Message();
				msg.obj = resultList;
				playerRatingHandler.sendMessage(msg);
			} catch (ClientProtocolException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

		public void setData(int id, int score) {
			this.id = Integer.toString(id);
			this.score = Integer.toString(score);
		}
	}

	// add menu
	// ==================================================================================
	private int group1Id = 1;

	int BACK = Menu.FIRST;

	// @Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(group1Id, BACK, BACK, getString(R.string.chatroom_menu_back));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		stopThread();
		switch (item.getItemId()) {
		case 1:
			stopThread();

			Intent intent2 = new Intent();
			intent2.setClass(this, ChatroomPage.class);

			Bundle bundle2 = new Bundle();
			bundle2.putString("Username", Username);
			bundle2.putString("ChatroomID", ChatroomID);
			intent2.putExtras(bundle2);

			startActivity(intent2);
			this.finish();
			return true;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	// ================

}

class ArrayAdapterItem extends ArrayAdapter<PlayerRatingItem> {

	Context mContext;
	int layoutResourceId;
	List<PlayerRatingItem> data = new ArrayList<PlayerRatingItem>();
	Context appContext;

	public ArrayAdapterItem(Context mContext, int layoutResourceId,
			List<PlayerRatingItem> data, Context appContext) {
		super(mContext, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.mContext = mContext;
		this.data = data;
		this.appContext = appContext;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) { 
		if (convertView == null) {
			// inflate the layout
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			convertView = inflater.inflate(layoutResourceId, parent, false);
		}

		// object item based on the position
		PlayerRatingItem playerRatingItem = data.get(position);
		// get the TextView and then set the text (item name) and tag (item ID)
		// values
		TextView textViewItem = (TextView) convertView
				.findViewById(R.id.GamePlayerTextView1);
		textViewItem.setText(playerRatingItem.gamePlayerTeamName + " - No. "
				+ String.valueOf(playerRatingItem.gamePlayerNo) + " "
				+ playerRatingItem.gamePlayerName);
		textViewItem.setTag(playerRatingItem.gamePlayerId);

		// get the TextView and then set the text (item name) and tag (item ID)
		// values
		TextView textViewItem2 = (TextView) convertView
				.findViewById(R.id.ratingTextView1);
		textViewItem2.setText(String
				.valueOf(playerRatingItem.gamePlayerAllAvgRating));

		RatingBar ratingBarSmall1 = (RatingBar) convertView
				.findViewById(R.id.ratingBarSmall1);
		ratingBarSmall1.setMax(5);
		ratingBarSmall1
				.setRating((float) playerRatingItem.gamePlayerAllAvgRating);

		RatingBar ratingBar1 = (RatingBar) convertView
				.findViewById(R.id.ratingBar1);
		ratingBar1.setMax(5);
		ratingBar1.setRating((float) playerRatingItem.gamePlayerUserRating);
		addListenerOnRatingBar(ratingBar1, position, playerRatingItem.team);

		return convertView;

	}

	public void addListenerOnRatingBar(RatingBar ratingBar1,
			final int position, final String team) {
		// if rating value is changed,
		// display the current rating value in the result (textview)
		// automatically
		ratingBar1
				.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
					public void onRatingChanged(RatingBar ratingBar,
							float rating, boolean fromUser) {

						((PlayerRatingList) mContext).changeRating(team,
								position, rating);
						Toast.makeText(appContext,
								"New Rating: " + (int) rating,
								Toast.LENGTH_LONG).show();
					}
				});
	}

}
