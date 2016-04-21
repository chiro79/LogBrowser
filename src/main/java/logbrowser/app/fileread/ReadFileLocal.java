package logbrowser.app.fileread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import logbrowser.app.LogLine;

/**
 * ReadFile implementation for local files.
 * 
 * @author rodriag
 * @since 1.0
 */
public class ReadFileLocal implements ReadFile {

	private String path;

	public ReadFileLocal(String path) throws IOException {
		this.path = path;
	}
	
	// ReadFile implementation --------------------------------------------------------------
	
	@Override
	public boolean exists() throws IOException {
		return new File(path).exists();
	}
	
	@Override
	public List<LogLine> read() throws IOException {
		List<LogLine> logLines = new ArrayList<>();

		BufferedReader br = new BufferedReader(new FileReader(path));
		String line;
		try {
			for (int i = 0;(line = br.readLine()) != null; i++) {
				logLines.add(new LogLine(i, line));
			}
        } finally {
            br.close();
        }
		return logLines;
	}
	
	@Override
	public void copy(File destFile) throws IOException {
		File srcFile = new File(path);
		FileUtils.copyFile(srcFile, destFile);
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	// Object implementation --------------------------------------------------------
	
	@Override
	public int hashCode() {
		return path == null ? 0 : path.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && 
			obj instanceof ReadFileLocal &&
			((ReadFileLocal) obj).path != null &&
			((ReadFileLocal) obj).path.equals(path)) {
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
