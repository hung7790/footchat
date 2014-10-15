package comp437.footchat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Setting extends Activity implements OnClickListener {

	private String Username;

	private EditText m_edt_password;
	private EditText m_edt_nickname;
	private TextView m_txt_error_password;
	private TextView m_txt_error_nickname;
	private Button m_btn_confirm;
	private Button m_btn_back;
	private Spinner m_sp_team;
	private List<String> dbinfo;

	protected settingThread m_settingThread;

	// protected registerThread m_registerThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);

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

	private void init() {
		getAccountData();
		getWidget();
		hideErrorMsg();
		addListener();
		m_settingThread = new settingThread();
		m_settingThread.start();
	}

	private void getAccountData() {
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		Username = bundle.getString("Username");
	}

	private void getWidget() {
		m_edt_password = (EditText) findViewById(R.id.s_edt_password);
		m_edt_nickname = (EditText) findViewById(R.id.s_edt_nickname);

		m_txt_error_password = (TextView) findViewById(R.id.txt_s_error_password);
		m_txt_error_nickname = (TextView) findViewById(R.id.txt_s_error_nickname);

		m_btn_confirm = (Button) findViewById(R.id.btn_confirm);
		m_btn_back = (Button) findViewById(R.id.btn_back);
		m_sp_team = (Spinner) findViewById(R.id.sp_teamlist);
	}

	private void setField(ArrayList<String> dbList,
			List<teamListObject> teamList) {

		m_edt_nickname.setText(dbList.get(0));
		List<String> team = new ArrayList<String>();
		for (int i = 0; i < teamList.size(); i++) {
			team.add(teamList.get(i).teamName);
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, team);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_sp_team.setAdapter(dataAdapter);

		int item_index = 0;
		for (int i = 0; i < teamList.size(); i++) {
			if (teamList.get(i).teamName.equals(dbList.get(1))) {
				item_index = i;
				break;
			}
		}

		m_sp_team.setSelection(item_index);

	}

	private void hideErrorMsg() {
		m_txt_error_password.setVisibility(m_txt_error_password.INVISIBLE);
		m_txt_error_nickname.setVisibility(m_txt_error_nickname.INVISIBLE);
	}

	private void addListener() {
		m_btn_confirm.setOnClickListener(this);
		m_btn_back.setOnClickListener(this);
		m_edt_nickname.setOnClickListener(this);
		m_edt_password.setOnClickListener(this);
	}

	private void hideAll() {
		hideErrorMsg();
		m_btn_confirm.setVisibility(m_btn_confirm.INVISIBLE);
		m_btn_back.setVisibility(m_btn_back.INVISIBLE);
	}

	private void showAll() {
		m_btn_confirm.setVisibility(m_btn_confirm.VISIBLE);
		m_btn_back.setVisibility(m_btn_back.VISIBLE);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_confirm:
			hideErrorMsg();

			if (checkFormat()) {

				modifyThread m_modifyThread = new modifyThread();
				m_modifyThread.start();

				if (senddb())
					Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
				break;

			} else {
				showAll();
			}
			break;
		case R.id.btn_back:
			Intent intent = new Intent();
			intent.setClass(this, ChatRoomList.class);

			Bundle bundle = new Bundle();
			bundle.putString("Username", Username);
			intent.putExtras(bundle);

			startActivity(intent);
			this.finish();
			break;

		case R.id.s_edt_nickname:
			m_edt_nickname.setText("");
			break;

		case R.id.s_edt_password:
			m_edt_password.setText("");
			break;
		}
	}

	private boolean senddb() {
		String password = m_edt_password.getText().toString();
		String nicknamed = m_edt_nickname.getText().toString();
		String selected = m_sp_team.getSelectedItem().toString();
		if (password.length() == 0) {
			// db only send nickname and selected
		} else {
			// db send all;
		}
		return true;

	}

	private boolean checkFormat() {
		boolean returnBl = true;
		String passwordStr = m_edt_password.getText().toString();
		String nicknamedStr = m_edt_nickname.getText().toString();

		if (passwordStr.length() != 8 && passwordStr.length() != 0) {
			returnBl = false;
			m_txt_error_password.setVisibility(m_txt_error_password.VISIBLE);
		}

		if (nicknamedStr.length() == 0) {
			returnBl = false;
			m_txt_error_nickname.setVisibility(m_txt_error_nickname.VISIBLE);
		}

		return returnBl;
	}

	// ======================================================================================

	public class modifyThread extends Thread {
		public void run() {

			PostRequest pr = new PostRequest();

			pr.setUrl(getString(R.string.post_url));
			pr.addPost(getString(R.string.post_requireType),
					getString(R.string.post_rt_modifyAccount));
			pr.addPost(getString(R.string.post_sql_Username), Username);

			if (m_edt_password.getText().toString().equals("")) {
				pr.addPost(getString(R.string.post_sql_Password), "@");
			} else {
				pr.addPost(getString(R.string.post_sql_Password),
						m_edt_password.getText().toString());
			}
			pr.addPost(getString(R.string.post_sql_Nickname), m_edt_nickname
					.getText().toString());
			pr.addPost(getString(R.string.post_sql_favor_team_id),
					Integer.toString(m_sp_team.getSelectedItemPosition()));

			try {
				pr.post();
			} catch (ClientProtocolException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	public class settingThread extends Thread {
		public void run() {
			PostRequest pr = new PostRequest();

			pr.setUrl(getString(R.string.post_url));
			pr.addPost(getString(R.string.post_requireType),
					getString(R.string.post_rt_accountInfor));
			pr.addPost(getString(R.string.post_sql_Username), Username);

			InputStream in;

			try {
				in = pr.post();
				InputStreamConverter isc = new InputStreamConverter();
				String accountResult = isc.isToString(in);
				Message msg = new Message();
				msg.obj = accountResult;
				settingHandler.sendMessage(msg);
			} catch (ClientProtocolException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	public Handler settingHandler = new Handler() {
		public void handleMessage(Message msg) {

			if (m_settingThread != null) {
				if (!m_settingThread.isInterrupted()) {
					m_settingThread.interrupt();
				}
			}
			String bl = (String) msg.obj;
			String[] parts = bl.split(";");
			ArrayList<String> info = new ArrayList<String>();
			info.add(parts[0]);
			info.add(parts[1]);
			List<teamListObject> teamList = new ArrayList<teamListObject>();
			for (int i = 2; i < parts.length; i++) {
				teamListObject team1 = new teamListObject(001, parts[i]);
				teamList.add(team1);
			}
			setField(info, teamList);
		}
	};

}
