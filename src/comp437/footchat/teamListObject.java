package comp437.footchat;

import java.io.Serializable;

public class teamListObject implements Serializable {
	public int teamId;
	public String teamName;
	private int imgId;

	/**
	 * @param args
	 */
	public teamListObject(int teamId, String teamName) {
		this.teamId = teamId;
		this.teamName = teamName;
	}

}
