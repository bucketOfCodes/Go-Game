package swe681.resources;

import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DataDriver {

	private Connection conn = null;
 
	private PreparedStatement pStmt;

	// Connection to database
	public DataDriver() {
		try {
			Class.forName("com.mysql.jdbc.Driver");  
			conn=DriverManager.getConnection(  
			"jdbc:mysql://localhost:3306/swe681","root","root"); 
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			DataSource ds = (DataSource) envContext.lookup("jdbc/swe681");
			conn = ds.getConnection();
		} catch (Exception ex) {
			System.out.println("exception initalizing DataDriver: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private void closePrepSatement() {
		try {
			if (!pStmt.isClosed())
				pStmt.close();
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in running DataDriver.closePrepSatement: " + e.getMessage());
		}
	}

	private void closeRS(ResultSet rs) {
		try {
			if (!rs.isClosed()) {
				rs.close();
			}
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in running DataDriver.closeRS: " + e.getMessage());
		}
	}
	
	// Creating profile for the user who creates account
	public UserProfile createUserProfile(String username, String loginname, byte[] pass, byte[] salt) {
		try {
			boolean userCreated = insertUserProfile(username, loginname, pass, salt);
			if (!userCreated) {
				return null;
			}
			ResultSet rs = getPlayerByLoginnameRS(loginname);
			if (rs == null) {
				closePrepSatement();
				return null;
			}
			ArrayList<UserProfile> users = this.mapResultSetToUserProfiles(rs);
			if (users != null && !users.isEmpty()) {
				closeRS(rs);
				closePrepSatement();
				return users.get(0);
			}
			closeRS(rs);
			closePrepSatement();
		} catch (Exception e) {
			AppLog.getLogger().severe(
					"There was an exception running DataDriver.createUserProfile: " + e.getMessage());
		}
		return null;
	}

	private boolean insertUserProfile(String username, String loginname, byte[] pass, byte[] salt) {
		int rowsUpdated = 0;
		try {
			pStmt = conn.prepareStatement(
					"INSERT INTO UserProfile(Username, Loginname, PasswordHash, Hashsalt, CurrentGameId, Wins, Losses, PasswordAttempts, PasswordLockout) SELECT ?, ?, ?, ?, null, 0, 0, 0, null FROM dual WHERE NOT EXISTS (SELECT * FROM UserProfile Where Username = ? OR Loginname = ?)");
			pStmt.setString(1, username);
			pStmt.setString(2, loginname);
			pStmt.setBytes(3, pass);
			pStmt.setBytes(4, salt);			
			pStmt.setString(5, username);
			pStmt.setString(6, loginname);
			rowsUpdated = pStmt.executeUpdate();
			closePrepSatement();
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("There was an exception running DataDriver.insertUserProfile: " + e.getMessage());
		}
		return rowsUpdated > 0;
	}
	
	public boolean setPasswordLockout(String loginname, Timestamp time) {
		try {
			pStmt = conn.prepareStatement("UPDATE UserProfile SET PasswordLockout = ? WHERE Loginname = ?");
			pStmt.setTimestamp(1, time);
			pStmt.setString(2, loginname);
			pStmt.execute();
			closePrepSatement();
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("Exception in running DataDriver.setPasswordLockout: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	// Updating Password attempts 
	public boolean setPasswordAttempts(String loginname, int attempts) {
		try {
			pStmt = conn.prepareStatement("UPDATE UserProfile SET PasswordAttempts = ? WHERE Loginname = ?");
			pStmt.setInt(1, attempts);
			pStmt.setString(2, loginname);
			pStmt.execute();
			closePrepSatement();
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("There was an exception running DataDriver.setPasswordAttempts: " + e.getMessage());
			return false;
		}
		return true;
	}

	// Updating the winner and looser
	public boolean updateWinnerAndLooser(String winner, String looser) {
		try {
			pStmt = conn.prepareStatement("UPDATE UserProfile SET Wins = Wins+1  WHERE Loginname = ?");
			pStmt.setString(1, winner);
			pStmt.execute();
			closePrepSatement();
			pStmt = conn.prepareStatement("UPDATE UserProfile SET Losses = Losses+1  WHERE Loginname = ?");
			pStmt.setString(1, looser);
			pStmt.execute();
			closePrepSatement();
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("Exception in running DataDriver.updateWinnerAndLooser: " + e.getMessage());
			return false;
		}
		return true;
	}


	public boolean clearCurrentGameForUser(String loginname) {
		try {
			pStmt = conn.prepareStatement("UPDATE UserProfile SET CurrentGameId = null WHERE Loginname = ?");
			pStmt.setString(1, loginname);
			pStmt.execute();
			closePrepSatement();
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("Exception in running DataDriver.clearCurrentGameForUser: " + e.getMessage());
			return false;
		}
		return true;
	}

	
	// Update Game in the database
	public boolean updateGameData(GameInstance gameinstance, String player, String move) {	
		boolean gameInstanceUpdated = updateGameInstance(gameinstance);
		boolean gameStateUpdated = updateGameState(gameinstance.gameId, gameinstance.gameState);
		boolean logSuccess = logGameMove(gameinstance.gameId, player, move);
		closePrepSatement();
		return gameInstanceUpdated && gameStateUpdated && logSuccess;
	}

	private boolean updateGameInstance(GameInstance gameinstance) {
		try {
			pStmt = conn.prepareStatement(
					"UPDATE GameInstance SET Player1 = ?, Player2 = ?, CurrentState = ?, CurrentPlayerTurn = ? , NumPasses = ?, Player1Prisoners = ?, Player2Prisoners = ?, Player1FinalScore = ?, Player2FinalScore = ?, Info = ?, TimeSinceLastMove = ? WHERE GameId = ?");
			pStmt.setString(1, gameinstance.player1);
			pStmt.setString(2, gameinstance.player2);
			pStmt.setString(3, gameinstance.currentState);
			pStmt.setString(4, gameinstance.currentPlayerTurn);
			pStmt.setInt(5, gameinstance.numPasses);
			pStmt.setInt(6, gameinstance.player1Prisoners);
			pStmt.setInt(7, gameinstance.player2Prisoners);
			pStmt.setInt(8, gameinstance.player1FinalScore);
			pStmt.setInt(9, gameinstance.player2FinalScore);
			pStmt.setString(10, gameinstance.info);
			java.util.Date date = new java.util.Date();
			pStmt.setLong(11, date.getTime());
			pStmt.setInt(12, gameinstance.gameId);
			pStmt.execute();
			closePrepSatement();
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("Exception in running DataDriver.updateGameInstance: " + e.getMessage());
			return false;
		}
		return true;
	}

	// Updating each position on the board 
	private boolean updateGameState(int gameId, String[][] gamestate) { 
		try {
			for (int r = 0; r < gamestate.length; r++) {
				for (int col = 0; col < gamestate[r].length; col++) {
					String row = null;
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
						continue;
					}
					
					String color = gamestate[r][col];
					if (color == null)
						color = "";
					pStmt = conn.prepareStatement(
							"insert into GameState (GameId, BoardRow, BoardCol, OwnedBy) values (?, ?, ?, ?) on DUPLICATE KEY UPDATE OwnedBy = ? ");
					pStmt.setInt(1, gameId);
					pStmt.setString(2, row);
					pStmt.setInt(3, col);
					pStmt.setString(4, color);					
					pStmt.setString(5, color);
					pStmt.execute();
				}
			}
		}	
		
		catch (Exception e) {
			AppLog.getLogger().severe("Exception in running DataDriver.updateGameState: " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean logGameMove(int gameId, String player, String move) {
		try { 
			AppLog.getLogger().severe("inside loggamemove");
			pStmt = conn.prepareStatement(
					"INSERT INTO GameLog(GameId, Player, MovePlayed, DatePlayed) values (?, ?, ?, ?)");
			pStmt.setInt(1, gameId);
			pStmt.setString(2, player);
			pStmt.setString(3, move);
			pStmt.setString(4,
					new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Timestamp(System.currentTimeMillis())));
			pStmt.executeUpdate();
			closePrepSatement();
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in running DataDriver.logGameMove: " + e.getMessage());
		}

		return true;
	}

	
	// Updating Second Player to the database
	public boolean userJoinGame(double gameId, String loginnamePlayer2) {
		try {
			conn.setAutoCommit(false);
			pStmt = conn.prepareStatement(
					"update UserProfile set CurrentGameId = ? where Loginname = ? and exists (select Loginname from (Select * from UserProfile) as t where Loginname = ? AND (CurrentGameId = 0 OR CurrentGameId IS NULL))");
			pStmt.setDouble(1, gameId);
			pStmt.setString(2, loginnamePlayer2);
			pStmt.setString(3, loginnamePlayer2);
			pStmt.execute();

			pStmt = conn.prepareStatement("update GameInstance set Player2 = ?, TimeSinceLastMove = ? where GameId = ? and exists (select Player1 from (Select * from GameInstance) as j where GameId = ? AND Player2 is null) ");
			pStmt.setString(1, loginnamePlayer2);
			java.util.Date date = new java.util.Date();
			pStmt.setLong(2, date.getTime());
			pStmt.setDouble(3, gameId);
			pStmt.setDouble(4, gameId);
			pStmt.execute();
			conn.commit();
			conn.setAutoCommit(true);
			closePrepSatement();
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				AppLog.getLogger().severe("Exception in running DataDriver.userJoinGame when trying to rollback transaction: " + e1.getMessage());
				return false;
			}
			AppLog.getLogger().severe("Exception in running DataDriver.userJoinGame: " + e.getMessage());
			return false;
		}
		return true;
	}

	public GameInstance getGameInstanceDetails(int gameId) {
		try {
			ResultSet rsInstance = getGameInstanceRS(gameId);
			ArrayList<GameInstance> games = mapResultSetToGameInstance(rsInstance);
			closeRS(rsInstance);
			closePrepSatement();
			if (games != null && !games.isEmpty()) {
				GameInstance game = games.get(0);
				ResultSet rsState = getGameStateRS(gameId);
				game.gameState = mapResultSetToStringArray(rsState);
				closeRS(rsState);
				closePrepSatement();
				return game;
			}
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("Exception in running DataDriver.getGameInstanceDetails: " + e.getMessage());
		}
		return null;
	}

	public ArrayList<UserProfile> getAllPlayers() {
		try {
			ResultSet rs = this.getAllPlayersRS();
			ArrayList<UserProfile> players = this.mapResultSetToUserProfiles(rs);
			closeRS(rs);
			closePrepSatement();
			return players;
		} catch (Exception e) {
			AppLog.getLogger().severe("There was an exception running DataDriver.getAllPlayers: " + e.getMessage());
		}
		return null;
	}

	private ResultSet getAllPlayersRS() {
		ResultSet rs = null;
		try {	
			pStmt = conn.prepareStatement(
					"select Username, Loginname, PasswordHash, HashSalt, CurrentGameId, Wins, Losses, PasswordAttempts, PasswordLockout  from UserProfile");
			rs = pStmt.executeQuery();
		}
		catch (Exception e) {
			AppLog.getLogger().severe("Exception in running DataDriver.getAllPlayersRS: " + e.getMessage());
		}
		return rs;
	}

	public UserProfile getPlayerByLoginname(String loginname) {
		try {
			ResultSet rs = getPlayerByLoginnameRS(loginname);
			if (rs == null) {
				return null;
			}
			ArrayList<UserProfile> users = this.mapResultSetToUserProfiles(rs);
			if (users != null && !users.isEmpty()) {
				return users.get(0);
			}
			closeRS(rs);
			closePrepSatement();
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("Exception in running DataDriver.getPlayerByLoginname: " + e.getMessage());
		}
		return null;
	}

	public boolean usernameOrLoginnameAvailable(String username, String loginname) {
		try {
			ResultSet rs = getPlayerByUsernameOrLoginnameRS(username, loginname);
			if (rs == null) {
				return false; 
			}
			ArrayList<UserProfile> users = this.mapResultSetToUserProfiles(rs);
			closeRS(rs);
			closePrepSatement();
			if (users == null || users.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in running DataDriver.usernameOrLoginnameAvailable: " + e.getMessage());
		}
		return false;
	}


	private ResultSet getPlayerByLoginnameRS(String loginname) {
		ResultSet rs = null;
		try {
			pStmt = conn.prepareStatement(
					"select Username, Loginname, PasswordHash, HashSalt, CurrentGameId, Wins, Losses, PasswordAttempts, PasswordLockout from UserProfile where Loginname = ?");
			
			pStmt.setString(1, loginname);
			rs = pStmt.executeQuery();
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("There was an exception running DataDriver.getPlayerByLoginnameRS: " + e.getMessage());
		}
		return rs;
	}

	private ResultSet getPlayerByUsernameOrLoginnameRS(String username, String loginname) {
		ResultSet rs = null;
		try {
			pStmt = conn.prepareStatement(
					"select Username, Loginname, PasswordHash, HashSalt, CurrentGameId, Wins, Losses, PasswordAttempts, PasswordLockout  from UserProfile where Loginname = ? OR Username = ? ");
			pStmt.setString(1, loginname);
			pStmt.setString(2, username);
			rs = pStmt.executeQuery();
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("There was an exception running DataDriver.getPlayerByUsernameOrLoginnameRS: " + e.getMessage());
		}
		return rs;
	}

	private ArrayList<UserProfile> mapResultSetToUserProfiles(ResultSet rs) {
		ArrayList<UserProfile> ul = new ArrayList<UserProfile>();
		if (rs == null) {
			return ul;
		}
		try {
			while (rs.next()) {
				String Username = rs.getString("Username");
				String Loginname = rs.getString("Loginname");
				byte[] PasswordHash = rs.getBytes("PasswordHash");
				byte[] HashSalt = rs.getBytes("HashSalt");
				int CurrentGameId = rs.getInt("CurrentGameId");
				int Wins = rs.getInt("Wins");
				int Losses = rs.getInt("Losses");
				int attempts = rs.getInt("PasswordAttempts");
				Timestamp lockout = rs.getTimestamp("PasswordLockout");

				UserProfile user = new UserProfile();
				user.username = Username;
				user.loginname = Loginname;
				user.passwordHash = PasswordHash;
				user.hashSalt = HashSalt;
				user.currentGameId = CurrentGameId;
				user.wins = Wins;
				user.losses = Losses;
				user.passwordAttempts = attempts;
				user.passwordLockout = lockout;

				ul.add(user);
			}
		} catch (Exception se) {
			AppLog.getLogger().severe("Exception in DataDriver.mapResultSetToUserProfiles: " + se.getMessage());
		}
		return ul;
	}

	public ArrayList<GameInstance> getGameHistoryForPlayer(String playerLoginName) {
		ResultSet rs = getGameHistoryRS(playerLoginName);
		ArrayList<GameInstance> gs = mapResultSetToGameInstance(rs);
		closeRS(rs);
		closePrepSatement();
		return gs;
	}

	public ArrayList<GameInstance> getGameInstanceJoinable(String loginname) {
		ResultSet rs = getGameInstanceJoinableRS(loginname);
		ArrayList<GameInstance> gs = mapResultSetToGameInstance(rs);
		closeRS(rs);
		closePrepSatement();
		return gs;
	}

	public GameInstance getGameInProgressForUser(String loginname) {
		ResultSet rs = getGameInProgressForUserRS(loginname);
		ArrayList<GameInstance> games = mapResultSetToGameInstance(rs);
		if (games != null && !games.isEmpty()) {
			return games.get(0);
		}
		closeRS(rs);
		closePrepSatement();
		return null;
	}

	public boolean startNewGame(String loginname) {
		boolean gameCreated = false;
		try { 
			AppLog.getLogger().severe("--Game Started--");
			conn.setAutoCommit(false);
			pStmt = conn.prepareStatement(
					"INSERT INTO GameInstance(Player1, CurrentPlayerTurn, CurrentState, GameId) SELECT ?, ?, 'inprogress', ? where not exists (select GameId from GameInstance where (Player1 = ? OR Player2 = ?) AND CurrentState = 'inprogress') ");
			//TODO: Make Random secure
			Random rand = new Random();
	        int low = 10;
	        int high = 1000;
	        int result = 0;
	        result = rand.nextInt(high - low) + low;
	        String result1 = Integer.toString(result);
			pStmt.setString(1, loginname);
			pStmt.setString(2, loginname);		
			pStmt.setString(3, result1);
			pStmt.setString(4, loginname);
			pStmt.setString(5, loginname);
			pStmt.execute();
			closePrepSatement();
			
			pStmt = conn.prepareStatement(
					"Update UserProfile SET CurrentGameId = (Select GameId from GameInstance where Player1 = ? AND CurrentState = 'inprogress') WHERE Loginname = ?");
			pStmt.setString(1, loginname);
			pStmt.setString(2, loginname);
			pStmt.execute();
			conn.commit();
			conn.setAutoCommit(true);
			closePrepSatement();
			gameCreated = true;
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in running DataDriver.startNewGame: " + e.getMessage());
		}
		return gameCreated;
	}
	
	private ResultSet getGameInstanceJoinableRS(String loginname) { 
		ResultSet rs = null;
		try {
			pStmt = conn.prepareStatement(
					"select GameId, Player1, Player2, CurrentState, CurrentPlayerTurn, NumPasses,  Player1Prisoners, Player2Prisoners, Player1FinalScore, Player2FinalScore, Info, TimeSinceLastMove from GameInstance  where Player1 <> ? AND Player2 is null and CurrentState = 'inprogress'");
			pStmt.setString(1, loginname);
			rs = pStmt.executeQuery();
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("Exception in running DataDriver.getGameInstanceJoinableRS: " + e.getMessage());
		}
		return rs;
	}

	private ResultSet getGameInstanceRS(int gameId) {
		ResultSet rs = null;
		try {
			pStmt = conn.prepareStatement(
					"select GameId, Player1, Player2, CurrentState, CurrentPlayerTurn,NumPasses, Player1Prisoners, Player2Prisoners, Player1FinalScore, Player2FinalScore, Info, TimeSinceLastMove from GameInstance where GameId = ? ");
			pStmt.setInt(1, gameId);
			rs = pStmt.executeQuery();
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in running DataDriver.getGameInstanceRS: " + e.getMessage());
		}
		return rs;
	}

	private ResultSet getGameInProgressForUserRS(String loginname) {
		ResultSet rs = null;
		try {
			pStmt = conn.prepareStatement(
					"select GameId, Player1, Player2, CurrentState, CurrentPlayerTurn,NumPasses, Player1Prisoners, Player2Prisoners, Player1FinalScore, Player2FinalScore, Info, TimeSinceLastMove from GameInstance where (Player1 = ? OR Player2 = ?) AND CurrentState = 'inprogress'");
			pStmt.setString(1, loginname);
			pStmt.setString(2, loginname);
			rs = pStmt.executeQuery();
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("Exception in running DataDriver.getGameInProgressForUserRS: " + e.getMessage());
		}
		return rs;
	}

	private ResultSet getGameStateRS(int gameId) {
		ResultSet rs = null;
		try {
			pStmt = conn.prepareStatement("select GameId, BoardRow, BoardCol, OwnedBy from GameState where GameId = ? ");
			pStmt.setInt(1, gameId);
			rs = pStmt.executeQuery();
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in running DataDriver.getGameStateRS: " + e.getMessage());
		}
		return rs;
	}

	private ResultSet getGameHistoryRS(String playerLoginName) {
		ResultSet rs = null;
		try {
			pStmt = conn.prepareStatement("select GameId, Player1, Player2, CurrentState, CurrentPlayerTurn,NumPasses, Player1Prisoners, Player2Prisoners, Player1FinalScore, Player2FinalScore, Info, TimeSinceLastMove from GameInstance where (Player2 = ? or Player1 = ?) and CurrentState = 'done'");
			pStmt.setString(1, playerLoginName);
			pStmt.setString(2, playerLoginName);
			rs = pStmt.executeQuery();
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in running DataDriver.getGameHistoryRS: " + e.getMessage());
		}
		return rs;
	}

	private ArrayList<GameInstance> mapResultSetToGameInstance(ResultSet rs) {
		ArrayList<GameInstance> ll = new ArrayList<GameInstance>();
		if (rs == null) {
			return ll;
		}
		try {

			// Fetch each row from the result set
			while (rs.next()) {
				int gameId = rs.getInt("GameId");
				String player1 = rs.getString("Player1");
				String player2 = rs.getString("Player2");
				String currentState = rs.getString("CurrentState");
				String currentPlayerTurn = rs.getString("CurrentPlayerTurn");
				int numPasses = rs.getInt("NumPasses");
				int player1Prisoners = rs.getInt("Player1Prisoners");
				int player2Prisoners = rs.getInt("Player2Prisoners");
				int player1FinalScore = rs.getInt("Player1FinalScore");
				int player2FinalScore = rs.getInt("Player2FinalScore");
				String info = rs.getString("Info");
				long timeSinceLastMove = rs.getLong("TimeSinceLastMove");

				GameInstance game = new GameInstance();
				game.gameId = gameId;
				game.player1 = player1;
				game.player2 = player2;
				game.currentState = currentState;
				game.currentPlayerTurn = currentPlayerTurn;
				game.numPasses = numPasses;
				game.player1Prisoners = player1Prisoners;
				game.player2Prisoners = player2Prisoners;
				game.player1FinalScore = player1FinalScore;
				game.player2FinalScore = player2FinalScore;
				game.info = info;
				game.timeSinceLastMove = timeSinceLastMove;

				ll.add(game);
			}
		} catch (Exception se) {
			AppLog.getLogger().severe("Exception in DataDriver.mapResultSetToGameInstance: " + se.getMessage());
		}
		return ll;
	}

	public String[][] mapResultSetToStringArray(ResultSet rs) {
		String[][] stringArray = new String[9][9];
		try {
			while (rs.next()) {
				String row = rs.getString("BoardRow");
				int col = rs.getInt("BoardCol");
				String player = rs.getString("OwnedBy");
				if (player == null)
					player = "";
				if (row.equals("A")) {
					stringArray[0][col] = player;
				} else if (row.equals("B")) {
					stringArray[1][col] = player;
				} else if (row.equals("C")) {
					stringArray[2][col] = player;
				} else if (row.equals("D")) {
					stringArray[3][col] = player;
				} else if (row.equals("E")) {
					stringArray[4][col] = player;
				} else if (row.equals("F")) {
					stringArray[5][col] = player;
				} else if (row.equals("G")) {
					stringArray[6][col] = player;
				} else if (row.equals("H")) {
					stringArray[7][col] = player;
				} else if (row.equals("I")) {
					stringArray[8][col] = player;
				}
			}
		} catch (Exception e) {
			AppLog.getLogger()
					.severe("Exception in running DataDriver.mapResultSetToStringArray: " + e.getMessage());
		}
		return stringArray;
	}

	public ArrayList<GameLog> getGameLogForInstance(double gameId) {
		ResultSet rs = getGameLogForInstanceResultSet(gameId);
		ArrayList<GameLog> logs = mapResultSetToGameLog(rs);
		closeRS(rs);
		closePrepSatement();
		return logs;
	}

	private ResultSet getGameLogForInstanceResultSet(double gameId) {
		ResultSet rs = null;
		try {
			pStmt = conn.prepareStatement("select GameId, Player, MovePlayed, DatePlayed from GameLog where GameId = ? order by DatePlayed desc");
			pStmt.setDouble(1, gameId);
			rs = pStmt.executeQuery();
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in running DataDriver.getGameLogForInstanceResultSet: " + e.getMessage());
		}
		return rs;
	}

	private ArrayList<GameLog> mapResultSetToGameLog(ResultSet rs) {
		ArrayList<GameLog> ul = new ArrayList<GameLog>();
		try {
			// Fetch each row from the result set
			while (rs.next()) {
				int gameId = rs.getInt("GameId");
				String player = rs.getString("Player");
				String move = rs.getString("MovePlayed");
				String time = rs.getString("DatePlayed");
				GameLog log = new GameLog();
				log.gameId = gameId;
				log.player = player;
				log.movePlayed = move;
				log.datePlayed = time;
				ul.add(log);
			}
		} catch (Exception se) {
			AppLog.getLogger().severe("Exception in DataDriver.mapResultSetToGameLog: " + se.getMessage());
		}
		return ul;
	}

}
