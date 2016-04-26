package logbrowser.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The parent of the configuration.
 * Contains a list of configurations for the different applications.
 * 
 * @author rodriag
 * @since 1.0
 */
@XmlRootElement
public class Config {

	private String dateFormat;
	private String downloadBaseFolder;
	private String downloadExtension;
	private List<AppConfig> apps;

	public String getDateFormat() {
		return dateFormat;
	}

	@XmlElement(name="dateFormat")
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getDownloadBaseFolder() {
		return downloadBaseFolder;
	}

	@XmlElement(name="downloadBaseFolder")
	public void setDownloadBaseFolder(String downloadBaseFolder) {
		this.downloadBaseFolder = downloadBaseFolder;
	}

	public String getDownloadExtension() {
		return downloadExtension;
	}

	@XmlElement(name="downloadExtension")
	public void setDownloadExtension(String downloadExtension) {
		this.downloadExtension = downloadExtension;
	}

	public List<AppConfig> getApps() {
		return apps;
	}

	@XmlElementWrapper
	@XmlElement(name="appConfig")
	public void setApps(List<AppConfig> apps) {
		this.apps = apps;
	}

	@Override
	public String toString() {
		return "LogBrowser configuration";
	}
}
