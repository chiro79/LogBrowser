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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory in charge of building LogFile objects.
 * Searchs in the servers looking for files that match the parameters included in the log configuration (LogConfig) and
 * the provided dates.
 *  
 * @author rodriag
 * @since 1.0
 */
class LogFileFactory {
	Logger logger = LoggerFactory.getLogger(LogFileFactory.class);

	public static final String DATE_HOLDER = "{date}";
	public static final String DATE_HOLDER_EXPR = "\\{date\\}";
	public static final String PATH_SEPARATOR = "/";

	private String dateFormat;

	LogFileFactory(String dateFormat) {
		this.dateFormat = dateFormat;
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

			// First, discard files without dateholder when searching for files from a certain date:
			if (!file.contains(DATE_HOLDER)) {
				ReadFile readFile = buildStrategy(type, host, user, pwd, basedir, file, null);
				if (readFile.exists()) {
					String name = file.contains(PATH_SEPARATOR) ? file.substring(file.lastIndexOf(PATH_SEPARATOR) + 1) : file;
					logFiles.add(new LogFile(name, alias, readFile));
				}
			} else {
				// For each date selected...
				calendar.setTime(fromDate);
				while (!calendar.getTime().after(toDate))  {
					Date date = calendar.getTime();
					String path = file;

					// First, discard files without dateholder when searching for files from a certain date:
					if (!path.contains(DATE_HOLDER)) {
						calendar.add(Calendar.DATE, 1);
						continue;
					}

					// Build the name of the LogFile with the date:
					String dateString = new SimpleDateFormat(dateFormat).format(date);
					path = path.replaceAll(DATE_HOLDER_EXPR, dateString);

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
			String access = host.toLowerCase().startsWith("http") ? "" : "HTTP://";
			URL url = new URL(access + host + basedir + path);
			readFile = new ReadFileHttp(url, compressed, user, pwd);
			break;
		case HTTPS:
			access = host.toLowerCase().startsWith("https") ? "" : "HTTPS://";
			url = new URL(access + host + basedir + path);
			readFile = new ReadFileHttp(url, compressed, user, pwd);
			break;
		case SSH:
			if (compressed != null) {
				throw new LogBrowserException("SSH compressed files are not implemented");
			}
			readFile = new ReadFileSsh(host, user, pwd, (basedir == null ? path : basedir + path));
			break;
		case LOCAL:
			readFile = new ReadFileLocal(basedir + path, compressed);
			break;
		default:
			throw new LogBrowserException("Invalid type: " + type);
		}
		return readFile;
	}
}
