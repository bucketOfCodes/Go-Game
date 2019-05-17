package swe681.resources;


import java.util.ArrayList;
import java.util.Locale;

public class GoLogic {

	private GameInstance gameInstance;
	private ArrayList<String> potentialCaptures;
	
	
	public enum MoveResult {
		MoveInvalid, MoveSuccess, GameOver, MoveTaken, CaptureInvalid
	}

	public GoLogic(GameInstance gmIns) {
		this.gameInstance = gmIns;
	}

	public GameInstance getGameInstance() {
		return this.gameInstance;
	}

	public MoveResult playMove(String color, String move, String capture) {
		if(color == null ||  move == null || move.isEmpty())
			return MoveResult.MoveInvalid;
		if(!color.equals("white") && !color.equals("black")) 
			return MoveResult.MoveInvalid; 
		
		if (move.toUpperCase(Locale.ENGLISH).equals("PASS")) {
			// set pass on game instance
			if (this.gameInstance.numPasses < 2) {
				this.gameInstance.numPasses += 1;
				changeCurrentPlayer();
				return MoveResult.MoveSuccess;
			} else {
				// compute scores
				computeScores();
				this.gameInstance.currentState = "done";
				return MoveResult.GameOver;
			}
		} else {
			if (!getColorAtPosition(move).equals("")) {
				return MoveResult.MoveTaken;
			} else if (capture != null && !capture.isEmpty()
					&& color.equals(getColorAtPosition(capture))) {
				return MoveResult.CaptureInvalid;
			}
			if (!setBoardPositionColor(color, move)) {
				return MoveResult.MoveInvalid;
			}
			// Checking the capture after the move is set
			computeCapture(color, capture);
			changeCurrentPlayer();
			// resetting the pass
			this.gameInstance.numPasses = 0;
			return MoveResult.MoveSuccess;
		}
	}

	private ArrayList<String> controlledEmptySpaces;
	private int numSpacesInArea;
	private String controllingColor;
	private boolean controlledBySingleColor;
	
	private void computeScores() {
		// check for controlled sections of board
		// find an empty spot
		controlledEmptySpaces = new ArrayList<String>();
		String emptySpot = getNextEmptySpot();
		while (emptySpot != null && !emptySpot.isEmpty()) {
			numSpacesInArea = 0;
			controlledBySingleColor = true;
			controllingColor = null;
			recursiveControlCheck(emptySpot);
			if(controlledBySingleColor) {
				if(controllingColor.equals("black")) {
					this.gameInstance.player1FinalScore += numSpacesInArea;
				}else {
					this.gameInstance.player2FinalScore += numSpacesInArea;					
				}
			}
			emptySpot = getNextEmptySpot();
		}
		this.gameInstance.player1FinalScore = this.gameInstance.player1FinalScore + this.gameInstance.player1Prisoners;
		this.gameInstance.player2FinalScore = this.gameInstance.player2FinalScore + this.gameInstance.player2Prisoners;		
	}
		
	private void recursiveControlCheck(String checkPosition) {
		if (checkPosition == null || checkPosition.isEmpty()) {
			return;
		}
		String colorAtPosition = getColorAtPosition(checkPosition);
		if(colorAtPosition != null && !colorAtPosition.isEmpty()) {
			if(controllingColor == null || controllingColor.isEmpty()) {
				controllingColor = colorAtPosition;
			}else if(!controllingColor.equals(colorAtPosition)) {
				controlledBySingleColor = false;
			}
		}
		else {
			numSpacesInArea += 1;
			controlledEmptySpaces.add(checkPosition);
			String top = getPositionAbove(checkPosition);
			if (top != null && !top.isEmpty() && !controlledEmptySpaces.contains(top)) {
				recursiveControlCheck(top);
			}
			String bottom = getPositionBelow(checkPosition);
			if (bottom != null && !bottom.isEmpty() && !controlledEmptySpaces.contains(bottom)) {
				recursiveControlCheck(bottom);
			}
			String left = getPositionLeft(checkPosition);
			if (left != null && !left.isEmpty() && !controlledEmptySpaces.contains(left)) {
				recursiveControlCheck(left);
			}
			String right = getPositionRight(checkPosition);
			if (right != null && !right.isEmpty() && !controlledEmptySpaces.contains(right)) {
				recursiveControlCheck(right);
			}			
		}	
	}
	
