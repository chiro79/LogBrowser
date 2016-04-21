package logbrowser.app;


/**
 * This class represents a line in a log file, where the searched text has been found.
 * 
 * @author rodriag
 * @since 1.0
 */
 public class LogLine {

	private LogFile logFile;
	private int lineNumber;
	private String text;
	
	public LogLine(int lineNumber, String text) {
		this.lineNumber = lineNumber;
		this.text = text;
	}

	public void setLogFile(LogFile logFile) {
		this.logFile = logFile;
	}
	
	public LogFile getLogFile() {
		return logFile;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getText() {
		return text;
	}

	// Object implementation --------------------------------------------------------

	@Override
	public int hashCode() {
		return text.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		
		// Two log lines are equal if their text is equal
		if (obj != null &&
			obj instanceof LogLine &&
			((LogLine)obj).text.equals(text)) {
			return true;
		} else {
			return false;
		}
	}
}
