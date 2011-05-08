@echo off
setlocal

set classpath=%ANT_HOME%/lib/ant.jar

%JAVA_HOME%\bin\java org.apache.tools.ant.Main %1 %2 %3 %4

endlocal

