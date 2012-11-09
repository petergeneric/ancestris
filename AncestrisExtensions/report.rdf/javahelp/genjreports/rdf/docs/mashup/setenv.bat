set APP=C:\Users\Falkink\Documents\ancestris-20121002\ancestris
set MODS=C:\Users\Falkink\Appdata\Roaming\.ancestris\trunk

rem ### required for semweb 
set CLASSPATH=
set CLASSPATH=%CLASSPATH%;%MODS%\modules\genjreports-rdf.jar
set CLASSPATH=%CLASSPATH%;%MODS%\modules\docs\genjreports-rdf.jar
set CLASSPATH=%CLASSPATH%;%MODS%\modules\ext\*
set JAVA_OPTS= -Xmx1024M -Dlog4j.configuration=file:log4j.properties

rem ### required for gedsem 
set CLASSPATH=%CLASSPATH%;%APP%\ancestris\modules\ancestris-core.jar
set CLASSPATH=%CLASSPATH%;%APP%\ancestris\modules\ancestris-libs-genj.jar
set CLASSPATH=%CLASSPATH%;%APP%\platform\lib\*
set JAVA_OPTS=%JAVA_OPTS% -Dnetbeans.user.dir=%APP%\bin
set JAVA_OPTS=%JAVA_OPTS% -Dnetbeans.home=%APP%\platform
set JAVA_OPTS=%JAVA_OPTS% -Dnetbeans.user=%MODS%
set JAVA_OPTS=%JAVA_OPTS% -Dnetbeans.dirs=%APP%\ancestris

