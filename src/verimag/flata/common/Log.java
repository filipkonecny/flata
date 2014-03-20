package verimag.flata.common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;


/**
 * class designed to help logging into a file.
 *  
 *
 */
public class Log extends IndentedWriter{
	
	private String nameOfLog;
	private File logFilePath;
	
	public File logFile() { return logFilePath; }
	
	/**
	 * creates a Log object
	 * 
	 * @param aNameOfLog the name of the log (log's ID)
	 * @param aLogFilePath the file to log into 
	 */
	public Log() {
		this.isLogging = false;
		this.logOpen = false;
	}
	public Log(String aNameOfLog, File aLogFilePath) {
		nameOfLog = aNameOfLog;
		logFilePath = aLogFilePath;
	}
	
	/**
	 * places from where {@link #log(StringBuffer)} method can be invoked
	 *
	 */
	public enum LogSite {
		addTransition,
		addTransitions,
		processTransition,
		elimState,
		removeUselessStates,
		oneStateUnrolling,
		nonLoopElimination,
		oneLoopUnrolling,
		dependencyAnalysis
		
	}
	public boolean canLog() {
		return (logOpen && isLogging);
	}
	public boolean canLog(LogSite aLogSite) {
		return canLog();
		/*return
		(	(	(true)
				&&
				((aLogSite == LogSite.elimState)
						|| (aLogSite == LogSite.addTransition)
				)
			)
			||
			(false)
		)
		;*/
		//return true;
	}
	
	private boolean logOpen = false;		// logging possible = files successfully/unsuccessfully opened
	private boolean isLogging = false;		// logging is switched on/off
	
	/**
	 * initializes the log by opening the log-file
	 */
	public void initLog() {
		try {
			FileWriter logFileWriter = new java.io.FileWriter(logFilePath);
			setWriter(new java.io.BufferedWriter(logFileWriter));
			
			logOpen = true;
		} catch (IOException e) {
			System.err.println("I/O problems with a log file for CA "+nameOfLog);
			System.err.println(e.getMessage());
		}
	}
	public void pauseLog() {
		isLogging = false;
	}
	public void continueLog() {
		isLogging = true;
	}
	public void flushLog() {
		flush();
	}
	public void endLog() {
		if (!logOpen)
			return;
		
		flush();
		
		try {
			close();
		} catch (IOException e) {
			System.err.println("I/O problems with a log file for CA "+nameOfLog);
			System.err.println(e.getMessage());
		} finally {
			isLogging = false;
			logOpen = false;
		}
	}
	public void log(String aStr) {
		log(new StringBuffer(aStr));
	}
	public void log(StringBuffer aSb) {
		if (logOpen && isLogging)
			write(aSb);
	}

	/*public static CATransitionStatus createTransitionStatus() {
		return new CATransitionStatus(verimag.flata.linconstraints.LinConstraints.LogType.PARTIAL);
	}*/

//	public static LinConstraintsStatus createLinContraintsStatus() {
//		return new LinConstraintsStatus(verimag.flata.presburger.LinearRel.LogType.PARTIAL);
//	}
	
}
