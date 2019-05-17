package swe681.resources;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.http.HttpServletRequest;

public class AuthenticationService {

	public UserProfile user;
	private DataDriver dataDriver;
	private ContextHelper contextHelper;
	static final int HASH_ITERATIONS = 10;
	static final int HASH_KEYLEN = 256;
	
	public AuthenticationService(HttpServletRequest request, DataDriver dataDriver, ContextHelper contextHelper) {
		this.dataDriver = dataDriver;
		this.contextHelper = contextHelper;
	}
	public enum AuthResult {
		UserTaken, UserCreated, Error, LoginSuccess, LockedOut, LoginFail
	};

	// Authenticating User based on password
	public AuthResult loginUser(String loginName, String password) throws InvalidKeySpecException {
		user = dataDriver.getPlayerByLoginname(loginName);
		if (user == null) {
			return AuthResult.LoginFail;
		} else if (user.passwordLockout != null && (user.passwordLockout.getTime() - System.currentTimeMillis() > 0)) {
			return AuthResult.LockedOut; // User is locked
		} else {
			byte[] hashPassword = hashPassword(password.toCharArray(), user.hashSalt, HASH_ITERATIONS, HASH_KEYLEN); // Hash is calculated
			if (Arrays.equals(hashPassword, user.passwordHash)) {
				contextHelper.setLoggedInUser(user);
				dataDriver.setPasswordAttempts(user.loginname, 0);
				dataDriver.setPasswordLockout(user.loginname, new Timestamp(System.currentTimeMillis() - 300000));
				return AuthResult.LoginSuccess;
			} else {
				int attempts = user.passwordAttempts + 1;
				if (attempts < 3) {
					dataDriver.setPasswordAttempts(user.loginname, attempts);
					return AuthResult.LoginFail;	// Login fails if number if password attempts exceeds
				} else {
					dataDriver.setPasswordLockout(user.loginname, new Timestamp(System.currentTimeMillis() + 300000));
					dataDriver.setPasswordAttempts(user.loginname, 0);
					return AuthResult.LoginFail;
				}
			}
		}
	}

	
	// Check Account available in database and create account
	public AuthResult createAccount(String userName, String loginname, String password) throws InvalidKeySpecException {
		if (userName == null || userName.trim().isEmpty() || loginname == null || loginname.trim().isEmpty()
				|| password == null || password.trim().isEmpty()) {
			return null;
		}
		boolean nameAvailable = dataDriver.usernameOrLoginnameAvailable(userName, loginname);
		if (!nameAvailable) {
			return AuthResult.UserTaken;
		}
		
		// Salt is created
		byte[] salt = new byte[100];
		new SecureRandom().nextBytes(salt);
		byte[] hashPassword = hashPassword(password.toCharArray(), salt, HASH_ITERATIONS, HASH_KEYLEN);

		user = dataDriver.createUserProfile(userName, loginname, hashPassword, salt);
		if (user != null) {
			contextHelper.setLoggedInUser(user);
			return AuthResult.UserCreated;
		}
		return AuthResult.Error;
	}

		
	// Hash is created for the password
	public static byte[] hashPassword(final char[] password, final byte[] salt, final int iterations,
			final int keyLength) throws InvalidKeySpecException {

		try {
			SecretKeyFactory seckey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
			SecretKey key = seckey.generateSecret(spec);
			byte[] res = key.getEncoded();
			return res;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
