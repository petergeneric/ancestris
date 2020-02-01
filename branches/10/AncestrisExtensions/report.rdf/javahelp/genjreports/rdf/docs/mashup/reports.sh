#####################################################
### you might need other paths depending 
### on the configuration of your system
###--------------------------------------------------
export APP=~/Library/Application%20Support/ancestris
export MODS=~/.ancestris otherwise
#####################################################
### put the desired file last
###--------------------------------------------------
export GED=$APP$/ancestris/exemples/gen-bourbon/bourbon.ged
export GED=$APP$/ancestris/exemples/gen-kennedy/kennedy.ged
###
#####################################################
### typically where you publish your gedcom
### do not omit the terminator
###--------------------------------------------------
export URI=http://my.domain.com/gedcom/
#####################################################

### required for semweb 
export CLASSPATH=
export CLASSPATH=$CLASSPATH$,$MODS$/modules/genjreports-rdf.jar
export CLASSPATH=$CLASSPATH$,$MODS$/modules/docs/genjreports-rdf.jar
export CLASSPATH=$CLASSPATH$,$MODS$/modules/ext/*
export JAVA_OPTS= -Xmx1024M -Dlog4j.configuration=file:log4j.properties

### required for gedsem 
export CLASSPATH=$CLASSPATH$,$APP$/ancestris/modules/ancestris-core.jar
export CLASSPATH=$CLASSPATH$,$APP$/ancestris/modules/ancestris-libs-genj.jar
export CLASSPATH=$CLASSPATH$,$APP$/platform/lib/*
export JAVA_OPTS=$JAVA_OPTS$ -Dnetbeans.user.dir=$APP$/bin
export JAVA_OPTS=$JAVA_OPTS$ -Dnetbeans.home=$APP$/platform
export JAVA_OPTS=$JAVA_OPTS$ -Dnetbeans.user=$MODS$
export JAVA_OPTS=$JAVA_OPTS$ -Dnetbeans.dirs=$APP$/ancestris

echo on

echo =============
echo convert? CTRL-C to abort
pause
java $JAVA_OPTS$ genjreports.rdf.gedsem.Convert -gedcom $GED$ -uri $URI$ -format ttl > gedcom.ttl 

echo =============
echo collect place names? CTRL-C to abort
pause
java $JAVA_OPTS$ genjreports.rdf.semweb.Select mashup.tsv gedcom.ttl mashup.tsv mashup.arq

echo =============
echo PLEASE: add missing GeoNameIDs to mashup.tsv
echo ready to collect data from GeoNames and DbPedia? CTRL-C to abort
pause
java $JAVA_OPTS$ genjreports.rdf.semweb.Mashup mashup.tsv $URI$ mashup.ttl "de|fr"

echo =============
echo create reports? CTRL-C to abort
pause
java $JAVA_OPTS$ genjreports.rdf.semweb.Select gedcom.ttl mashup.ttl *.rdf report.txt classmates.arq

##########################################################
### Extend/duplicate the lat semwb.Select to add
### the reports you want. You might need to add
### "-rules slowRules.txt" to the gedsem.Convert command
### 
### Omit all arguments for more help on the commands
### gedsem.Convert, semweb.Mashup, semweb.Select
##########################################################

pause

