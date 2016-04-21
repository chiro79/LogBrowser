package logbrowser.app;

import java.io.File;

/**
 * LogBrowserException thrown when the specified folder (typically the download folder) already exists.
 * 
 * @author rodriag
 * @since 1.0
 */
public class FolderExistsException extends LogBrowserException {
	private static final long serialVersionUID = 1L;
	public static final String MESSAGE = "Download folder already exists";
	
	private File folder;

	public FolderExistsException(File folder) {
		super(MESSAGE + ": " + folder.getPath());
		this.folder = folder;
	}

	public File getFolder() {
		return folder;
	}
}
