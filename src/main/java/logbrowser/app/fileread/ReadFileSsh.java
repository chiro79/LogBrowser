	package logbrowser.app.fileread;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import logbrowser.app.LogLine;

/**
 * ReadFile implementation for SSH files.
 * 
 * @author rodriag
 * @since 1.0
 */
public class ReadFileSsh implements ReadFile {
	
	public static final int PORT = 22;

	private String host;
	private String user;
	private String pwd;
	private String path;
	
	private JSch jsch;
	private Properties config;
	private Session session;
	private ChannelSftp sftpChannel;

	public ReadFileSsh(String host, String user, String pwd, String path) throws JSchException {
		this.host = host;
		this.path = path;
		this.user = user;
		this.pwd = pwd;
		
		jsch = new JSch();

		config = new java.util.Properties(); 
		config.put("StrictHostKeyChecking", "no");
	}
	
	private void connect() throws JSchException {
		session = jsch.getSession(user, host, PORT);
		session.setPassword(pwd);
		session.setConfig(config);
		session.connect();
		
		Channel channel = session.openChannel("sftp");
		channel.connect();
		sftpChannel = (ChannelSftp) channel;
	}
	
	private void disconnect() throws JSchException {
		sftpChannel.disconnect();
		session.disconnect();
	}

	// ReadFile implementation --------------------------------------------------------

	@Override
	public boolean exists() throws IOException {
		try {
			connect();
			InputStream is = null;
			try {
				is = sftpChannel.get(path);
				return true;

			} catch(SftpException e) {
				return false;
			} finally {
				if (is != null) {
					is.close();
				}
				disconnect();
			}
		} catch(JSchException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public List<LogLine> read() throws IOException {
		List<LogLine> logLines = new ArrayList<>();
		try {
			connect();
			InputStream is = sftpChannel.get(path);
			String line;
			try {
				if (is != null) {                            
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	                for (int i = 0; (line = reader.readLine()) != null; i++) {    
	                	logLines.add(new LogLine(i, line));
	                }                
	            }
				return logLines;

			} finally {
				if (is != null) {                            
					is.close();
				}
				disconnect();
	        }
		} catch(JSchException e) {
			throw new IOException(e.getMessage());
		} catch(SftpException e) {
			throw new IOException(e.getMessage());
		}
	}
	
	@Override
	public void copy(File destFile) throws IOException {
		try {
			connect();
			try {
				sftpChannel.get(path, destFile.getPath());
			} finally {
	            disconnect();
	        }
		} catch(JSchException e) {
			throw new IOException(e.getMessage());
		} catch(SftpException e) {
			throw new IOException(e.getMessage());
		}
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	// Object implementation --------------------------------------------------------
	
	@Override
	public int hashCode() {
		return (host == null ? 0 : host.hashCode()) * (path == null ? 0 : path.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && 
			obj instanceof ReadFileSsh &&
			((ReadFileSsh) obj).host != null &&
			((ReadFileSsh) obj).host.equals(host) &&
			((ReadFileSsh) obj).path != null &&
			((ReadFileSsh) obj).path.equals(path)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return host + path;
	}

}
