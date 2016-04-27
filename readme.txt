LogBrowser: tool for searching in local and remote log files, and for downloading remote files.

Features:
*********
- Log files are configured in an XML (config.xml)
- Several applications can be configured, when there are several applications generating logs.
- Log files can be local or remote (with HTTP, HTTPS or SSH access).
- The program can search files of different dates.
- Log files can be compressed (currently with GZIP).

Functions:
**********
1. [Search] button: search in the log files configured in the selected application and dates. 
   If a text is entered, the program looks for the text; else, a list of the log files is shown.

	Parameters:
	  [Logs App]: the application of the files to search.
	  [Search Text]: the text to search (if blank, all the files of the selected dates).
	  [From] / [To]: the range of dates of the files.
	  	Note: 
	  	 - If [To] is changed to a date before [From], [From] is automatically set to [To].
	  	 - If [From] is changed to a date before [To], [To] is automatically set to [From].
	  	 (This allows to directly select a single date or a range of dates)
  
2. [Download] button: downloads to a local folder the files found in a previous search.
   A folder is created with the following name: application_search date(s). Eg: 
   		LOCAL_2016-04-12
   		PROD_2016-04-12_2016-04-14

3. Double click on a row of the results opens the corresponding file.
   If the row corresponds to a specific line of the file, when the file is opened the cursor goes to that line. Eg:
   		
4. [Search File] button: search a text in an opened file.
	Buttons [<] and [>] allow to navigate between the found occurrences.

Test:
*****
A 'test' folder is included with a test configuration, some local files and a folder to download:
   1. Download the application (github has a button 'Download ZIP') 
   2. Extract the folder called test.
   3. Run LogBrowser.bat

Configuration:
**************
(see the examples included in the provided config.xml)
 
1.General parameters:
	<dateFormat>	Determines the format of the date included in the name of the log files.
					Typically there will be log files for different dates: errors.2016-04-05.log, errors.2016-04-06.log, etc.
					This is configured using {date} in the name of the file: errors.{date}.log
					In this case, the parameter should be: <dateFormat>yyyy-MM-dd</dateFormat>
	 
	<downloadBaseFolder>	Specifies the local folder where the files will be downloaded.
							Eg: <downloadBaseFolder>c:/devel/logs/</downloadBaseFolder>

	<downloadExtension>		Specifies a common extension to be added to the name of the downloaded files (or nothing if left blank).
							Eg: <downloadExtension>.log</downloadExtension>

2.Application parameters:
	<apps>			List of <appConfig> elements for every application.
	
	<appConfig>		An application that generates logs.
					Attributes:
						name= 	Descriptive name for the app. Eg: 'PROD', or 'Development logs'
								Required.

	<logs>			List of <logConfig> elements for every group of log files
	 
	<logConfig>		A group of log files with similar characteristics.
					Attributes:
						type= 	Type of location/access of the files. Possible values: LOCAL, HTTPS, HTTP, SSH
								Required.

						basedir=	Common directory of the files of this group.
									Optional.

						alias= 		A name to be added to the name of the files of this group when downloaded.
									Can be used to identify the different servers of the downloaded files. 
									Optional. 

						canBeCompressed=	Indicates if this log files can be compressed.
											Typically old log files are compressed, while recent files (or just the current log) are not. 
											This is used in the search: the program looks first for a compressed version of the file: 
												When specified, the program will look for a file with name 'xxx.gz'. 
												If it doesn't find it, it will look for a file called 'xxx'. 
											Possible values: GZ (files compressed with GZIP).
											Optional.

						host, user, pwd=	Attributes used in for remote host access, in types: HTTPS, HTTP and SSH. 
											Examples:
											<logConfig type="SSH" host="test.rebex.net" user="demo" pwd="password">
											<logConfig type="HTTP" host="gnusha.org">
											<logConfig type="HTTPS" host="github.com/agustin-miquel/LogBrowser">
											Host is required for HTTPS, HTTP and SSH. User and pwd are optional.

	<file> 		A single file path. 
				The full path of the file will be composed with basedir + file. Eg:
					basedir="/logs/dev" 
					<file>/jboss/default/server.log</file>	
					...will result in: /logs/dev/jboss/default/server.log
				Note: be carefull with leading and trailing slashes. Eg	
					basedir="/logs/dev" 
					<file>jboss/default/server.log</file>	
					...will result in: /logs/devjboss/default/server.log	(wrong name)

Executable:
***********
The structure of the executable is:
	target
		dependency-jars
			commons-io-2.4.jar
			...
		config.xml
		LogBrowser.jar
	LogBrowser.bat

The JAR is generated with maven install. The POM includes a task to copy the configuration file to the target folder.

To Do:
******
- Case sensitive search
- Today's date replacement in the name of the file should be configurable.
- Date format configured per LogConfig
