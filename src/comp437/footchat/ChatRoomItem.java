package comp437.footchat;

import java.io.Serializable;

import android.R.string;

public class ChatRoomItem implements Serializable {
	public int chatroomId;
	public String description;
	public String status;
	public int gameId;
	public boolean favour;

	/**
	 * @param args
	 */

	public ChatRoomItem(int chatroomId, String description, String status,
			int gameId, boolean favour) {
		this.chatroomId = chatroomId;
		this.description = description;
		this.status = status;
		this.gameId = gameId;
		this.favour = favour;
	}

}
