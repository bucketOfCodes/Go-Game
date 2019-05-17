package swe681;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import swe681.resources.AppLog;
import swe681.resources.GameInstance;
import swe681.resources.GoLogic;
import swe681.resources.GoLogic.MoveResult;
import swe681.resources.UserProfile;

@ManagedBean
@RequestScoped
public class GameBean extends BaseBean {

	public GameInstance gameInstance;
	public String submittedMove;
	public String submittedCapture;
	public boolean isLoggedInUsersTurn;
	public boolean waitingForAnotherPlayer;
	public boolean isGameOver;
	public boolean isInvalid;
	public boolean won;
	public boolean lost;
	public boolean tie;

	public GameBean() {
		super();
	}

	private UserProfile currentUser;

	public String getSubmittedMove() {
		return this.submittedMove;
	}
	
	public void setSubmittedMove(String str) {
		this.submittedMove = str.trim();
	}

	public String getSubmittedCapture() {
		return this.submittedCapture;
	}

	public void setSubmittedCapture(String str) {
		this.submittedCapture = str.trim();
	}

	public GameInstance getGameInstance() {
		return this.gameInstance;
	}

	public boolean getIsLoggedInUsersTurn() {
		return this.isLoggedInUsersTurn;
	}

	public boolean getWaitingForAnotherPlayer() {
		return this.waitingForAnotherPlayer;
	}

	public boolean getIsGameOver() {
		return this.isGameOver;
	}

	public boolean getIsInvalid() {
		return this.isInvalid;
	}

	public boolean getWon() {
		return this.won;
	}

	public boolean getLost() {
		return this.lost;
	}

	public boolean getTie() {
		return this.tie;
	}
	
	
   // Getting Current Player color
	public String getCurrentPlayerColor() {
		try {
			if (this.currentUser.loginname.equals(this.gameInstance.player1)) {
				return "black";
				
			} else if (this.currentUser.loginname.equals(this.gameInstance.player2)) {
				return "white";
			
			} else {
				FacesContext.getCurrentInstance().addMessage("",
						new FacesMessage("You are not a valid player for this game."));
				return "";
			}
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in DataDriver.getCurrentPlayerColor: " + e.getMessage());
			return "";
		}
	}

	@PostConstruct
	public void init() {
		try {
			this.won = false;
			this.lost = false;
			this.tie = false;
			this.waitingForAnotherPlayer = false;
			this.isLoggedInUsersTurn = false;
			this.isGameOver = false;
			this.isInvalid = false;

			currentUser = contextHelper.getLoggedInUser();
			currentUser = dataDriver.getPlayerByLoginname(currentUser.loginname);
			contextHelper.setLoggedInUser(currentUser);
			this.gameInstance = dataDriver.getGameInstanceDetails(currentUser.currentGameId);

			if (gameInstance == null) {
				this.isInvalid = true;
				return;
			} else {
				this.isInvalid = false;
			}
			// "done" when game is completed
			if (this.gameInstance.currentState.equals("done")) {
				this.isGameOver = true;
			} else {
				this.isGameOver = false;
			}

			if (!this.gameInstance.player1.equals(currentUser.loginname)
					&& !this.gameInstance.player2.equals(currentUser.loginname)) {
				this.gameInstance = null;
				FacesContext.getCurrentInstance().addMessage("",
						new FacesMessage("You are not a valid player for this game."));
				return;
			}

			if (!this.gameInstance.player1.equals(this.gameInstance.currentPlayerTurn)
					&& !this.gameInstance.player2.equals(this.gameInstance.currentPlayerTurn)) {
				this.gameInstance = null;
				FacesContext.getCurrentInstance().addMessage("",
						new FacesMessage("Game has entered an illegal state and is invalidated."));
				this.isLoggedInUsersTurn = false;
				return;
			}

			
			if (this.gameInstance.player2 == null) {
				this.waitingForAnotherPlayer = true;
				return;
			} else {
				//Gameout is set to 3 minutes
				long threeMins = TimeUnit.MINUTES.toMillis(3);
				java.util.Date date = new java.util.Date();
				long currTime = date.getTime();
				if (this.gameInstance.timeSinceLastMove > 0
						&& currTime - this.gameInstance.timeSinceLastMove > threeMins) {
					// it's been longer than 3 mins, the current player looses
					String winner = "";
					String looser = "";
					if (this.gameInstance.currentPlayerTurn.equals(this.gameInstance.player1)) {
						looser = this.gameInstance.player1;
						winner = this.gameInstance.player2;
					} else {
						looser = this.gameInstance.player2;
						winner = this.gameInstance.player1;
					}
					this.gameInstance.currentState = "done";
					this.gameInstance.info = "Game ended because player timed out.";
					dataDriver.updateGameData(this.gameInstance, this.gameInstance.currentPlayerTurn, "Timeout");
					dataDriver.updateWinnerAndLooser(winner, looser);
					dataDriver.clearCurrentGameForUser(this.gameInstance.player1);
					dataDriver.clearCurrentGameForUser(this.gameInstance.player2);
					return;
				}
			}

			if (this.gameInstance.currentPlayerTurn.equals(currentUser.loginname)) {
				this.isLoggedInUsersTurn = true;
			}

		} catch (Exception e) {
			AppLog.getLogger().severe("There was an exception in GameBean.init(): " + e.getMessage());
		}
	}

