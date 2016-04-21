package logbrowser.app;

/**
 * A line of information about the logs
 * 
 * @author rodriag
 * @since 1.0
 */
public class InfoLine {
	public enum Type {FILE,LINE};
	
	private Type type; 
	private LogFile file;
	private LogLine line;

	/**
	 * Constructor of a header line, showing info of the log file.
	 */
	public InfoLine(LogFile file) {
		this.file = file;
		this.type = Type.FILE;
	}
	
	/**
	 * Constructor of a detail line, showing info of a line of the log file.
	 */
	public InfoLine(LogFile file, LogLine line) {
		this.line = line;
		this.file = file;
		this.type = Type.LINE;
	}
	
	public LogFile getFile() {
		return file;
	}

	public LogLine getLine() {
		return line;
	}

	public Type getType() {
		return type;
	}
}
