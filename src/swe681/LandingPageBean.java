package swe681;

import swe681.resources.*;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@RequestScoped
public class LandingPageBean extends BaseBean {

	public List<GameInstance> allJoinableGames;

	public LandingPageBean() {
		super();
	}

	@PostConstruct
	public void init() {
		UserProfile user = contextHelper.getLoggedInUser();
		user = dataDriver.getPlayerByLoginname(user.loginname);
		contextHelper.setLoggedInUser(user);
		this.allJoinableGames = dataDriver.getGameInstanceJoinable(user.loginname);
	}

	public void setAllJoinableGames(LinkedList<GameInstance> games) {
		this.allJoinableGames = games;
	}

	public List<GameInstance> getAllJoinableGames() {
		return this.allJoinableGames;
	}

	// To check the player eligible to join
	public String joinGame(GameInstance game) {
		AppLog.getLogger().info("Request to join game by he user: " + contextHelper.getLoggedInUser().loginname + ", " + game.gameId);
		try {
			UserProfile userInSession = contextHelper.getLoggedInUser();
			if (userInSession == null) {
				FacesContext.getCurrentInstance().addMessage("",
						new FacesMessage("There was a problem getting your account, please log in again."));
				return null;
			}

			UserProfile user = dataDriver.getPlayerByLoginname(userInSession.loginname);
			contextHelper.setLoggedInUser(user);
			if (user.currentGameId > 0) {
				FacesContext.getCurrentInstance().addMessage("",
						new FacesMessage("Already in a game, not allowed to join new"));
				return null;
			} else {
				// set gameId to the player
				boolean joinSuccess = dataDriver.userJoinGame(game.gameId, user.loginname);
				if (joinSuccess) {
					userInSession = dataDriver.getPlayerByLoginname(user.loginname);
					contextHelper.setLoggedInUser(userInSession);
					return "Game";
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage("",
					new FacesMessage("Error Occurred..!! -  Try Again"));
			AppLog.getLogger().severe("Exception in LandingPageBean.startNewGame " + e.getMessage());
			return null;
		}
	}

	public String startNewGame() {
		AppLog.getLogger().info("Request to start new game by user: " + contextHelper.getLoggedInUser().loginname);
		try {
			// check that this user is not already in a game
			UserProfile user = contextHelper.getLoggedInUser();
			GameInstance game = dataDriver.getGameInProgressForUser(contextHelper.getLoggedInUser().loginname);
			if (game != null || user.currentGameId > 0) {
				FacesContext.getCurrentInstance().addMessage("",
						new FacesMessage("You are already in a game, and cannot start a new one."));
				return null;
			}
			
			boolean gameCreated = dataDriver.startNewGame(user.loginname);
			if(gameCreated) {
				user = dataDriver.getPlayerByLoginname(user.loginname);
				contextHelper.setLoggedInUser(user);
				return "Game";
			}else {
				FacesContext.getCurrentInstance().addMessage("",
						new FacesMessage("An error has occurred, please close your browser and try again - startnewgame--."));
			}

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage("",
					new FacesMessage("An error has occurred, please close your browser and try again."));
			AppLog.getLogger().severe("An excpetion was thrown in LandingPageBean.startNewGame " + e.getMessage());
		}

		return null;
	}
	
	public String logout() {
		contextHelper.setLoggedInUser(null);
		contextHelper.abandonSession();
		return "Welcome";
	}

}
