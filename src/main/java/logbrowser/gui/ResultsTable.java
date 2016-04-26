package logbrowser.gui;

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

import logbrowser.app.InfoLine;
import logbrowser.app.LogBrowserException;
import logbrowser.app.LogFile;

/**
 * JTable for the results of the search
 * 
 * @author rodriag
 * @since 1.0
 */
class ResultsTable extends JTable implements ContentTable {
	private static final long serialVersionUID = 1L;
	
	private ResultsTableModel resultsModel;
	private List<InfoLine> infoLines;

	ResultsTable(LogBrowserWindow frame) {
		resultsModel = new ResultsTableModel(frame);
		this.setModel(resultsModel);
		this.setDefaultRenderer(String.class, new ResultsRenderer());
		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.setShowGrid(false);
	}

	/**
	 * Set the found results in the JTable
	 * @param infoLines
	 */
	void setResults(List<InfoLine> infoLines) {
		this.infoLines = infoLines;

		// Set the content in the model, calculate max width (adding 20% extra space):
		int maxLineWidth = (int)(resultsModel.setResults(infoLines) * 1.2);
		
		// Resize the table:
		int height = this.getRowHeight() * infoLines.size();
		this.getParent().setPreferredSize(new Dimension(maxLineWidth, height));
		this.getColumnModel().getColumn(0).setPreferredWidth(maxLineWidth);
		
		this.fireTableDataChanged();
	}
    
    /**
     * Return the LogFile of the selected row
     */
    LogFile getLogFile(int rowIndex) {
    	return infoLines.get(rowIndex).getFile();
    }
    
    /**
     * Return the line number of the selected row
     * (will be null if it is a File header line)
     */
    Integer getLineNumber(int rowIndex) {
    	if (infoLines.get(rowIndex).getLine() == null) {
    		return null;
    	} else {
    		return infoLines.get(rowIndex).getLine().getLineNumber();
    	}
    }

	// ContentTable implementation ------------------------------------------------
	
	public List<String> getContent() throws LogBrowserException, IOException {
		return resultsModel.getResults();
	}
	
	public void updateLine(int index, String text) {
		resultsModel.updateLine(index, text);
	}
	
	public void fireTableDataChanged() {
		resultsModel.fireTableDataChanged();
	}
	
	public void showLine(int lineNumber) {
		this.scrollRectToVisible(new Rectangle(this.getCellRect(lineNumber, 0, true)));
	}
}

/******************************************************************
 * Results data model
 */
class ResultsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private List<String> results;

	private LogBrowserWindow frame;
	
	ResultsTableModel(LogBrowserWindow frame) {
		this.frame = frame;
		results = new ArrayList<>();
	}

	List<String> getResults() {
		return results;
	}

	/**
	 * Set the results in the datamodel
	 * Also: calculate and return the length of the longest line
	 * @param lines
	 * @return
	 */
	int setResults(List<InfoLine> results) {
		
		this.results.clear();

		int maxLineWidth = 0;
		for (InfoLine line : results) {
			String text = "";
			if (line.getType() == InfoLine.Type.FILE) {
				text = "<span style=\"color:blue\">" + line.getFile() + "</span>";
			} 
			else if (line.getType() == InfoLine.Type.LINE){
				// Line number is 0 based:
				int lineNumber = line.getLine().getLineNumber() + 1;
				
				text = "<span style=\"color:red;\">" + String.format("%05d", lineNumber) + "</span> " + 
			    "<code>" + StringEscapeUtils.escapeHtml(line.getLine().getText()) + "</code>";
			}

			this.results.add(text);
			int size = frame.metrics.stringWidth(text);
			maxLineWidth = Math.max(size, maxLineWidth);
		}
		return maxLineWidth;
	}
    
	void updateLine(int index, String text) {
		this.results.set(index, text);
	}
	
	// AbstractTableModel implementation -----------------------------------------

	@Override
	public int getRowCount() {
		return results == null ? 0 : results.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return results.get(rowIndex);
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
 * ResultsTable cell renderer
 */
class ResultsRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	public ResultsRenderer() {
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
