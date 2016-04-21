package logbrowser.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang.StringEscapeUtils;

import logbrowser.app.LogBrowserException;
import logbrowser.app.LogLine;

/**
 * JTable for the opened files
 *
 * @author rodriag
 * @since 1.0
 */
class FileTable extends JTable implements ContentTable {
	private static final long serialVersionUID = 1L;

	private FileTableModel fileModel;
	private int maxLineWidth;
	private Container parent;

	/**
	 * Constructor
	 * @param parent : the Container of this JTable, required so it can be resized in setContent()
	 */
	FileTable(Container parent, LogBrowserWindow window) {
		fileModel = new FileTableModel(window);
		this.setModel(fileModel);
		this.setDefaultRenderer(String.class, new FileRenderer());
		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.setShowGrid(false);
		
		parent.add(this);
		this.parent = parent;
	}

	/**
	 * Set the content of the file in the JTable.
	 * @param content
	 * @return
	 * @throws IOException
	 */
	int setContent(List<LogLine> content) throws IOException {
		maxLineWidth = fileModel.setLines(content);
		
		// Resize the table:
		int height = this.getRowHeight() * fileModel.getRowCount();
		parent.setPreferredSize(new Dimension(maxLineWidth, height));
		this.getColumnModel().getColumn(0).setPreferredWidth(maxLineWidth);
		
		return content.size();
	}
	
	/**
	 * Go to the specified row
	 */
	public void selectRow(int lineNumber) {
		
		// Scroll to line minus three so it is more visible:
		int firstLine = lineNumber - 3;
		if (firstLine < 0) {
			firstLine = 0;
		}
		this.scrollRectToVisible(new Rectangle(this.getCellRect(firstLine, 0, true)));
		
		// Highlight:
		this.getSelectionModel().setSelectionInterval(lineNumber, lineNumber);
	}

	// ContentTable implementation ------------------------------------------------
	
	public List<String> getContent() throws LogBrowserException, IOException {
		return fileModel.getLines();
	}
	
	public void updateLine(int index, String text) {
		fileModel.updateLine(index, text);
	}
	
	public void fireTableDataChanged() {
		fileModel.fireTableDataChanged();
	}
	
	public void showLine(int lineNumber) {
		this.scrollRectToVisible(new Rectangle(this.getCellRect(lineNumber, 0, true)));
	}
}


/******************************************************************
 * File data model
 */
class FileTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private List<String> lines;
	private LogBrowserWindow frame;
	
	FileTableModel(LogBrowserWindow frame) {
		this.frame = frame;
		lines = new ArrayList<>();
	}
	
	/**
	 * Set the content of the file in the datamodel
	 * Also: calculate and return the length of the longest line
	 * @param lines
	 * @return
	 */
	int setLines(List<LogLine> logLines) {

		int maxLineWidth = 0;
		for (LogLine logLine : logLines) {
			int size = frame.metrics.stringWidth(logLine.getText());
			maxLineWidth = Math.max(size, maxLineWidth);

			this.lines.add("<code>" + StringEscapeUtils.escapeHtml(logLine.getText()) + "</code>");
		}
		return maxLineWidth;
	}

	List<String> getLines() {
		return lines;
	}
	
	void updateLine(int index, String text) {
		this.lines.set(index, text);
	}
	
	// AbstractTableModel implementation -----------------------------------------

	@Override
	public int getRowCount() {
		return lines == null ? 0 : lines.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// Note rowIndex + 1: lines are numbered starting in 1:
		return "<span style=\"color:red\">" + String.format("%05d", rowIndex + 1) + "</span> " +
			   lines.get(rowIndex);
	}

    @Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

    @Override
    public String getColumnName(int index) {
        return "";
    }
}

/**************************************************************
 * FileTable cell renderer
 */
class FileRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	public FileRenderer() {
		super();
	}

	@Override
	public void setValue(Object value) {
		if (value == null) {
			this.setText(null);
		} else {
			this.setText("<html>" + value + "</html>");
		}
	}
}	
