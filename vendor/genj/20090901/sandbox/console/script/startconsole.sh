#!/bin/sh
# Modifiy the two environment variables to meet your needs
GENJ_INSTALL_DIR=~/gj
GENJ_CONSOLE_INSTALL_DIR=~/gj-console
USE_SOUND=-Dconsole.sound=true
USE_READLINE=-Dconsole.use-readline=true
java $USE_SOUND $USE_READLINE \
     -classpath $GENJ_CONSOLE_INSTALL_DIR/lib/libreadline-java.jar:$GENJ_INSTALL_DIR/lib/genj.jar:$GENJ_CONSOLE_INSTALL_DIR/lib/console.jar \
     com.sadinoff.genj.console.Console "$@"
