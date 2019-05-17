package swe681.resources;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AppLog {
	
	private final static String LOGFILE = "C:\\Users\\sweet\\Desktop\\logfile.log";

	private static volatile Logger LOGGER;
	
	public synchronized static Logger getLogger() {
		if(LOGGER == null) {
			LOGGER = Logger.getLogger("AppLogger");
			configLogger();
		}
		return LOGGER;
	}
	
	//TODO: set log file to store logs on the location specified
    private static void configLogger() {
        FileHandler filehandle;  

        try { 
            // Configuration the logger with handler and formatter  
        	filehandle = new FileHandler(LOGFILE);  
            LOGGER.addHandler(filehandle);
            SimpleFormatter formatter = new SimpleFormatter();  
            filehandle.setFormatter(formatter);   

        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }
}
