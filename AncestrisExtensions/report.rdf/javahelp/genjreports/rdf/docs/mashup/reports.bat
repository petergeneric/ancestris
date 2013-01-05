@echo off
rem #####################################################
rem ### replace USERNAME; put the proper duo last
rem ### depending on the configuration of you system
rem ### you might need other paths to ancestris
rem ###--------------------------------------------------
set APP=C:\Program%20Files\ancestris
set MODS=C:\Users\USERNAME\Appdata\Roaming\.ancestris\0.8
rem ###
set APP=C:\Users\USERNAME\Documents\ancestris-20121002\ancestris
set MODS=C:\Users\USERNAME\Appdata\Roaming\.ancestris\trunk
rem #####################################################
rem ### put the desired file last
rem ###--------------------------------------------------
set GED=%APP%\ancestris\exemples\gen-bourbon\bourbon.ged
set GED=%APP%\ancestris\exemples\gen-kennedy\kennedy.ged
rem ###
rem #####################################################
rem ### typically where you publish your gedcom
rem ### do not omit the terminator
rem ###--------------------------------------------------
set URI=http://my.domain.com/gedcom/
rem #####################################################

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

echo on

@echo =============
@echo convert? CTRL-C to abort
@pause
java %JAVA_OPTS% genjreports.rdf.gedsem.Convert -gedcom %GED% -uri %URI% -format ttl > gedcom.ttl 

@echo =============
@echo collect place names? CTRL-C to abort
@pause
java %JAVA_OPTS% genjreports.rdf.semweb.Select mashup.ttl gedcom.ttl mashup.tsv mashup.arq

@echo =============
@echo PLEASE: add missing GeoNameIDs to mashup.tsv
@echo ready to collect data from GeoNames and DbPedia? CTRL-C to abort
@pause
java %JAVA_OPTS% genjreports.rdf.semweb.Mashup mashup.tsv %URI% mashup.ttl "de|fr"

@echo =============
@echo create reports? CTRL-C to abort
@pause
java %JAVA_OPTS% genjreports.rdf.semweb.Select gedcom.ttl mashup.ttl *.rdf report.txt classmates.arq

@rem ##########################################################
@rem ### Extend/duplicate the lat semwb.Select to add
@rem ### the reports you want. You might need to add
@rem ### "-rules slowRules.txt" to the gedsem.Convert command
@rem ### 
@rem ### omit all arguments for more help on the commands
@rem ### gedsem.Convert, semweb.Mashup, semweb.Select
@rem ##########################################################

@pause
@exit /B