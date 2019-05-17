package swe681;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import swe681.resources.ContextHelper;
import swe681.resources.DataDriver;

public class BaseBean {


	public DataDriver dataDriver = new DataDriver();
	public ContextHelper contextHelper = new ContextHelper();
	
	public BaseBean() {
		setHeaders();
	}
	
    public void setHeaders() {
        HttpServletResponse response = (HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.setHeader("Cache-Control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
    }        
}
