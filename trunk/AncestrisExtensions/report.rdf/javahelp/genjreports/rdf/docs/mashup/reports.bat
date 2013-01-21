@echo off

rem #### put your choice last, adjust username and version numbers
set  APP=C:\Program%20Files\ancestris
set MODS=C:\Users\USERNAME\Appdata\Roaming\.ancestris\0.8
rem
set  APP=C:\Users\USERNAME\Documents\ancestris-20121002\ancestris
set MODS=C:\Users\USERNAME\Appdata\Roaming\.ancestris\trunk

rem #### required for semweb 
set CLASSPATH=
set CLASSPATH=%CLASSPATH%;%MODS%\modules\genjreports-rdf.jar
set CLASSPATH=%CLASSPATH%;%MODS%\modules\docs\genjreports-rdf.jar
set CLASSPATH=%CLASSPATH%;%MODS%\modules\ext\*
set JAVA_OPTS= -Xmx1024M -Dlog4j.configuration=file:log4j.properties

rem #### required for gedsem 
set CLASSPATH=%CLASSPATH%;%APP%\ancestris\modules\ancestris-core.jar
set CLASSPATH=%CLASSPATH%;%APP%\ancestris\modules\ancestris-libs-genj.jar
set CLASSPATH=%CLASSPATH%;%APP%\platform\*
set CLASSPATH=%CLASSPATH%;%APP%\platform\lib\*
set JAVA_OPTS=%JAVA_OPTS% -Dnetbeans.user.dir=%APP%\bin
set JAVA_OPTS=%JAVA_OPTS% -Dnetbeans.home=%APP%\platform
set JAVA_OPTS=%JAVA_OPTS% -Dnetbeans.user=%MODS%
set JAVA_OPTS=%JAVA_OPTS% -Dnetbeans.dirs=%APP%\ancestris

set GEDCOM_BOURBON=%APP%\ancestris\exemples\gen-bourbon\bourbon.ged
set GEDCOM_JFK=%APP%\ancestris\exemples\gen-kennedy\kennedy.ged

rem #### end of environment configuration
rem #####################################################################

rem #### download http://anonymous:password@arvernes.dyndns.org/fgenj/trunk/AncestrisExtensions/report.rdf/javahelp/genjreports/rdf/docs/reports/*Rules.txt
rem #### download http://anonymous:password@arvernes.dyndns.org/fgenj/trunk/AncestrisExtensions/report.rdf/javahelp/genjreports/rdf/docs/mashup/*.arq
set   DOCS=C:\YOUR_DOWNLOAD_FOLDER
set    URI=http://my.domain.com/gedcom/
set GEDCOM=-gedcom %GEDCOM_JFK%
set  RULES=-rules %DOCS%\reports\SlowRules.txt
set GEDTTL=gedcom.ttl

echo on

@rem #### see http://anonymous:password@arvernes.dyndns.org/fgenj/trunk/AncestrisExtensions/report.rdf/javahelp/genjreports/rdf/docs/images/chart.jpg

java %JAVA_OPTS% genjreports.rdf.gedsem.Convert %GEDCOM% -uri %URI% -format ttl 1>gedcom.ttl 
java %JAVA_OPTS% genjreports.rdf.semweb.Select gedcom.ttl mashup.ttl mashup.tsv %DOCS%\mashup\mashup.arq
@echo hit any key when ready adding missing GeoNameIDs to mashup.tsv
@pause >null
java %JAVA_OPTS% genjreports.rdf.semweb.Mashup gedcom.ttl mashup.ttl %URI% mashup.tsv "de|fr"
java %JAVA_OPTS% genjreports.rdf.semweb.Select gedcom.ttl mashup.ttl *.rdf report.txt %DOCS%\mashup\classmates.arq
@rem #### add/delete the reports you want

@pause
@exit /B