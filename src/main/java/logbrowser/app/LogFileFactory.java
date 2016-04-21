package logbrowser.app;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import com.jcraft.jsch.JSchException;

import logbrowser.app.fileread.ReadFile;
import logbrowser.app.fileread.ReadFileHttp;
import logbrowser.app.fileread.ReadFileLocal;
import logbrowser.app.fileread.ReadFileSsh;
import logbrowser.config.LogConfig;

/**
 * Factory in charge of building LogFile objects.
 * Searchs in the servers looking for files that match the parameters included in the log configuration (LogConfig) and
 * the provided dates.
 *  
 * @author rodriag
 * @since 1.0
 */
class LogFileFactory {

	public static final String DATE_HOLDER = "{date}";
	public static final String DATE_HOLDER_EXPR = "\\{date\\}";
	public static final String PATH_SEPARATOR = "/";

	private String dateFormat;
	private String dateSeparator;

	LogFileFactory(String dateFormat, String dateSeparator) {
		this.dateFormat = dateFormat;
		this.dateSeparator = dateSeparator;
	};
	
	/**
	 * This is going to build one LogFile for each date, 
	 * And, also, if the file can be found compressed, one compressed LogFile for eache date.
	 *  
	 * @param fromDate
	 * @param toDate
	 * @param logConfig
	 * @return a list of the LogFiles built.
	 * @throws JSchException 
	 * @throws IOException 
	 * @throws LogBrowserException 
	 */
	List<LogFile> build(Date fromDate, Date toDate, LogConfig logConfig) throws LogBrowserException, IOException, JSchException {
	    Calendar calendar = new GregorianCalendar();
		List<LogFile> logFiles = new ArrayList<>();
		
		Date today = new Date();

		// Common parameters for all files in this LogConfig:
		LogConfig.Type type = logConfig.getType();
		String host = logConfig.getHost();
		String alias = logConfig.getAlias();
		String user = logConfig.getUser();
		String pwd = logConfig.getPwd();
		String basedir = logConfig.getBasedir();
		LogConfig.Compression canBeCompressed = logConfig.getCanBeCompressed();

		// For each logfile definition in the configuration...
		for (String file : logConfig.getFiles()) {
		
			// For each date selected...
		    calendar.setTime(fromDate);
		    while (!calendar.getTime().after(toDate))  {
				Date date = calendar.getTime();
				String path = file;

				// if searching for today's files:
				if (DateUtils.isSameDay(date, today)) {
					path = path.replaceAll(DATE_HOLDER_EXPR, "");
				}
				// if searching for old files:
				else {
					// First, discard files without dateholder when searching for files from a certain date:
					if (!path.contains(DATE_HOLDER)) {
				        calendar.add(Calendar.DATE, 1);
						continue;
					}
					
					// Build the name of the LogFile with the date:
					String dateString = new SimpleDateFormat(dateFormat).format(date);
					path = path.replaceAll(DATE_HOLDER_EXPR, dateSeparator + dateString);
				}
				
				// Prepare the strategy:
				ReadFile readFile = null;

				// 1. First try the compressed version of the file:
				if (canBeCompressed != null) {
					
					readFile = buildStrategy(type, host, user, pwd, basedir, path, canBeCompressed);
					if (readFile.exists()) {
						String name = path.contains(PATH_SEPARATOR) ? path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1) : path;
						logFiles.add(new LogFile(name, alias, readFile));
					} else {
						readFile = null;
					}
				}
				
				// 2. Normal version of the file:
				if (readFile == null) {
					
					readFile = buildStrategy(type, host, user, pwd, basedir, path, null);
					if (readFile.exists()) {
						String name = path.contains(PATH_SEPARATOR) ? path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1) : path;
						logFiles.add(new LogFile(name, alias, readFile));
					}
				}		
				
		        calendar.add(Calendar.DATE, 1);
		    }
		}
		
		return logFiles;
	}

	private ReadFile buildStrategy(LogConfig.Type type, String host, String user, String pwd, String basedir, String path,
			LogConfig.Compression compressed) throws LogBrowserException, IOException, JSchException {
		ReadFile readFile;

		if (compressed != null) {
			path += compressed.getFileName();
		}
		
		// Build the Reading Strategy:
		switch(type) {
		case HTTP:
			URL url = new URL("HTTP://" + host + basedir + path);
			readFile = new ReadFileHttp(url, compressed, user, pwd);
			break;
		case SSH:
			readFile = new ReadFileSsh(host, user, pwd, (basedir == null ? path : basedir + path));
			break;
		case LOCAL:
			readFile = new ReadFileLocal(basedir + path);
			break;
		default:
			throw new LogBrowserException("Invalid type: " + type);
		}
		return readFile;
	}
}
