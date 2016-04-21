package logbrowser.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration of an application that generates logs
 * Eg: Payments, eFiling-PROD...
 *   
 * @author rodriag
 * @since 1.0
 */
@XmlRootElement
public class AppConfig {
	
	private String name;
	private List<LogConfig> logs;
	
	public String getName() {
		return name;
	}
	
	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}
	
	public List<LogConfig> getLogs() {
		return logs;
	}
	
	@XmlElementWrapper(name="logs")
	@XmlElement(name="logConfig")
	public void setLogs(List<LogConfig> logs) {
		this.logs = logs;
	}

	@Override
	public String toString() {
		return "AppConfig: " + name;
	}
	
}
