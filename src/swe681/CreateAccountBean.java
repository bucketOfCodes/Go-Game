package swe681;

import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import swe681.resources.AppLog;
import swe681.resources.AuthenticationService;
import swe681.resources.AuthenticationService.AuthResult;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;

@ManagedBean
@RequestScoped
public class CreateAccountBean extends BaseBean {
	public String username = "";
	public String loginname = "";
	public String password = "";
	public String passwordConfirm = "";

	public CreateAccountBean() {
		super();
	}

	public CreateAccountBean(String username, String loginname, String password, String passwordConfirm) {
		super();
		this.username = username;
		this.loginname = loginname;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
	}

	public void setUsername(String x) {
		this.username = x.trim();
	}

	public String getUsername() {
		return username;
	}

	public void setLoginname(String x) {
		this.loginname = x.trim();
	}

	public String getLoginname() {
		return loginname;
	}

	public void setPassword(String x) {
		this.password = x.trim();
	}

	public String getPassword() {
		return this.password;
	}

	public void setPasswordConfirm(String x) {
		this.passwordConfirm = x;
	}

	public String getPasswordConfirm() {
		return this.passwordConfirm.trim();
	}

	//Account creation is done with valid data
	public String createAccount() {
		try {
			if (	this.username == null || this.username.isEmpty() 
					|| this.loginname == null || this.loginname.isEmpty()
					|| this.password == null || this.password.isEmpty()
					|| this.passwordConfirm == null || this.passwordConfirm.isEmpty()) {
				FacesContext.getCurrentInstance().addMessage("",
						new FacesMessage("Enter a valid user name, login name, password, and password confirmation."));
				return null;
			}
			
			if(!this.password.equals(this.passwordConfirm)) {
				FacesContext.getCurrentInstance().addMessage("", new FacesMessage("Password do not match."));		
				return null;
			}
			
			if(!this.username.matches("^([A-Za-z0-9]{5,20})$")) {
				FacesContext.getCurrentInstance().addMessage("", new FacesMessage("User name must be between 5 and 20 characters - (Alphanumeric only)"));			
				return null;
			}
			
			if(!this.loginname.matches("^([A-Za-z0-9]{5,20})$")) {
				FacesContext.getCurrentInstance().addMessage("", new FacesMessage("Login name must be between 5 and 20 characters - (Alphanumeric only)"));			
				return null;
			}
			
			if(!this.password.matches("^([A-Za-z0-9\\!\\@\\#\\$\\%\\^\\&\\*\\?\\.\\,\\;\\:]{5,20})$")) {
				FacesContext.getCurrentInstance().addMessage("", new FacesMessage("Password must be between 5 and 20 characters, alphanumeric or special characters"));			
				return null;
			}
			
			if(!this.passwordConfirm.matches("^([A-Za-z0-9\\!\\@\\#\\$\\%\\^\\&\\*\\?\\.\\,\\;\\:]{5,20})$")) {
				FacesContext.getCurrentInstance().addMessage("", new FacesMessage("Password confirm must be between 5 and 20 characters, alphanumeric or special characters"));			
				return null;
			}
			
			// After Valid Account Creation 
			AuthenticationService auth = new AuthenticationService(contextHelper.getRequest(), dataDriver, contextHelper);
			AuthResult result = auth.createAccount(this.username, this.loginname, this.password);
			switch (result) {
			case UserTaken:
				FacesContext.getCurrentInstance().addMessage("", new FacesMessage("Try with different Username/Loginname"));
				AppLog.getLogger().info("User name was already taken: " +this.username + ", " + this.loginname );
				break;
			case UserCreated:
				AppLog.getLogger().info("Player Account created: " + auth.user.loginname);
				return "AccountCreated";
			case Error:
				AppLog.getLogger().info("Error occurred in account creation: " + this.username + ", " + this.loginname );
				FacesContext.getCurrentInstance().addMessage("", new FacesMessage("An error occurred, please close your browser and try again."));
				return null;
			default:
				return null;
			}
		} catch (Exception e) {
			AppLog.getLogger().severe("Exception in CreateAccountBean.createAccount(): " + e.getMessage());
		}
		// all else, return to same page
		return null;
	}

	public String returnToWelcome() {
		return "Welcome";
	}

}
