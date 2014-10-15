package comp437.footchat;

import java.io.IOException;
import java.io.InputStream;

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

public class LogInPage extends Activity implements OnClickListener {

	private final String login_ok = "ok";
	private final String login_not = "not";

	private EditText m_edt_username;
	private EditText m_edt_password;
	private TextView m_txt_error_username;
	private TextView m_txt_error_password;
	private Button m_btn_logIn;
	private TextView m_txt_register_link;

	protected loginThread m_loginThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_page);

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

	private void addListener() {
		m_btn_logIn.setOnClickListener(this);
		m_txt_register_link.setOnClickListener(this);
	}

	private void getWidget() {
		m_edt_username = (EditText) findViewById(R.id.edt_username);
		m_edt_password = (EditText) findViewById(R.id.edt_password);
		m_txt_error_username = (TextView) findViewById(R.id.txt_error_username);
		m_txt_error_password = (TextView) findViewById(R.id.txt_error_password);
		m_btn_logIn = (Button) findViewById(R.id.btn_logIn);
		m_txt_register_link = (TextView) findViewById(R.id.txt_register_link);
	}

	private void hideErrorMsg() {
		m_txt_error_username.setVisibility(m_txt_error_username.INVISIBLE);
		m_txt_error_password.setVisibility(m_txt_error_password.INVISIBLE);
	}

	private void hideAll() {
		// m_edt_username.setVisibility(m_edt_username.INVISIBLE);
		// m_edt_password.setVisibility(m_edt_password.INVISIBLE);
		hideErrorMsg();
		m_btn_logIn.setVisibility(m_btn_logIn.INVISIBLE);
		m_txt_register_link.setVisibility(m_txt_register_link.INVISIBLE);
	}

	private void showAll() {
		// m_edt_username.setVisibility(m_edt_username.VISIBLE);
		// m_edt_password.setVisibility(m_edt_password.VISIBLE);
		m_btn_logIn.setVisibility(m_btn_logIn.VISIBLE);
		m_txt_register_link.setVisibility(m_txt_register_link.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_logIn:
			hideErrorMsg();
			if (checkLength()) {
				hideAll();
				m_loginThread = new loginThread();
				m_loginThread.start();
			}
			break;
		case R.id.txt_register_link:
			Intent intent = new Intent();
			intent.setClass(this, RegistrationPage.class);
			startActivity(intent);
			this.finish();
			break;
		}
	}

	private boolean checkLength() {
		boolean returnBl = true;
		String usernameStr = m_edt_username.getText().toString();
		String passwordStr = m_edt_password.getText().toString();

		if (usernameStr.length() != 8) {
			returnBl = false;
			m_txt_error_username.setVisibility(m_txt_error_username.VISIBLE);
		}

		if (passwordStr.length() != 8) {
			returnBl = false;
			m_txt_error_password.setVisibility(m_txt_error_password.VISIBLE);
		}

		return returnBl;
	}

	public Handler loginHandler = new Handler() {
		public void handleMessage(Message msg) {
			String bl = (String) msg.obj;
			if (bl.equals(login_ok)) {
				login();
			} else {
				notLogin();
			}

			if (m_loginThread != null) {
				if (!m_loginThread.isInterrupted()) {
					m_loginThread.interrupt();
				}
			}
		}
	};

	private class loginThread extends Thread {
		public void run() {

			Message msg = new Message();
			boolean teapBl = false;
			try {
				teapBl = checkAccount(m_edt_username.getText().toString(),
						m_edt_password.getText().toString());
			} catch (IOException e) {

				e.printStackTrace();
			}
			if (teapBl) {
				msg.obj = login_ok;
			} else {
				msg.obj = login_not;
			}

			loginHandler.sendMessage(msg);
		}

		private boolean checkAccount(String un, String pw) throws IOException {
			PostRequest pr = new PostRequest();

			pr.setUrl(getString(R.string.post_url));
			pr.addPost(getString(R.string.post_requireType),
					getString(R.string.post_rt_accountChecking));
			pr.addPost(getString(R.string.post_sql_Username), m_edt_username
					.getText().toString());
			pr.addPost(getString(R.string.post_sql_Password), m_edt_password
					.getText().toString());

			InputStream in = pr.post();
			InputStreamConverter isc = new InputStreamConverter();
			String judge = isc.isToString(in);

			if (judge.equals(getString(R.string.post_return_OK))) {
				return true;
			} else {
				return false;
			}
		}

	}

	// =======================================

	private void notLogin() {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setMessage("Wrong password or username");
		dlgAlert.setTitle("Log in");
		dlgAlert.setPositiveButton("OK", null);
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();
		m_edt_username.setText("");
		m_edt_password.setText("");

		showAll();
	}

	private void login() {
		Intent intent = new Intent();
		intent.setClass(this, ChatRoomList.class);

		Bundle bundle = new Bundle();
		bundle.putString("Username", m_edt_username.getText().toString());
		intent.putExtras(bundle);

		startActivity(intent);
		this.finish();
	}
}
