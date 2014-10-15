package comp437.footchat;

import java.io.Serializable;

public class PlayerRatingItem implements Serializable {

	public int chatroomId;
	public int gameId;
	public int gamePlayerId;
	public int gamePlayerNo;
	public double gamePlayerAllAvgRating;
	public int gamePlayerUserRating;
	public String gamePlayerName;
	public String gamePlayerPositionType;
	public String gamePlayerTeamName;
	public String team;

	/**
	 * @param args
	 * @return
	 */

	public PlayerRatingItem(int chatroomId, int gameId, int gamePlayerId,
			int gamePlayerNo, double gamePlayerAllAvgRating,
			int gamePlayerUserRating, String gamePlayerName,
			String gamePlayerPositionType, String gamePlayerTeamName,
			String team) {
		this.chatroomId = chatroomId;
		this.gameId = gameId;
		this.gamePlayerId = gamePlayerId;
		this.gamePlayerNo = gamePlayerNo;

		this.gamePlayerAllAvgRating = gamePlayerAllAvgRating;
		this.gamePlayerUserRating = gamePlayerUserRating;

		this.gamePlayerName = gamePlayerName;
		this.gamePlayerPositionType = gamePlayerPositionType;
		this.gamePlayerTeamName = gamePlayerTeamName;
		this.team = team;

	}

}