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
       echo " "
        eval exec sh '"$nbexec"' \
            --jdkhome '"$jdkhome"' \
            -J-Dcom.apple.mrj.application.apple.menu.about.name='"$APPNAME"' \
            -J-Xdock:name='"Ancestris"' \
            '"-J-Xdock:icon=$progdir/../../$APPNAME.icns"' \
            --clusters '"$clusters"' \
            --userdir '"${userdir}"' \
            ${default_options} \
            "$args"
        ;;
    *)  
       echo "   => Linux system detected..."
       echo " "
       echo " "
       sh=sh
       # #73162: Ubuntu uses the ancient Bourne shell, which does not implement trap well.
       if [ -x /bin/bash ]
       then
           sh=/bin/bash
       fi
       eval exec $sh '"$nbexec"' \
            --jdkhome '"$jdkhome"' \
            --clusters '"$clusters"' \
            --userdir '"${userdir}"' \
            ${default_options} \
            "$args"
       exit 1
        ;;
esac
echo " "
echo " "
echo " "
echo " "
echo " "



