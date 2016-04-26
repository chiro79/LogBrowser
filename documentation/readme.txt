LogBrowser
**********
Aplicacion para buscar textos en los ficheros de logs.

Caracteristicas:
***************
- Los ficheros de logs se configuran en el fichero de configuración (config.xml)
- Se pueden configurar los logs de varias aplicaciones.
- Los ficheros pueden ser locales o remotos (con acceso HTTP o SSH).
- Los ficheros tienen que ser ficheros de texto.

Funcionalidad:
**************
1. SEARCH: search for a text in the log files. Parameters:
  - Logs App: the application of the files to search.
  - Search Text: si se deja en blanco, se listan todos los ficheros de las fechas especificadas.
  - From / To: el rango de fechas de los ficheros a examinar (ver configuracion). 
  	Si en To se selecciona una fecha anterior a From, From se cambia a To.
  	Si en From se selecciona una fecha anterior a To, To se cambia a From.
  		>> De esta forma se puede seleccionar directamente una sola fecha o un rango
  
2. DOWNLOAD: permite bajar los ficheros encontrados a un directorio local (ver configuracion).
   Se crea una carpeta con nombre: application_search date(s). Ej: 
   		LOCAL_2016-04-12
   		LOCAL_2016-04-12_2016-04-14

3. Abrir un fichero: doble click en la table de resultados abre el fichero correspondiente.
   Si el ratón está sobre una linea, el fichero se abre en esa linea.
   
4. SEARCH FILE: busca un texto en el fichero abierto.
	Los botones 'anterior' (<) y 'siguiente' (>) permiten desplazarse entre los resultados encontrados en el fichero abierto.

BUSQUEDA:
la fecha de hoy se sustituye o no.

Configuracion:
**************
1.Parametros generales:
	<dateFormat>yyyy-MM-dd</dateFormat> 
	<dateSeparator>.</dateSeparator>
	Determinan el formato de la fecha en el nombre de los ficheros de log. 
	Ej: operation{date}  -> server.2016-04-05.log
	 
	<downloadBaseFolder>c:/devel/logs/</downloadBaseFolder>
	<downloadExtension>.log</downloadExtension>
	Configuran la descarga de archivos.
	downloadExtension permite opcionalmente añadir una extension comun a los archivos descargados.

2.Applicaciones

OJO: comprobar la coherencia entre los slash de inicio/fin del directorio base y el path de los ficheros. Ej:
OK:		
	basedir="/logs/dev" 
	<file>/jboss/default/server.log</file>	
	-> Resultado: /logs/dev/jboss/default/server.log
WRONG:	
	basedir="/logs/dev" 
	<file>jboss/default/server.log</file>	
	-> Resultado: /logs/devjboss/default/server.log

Ejecutable:
**********
Estructura:
	target
		dependency-jars
			commons-io-2.4.jar
			...
		config.xml
		LogBrowser.jar
	LogBrowser.bat

El JAR se genera con maven install.
El POM incluye la copia del fichero de configuración (config.xml) al directorio target.
