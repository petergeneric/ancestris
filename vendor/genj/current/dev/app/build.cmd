@echo off
setlocal

set classpath=%JAVA_HOME%/lib/tools.jar;./contrib/ant/ant.jar

echo Classpath is %classpath%

"%JAVA_HOME%\bin\java" org.apache.tools.ant.Main %1 %2 %3 %4

endlocal