	private void computeCapture(String colorOfCurrentPlayer, String captureMove) {
		// see if any tokens are captured
		potentialCaptures = new ArrayList<String>();
		String captureColor = "";
		if (colorOfCurrentPlayer.equals("white")) {
			captureColor = "black";
		} else {
			captureColor = "white";
		}
		recursiveCaptureCheck(captureMove, captureColor);
		// update the game board
		int prisoners = 0;
		if (potentialCaptures != null && potentialCaptures.size() > 0) {
			prisoners = potentialCaptures.size();
			for (String str : potentialCaptures) {
				setBoardPositionColor("", str);
			}
			if (this.gameInstance.currentPlayerTurn.equals(this.gameInstance.player1)) {
				this.gameInstance.player1Prisoners += prisoners;
			} else {
				this.gameInstance.player2Prisoners += prisoners;
			}
		}
	}


	// Checking for the empty spot
	private String getNextEmptySpot() {
		for (int r = 0; r <= 8; r++) {
			for (Integer c = 0; c <= 8; c++) {
				String thisPosColor = this.gameInstance.gameState[r][c];
				if (thisPosColor == null || thisPosColor.isEmpty()) {
					String row = "";
					switch (r) {
					case 0:
						row = "A";
						break;
					case 1:
						row = "B";
						break;
					case 2:
						row = "C";
						break;
					case 3:
						row = "D";
						break;
					case 4:
						row = "E";
						break;
					case 5:
						row = "F";
						break;
					case 6:
						row = "G";
						break;
					case 7:
						row = "H";
						break;
					case 8:
						row = "I";
						break;
					default:
						return null;
					}
					String position = row + c.toString();
					if(!controlledEmptySpaces.contains(position)) {
						return position;
					}
				}
			}
		}
		return null;
	}


	private void recursiveCaptureCheck(String checkPosition, String colorToCapture) {
		if (checkPosition == null || checkPosition.isEmpty()) {
			return;
		}
		String colorAtPosition = getColorAtPosition(checkPosition);
		if (colorAtPosition == null || colorAtPosition.isEmpty()) {
			// position is an open slot, clear all potential captures and return
			potentialCaptures = new ArrayList<String>();
			return;
		}
		if (colorAtPosition.equals(colorToCapture)) {
			potentialCaptures.add(checkPosition);
			String top = getPositionAbove(checkPosition);
			if (top != null && !top.isEmpty() && !potentialCaptures.contains(top)) {
				recursiveCaptureCheck(top, colorToCapture);
			}
			String bottom = getPositionBelow(checkPosition);
			if (bottom != null && !bottom.isEmpty() && !potentialCaptures.contains(bottom)) {
				recursiveCaptureCheck(bottom, colorToCapture);
			}
			String left = getPositionLeft(checkPosition);
			if (left != null && !left.isEmpty() && !potentialCaptures.contains(left)) {
				recursiveCaptureCheck(left, colorToCapture);
			}
			String right = getPositionRight(checkPosition);
			if (right != null && !right.isEmpty() && !potentialCaptures.contains(right)) {
				recursiveCaptureCheck(right, colorToCapture);
			}
		} else {
			return;
		}
	}

	private void changeCurrentPlayer() {
		if (this.gameInstance.currentPlayerTurn.equals(this.gameInstance.player1)) {
			this.gameInstance.currentPlayerTurn = this.gameInstance.player2;
		} else {
			this.gameInstance.currentPlayerTurn = this.gameInstance.player1;
		}
	}

