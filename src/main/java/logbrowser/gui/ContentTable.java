package logbrowser.gui;

import java.io.IOException;
import java.util.List;

import logbrowser.app.LogBrowserException;

/**
 * Common interface for ResultsTable and FileTable:
 *   The content is a List of Strings
 * 
 * @author rodriag
 * @since 1.0
 */
interface ContentTable {
	
	/**
	 * Return the content of the table.
	 * @return
	 * @throws LogBrowserException
	 * @throws IOException
	 */
	public List<String> getContent() throws LogBrowserException, IOException;
	
	/**
	 * Update a line in the table.
	 * @param index
	 * @param content
	 */
	public void updateLine(int index, String content);
	
	/**
	 * Fire content changed event.
	 */
	public void fireTableDataChanged();

	/**
	 * Show a line in the screen.
	 * @param lineNumber
	 */
	public void showLine(int lineNumber);
}
