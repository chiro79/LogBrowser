package logbrowser.app.fileread;

import java.io.File;
import java.io.IOException;
import java.util.List;

import logbrowser.app.LogLine;

/**
 * Interface with the different treatments (read, search, copy..) for the different types of log files.
 * This is an implementation of the Strategy pattern.
 *   
 * @author rodriag
 * @since 1.0
 */
public interface ReadFile {
	
	/**
	 * Check if this log file exists.
	 * @return true if exists, false otherwise
	 * @throws IOException
	 */
	public boolean exists() throws IOException;
	
	/**
	 * Read this log file
	 * @return a list of the lines (LogLine) of the file (LogFile).
	 * @throws IOException
	 */
	public List<LogLine> read() throws IOException;
	
	/**
	 * Copy this log file to a file destination.
	 * @param destFile
	 * @throws IOException
	 */
	public void copy(File destination) throws IOException;
	
	/**
	 * Return the path of this log file as a String. 
	 * @return
	 */
	public String getPath();
}
