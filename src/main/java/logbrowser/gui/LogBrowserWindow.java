package logbrowser.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.xml.bind.JAXBException;

import com.jcraft.jsch.JSchException;

import logbrowser.app.FolderExistsException;
import logbrowser.app.InfoLine;
import logbrowser.app.LogBrowser;
import logbrowser.app.LogBrowserException;
import logbrowser.app.LogFile;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

/**
 * The main component of the Swing GUI.
 * 
 * @author rodriag
 * @since 1.0
 */
public class LogBrowserWindow extends JFrame implements ActionListener, ChangeListener, MouseListener {
	private static final long serialVersionUID = 1L;

	public static final double SCREEN_SIZE_RATIO = 4.0 / 5.0;
	public static final int MESSAGES_HEIGHT = 64;
	public static final int SCROLL_INCREMENT = 16;

	// Action events:
	public static final String PREV = "prev";
	public static final String NEXT = "next";
	public static final String SEARCH = "search";
	public static final String DOWNLOAD = "download";
	public static final String SEARCH_FILE = "searchFile";

	// GUI components:
	private JComboBox<String> appCombo;
	private JTextField textToSearch;
	private UtilDateModel fromDateModel, toDateModel;
	private JTabbedPane tabsPanel;
	private ResultsTable resultsTable;
	private JButton searchButton, downloadButton, searchFilesButton, prevButton, nextButton;
	private DefaultStyledDocument messages;
    private Style normalStyle, errorStyle;
	
    // Used by the JTable objects to calculate their size:
	FontMetrics metrics;
	
	private LogBrowser logBrowser;
	
