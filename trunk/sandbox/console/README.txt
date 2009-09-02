This is GenJ:Textmode
by Danny Sadinoff  Copyright (c) 2006
http://sadinoff.com/

GenJ:Textmode exposes a console view on the GenJ Gedcom API.  
The intent is to allow for purely keyboard-driven genealogical data entry with
an emphasis on speed.

Right now, this textual user interface is to be used as a compliment to the main
GenJ GUI.   In order to streamline data entry, it has a bare-bones simple model
for how Individuals relate to Families, and cannot explore the full range of
expression that the (impliedd) GEDCOM datamodel allows.

The idea is to:
1) Do the bulk of the data entry in Textmode
2) save the  GEDCOM file and quit, then 
3) Re-open the file in GenJ proper, and do whatever cleanups/enhancement 
   are necessary

BUILD:
   run "ant"
   
running:
  java -classpath PATH_TO_GENJ/genj.jar:PATH_TO_CONSOLE/console.jar com.sadinoff.genj.console.Console  myfile.ged
or
  java [classpath_option] com.sadinoff.genj.console.Console -u URL_TO_GEDCOM
  
There is no runtime-dependency on the GNU Readline library, but it's necessary in order to compile.
download and install it in order to build.
get it from http://java-readline.sf.net/
