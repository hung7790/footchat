package comp437.footchat;

import java.io.IOException;
import java.io.InputStream;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegistrationPage extends Activity implements OnClickListener {

	private EditText m_edt_username;
	private EditText m_edt_password;
	private EditText m_edt_nickname;
	private TextView m_txt_error_username;
	private TextView m_txt_error_password;
	private TextView m_txt_error_nickname;
	private Button m_btn_register;
	private Button m_btn_back;

	protected registerThread m_registerThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);

		init();
	}

	private void init() {
		getWidget();
		hideErrorMsg();
		addListener();
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

	private void getWidget() {
		m_edt_username = (EditText) findViewById(R.id.edt_username);
		m_edt_password = (EditText) findViewById(R.id.edt_password);
		m_edt_nickname = (EditText) findViewById(R.id.edt_nickname);

		m_txt_error_username = (TextView) findViewById(R.id.txt_error_username);
		m_txt_error_password = (TextView) findViewById(R.id.txt_error_password);
		m_txt_error_nickname = (TextView) findViewById(R.id.txt_error_nickname);

		m_btn_register = (Button) findViewById(R.id.btn_register);
		m_btn_back = (Button) findViewById(R.id.btn_back);
	}

	private void hideErrorMsg() {
		m_txt_error_username.setVisibility(m_txt_error_username.INVISIBLE);
		m_txt_error_password.setVisibility(m_txt_error_password.INVISIBLE);
		m_txt_error_nickname.setVisibility(m_txt_error_nickname.INVISIBLE);
	}

	private void addListener() {
		m_btn_register.setOnClickListener(this);
		m_btn_back.setOnClickListener(this);
	}

	private void hideAll() {
		hideErrorMsg();
		m_btn_register.setVisibility(m_btn_register.INVISIBLE);
		m_btn_back.setVisibility(m_btn_back.INVISIBLE);
	}

	private void showAll() {
		m_btn_register.setVisibility(m_btn_register.VISIBLE);
		m_btn_back.setVisibility(m_btn_back.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		hideAll();
		switch (v.getId()) {
		case R.id.btn_register:
			if (checkFormat()) {
				m_registerThread = new registerThread();
				m_registerThread.start();
			} else {
				showAll();
			}
			break;
		case R.id.btn_back:
			Intent intent = new Intent();
			intent.setClass(this, LogInPage.class);
			startActivity(intent);
			this.finish();
			break;
		}
	}

	private boolean checkFormat() {
		boolean returnBl = true;
		String usernameStr = m_edt_username.getText().toString();
		String passwordStr = m_edt_password.getText().toString();
		String nicknamedStr = m_edt_nickname.getText().toString();

		if (usernameStr.length() != 8) {
			returnBl = false;
			m_txt_error_username.setVisibility(m_txt_error_username.VISIBLE);
		}

		if (passwordStr.length() != 8) {
			returnBl = false;
			m_txt_error_password.setVisibility(m_txt_error_password.VISIBLE);
		}

		if (nicknamedStr.length() == 0) {
			returnBl = false;
			m_txt_error_nickname.setVisibility(m_txt_error_nickname.VISIBLE);
		}

		return returnBl;
	}

	public Handler registerHandler = new Handler() {
		public void handleMessage(Message msg) {
			String bl = (String) msg.obj;

			if (bl.equals(getString(R.string.post_return_OK))) {
				showdiaolg("Registration Succeed!");

				if (m_registerThread != null) {
					if (!m_registerThread.isInterrupted()) {
						m_registerThread.interrupt();
					}
				}

				Login();
			} else if (bl.equals(getString(R.string.post_return_exist))) {
				showdiaolg("Account exist!\nPlease use other username.");
			} else {
				showdiaolg("There are error in connection.");
			}

			if (m_registerThread != null) {
				if (!m_registerThread.isInterrupted()) {
					m_registerThread.interrupt();
				}
			}

			m_edt_username.setText("");
			m_edt_password.setText("");

			showAll();
			clearInput();
		}

	};

	private void showdiaolg(String s) {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setMessage(s);
		dlgAlert.setTitle("Registration");
		dlgAlert.setPositiveButton("OK", null);
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();
	}

	private void clearInput() {
		m_txt_error_username.setText("");
		m_txt_error_password.setText("");
		m_txt_error_nickname.setText("");
	}

	private class registerThread extends Thread {
		public void run() {

			Message msg = new Message();

			String registerResult = "";

			try {
				registerResult = register(m_edt_username.getText().toString(),
						m_edt_password.getText().toString(), m_edt_nickname
								.getText().toString());
			} catch (ClientProtocolException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			msg.obj = registerResult;
			registerHandler.sendMessage(msg);
		}

		private String register(String un, String pw, String nn)
				throws ClientProtocolException, IOException {
			PostRequest pr = new PostRequest();

			pr.setUrl(getString(R.string.post_url));
			pr.addPost(getString(R.string.post_requireType),
					getString(R.string.post_rt_registration));
			pr.addPost(getString(R.string.post_sql_Username), m_edt_username
					.getText().toString());
			pr.addPost(getString(R.string.post_sql_Password), m_edt_password
					.getText().toString());
			pr.addPost(getString(R.string.post_sql_Nickname), m_edt_nickname
					.getText().toString());

			InputStream in = pr.post();
			InputStreamConverter isc = new InputStreamConverter();
			String returnStr = isc.isToString(in);

			return returnStr;
		}
	}

	private void Login() {
		Intent intent = new Intent();
		intent.setClass(this, ChatRoomList.class);

		Bundle bundle = new Bundle();
		bundle.putString("Username", m_edt_username.getText().toString());
		intent.putExtras(bundle);

		startActivity(intent);
		this.finish();
	}
}
