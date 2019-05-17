package swe681.resources;


public class GameInstance {
	public int gameId ;
	public String player1;
	public String player2;
	public String currentState;
	public String currentPlayerTurn;
	public int numPasses;
	public int player1Prisoners;
	public int player2Prisoners;;
	public int player1FinalScore;
	public int player2FinalScore;	
	public String info;
	public long timeSinceLastMove;
	


	public String[][] gameState;
	
	public String[][] getGameState(){
		return this.gameState;
	}
	
	public int getGameId() {
		return this.gameId;
	}
	
	public int getNumPasses() {
		return this.numPasses;
	}
		
	public String getPlayer1() {
		return this.player1;
	}

	public String getPlayer2() {
		return this.player2;
	}
	
	public String getCurrentState() {
		return this.currentState;
	}
	
	public String getCurrentPlayerTurn() {
		return this.currentPlayerTurn;
	}
	
	public String getInfo() {
		return this.info;
	}
		
	public int getPlayer1Prisoners() {
		return this.player1Prisoners;
	}
	
	public int getPlayer2Prisoners() {
		return this.player2Prisoners;
	}
	
	public int getPlayer1FinalScore() {
		return this.player1FinalScore;
	}
	
	public int getPlayer2FinalScore() {
		return this.player2FinalScore;
	}
	
	public long getTimeSinceLastMove() {
		return this.timeSinceLastMove;
	}
}
