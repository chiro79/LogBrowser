package logbrowser.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the configuration main element.
 * It will usually correspond to a group of files in a server that share similar characteristics.
 * Example:
 * 		type="HTTP", "SSH", "LOCAL"
 * 		host="myhost.com", "serverX" (or nothing for LOCAL configurations)
 * 		user="myuser" 
 * 		pwd="mypwd"  
 *		basedir="/common/logs/" 
 *		canBeCompressed="GZ"
 * 
 * @author rodriag
 * @since 1.0
 */
@XmlRootElement
public class LogConfig {
	
	/**
	 * The implemented types of access.
	 */
	public enum Type {HTTP, SSH, LOCAL};
	
	/**
	 * The implemented types of compression.
	 */
	public enum Compression {
		GZ(".gz");
		
		private String fileName;
		Compression(String fileName) {
			this.fileName = fileName;
		}
		public String getFileName() { 
			return fileName; 
		}
	}

	private Type type;
	private String host;
	private String alias;
	private String user;
	private String pwd;
	private String basedir;
	private Compression canBeCompressed;
	private List<String> files;

	public Type getType() {
		return type;
	}

	@XmlAttribute
	public void setType(Type type) {
		this.type = type;
	}

	public String getHost() {
		return host;
	}

	@XmlAttribute
	public void setHost(String host) {
		this.host = host;
	}

	public String getAlias() {
		return alias;
	}

	@XmlAttribute
	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getUser() {
		return user;
	}

	@XmlAttribute
	public void setUser(String user) {
		this.user = user;
	}

	public String getBasedir() {
		return basedir == null ? "" : basedir;
	}

	@XmlAttribute
	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public String getPwd() {
		return pwd;
	}

	@XmlAttribute
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public Compression getCanBeCompressed() {
		return canBeCompressed;
	}

	@XmlAttribute
	public void setCanBeCompressed(Compression canBeCompressed) {
		this.canBeCompressed = canBeCompressed;
	}

	public List<String> getFiles() {
		return files;
	}

	@XmlElement(name="file")
	public void setFiles(List<String> files) {
		this.files = files;
	}

	@Override
	public String toString() {
		return "LogConfig for host: " + host + " (" + type + ")";
	}
}
