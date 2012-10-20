set JENAROOT=D:\apache-jena-2.7.3\apache-jena-2.7.3
set MODS=C:\Users\user\Appdata\Roaming\.ancestris\trunk\modules

set CLASSPATH=.;%MODS%\genjreports-rdf.jar

rem CLASSPATH=%CLASSPATH%;%MODS%\ext\arq-2.8.7.jar
rem CLASSPATH=%CLASSPATH%;%MODS%\ext\iri-0.8.jar
rem CLASSPATH=%CLASSPATH%;%MODS%\ext\jena-2.6.4.jar
set CLASSPATH=%CLASSPATH%;%MODS%\ext\log4j-1.2.13.jar
set CLASSPATH=%CLASSPATH%;%MODS%\ext\slf4j-api-1.5.8.jar
set CLASSPATH=%CLASSPATH%;%MODS%\ext\slf4j-log4j12-1.5.8.jar
rem CLASSPATH=%CLASSPATH%;%MODS%\ext\xercesImpl-2.7.1.jar

set CLASSPATH=%CLASSPATH%;%JENAROOT%\lib\jena-arq-2.9.3.jar
set CLASSPATH=%CLASSPATH%;%JENAROOT%\lib\jena-core-2.7.3.jar
set CLASSPATH=%CLASSPATH%;%JENAROOT%\lib\jena-iri-0.9.3.jar
rem CLASSPATH=%CLASSPATH%;%JENAROOT%\lib\log4j-1.2.13.jar
rem CLASSPATH=%CLASSPATH%;%JENAROOT%\lib\slf4j-api-1.5.8.jar
rem CLASSPATH=%CLASSPATH%;%JENAROOT%\lib\slf4j-log4j12-1.6.4.jar
set CLASSPATH=%CLASSPATH%;%JENAROOT%\lib\xercesImpl-2.10.0.jar
set CLASSPATH=%CLASSPATH%;%JENAROOT%\lib\xml-apis-1.4.01.jar

java genjreports.rdf.semweb.Mashup %1 http://my.domain.com/gedcom/places/ %2 "de|fr"