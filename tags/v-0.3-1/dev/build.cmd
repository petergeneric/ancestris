@echo off
setlocal

set classpath=%JAVA_HOME%/lib/tools.jar;./app/contrib/ant/ant.jar
"%JAVA_HOME%\bin\java" org.apache.tools.ant.Main %1 %2 %3 %4

endlocal

