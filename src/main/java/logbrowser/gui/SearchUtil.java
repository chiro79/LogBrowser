package logbrowser.gui;

import java.io.IOException;
import java.util.List;

import logbrowser.app.LogBrowserException;

/**
 * Utility for searching text in the Results table and the opened files. 
 * 
 * @author rodriag
 * @since 1.0
 */
class SearchUtil {
	static final String HIGHLIGHT = "<span style=\"background-color:#00FF00;\">";
	static final String HIGHLIGHT_END = "</span>";
	static final String CURRENT_HIGHLIGHT = "<span style=\"background-color:#FF0000;\">";
	
	static class Results {
		int linesFound;
		int totalFound;
		int firstFoundLine;
	}
	
	static class Position {
		Position(int line, int pos) {
			this.line = line;
			this.pos = pos;
		}
		int line;
		int pos;
	}
	
	/**
	 * Search a text in a FileTable and highlight results
	 * @param table
	 * @param text
	 * @return Results object
	 * @throws LogBrowserException
	 * @throws IOException
	 */
	static Results highlightText(ContentTable table, String text) throws LogBrowserException, IOException {
		// Clear previous highlights:
		clearHighlights(table);

		// Search text and highlight results:
		List<String> content = table.getContent();

		Results results = new Results();
		boolean first = true;
		for(int i = 0; i < content.size(); i++) {
			String line = content.get(i);
			
			// Find occurrences inside the line:
			int pos = 0;
			boolean foundInLine = false;
			while((pos = line.indexOf(text, pos)) != -1) {
				results.totalFound++;
				foundInLine = true;
				pos++;
			}
			
			if (foundInLine) {
				results.linesFound++;

				String newText = HIGHLIGHT + text + HIGHLIGHT_END;
				line = line.replace(text, newText);

				// Mark the first occurrence of the first line:
				if (first) {
					line = line.replaceFirst(HIGHLIGHT, CURRENT_HIGHLIGHT);
					results.firstFoundLine = i;
					first = false;
				}
				
				// Update the line in the table:
				table.updateLine(i, line);           
			}
		}
		if (results.totalFound > 0) {
			table.fireTableDataChanged();
		}
		return results;
	}
	
	/**
	 * Clear all highlighted occurrences of a previously searched text.
	 * @param table
	 */
	static void clearHighlights(ContentTable table) throws LogBrowserException, IOException {
		List<String> content = table.getContent();

		boolean found = false;
		for(int i = 0; i < content.size(); i++) {
			String line = content.get(i);
			if (line.indexOf(HIGHLIGHT) != -1) {
				line = line.replace(HIGHLIGHT, "");
				found = true;
			} 
			if (line.indexOf(CURRENT_HIGHLIGHT) != -1) {
				line = line.replace(CURRENT_HIGHLIGHT, "");
				found = true;
			}
			if (found) {
				line = line.replace(HIGHLIGHT_END, "");
				table.updateLine(i, line);
			}
		}
		if (found) {
			table.fireTableDataChanged();
		}
	}
	
	/**
	 * Go to the previous highlighted result.
	 * @param table
	 * @return the row with the new current result, or -1 if not found.
	 * @throws LogBrowserException
	 * @throws IOException
	 */
	static int previous(ContentTable table) throws LogBrowserException, IOException {
		List<String> content = table.getContent();

		Position cp = getCurrent(content);
		if (cp == null) {
			return -1;
		}
		
		Position np = null;
		// First search in the same line:
		int pos = content.get(cp.line).indexOf(HIGHLIGHT);
		if (pos != -1 && pos < cp.pos) {
			np = new Position(cp.line, pos);
		} else {
			// ...then search in the previous lines:
			for(int i = cp.line - 1; i >= 0; i--) {
				pos = content.get(i).lastIndexOf(HIGHLIGHT);
				if (pos != -1) {
					np = new Position(i, pos);
					break;
				}
			}
		}

		if (cp != null && np != null) {
			String line = replace(content.get(cp.line), CURRENT_HIGHLIGHT, HIGHLIGHT, cp.pos);
			if (cp.line == np.line) {
				// Both current and next in same line:
					String subLine = line.substring(0, cp.pos);
					pos = subLine.lastIndexOf(HIGHLIGHT);
				String newLine = replace(line, HIGHLIGHT, CURRENT_HIGHLIGHT, pos);
				table.updateLine(cp.line, newLine);
			} else {
				// Different lines:
				table.updateLine(cp.line, line);
				String newLine = replace(content.get(np.line), HIGHLIGHT, CURRENT_HIGHLIGHT, np.pos);
				table.updateLine(np.line, newLine);
			}
			return np.line;
		} else {
			return -1;
		}
	}
	
	/**
	 * Go to the next highlighted result.
	 * @param table
	 * @return the row with the new current result, or -1 if not found.
	 * @throws LogBrowserException
	 * @throws IOException
	 */
	static int next(ContentTable table) throws LogBrowserException, IOException {
		List<String> content = table.getContent();

		Position cp = getCurrent(content);
		if (cp == null) {
			return -1;
		}
		
		Position np = null;
		// First search in the same line:
		int pos = content.get(cp.line).indexOf(HIGHLIGHT, cp.pos);
		if (pos != -1) {
			np = new Position(cp.line, pos);
		} else {
			// ...then search in the following lines:
			for(int i = cp.line + 1; i < content.size(); i++) {
				pos = content.get(i).indexOf(HIGHLIGHT);
				if (pos != -1) {
					np = new Position(i, pos);
					break;
				}
			}
		}

		if (cp != null && np != null) {
			String line = replace(content.get(cp.line), CURRENT_HIGHLIGHT, HIGHLIGHT, cp.pos);
			if (cp.line == np.line) {
				// Both current and next in same line:
				pos = line.indexOf(HIGHLIGHT, cp.pos + CURRENT_HIGHLIGHT.length() + 1);
				String newLine = replace(line, HIGHLIGHT, CURRENT_HIGHLIGHT, pos);
				table.updateLine(cp.line, newLine);
			} else {
				// Different lines:
				table.updateLine(cp.line, line);
				String newLine = replace(content.get(np.line), HIGHLIGHT, CURRENT_HIGHLIGHT, np.pos);
				table.updateLine(np.line, newLine);
			}
			return np.line;
		} else {
			return -1;
		}
	}
	
	static Position getCurrent(List<String> content) {
		for(int i = 0; i < content.size(); i++) {
			String line = content.get(i);
			int pos = line.indexOf(CURRENT_HIGHLIGHT); 
			if (pos != -1) {
				return new Position(i, pos);
			}
		}
		return null;
	}
	
	static String replace(String line, String oldText, String newText, int from) {
		int pos = line.indexOf(oldText, from);
		return line.substring(0, pos) + newText + line.substring(pos + newText.length());
	}
}
