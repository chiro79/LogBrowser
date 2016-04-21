package logbrowser.app.fileread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;

import logbrowser.app.LogLine;
import logbrowser.config.LogConfig;

/**
 * ReadFile implementation for HTTP files.
 * 
 * @author rodriag
 * @since 1.0
 */
public class ReadFileHttp implements ReadFile {
	
	private URL url;
	private LogConfig.Compression compression;

	public ReadFileHttp(URL url, LogConfig.Compression compression, final String user, final String pwd) {
		this.url = url;
		this.compression = compression;

		// Set the authenticator that will be used by the networking code
	    Authenticator.setDefault(new Authenticator() {
	        protected PasswordAuthentication getPasswordAuthentication() {
	            // Return the information (a data holder that is used by Authenticator)
	            return new PasswordAuthentication(user, pwd.toCharArray());
	        }
	    });
	}
	
	// ReadFile implementation ------------------------------------------------------
	
	@Override
	public boolean exists() throws IOException {
		InputStream is = null;
		try {
			is = url.openStream();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} finally {
			if (is != null) is.close();
		}
	}

	@Override
	public List<LogLine> read() throws IOException {
		List<LogLine> logLines = new ArrayList<>();

		InputStream is = url.openStream();
		InputStreamReader isr;

		if (compression == null) {
			isr = new InputStreamReader(is);
		} else {
			InputStream gzis = new GZIPInputStream(is);
			isr = new InputStreamReader(gzis);
		}
		BufferedReader reader = new BufferedReader(isr);
		try {
	        String line;
	        for (int i = 0; (line = reader.readLine()) != null; i++) {
	        	logLines.add(new LogLine(i, line));
			}
		} finally {
			reader.close();
		}
		return logLines;
	}
	
	@Override
	public void copy(File destFile) throws IOException {
		InputStream input;

		if (compression == null) {
			input = url.openStream();
		} else {
			input = new GZIPInputStream(url.openStream());
		}
		try {
			FileUtils.copyInputStreamToFile(input, destFile);
		} finally {
			input.close();
		}
	}
	
	@Override
	public String getPath() {
		return url.getPath();
	}

	// Object implementation --------------------------------------------------------
	
	@Override
	public int hashCode() {
		return url == null ? 0 : url.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && 
			obj instanceof ReadFileHttp &&
			((ReadFileHttp) obj).url != null &&
			((ReadFileHttp) obj).url.equals(url)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.getPath();
	}
}
