#!/bin/sh
#

$JAVA_HOME/bin/java -cp $JAVA_HOME/lib/tools.jar:./app/contrib/ant/ant.jar org.apache.tools.ant.Main $1 $2 $3

