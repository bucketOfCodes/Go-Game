package swe681;

import java.util.List;

import javax.annotation.PostConstruct;

import swe681.resources.UserProfile;

public class WinsLossesBean extends BaseBean {

	public List<UserProfile> allUsers;
	
	@PostConstruct
	public void init() {
		this.allUsers = dataDriver.getAllPlayers();
	}
	
	public List<UserProfile> getAllUsers(){
		return this.allUsers;
	}	
}
