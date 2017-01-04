/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.logging;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import org.medipi.utilities.ConfigurationStringTokeniser;
import org.medipi.utilities.Utilities;
/**
 *  A Singleton class for logging.
 *
 *  The logfile name pattern: [application name]_yyyyMMddHHmmss.log in the specified directory
 *
 *  Note:  setAppName(String name, String ldir) should be called before any logging call.  
 *         closeLog() will close the logging file.
 *         setAppName() and closeLog() should be called as a pair with a logging file.
 * 
 * @author Damian Murphy <murff@warlock.org>
 */
public class MediPiLogger {
    
    private static java.util.logging.Logger appLogger = null;
    private static final String INTERNALLOGLEVELS = "MediPiInternalLoggingLevels.txt";

    /**
     *
     */
    public static final String CONSOLE_LOGGER = "Console";
    private HashMap<String, Level>logLevelsMap = new HashMap<>();
    private static String logDir = null;
    private String appName = null;
    private static String dateString = null;
    private static MediPiLogger me = null;
    private static String logFileName = null;
    private boolean useAppLogger = false;
    
    /** Creates a new instance of Logger */
    private MediPiLogger() {
        dateString = Utilities.INTERNAL_FORMAT_UTC.format(Instant.now());
        java.util.logging.Logger consoleLogger = java.util.logging.Logger.getLogger(CONSOLE_LOGGER);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new SimpleFormatter());
        consoleLogger.addHandler(ch);
    
        /* load the Internal Loggin Levels file - this may be updated at a later stage by having an 
         external one but for the time being this will be sufficient */
        InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream(INTERNALLOGLEVELS));
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        try{
        while ((line = br.readLine()) != null) {
            if (line.trim().startsWith("#")){
                continue;
            }
            if (line.trim().length() == 0){
                continue;
            }
            
            ConfigurationStringTokeniser st = new ConfigurationStringTokeniser(line);
            String key = st.nextToken(); 
            Level value = Level.parse(st.nextToken());
            if(st.hasMoreTokens()){
                throw new Exception("Too many elements in the logging Levels file" + INTERNALLOGLEVELS);
            }
            logLevelsMap.put(key,value);
            
        }
        br.close();
        } catch (Exception e){
            System.err.println("Internal logging level file " + INTERNALLOGLEVELS +" has not loaded properly - "+e.toString());
        }

    }
        
      
    public static String getDate() { return Utilities.INTERNAL_FORMAT_UTC.format(Instant.now()); }

    /**
     * Log the given exception.
     * 
     * @param location  where the message has been raised.
     * @param e Exception to be logged
     */
    public void log(String location, Exception e)
    {
       log(location, makeMessage(e));
    }    
    
    private String makeMessage(Exception e)
    {
       StringBuilder sb = new StringBuilder("Exception: ");
       sb.append(e.getClass().getName());
       sb.append(" Message: ");
       sb.append(e.getMessage());
       Throwable t = e;
       while ((t = t.getCause())!= null) {
           sb.append(" Caused by: ");
           sb.append(t.getClass().getName());
           sb.append(" Message: ");
           sb.append(t.getMessage());
       }
       return sb.toString();
    }
    
    public void log(String location, String message) {
        log(logLevelsMap.get(location), location, message);
    }
    
    public void warn(String location, String message) {
        log(Level.WARNING, location, message);
    }
    public void fine(String location, String message) {
        log(Level.FINE, location, message);
    }
    public void finer(String location, String message) {
        log(Level.FINER, location, message);
    }
    public void finest(String location, String message) {
        log(Level.FINEST, location, message);
    }
    public void info(String location, String message) {
        log(Level.INFO, location, message);
    }
    
    public void error(String location, String message) {
        log(Level.SEVERE, location, message);
    }
    
    /**
     * Log the given exception.
     * 
     * @param l java.util.logging.Level
     *  @param location  where the message has been raised.
     * @param e Exception to be logged
     */
    public void log(Level l, String location, Exception e)
    {
       log(l, location, makeMessage(e));
    }
    
    /**
     *  Log the given message, declare it as coming from the given location.
     *
     *  If the log file has not been opened successfully, the error will be sent via the standard err channel.
     *   
     * @param l java.util.logging.Level
     *  @param location  where the message has been raised.
     *  @param message   the message to be logged. 
     */
    public void log(Level l, String location, String message) {
        StringBuilder sb = new StringBuilder();        
        sb.append("Location: ");
        if ((location == null) || (location.trim().length() == 0))
            sb.append("Not given");
        else
            sb.append(location);
        sb.append(" : Message: ");
        if ((message == null) || (message.trim().length() == 0))
            sb.append("Not given");
        else
            sb.append(message);

        java.util.logging.Logger eventlog = null;
        if (useAppLogger) {
            eventlog = appLogger;
        } else {
            eventlog = java.util.logging.Logger.getLogger(CONSOLE_LOGGER);
        }
        eventlog.setUseParentHandlers(false);
        eventlog.setLevel(Level.ALL);
        if(l == null){
            eventlog.log(Level.INFO, sb.toString());
        }else{
            eventlog.log(l, sb.toString());
        }
    }
    
    /**
     *  return the singleton instance of this class.
     * @return 
     */
    public static synchronized MediPiLogger getInstance() {
        if (me == null)
            me = new MediPiLogger();
        return me;
    }
    
    public void close() {
        if (appName != null) {
            java.util.logging.Logger log = null;
            log = java.util.logging.Logger.getLogger(appName);
            for (Handler h : log.getHandlers()) {
                h.close();
            }
        }
    }
    
    /**
     *  set the application name and logging direction for the composition of 
     *  log filename 
     *  
     *  the function will attempt to create a log file;
     *  if successful, the instance is ready for logging, 
     *  otherwise an error will be sent via the standard err channel.  
     *
     *  @param  name    application name
     *  @param  ldir    the log file directory
     */
    public void setAppName(String name, String ldir) {
        if ((name == null) || (name.trim().length() == 0))
            return;
        if ((ldir == null) || (ldir.trim().length() == 0))
            return;
        if (appName == null) {
            logDir = ldir;
            appName = name;
            
            appLogger = java.util.logging.Logger.getLogger(appName); 
            appLogger.setUseParentHandlers(false);
            StringBuilder sb = new StringBuilder(logDir);
            if(!(logDir.endsWith("/") || logDir.endsWith("\\"))) {
                sb.append("/");
            }
            sb.append(appName);
            sb.append("_");
            sb.append(dateString);
            sb.append(".log");
            logFileName = sb.toString();
            try {
                FileHandler fh = new FileHandler(logFileName, 0, 1, true);
                fh.setFormatter(new SimpleFormatter());
                appLogger.addHandler(fh);
                useAppLogger = true;
            }
            catch (Exception e) {
                java.util.logging.Logger consoleLogger = java.util.logging.Logger.getLogger(CONSOLE_LOGGER);
                StringBuilder sbe = new StringBuilder("Failed to initialise logger ");
                sbe.append(appName);
                sbe.append(" to ");
                sbe.append(logFileName);
                sbe.append(" : ");
                sbe.append(e.toString());
                consoleLogger.log(Level.SEVERE,  sbe.toString());
            }            
        }
    }

    /**
     * Logs the given string with a level of "INFO".
     * @param string Message to log
     */
    public void log(String string) {
        this.log(Level.INFO, "Location not given", string);
    }
    
}
