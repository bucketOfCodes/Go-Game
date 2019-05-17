package swe681;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.model.ListDataModel;

import swe681.resources.GameInstance;
import swe681.resources.GameLog;

@ViewScoped
public class HistoryBean  extends BaseBean  {
	
	public HistoryBean() {
		super();
	}

	public ListDataModel<GameInstance> allPastGames;
	public List<GameLog> gameLogForInstance;
	
	
	@PostConstruct
	public void init() {		
		this.allPastGames = new ListDataModel<GameInstance>(dataDriver.getGameHistoryForPlayer(contextHelper.getLoggedInUser().loginname));
	}
	
	public ListDataModel<GameInstance> getAllPastGames(){
		return this.allPastGames;
	}	
	
	
	public List<GameLog> getGameLogForInstance(){
		return this.gameLogForInstance;
	}
		

	public String seeHistory() {		
		GameInstance game = allPastGames.getRowData();
		this.gameLogForInstance = dataDriver.getGameLogForInstance(game.gameId);
		
		return null;
	}
}
