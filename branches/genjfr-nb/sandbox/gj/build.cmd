@echo off
setlocal

if not exist "%ANT_HOME%\lib\ant.jar"  goto misconfigured
if not exist "%JAVA_HOME%\lib\tools.jar"  goto misconfigured

set classpath=%ANT_HOME%/lib/ant.jar;%CLASSPATH%

"%JAVA_HOME%\bin\java.exe" org.apache.tools.ant.Main %1 %2 %3 %4

goto end

:misconfigured
echo Please define ANT_HOME and JAVA_HOME

:end
endlocal

