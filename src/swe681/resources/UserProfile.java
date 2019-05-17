package swe681.resources;

import java.sql.Timestamp;

public class UserProfile {

	public String username;
	public String loginname;
	public byte[] passwordHash;
	public byte[] hashSalt;	
	public int currentGameId;
	public int wins;
	public int losses;
	public boolean isInGame;
	public int passwordAttempts;
	public Timestamp passwordLockout;

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String str) {
		this.username = str.trim();
	}

	public String getLoginname() {
		return this.loginname;
	}

	public void setLoginname(String str) {
		this.loginname = str.trim();
	}

	public byte[] getPasswordHash() {
		return this.passwordHash.clone();
	}

	public void setPasswordHash(byte[] str) {
		this.passwordHash = str.clone();
	}

	public byte[] getHashSalt() {
		return this.hashSalt.clone();
	}

	public void setHashSalt(byte[] str) {
		this.hashSalt = str.clone();
	}

	public int getCurrentGameId() {
		return this.currentGameId;
	}
	
	public boolean getIsInGame() {
		return this.currentGameId > 0;
	}

	public void setCurrentGameId(int id) {
		this.currentGameId = id;
	}

	public int getWins() {
		return this.wins;
	}

	public void setWins(int id) {
		this.wins = id;
	}

	public int getLosses() {
		return this.losses;
	}
	
	public int getPasswordAttempts() {
		return this.passwordAttempts;
	}
	
	public Timestamp getPasswordLockout() {
		if(this.passwordLockout == null) {
			return null;
		}
		return new Timestamp(this.passwordLockout.getTime());
	}

	public void setLosses(int id) {
		this.losses = id;
	}
}