	public static void main(String[] args) {
		// Schedule for the event-dispatching thread:
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new LogBrowserWindow().setVisible(true);
			}
		});
	}
	
    public LogBrowserWindow() {
        super("LogBrowser");
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int height = (int)(screenSize.height * SCREEN_SIZE_RATIO);
		int width = (int)(screenSize.width * SCREEN_SIZE_RATIO);
		this.setPreferredSize(new Dimension(width, height));
		this.setSize(width, height);
		this.setLocationRelativeTo(null);
		
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(true);
        this.setContentPane(mainPanel);
	
        mainPanel = new JPanel(new BorderLayout());
        this.add(mainPanel);

		// TOP: CONTROL PANEL
		// --------------------------------------------------------------
		
		JPanel controlPanel = new JPanel(new GridLayout(2, 1));
		controlPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		mainPanel.add(controlPanel, BorderLayout.NORTH);

		// ---- First row --------
		
		JPanel controlPanelFirstRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		controlPanel.add(controlPanelFirstRow);
		
		// APPs combo:
		controlPanelFirstRow.add(new JLabel("Logs App: "));
		appCombo = new JComboBox<>(new String[] { "Loading..." });
		appCombo.addActionListener(this);
		controlPanelFirstRow.add(appCombo);

		// Text to search:
		controlPanelFirstRow.add(new JLabel("Search text: "));
		textToSearch = new JTextField("", 30);
		controlPanelFirstRow.add(textToSearch);
		
		// Dates:
		fromDateModel = new UtilDateModel();
		toDateModel = new UtilDateModel();
		fromDateModel.setValue(new Date());
		toDateModel.setValue(new Date());
		controlPanelFirstRow.add(new JLabel("From: "));
		controlPanelFirstRow.add(new JDatePickerImpl(new JDatePanelImpl(fromDateModel)));
		fromDateModel.addChangeListener(this);
		controlPanelFirstRow.add(new JLabel("To: "));
		controlPanelFirstRow.add(new JDatePickerImpl(new JDatePanelImpl(toDateModel)));
		toDateModel.addChangeListener(this);

		// ---- Second row --------
		
		JPanel controlPanelSecondRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		controlPanel.add(controlPanelSecondRow);

		// Buttons:
		searchButton = new JButton("Search");
		searchButton.setActionCommand(SEARCH);
		searchButton.addActionListener(this);
		controlPanelSecondRow.add(searchButton);
		
		downloadButton = new JButton("Download");
		downloadButton.setActionCommand(DOWNLOAD);
		downloadButton.addActionListener(this);
		controlPanelSecondRow.add(downloadButton);
		
		searchFilesButton = new JButton("Search File");
		searchFilesButton.setActionCommand(SEARCH_FILE);
		searchFilesButton.addActionListener(this);
		controlPanelSecondRow.add(searchFilesButton);

		prevButton = new JButton("<");
		prevButton.setActionCommand(PREV);
		prevButton.addActionListener(this);
		controlPanelSecondRow.add(prevButton);
		
		nextButton = new JButton(">");
		nextButton.setActionCommand(NEXT);
		nextButton.addActionListener(this);
		controlPanelSecondRow.add(nextButton);
		
		this.getRootPane().setDefaultButton(searchButton);

		// CENTER: TABBED PANEL FOR THE RESULTS & OPENED FILES
		// -----------------------------------------------------------------
		
		tabsPanel = new JTabbedPane();
		mainPanel.add(tabsPanel, BorderLayout.CENTER);

		// Results table:
		resultsTable = new ResultsTable(this);
		resultsTable.addMouseListener(this);

		JPanel resultsTablePanel = new JPanel(new GridLayout(1,0));
		resultsTablePanel.add(resultsTable);

        JScrollPane resultsScroll = new JScrollPane(resultsTablePanel);
        resultsScroll.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
        tabsPanel.add("Search results", resultsScroll);

		// MESSAGES PANEL
		// -----------------------------------------------------------------

        StyleContext sc = new StyleContext();
        normalStyle = sc.addStyle("Normal", null);
        normalStyle.addAttribute(StyleConstants.Foreground, Color.black);        
        errorStyle = sc.addStyle("Error", null);
        errorStyle.addAttribute(StyleConstants.Foreground, Color.red);        

        messages = new DefaultStyledDocument(sc);
        JTextPane messagesTextPane = new JTextPane(messages);
        messagesTextPane.setEditable(false);
        
    	JPanel messagesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		messagesPanel.setBackground(Color.white);
		messagesPanel.add(messagesTextPane);

		JScrollPane msgScroll = new JScrollPane(messagesPanel);
		msgScroll.setPreferredSize(new Dimension(0, MESSAGES_HEIGHT));
		msgScroll.setMaximumSize(new Dimension(0, MESSAGES_HEIGHT));
		msgScroll.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
		mainPanel.add(msgScroll, BorderLayout.SOUTH);
		 
		// ------------------------------------------------------------------

        loadConfiguration();
        
        this.setVisible(true);
		textToSearch.requestFocus();
        metrics = this.getGraphics().getFontMetrics();
	}
	
	/**
	 * Load the application.
	 */
	private void loadConfiguration() {
		try {
			logBrowser = new LogBrowser();
			
			// Load the apps names in the combo:
			List<String> names = logBrowser.getAppNames();
			appCombo.removeAllItems();
			appCombo.setModel(new DefaultComboBoxModel<String>(names.toArray(new String[0])));
			
			showMessage("Configuration loaded from " + LogBrowser.CONFIG_FILE);
			
		} catch (JAXBException e) {
			showError("JAXB Exception loading the configuration from " + LogBrowser.CONFIG_FILE + ": " + e.getMessage());
		} catch (LogBrowserException e) {
			showError("Error loading the configuration from " + LogBrowser.CONFIG_FILE + ": " + e.getMessage());
		}
	}

	/**
	 * ACTIONS: Search.
	 */
	private void search() {
		try {
			long t1 = System.currentTimeMillis();
			actionStarted();

			String appName = appCombo.getSelectedItem().toString();
			String text = textToSearch.getText();
			Date fromDate = fromDateModel.getValue();
			Date toDate = toDateModel.getValue();

			// Search and load results:
			List<InfoLine> infoLines = logBrowser.search(appName, fromDate, toDate, text);
			resultsTable.setResults(infoLines);
			
			// Select the search tab:
			tabsPanel.setSelectedIndex(0);
			
			long duration = (System.currentTimeMillis() - t1) / 1000;
			if (!text.isEmpty()) {
				// Hightlight results (only if a text was searched):
				SearchUtil.Results results = SearchUtil.highlightText(resultsTable, text);
				showMessage("Found " + results.totalFound + " results in " + results.linesFound + " lines (" + duration + " seconds)");
			} else {
				showMessage("Found " + infoLines.size() + " files (" + duration + " seconds)");
			}
			
		} catch (LogBrowserException e) {
			showError("Error: " + e.getMessage());
		} catch (IOException e) {
			showError("IO Exception: " + e.getMessage());
		} catch (JSchException e) {
			showError("JSch Exception: " + e.getMessage());
		} finally {
			actionEnded();
		}
	}
	
	/**
	 * ACTIONS: Download the found files to a folder.
	 */
	private void download() {
		try {
			long t1 = System.currentTimeMillis();
			actionStarted();

			File downloadFolder = null;
			try {
				downloadFolder = logBrowser.prepareDownload();
			} catch(FolderExistsException e) {
				int anwser = JOptionPane.showConfirmDialog(this, e.getMessage() + ". Overwrite?", 
						"Confirm folder overwrite", JOptionPane.YES_NO_OPTION);
				
				if (anwser == JOptionPane.YES_OPTION) {
					downloadFolder = e.getFolder();
				} else {
					showError("Download cancelled.");
					return;
				}
			}
			
			// Download the files:
			int count = logBrowser.download(downloadFolder, true);
			
			long duration = (System.currentTimeMillis() - t1) / 1000;
			showMessage("Downloaded: " + count + " files to " + downloadFolder.getPath() + ", in " + duration + " seconds.");
			
		} catch(LogBrowserException e) {
			showError("Error: " + e.getMessage());
		} catch(IOException e) {
			showError("IO Exception: " + e.getMessage());
		} finally {
			actionEnded();
		}
	}
	
	/**
	 * ACTIONS: Open the selected file.
	 */
	private void openFile() {
		try {
			long t1 = System.currentTimeMillis();
			actionStarted();

			// Get the file corresponding to the selected row:
			int selectedRow = resultsTable.getSelectedRow();
			LogFile logFile = resultsTable.getLogFile(selectedRow);
			Integer lineNumber = resultsTable.getLineNumber(selectedRow);
			
			// Open the file in a new JTable:
			JPanel fileTablePanel = new JPanel(new GridLayout(1,0));
			
			FileTable fileTable = new FileTable(fileTablePanel, this);
			fileTable.addMouseListener(this);

			JScrollPane scrollPane = new JScrollPane(fileTablePanel);
			scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
			tabsPanel.add(logFile.getName(), scrollPane);

			// Set a ButtonTabComponent (that includes a close icon) in place of the added ScrollPane :
			int index = tabsPanel.getTabCount() - 1;
			ButtonTabComponent btc = new ButtonTabComponent(tabsPanel);
			tabsPanel.setTabComponentAt(index, btc);

			// Read the file and set the content:
			int lines = fileTable.setContent(logFile.getLogLines());
			
			// Go to the selected line in the file:
			if (lineNumber != null) {
				fileTable.selectRow(lineNumber);
			}
			
			// Select the tab:
			tabsPanel.setSelectedIndex(index);
			
			long duration = (System.currentTimeMillis() - t1) / 1000;
			showMessage("File read: " + lines + " lines in " + duration + " seconds.");

		} catch(IOException e) {
			showError("IO Exception: " + e.getMessage());
		} finally {
			actionEnded();
		}
	}
	
	/**
	 * ACTIONS: Search a text in the selected file.
	 */
	private void searchFile() {
		try {
			if (tabsPanel.getTabCount() < 2) {
				throw new LogBrowserException("There are no open files to search");
			} else if (tabsPanel.getSelectedIndex() == 0) {
				throw new LogBrowserException("Please select the file to search");
			}

			long t1 = System.currentTimeMillis();
			actionStarted();
			
			String text = JOptionPane.showInputDialog(this, "Text to search:");
			// ... content of the selected panel:
			JScrollPane scroll = (JScrollPane)tabsPanel.getSelectedComponent();
			FileTable fileTable = (FileTable)((JPanel)scroll.getViewport().getView()).getComponents()[0];

			SearchUtil.Results results = SearchUtil.highlightText(fileTable, text);
			// Go to the result:
			fileTable.showLine(results.firstFoundLine);

			long duration = (System.currentTimeMillis() - t1) / 1000;
			showMessage("Found " + results.totalFound + " occurrences in " + results.linesFound + " lines (" + duration + " seconds)");
				
		} catch(LogBrowserException e) {
			showError("Error: " + e.getMessage());
		} catch(IOException e) {
			showError("IOException: " + e.getMessage());
		} finally {
			actionEnded();
		}
	}
	
	/**
	 * ACTIONS: Go to the previous highlighted result
	 */
	private void previous() {
		try {
			if (tabsPanel.getTabCount() < 1) {
				throw new LogBrowserException("There is nothing to search");
			}

			// ... content of the selected panel:
			JScrollPane scroll = (JScrollPane)tabsPanel.getSelectedComponent();
			ContentTable table = (ContentTable)((JPanel)scroll.getViewport().getView()).getComponents()[0];
			
			int line = SearchUtil.previous(table);
			if (line != -1) {
				table.fireTableDataChanged();
				// Go to the result:
				table.showLine(line);
			}
				
		} catch(LogBrowserException e) {
			showError("Error: " + e.getMessage());
		} catch(IOException e) {
			showError("IOException: " + e.getMessage());
		}
	}
	
	/**
	 * ACTIONS: Go to the next highlighted result
	 */
	private void next() {
		try {
			if (tabsPanel.getTabCount() < 1) {
				throw new LogBrowserException("There is nothing to search");
			}

			// ... content of the selected panel:
			JScrollPane scroll = (JScrollPane)tabsPanel.getSelectedComponent();
			ContentTable table = (ContentTable)((JPanel)scroll.getViewport().getView()).getComponents()[0];
			
			int line = SearchUtil.next(table);
			if (line != -1) {
				table.fireTableDataChanged();
				// Go to the result:
				table.showLine(line);
			}
				
		} catch(LogBrowserException e) {
			showError("Error: " + e.getMessage());
		} catch(IOException e) {
			showError("IOException: " + e.getMessage());
		}
	}

	// Aux. functions --------------------------------------
	
	private void actionStarted() {
		setButtonsEnabled(false);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	private void actionEnded() {
		setButtonsEnabled(true);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void setButtonsEnabled(boolean enabled) {
		prevButton.setEnabled(enabled);
		nextButton.setEnabled(enabled);
		searchButton.setEnabled(enabled);
		downloadButton.setEnabled(enabled);
		searchFilesButton.setEnabled(enabled);
	}
	
	private void showMessage(String newMessage) {
		try {
			int pos = messages.getLength();
			messages.insertString(pos, "\n" + newMessage + "\n", null);
			messages.setParagraphAttributes(pos, pos + newMessage.length(), normalStyle, false);
			
		} catch (BadLocationException e) { e.printStackTrace(); }
	}
	
	private void showError(String newMessage) {
		try {
			int pos = messages.getLength();
			messages.insertString(pos, "\n" + newMessage + "\n", null);
			messages.setParagraphAttributes(pos, pos + newMessage.length(), errorStyle, false);
			
		} catch (BadLocationException e) { e.printStackTrace(); }
	}
	
	// ActionListener implementation ------------------------------------------
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case PREV:
			previous();
			break;
		case NEXT:
			next();
			break;
		case SEARCH:
			search();
			break;
		case DOWNLOAD:
			download();
			break;
		case SEARCH_FILE:
			searchFile();
		}
	}

	// Date picker ChangeListener implementation -------------------------------------------------------
	
	@Override
	public void stateChanged(ChangeEvent e) {
		UtilDateModel dateModel = (UtilDateModel) e.getSource();

		// If fromDate is after toDate:
		// ...set the value of the other date, to the value of the one that has been set by the user:
		if (fromDateModel.getValue().after(toDateModel.getValue())) {
			if (dateModel == fromDateModel) {
				toDateModel.setValue(fromDateModel.getValue());
			} else if (dateModel == toDateModel) {
				fromDateModel.setValue(toDateModel.getValue());
			}
		}
	}

	// MouseLinstener implementation -------------------------------------------------------------------
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == resultsTable) {
			if (e.getClickCount() >= 2) {
				if (resultsTable.getSelectedRow() != -1) {
					openFile();
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
}
