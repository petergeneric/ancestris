set JENAROOT=/PATH_TO/apache-jena-2.7.3
set MODS=/PATH_TO/.ancestris/trunk/modules

set CLASSPATH=$CLASSPATH:$MODS/genjreports-rdf.jar

#set CLASSPATH=$CLASSPATH$;$MODS$/ext/arq-2.8.7.jar
#set CLASSPATH=$CLASSPATH$;$MODS$/ext/iri-0.8.jar
#set CLASSPATH=$CLASSPATH$;$MODS$/ext/jena-2.6.4.jar
 set CLASSPATH=$CLASSPATH$;$MODS$/ext/log4j-1.2.13.jar
 set CLASSPATH=$CLASSPATH$;$MODS$/ext/slf4j-api-1.5.8.jar
 set CLASSPATH=$CLASSPATH$;$MODS$/ext/slf4j-log4j12-1.5.8.jar
#set CLASSPATH=$CLASSPATH$;$MODS$/ext/xercesImpl-2.7.1.jar

 set CLASSPATH=$CLASSPATH$;$JENAROOT$/lib/jena-arq-2.9.3.jar
 set CLASSPATH=$CLASSPATH$;$JENAROOT$/lib/jena-core-2.7.3.jar
 set CLASSPATH=$CLASSPATH$;$JENAROOT$/lib/jena-iri-0.9.3.jar
#set CLASSPATH=$CLASSPATH$;$JENAROOT$/lib/log4j-1.2.13.jar
#set CLASSPATH=$CLASSPATH$;$JENAROOT$/lib/slf4j-api-1.5.8.jar
#set CLASSPATH=$CLASSPATH$;$JENAROOT$/lib/slf4j-log4j12-1.6.4.jar
 set CLASSPATH=$CLASSPATH$;$JENAROOT$/lib/xercesImpl-2.10.0.jar
 set CLASSPATH=$CLASSPATH$;$JENAROOT$/lib/xml-apis-1.4.01.jar

set OPTS=-Xmx1024M -Dlog4j.configuration=file:$JENAROOT$/jena-log4j.properties

java $OPTS$ genjreports.rdf.semweb.Mashup kennedyMashupInput.txt http://my.domain.com/gedcom/places/ kennedyMashupOutput.n3 "de|fr"