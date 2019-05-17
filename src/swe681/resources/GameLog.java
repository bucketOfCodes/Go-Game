package swe681.resources;


public class GameLog {

	public double gameId;
	public String player;
	public String movePlayed;
	public String datePlayed;
	
	public double getGameId() {
		return this.gameId;
	}
	
	public String getPlayer() {
		return this.player;
	}
	
	public String getMovePlayed() {
		return this.movePlayed;
	}
	
	public String getDatePlayed() {
		String date = new String(this.datePlayed);
		return date;
	}
}
