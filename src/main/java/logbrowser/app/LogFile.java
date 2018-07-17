package logbrowser.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import logbrowser.app.fileread.ReadFile;

/**
 * This class represents an existing log file.
 * 
 * It is instantiated when it matches the specified criteria:
 * 	- Name, including the date part when it has been specified (eg: loxxx_2016-12-01 when searching for the logs of 01/12/2016)
 *  - The compression when it has been specified (eg: logxxx.gz)
 *  
 * @author rodriag
 * @since 1.0
 */
public class LogFile {
	
	private String hostAlias;
	private String name;
	private List<LogLine> logLines;
	private ReadFile readFile;
	
	public LogFile(String name, String hostAlias, ReadFile readFile) {
		this.name = name;
		this.hostAlias = hostAlias;
		this.readFile = readFile;
		
		logLines = new ArrayList<>();
	}

	/**
	 * Searchs in the file for a given text, and loads the lines that contain the text. 
	 * @param text
	 * @return true if the text has been found, false otherwise
	 * @throws IOException 
	 */
	public List<LogLine> search(String text) throws IOException {
		if (logLines.isEmpty()) {
			logLines = readFile.read();
		}

		List<LogLine> foundLines = new ArrayList<>();
		for (LogLine logLine : logLines) {
			if (logLine.getText().contains(text)) {
				foundLines.add(logLine);
			}
		}
		return foundLines;
	}
	
	/**
	 * Returns the content of the file.
	 * If not yet read, reads the file (using the reading strategy)
	 * @return
	 * @throws IOException
	 */
	public List<LogLine> getLogLines() throws IOException {
		if (logLines.isEmpty()) {
			logLines = readFile.read();
		}
		return logLines;
	}

	/**
	 * Downloads the file into the specified folder:
	 * @param folder
	 * @param extension
	 * @throws IOException
	 */
	public void download(File folder, String extension) throws IOException {
		
		String newName = hostAlias == null ? name : name + "_" + hostAlias;

		if (!StringUtils.isEmpty(extension) && !newName.endsWith(extension)) {
			newName += extension;
		}
		
		File destFile = new File(folder + "/" + newName);
		
		// There could be several logs with the same name:
		int count = 1;
		while (destFile.exists()) {
			String fixedPart = newName.substring(0, newName.lastIndexOf(extension));
			String changedName = fixedPart + "(" + count + ")" + extension; 
			
			destFile = new File(folder + "/" + changedName);
			count++;
		}
		readFile.copy(destFile);
	}

	/** 
	 * Return the name of the file.
	 * This is just the name. The real path will be in the reading strategy (ReadFile) and can be a local path, an URL...
	 * @return
	 */
	public String getName() {
		return name;
	}

	// Object implementation --------------------------------------------------------
	
	@Override
	public int hashCode() {
		return readFile.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		
		// Two log files are equal if their ReadFile component (that contains the path) is equal.
		if (obj != null && 
			obj instanceof LogFile &&
			((LogFile)obj).readFile != null &&
			((LogFile)obj).readFile.equals(readFile)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return readFile.getPath();
	}
}
