set CLASSPATH=$CLASSPATH:/PATH_TO/.ancestris/trunk/modules/genjreports-rdf.jar
set CLASSPATH=$CLASSPATH:/PATH_TO/.ancestris/trunk/modules/ext/jena-2.6.4.jar
set CLASSPATH=$CLASSPATH:/PATH_TO/.ancestris/trunk/modules/ext/arq-2.8.7.jar
set CLASSPATH=$CLASSPATH:/PATH_TO/.ancestris/trunk/modules/ext/log4j-1.2.13.jar
set CLASSPATH=$CLASSPATH:/PATH_TO/.ancestris/trunk/modules/ext/slf4j-api-1.5.8.jar
set CLASSPATH=$CLASSPATH:/PATH_TO/.ancestris/trunk/modules/ext/slf4j-log4j12-1.5.8.jar
set CLASSPATH=$CLASSPATH:/PATH_TO/.ancestris/trunk/modules/ext/xercesImpl-2.7.1.jar

java genjreports.rdf.semweb.Mashup kennedyMashupInput.txt http://my.domain.com/gedcom/places/ kennedyMashupOutput.n3 de|fr