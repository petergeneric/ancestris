#!/bin/sh

#
# resolve symlinks
#

PRG=$0

echo " "
echo " "
echo "Identifying path and application name:"
while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
	PRG="$link"
    else
	PRG="`dirname "$PRG"`/$link"
    fi
done

progdir=`dirname "$PRG"`
APPNAME=`basename "$PRG"`

echo "   progdir=$progdir"
echo "   APPNAME=$APPNAME"
echo " "



echo "Checking configuration file on progdir/../etc:"
if [ -f "$progdir/../etc/$APPNAME".conf ] ; then
    echo "   Configuration file found. Executing it."
    . "$progdir/../etc/$APPNAME".conf
else
    echo "   Configuration file not found."
fi
echo " "



# XXX does not correctly deal with spaces in non-userdir params
args=""


echo "Identifying userdir from configuration file:"
case "`uname`" in
    Darwin*)
        userdir="${default_mac_userdir}"
        ;;
    *)
        userdir="${default_userdir}"
        ;;
esac
echo "   userdir(from conf file)=$userdir"

while [ $# -gt 0 ] ; do
    case "$1" in
        --userdir) shift; if [ $# -gt 0 ] ; then userdir="$1"; fi
            ;;
        *) args="$args \"$1\""
            ;;
    esac
    shift
done
echo "   userdir(after argument overwrite)=$userdir"
echo " "

# 2021-02-03 - FL : this file can get locked on MacOS, and prevent Ancestris restart, so remove it before launch.
rm -fr $userdir/var/cache/netigso-bundles


echo "Checking configuration file on userdir/etc:"
if [ -f "${userdir}/etc/$APPNAME".conf ] ; then
    echo "   Configuration file found. Executing It."
    . "${userdir}/etc/$APPNAME".conf
else
    echo "   Configuration file from userdir not found."
fi
echo " "


echo "Checking if jdkhome is defined: (for MacOS, you might need to add /Contents/Home at the end)"
echo "   jdkhome=$jdkhome"
if [ -z "$jdkhome" ]; then
    echo "   jdkhome not defined."
fi
if [ -n "$jdkhome" -a \! -d "$jdkhome" -a -d "$progdir/../$jdkhome" ]; then
    # #74333: permit jdkhome to be defined as relative to app dir
    jdkhome="$progdir/../$jdkhome"
    echo "   jdkhome changed to:$jdkhome"
fi
#
# Test presence of JAVA
#
case "`uname`" in
    Darwin*)
        /usr/libexec/java_home &> /dev/null && {
          echo "================================================================";
          echo "JAVA is installed.";
          java -version;
          echo "================================================================";
          if [ -z "$jdkhome" ]; then
             jdkhome=`/usr/libexec/java_home`
             echo "jdkhome was empty and therefore changed to $jdkhome"
          fi
        } || {
          echo ""
          echo "   JAVA is NOT installed ! Please install it. Ancestris cannot start.";
          echo ""
          echo ""
          osascript -e 'display dialog "Ancestris launch alert !\n\nJAVA is missing. Ancestris requires JAVA.\n\nPlease install JAVA version 8 or 11. Feel free to follow the Ancestris instructions in the online documentation.\n" with icon POSIX file "/Applications/Ancestris.app/Contents/Resources/Ancestris.icns" buttons {"OK"} default button 1';
          exit 1;
        }

        ;;
    *)
        if type -p java; then
            echo "Found java executable in PATH"
            _java=`type -p java`
        elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
            echo "Found java executable in JAVA_HOME/bin"
            _java="$JAVA_HOME/bin/java"
        elif [[ -n "$jdkhome" ]];  then
            echo "jdkhome defined"
            _java="$jdkhome/bin/java"
        else
            echo ""
            echo "   JAVA is NOT installed ! Please install it. Ancestris cannot start.";
            echo ""
            echo ""
            zenity --notification \
                --window-icon="`pwd`/ancestris128.gif" \
                --text "Ancestris alert    -    JAVA is missing!\n\nAncestris requires JAVA.\n\nPlease install JAVA version 8 or 11. Feel free to follow the Ancestris instructions in the online documentation.\n"
            echo -e '\a'    
            exit 1;
        fi

        if [[ "$_java" ]]; then
            version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
            echo "================================================================";
            echo "JAVA is installed.";
            java -version;
            echo "================================================================";
            if [[ "$version" > "1.8" ]]; then
                echo "JAVA version is more than 1.8"
                if [ -z "$jdkhome" ]; then
                   echo "jdkhome was left empty."
                fi
            else         
                echo "JAVA version is less than 1.8"
                zenity --notification \
                --window-icon="`pwd`/ancestris128.gif" \
                --text "Ancestris warning    -    JAVA version should be 1.8 or more!\n\nAncestris requires JAVA version 1.8 or more.\n\nAncestris will try to launch anyway. Otherwise please install JAVA version 8 (i.e. 1.8) or 11. Feel free to follow the Ancestris instructions in the online documentation.\n"        
                echo -e '\a'
            fi
        fi

        ;;
esac
echo " "


readClusters() {
  if [ -x /usr/ucb/echo ]; then
    echo=/usr/ucb/echo
  else
    echo=echo
  fi
  while read X; do
    if [ "$X" \!= "" ]; then
      $echo "$progdir/../$X"
    fi
  done
}

absolutize_paths() {
    while read path; do
        if [ -d "$path" ]; then
            (cd "$path" 2>/dev/null && pwd)
        else
            echo "$path"
        fi
    done
}

echo "Defining clusters:"
clusters=`(cat "$progdir/../etc/$APPNAME".clusters; echo) | readClusters | absolutize_paths | tr '\012' ':'`
if [ ! -z "$extra_clusters" ] ; then
    clusters="$clusters:$extra_clusters"
fi
echo "   clusters=$clusters"
echo " "


echo "Defining exec command:"
nbexec=`echo "$progdir"/../platform*/lib/nbexec`
echo "   nbexec=$nbexec"
echo " "



echo "Running exec command:"
# On MAC OC appname should be displayed Ancestris and not ancestris
case "`uname`" in
    Darwin*)
       echo "   => MacOS system detected..."
       echo " "
       cmd="exec sh '"$nbexec"' \
            --jdkhome '"$jdkhome"' \
            -J-Dcom.apple.mrj.application.apple.menu.about.name='"$APPNAME"' \
            -J-Xdock:name='"Ancestris"' \
            '"-J-Xdock:icon=$progdir/../../$APPNAME.icns"' \
            --clusters '"$clusters"' \
            --userdir '"${userdir}"' \
            ${default_options} \
            "$args""
        ;;
    *)  
       echo "   => Linux system detected..."
       echo " "
       sh=sh
       # #73162: Ubuntu uses the ancient Bourne shell, which does not implement trap well.
       if [ -x /bin/bash ]
       then
           sh=/bin/bash
       fi
       cmd="exec $sh '"$nbexec"' \
            --jdkhome '"$jdkhome"' \
            --clusters '"$clusters"' \
            --userdir '"${userdir}"' \
            ${default_options} \
            "$args""
       
        ;;
esac
echo "Command to be executed:"
echo " "
echo "$cmd"
echo " "
eval $cmd
echo " "
echo " "
echo " "
echo " "
echo " "



