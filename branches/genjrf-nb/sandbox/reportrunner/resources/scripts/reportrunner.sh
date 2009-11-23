#!/bin/sh

# GenJ has to be run from inside it's installation directory
# so change into directory where this script was started
cd `dirname $0`

# check the script for being a symbolik link we can follow
SCRIPT=`basename $0`
while [ -h "$SCRIPT" ]; do
 SCRIPT=`ls -l $SCRIPT | grep -o '[-_/.[:alnum:]]*$'`
 echo "*** INFO: Following symlink $SCRIPT"
 cd `dirname $SCRIPT`
 SCRIPT=`basename $SCRIPT`
done

# final check if the GenJ main archive is right here
if [ ! -f "./run.jar"  ]; then
 echo "*** ERROR: Missing GenJ resource(s) in "`pwd`
 exit 1
fi

echo "*** INFO: Running GenJ from"`pwd`

# find java
JAVA=$JAVA_HOME/bin/java
if [ ! -x "$JAVA" ]; then
 JAVA=`which java`
 if [ $? -eq 1 ]; then
  echo "*** ERROR: Can't find java executable"
  exit 1
 fi
fi

# run it (we start the virtual machine with initially 32 MB and allocate a max of 512 MB)
CMD="$JAVA -Xmx512m -Xms32m -jar lib/genj-reportrunner.jar $1 $2 $3 $4 $5 $6 $7 $8 $9"

echo "*** INFO: Executing '$CMD'"

$CMD
