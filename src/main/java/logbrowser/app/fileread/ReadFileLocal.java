package logbrowser.app.fileread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;

import logbrowser.app.LogLine;
import logbrowser.config.LogConfig;

/**
 * ReadFile implementation for local files.
 * 
 * @author rodriag
 * @since 1.0
 */
public class ReadFileLocal implements ReadFile {

	private File file;
	private LogConfig.Compression compression;

	public ReadFileLocal(String path, LogConfig.Compression compression) throws IOException {
		this.file = new File(path);
		this.compression = compression;
	}
	
	// ReadFile implementation --------------------------------------------------------------
	
	@Override
	public boolean exists() throws IOException {
		return file.exists();
	}
	
	@Override
	public List<LogLine> read() throws IOException {
		List<LogLine> logLines = new ArrayList<>();

		InputStream is = new FileInputStream(file);
		InputStreamReader isr;
		
		if (compression == null) {
			isr = new InputStreamReader(is);
		} else {
			InputStream gzis = new GZIPInputStream(is);
			isr = new InputStreamReader(gzis);
		}
		BufferedReader reader = new BufferedReader(isr);
		String line;
		try {
			for (int i = 0;(line = reader.readLine()) != null; i++) {
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
			input = new FileInputStream(file);
		} else {
			InputStream is = new FileInputStream(file);
			input = new GZIPInputStream(is);
		}

		try {
			FileUtils.copyInputStreamToFile(input, destFile);
		} finally {
			input.close();
		}
	}
	
	@Override
	public String getPath() {
		return file.getPath();
	}
	
	// Object implementation --------------------------------------------------------
	
	@Override
	public int hashCode() {
		return getPath() == null ? 0 : getPath().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && 
			obj instanceof ReadFileLocal &&
			((ReadFileLocal) obj).getPath() != null &&
			((ReadFileLocal) obj).getPath().equals(getPath())) {
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
