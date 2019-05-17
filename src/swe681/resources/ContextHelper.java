package swe681.resources;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ContextHelper {

	// Assign session for the logged user
	public void setLoggedInUser(UserProfile userdata) {
		FacesContext context = FacesContext.getCurrentInstance();
		context.getExternalContext().getSessionMap().remove("user");
		context.getExternalContext().getSessionMap().put("user", userdata);		
	}
	
	
	public UserProfile getLoggedInUser() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		return (UserProfile) session.getAttribute("user");		
	}
	
	//terminating session
	public void abandonSession() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		session.invalidate();
	}
	
    public HttpServletRequest getRequest() {
    	HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
    	return request;
    }

}
