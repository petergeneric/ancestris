export MODS=/PATH_TO/.ancestris/trunk
export APP=/PATH_TO/ancestris

### required for semweb 
export CLASSPATH=
export CLASSPATH=$CLASSPATH$,$MODS$/modules/genjreports-rdf.jar
export CLASSPATH=$CLASSPATH$,$MODS$/modules/docs/genjreports-rdf.jar
export CLASSPATH=$CLASSPATH$,$MODS$/modules/ext/*
export JAVA_OPTS= -Xmx1024M -Dlog4j.configuration=file:log4j.properties

### additional requirements for gedsem 
export CLASSPATH=$CLASSPATH$,$APP$/ancestris/modules/ancestris-core.jar
export CLASSPATH=$CLASSPATH$,$APP$/ancestris/modules/ancestris-libs-genj.jar
export CLASSPATH=$CLASSPATH$,$APP$/platform/lib/*
export JAVA_OPTS=$JAVA_OPTS$ -Dnetbeans.user.dir=$APP$/bin
export JAVA_OPTS=$JAVA_OPTS$ -Dnetbeans.home=$APP$/platform
export JAVA_OPTS=$JAVA_OPTS$ -Dnetbeans.user=$MODS$
export JAVA_OPTS=$JAVA_OPTS$ -Dnetbeans.dirs=$APP$/ancestris
