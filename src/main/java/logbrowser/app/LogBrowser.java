package logbrowser.app;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;

import com.jcraft.jsch.JSchException;

import logbrowser.config.AppConfig;
import logbrowser.config.Config;
import logbrowser.config.LogConfig;

/**
 * Main class: access to the functions of the application.
 * 
 * @author rodriag
 * @since 1.0
 */
public class LogBrowser {

	// The location of the configuration file:
	public static final String CONFIG_FILE = "./config.xml";  

	private String downloadBaseFolder;
	private String downloadExtension;
	private Map<String, AppConfig> apps;
	private List<String> appNames;
	
	private LogFileFactory logFileFactory;

	// Current search results & parameters: 
	private List<LogFile> logFiles;
	private Date fromDate, toDate;
	private String appName;
	
	/**
	 * Constructor: load the application configuration.
	 * @throws JAXBException
	 * @throws LogBrowserException
	 */
	public LogBrowser() throws JAXBException, LogBrowserException {
		
		// Load the configuration:
		File file = new File(CONFIG_FILE);
		JAXBContext context = JAXBContext.newInstance(logbrowser.config.Config.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Config config = (Config)unmarshaller.unmarshal(file);
		
		// Validations:
		if (config.getApps().size() == 0) {
			throw new LogBrowserException("No applications found in the configuration.");
		}
		
		this.downloadBaseFolder = config.getDownloadBaseFolder();
		this.downloadExtension = config.getDownloadExtension();
		
		// Load the apps in the Map, with its names:
		apps = new TreeMap<>();
		appNames = new ArrayList<>();
		for (AppConfig appConfig : config.getApps()) {
			apps.put(appConfig.getName(), appConfig);
			appNames.add(appConfig.getName());
		}
		
		logFileFactory = new LogFileFactory(config.getDateFormat());
		logFiles = new ArrayList<>();
	}
	
	/**
	 * Return the names of the loaded applications
	 * @return List of Strings
	 */
	public List<String> getAppNames() {
		return appNames;
	}
	
	/**
	 * Search.
	 * @param appName
	 * @param fromDate
	 * @param toDate
	 * @param text
	 * @return a List of LogInfoLine objects with the found results
	 * @throws IOException
	 * @throws JSchException
	 * @throws LogBrowserException
	 */
	public List<InfoLine> search(String appName, Date fromDate, Date toDate, String text) throws IOException, JSchException, LogBrowserException {

		// Validations:
		if (text != null && text.trim().length() == 0) {
			// If text is null: search for the log files of the selected date(s):
			text = null;
		}
		if (fromDate == null || toDate == null) {
			throw new LogBrowserException("Search dates are required");
		}
		if (fromDate.after(toDate)) {
			throw new LogBrowserException("Date From cannot be after Date To");
		}
		
		// Reset the search results & parameters:
		this.logFiles.clear();
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.appName = appName;
        
		// Prepare the results information:
		List<InfoLine> infoLines = new ArrayList<>();

		// SEARCH every Log Configuration:
		for (LogConfig logConfig : apps.get(appName).getLogs()) {
			
			// ...every Log File:
			for (LogFile logFile : logFileFactory.build(fromDate, toDate, logConfig)) {
				logFiles.add(logFile);

				// If no text searched:
				if (text == null || text.trim().length() == 0) {
					// Return only header:
					infoLines.add(new InfoLine(logFile));
					continue;
				}
				
				// Search the text in the file:
				List<LogLine> results = logFile.search(text);
				if (!results.isEmpty()) {
					// Header:
					infoLines.add(new InfoLine(logFile));
					// Details:
					for (LogLine line : results) {
						infoLines.add(new InfoLine(logFile, line));
					}
				}
			}
		}
		return infoLines;
	}

	/**
	 * Prepare the folder where the files will be downloaded
	 * @return the folder
	 * @throws LogBrowserException
	 */
	public File prepareDownload() throws LogBrowserException {
		
		// Prepare the name of the download folder and create it:
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String dateString;
		if (fromDate.equals(toDate)) {
			dateString = df.format(fromDate);
		} else {
			dateString = df.format(fromDate) + "_" + df.format(toDate);
		}
		String folderName = downloadBaseFolder + appName + "_" + dateString;   
	
		File folder = new File(folderName);
		if (folder.exists()) {
			throw new FolderExistsException(folder);
		}
		return folder;
	}

	/**
	 * Download the currently found files to the specified folder.
	 * @param folder
	 * @param overwriteFolder
	 * @return the number of downloaded files
	 * @throws IOException
	 */
	public int download(File folder, boolean overwriteFolder) throws IOException {
		if (folder.exists() && overwriteFolder) {
			FileUtils.deleteDirectory(folder);
		}
		folder.mkdir();

		// Download files:
		for (LogFile logFile : logFiles) {
			logFile.download(folder, downloadExtension);
		}
		return logFiles.size();
	}
}