	// Assigning the color for the position
	private boolean setBoardPositionColor(String color, String position) {
		if (this.gameInstance == null || this.gameInstance.gameState == null || position == null
				|| position.length() != 2) {
			return false;
		}
		try {
			char rowChar = position.charAt(0);
			String col = String.valueOf(position.charAt(1));
			int colInt = Integer.parseInt(col);
			switch (rowChar) {
			case 'A':
				return setPieceInState(0, colInt, color);
			case 'B':
				return setPieceInState(1, colInt, color);
			case 'C':
				return setPieceInState(2, colInt, color);
			case 'D':
				return setPieceInState(3, colInt, color);
			case 'E':
				return setPieceInState(4, colInt, color);
			case 'F':
				return setPieceInState(5, colInt, color);
			case 'G':
				return setPieceInState(6, colInt, color);
			case 'H':
				return setPieceInState(7, colInt, color);
			case 'I':
				return setPieceInState(8, colInt, color);
			}
		} catch (Exception e) {
			AppLog.getLogger().severe("Excpetion in GoLogic.moveIsValid(): " + e.getMessage());
			return false;
		}
		return false;
	}

	
	private String getColorAtPosition(String position) {
		if (position == null || position.isEmpty()) {
			return "";
		}
		char rowChar = position.charAt(0);
		//String rowchar2 = String.valueOf(rowChar);
		String col = String.valueOf(position.charAt(1));
		int colInt = Integer.parseInt(col);
		String color = "";
		switch (rowChar) {
		case 'A':
			color = this.gameInstance.gameState[0][colInt];
			break;
		case 'B':
			color = this.gameInstance.gameState[1][colInt];
			break;
		case 'C':
			color = this.gameInstance.gameState[2][colInt];
			break;
		case 'D':
			color = this.gameInstance.gameState[3][colInt];
			break;
		case 'E':
			color = this.gameInstance.gameState[4][colInt];
			break;
		case 'F':
			color = this.gameInstance.gameState[5][colInt];
			break;
		case 'G':
			color = this.gameInstance.gameState[6][colInt];
			break;
		case 'H':
			color = this.gameInstance.gameState[7][colInt];
			break;
		case 'I':
			color = this.gameInstance.gameState[8][colInt];
			break;
		default:
			color = "";
		}
		if (color == null) {
			color = "";
		}
		AppLog.getLogger().severe("Now the color at this position is " + color);
		return color;
	}

	// Getting position from all directions
	
	private String getPositionAbove(String thisPosition) {
		char rowChar = thisPosition.charAt(0);
		String col = String.valueOf(thisPosition.charAt(1));
		switch (rowChar) {
		case 'A':
			return "";
		case 'B':
			return "A" + col;
		case 'C':
			return "B" + col;
		case 'D':
			return "C" + col;
		case 'E':
			return "D" + col;
		case 'F':
			return "E" + col;
		case 'G':
			return "F" + col;
		case 'H':
			return "G" + col;
		case 'I':
			return "H" + col;
		default:
			return "";
		}
	}

	private String getPositionBelow(String thisPosition) {
		char rowChar = thisPosition.charAt(0);
		String col = String.valueOf(thisPosition.charAt(1));
		switch (rowChar) {
		case 'A':
			return "B" + col;
		case 'B':
			return "C" + col;
		case 'C':
			return "D" + col;
		case 'D':
			return "E" + col;
		case 'E':
			return "F" + col;
		case 'F':
			return "G" + col;
		case 'G':
			return "H" + col;
		case 'H':
			return "I" + col;
		case 'I':
			return "";
		default:
			return "";
		}
	}

	private String getPositionLeft(String thisPosition) {
		char rowChar = thisPosition.charAt(0);
		String col = String.valueOf(thisPosition.charAt(1));
		Integer colInt = Integer.parseInt(col);
		if (colInt <= 0) {
			return "";
		} else {
			return rowChar + (--colInt).toString();
		}
	}

	private String getPositionRight(String thisPosition) {
		char rowChar = thisPosition.charAt(0);
		String col = String.valueOf(thisPosition.charAt(1));
		Integer colInt = Integer.parseInt(col);
		if (colInt >= 8) {
			return "";
		} else {
			return rowChar + (++colInt).toString();
		}
	}

	// Initializing the color to gamestate
	private boolean setPieceInState(int row, int col, String color) {
		if (row >= 0 && row <= 8 && col >= 0 && col <= 8 && color != null
				&& (color.equals("white") || color.equals("black") || color.equals(""))) {
			this.gameInstance.gameState[row][col] = color;
			return true;
		}
		return false;
	}

}
