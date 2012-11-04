set MODS=/PATH_TO/.ancestris/trunk
set APP=/PATH_TO/ancestris

export CLASSPATH=$MODS$/modules/genjreports-rdf.jar;$APP$/ancestris/modules/*;$APP$/platform/*;$APP$/platform/lib/*
export JAVA_OPTS= -Xmx1024M -Dlog4j.configuration=file:log4j.properties
