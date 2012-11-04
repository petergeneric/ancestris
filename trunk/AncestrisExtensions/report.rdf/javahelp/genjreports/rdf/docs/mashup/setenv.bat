set MODS=C:\Users\user\Appdata\Roaming\.ancestris\trunk
set APP=C:\Program Files\ancestris

@echo off

if EXIST %MODS% goto :okMods
echo please fix me: MODS does not exist
pause
exit /B
:okMods

if EXIST %APP% goto :okApp
echo please fix me: MODS does not exist
pause
exit /B
:okApp

set CLASSPATH=%MODS%\modules\genjreports-rdf.jar;%APP%\ancestris\modules\*;%APP%\platform\*;%APP%\platform\lib\*
set JAVA_OPTS= -Xmx1024M -Dlog4j.configuration=file:log4j.properties
echo on