	public String submitMove() {
		try {
			if (!submittedMove.matches("^([A-I][0-8]|(PASS)|(pass)|(Pass))$")) {
				FacesContext.getCurrentInstance().addMessage("", new FacesMessage("Not a valid move, enter position or 'PASS' to pass turn."));
				return null;
			}
			if (submittedCapture != null && !submittedCapture.isEmpty()
					&& !submittedCapture.matches("^([A-I][0-8])$")) {
				FacesContext.getCurrentInstance().addMessage("", new FacesMessage("Not a valid capture, enter position, or leave blank if not needed"));
				return null;
			}

			GoLogic go = new GoLogic(this.gameInstance);
			MoveResult result = go.playMove(getCurrentPlayerColor(), submittedMove, submittedCapture);

			switch (result) {
			case MoveInvalid:
				AppLog.getLogger().info("User tried to make an invalid move: " + this.currentUser.loginname + ", " + this.submittedMove);
				FacesContext.getCurrentInstance().addMessage("",
						new FacesMessage("That was not a valid move, please try again."));
				break;
			case MoveTaken:
				AppLog.getLogger().info("User tried to move to a spot that was already taken: " + this.currentUser.loginname + ", " + this.submittedMove);
				FacesContext.getCurrentInstance().addMessage("",
						new FacesMessage("That space is not empty, try another move."));
				break;
			case CaptureInvalid:
				AppLog.getLogger().info("User tried to make an illegal capture: " + this.currentUser.loginname + ", " + this.submittedCapture);
				FacesContext.getCurrentInstance().addMessage("",
						new FacesMessage("You cannot capture your own piece, try another move or capture."));
				break;
			case MoveSuccess:
				AppLog.getLogger().info("User made a successful move: " + this.currentUser.loginname + ", " + this.submittedMove);
				dataDriver.updateGameData(this.gameInstance, currentUser.username, submittedMove);
				this.isLoggedInUsersTurn = false;
				break;
			case GameOver:
				setWinner();
				String winner = "";
				String looser = "";
				this.isGameOver = true;
				if (this.gameInstance.player1FinalScore > this.gameInstance.player2FinalScore) {
					winner = this.gameInstance.player1;
					looser = this.gameInstance.player2;
				} else {
					winner = this.gameInstance.player2;
					looser = this.gameInstance.player1;
				}
				dataDriver.updateGameData(this.gameInstance, currentUser.username, submittedMove);
				dataDriver.updateWinnerAndLooser(winner, looser);
				dataDriver.clearCurrentGameForUser(this.gameInstance.player1);
				dataDriver.clearCurrentGameForUser(this.gameInstance.player2);
				AppLog.getLogger().info("Game ended: " + this.gameInstance.gameId);
				break;
			default:
				this.isLoggedInUsersTurn = false;
				break;
			}
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in GameBean.submitMove: " + e.getMessage());
		}
		return null;
	}

	// Assigning the Winner
	private void setWinner() {
		if (this.gameInstance.player1FinalScore == this.gameInstance.player2FinalScore) {
			this.tie = true;
		} else {
			if (this.gameInstance.player1.equals(currentUser.username)) {
				this.won = this.gameInstance.player1FinalScore > this.gameInstance.player2FinalScore;
				this.lost = this.gameInstance.player1FinalScore < this.gameInstance.player2FinalScore;
			} else {
				this.won = this.gameInstance.player2FinalScore > this.gameInstance.player1FinalScore;
				this.lost = this.gameInstance.player2FinalScore < this.gameInstance.player1FinalScore;
			}
		}
	}
}
