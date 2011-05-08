package genjreports;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even 
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.report.Report;
import genj.report.ReportLoader;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * GenJ - ReportRecompile - recompile all reports
 * 
 * author               = Nils Meier
 * version              = 1.0
 * category             = utility
 * updated              = $Date: 2010-01-28 02:51:13 $
 * name                 = Recompile Reports
 * name.de              = Reports rekompilieren 
 * name.fr              = Compilation des Rapports
 *
 * info = <h1><center>Recompile Reports</center></h1><p>This report recompiles all reports 
 *  in GenJ's report directory (./report). After a restart of GenJ 
 *  or by pressing the reload button in ReportView, recompiled reports can be reloaded into GenJ.</p>
 *  <p>For the compilation to succeed GenJ needs to be run inside a JDK's Java Virtual Machine, not in 
 *  a JRE's Java Virtual Machine.</p>
 *  
 * info.de = <h1><center>Reports rekompilieren</center></h1>
 *  <p>Dieser Report kompiliert alle Reports im GenJ Reportverzeichnis (./report). Nach Neustart von 
 *  GenJ oder einem Klick auf den Schalter 'Neu Einladen' in ReportView können kompilierte Reports neu eingeladen werden.</p>
 *  <p>Die Kompilierung funktioniert nur, wenn GenJ in einer JDK Java Virtual Machine, nicht in einer JRE JVM, läuft.</p>
 *
 * info.fr =  <h1><center>Outil de Compilation des Rapports</center></h1>
 *   <p>Ce rapport vous sert à recompiler tous les rapports qui se trouvent dans le répertoire des rapports de GenJ (./report).</p>
 *   <p>Après un redémarrage de GenJ ou après avoir cliqué sur le bouton de rechargement des rapports, tous les rapports nouvellement compilés 
 *   pourront être utilisés par GenJ.</p>
 *   <p>Pour que cela fonctionne, il faut cependant que GenJ tourne à l'intérieur d'une Machine Virtuelle Java JDK, et non une Machine Virtuelle Java JRE.
 *   Pour faire simple, il faut que le programme Java que vous avez téléchargé sur le site de Sun commence par "jdk", et non par "jre". </p>
 *  
 * nosources = No sources in {0}
 * nosources.de = Keine Quelldateien in {0}
 * nosources.fr = Il n'y a pas de fichier Source dans {0}
 * 
 * javac.success = {0} Sources (*.java) compiled into {1} - to activate press 'Reload report classes'
 * javac.success.de = {0} Quelldateien (*.java) kompiliert nach {1} - zum Aktivieren 'Neu Einladen' klicken 
 * javac.success.fr = \n\n{0} Fichiers Sources (*.java) ont été compilés 
 *  dans le répertoire :\n     {1}\n\n
 *  ATTENTION : Pour que cette compilation soit prise en considération,\n
 *              Veuillez cliquer sur le bouton de rechargement des rapports (2 flèches vertes imbriquées).
 * 
 * javac.error = Compilation failed - check compiler output above
 * javac.error.de = Kompilierung fehlgeschlagen - bitte Fehlermeldung(en) oben konsultieren
 * javac.error.fr = La compilation a échouée - Vérifiez les messages de sortie du compilateur ci-dessus.
 * 
 * javac.jre = Reports can't be recompiled - make sure GenJ is run inside a JDK's Java Virtual Machine (java.home={0}) 
 * javac.jre.de = Reports können nicht kompiliert werden - GenJ muß in einer JDK Java Virtual Machine ablaufen (java.home={0}) 
 * javac.jre.fr = \nLes Rapports n'ont pas pu être compilés.\n
 *  Assurez-vous que GenJ est lancé par l'intermédiaire de la machine virtuelle Java JDK (JDK et non JRE).\n\n     Pour information : (java.home={0}) 
 * 
 */
public class ReportRecompile extends Report {

  private final static String[] OPTIONS = {
      "-g", 
      "-nowarn",
      "-encoding", "utf8",
  };
  
  /**
   * main entry to the reports
   */
  public void start(Gedcom gedcom) {
    
    // prepare args
    List args = new ArrayList();
    
    for (int i = 0; i < OPTIONS.length; i++) 
      args.add(OPTIONS[i]);

    File reports = ReportLoader.getReportDirectory();
    args.add("-d");
    args.add(reports.getAbsolutePath());
    
    // collect all .java files
    int sources = findSources(reports, args);
    if (sources==0) {
      println(translate("nosources", reports));
      return;
    }
    
    // do the compile
    Object rc = null;
    try {
      Object javac = Class.forName("com.sun.tools.javac.Main").newInstance();
      rc = javac.getClass().getMethod("compile", new Class[]{ new String[0].getClass(), PrintWriter.class } )
            .invoke(javac, new Object[]{ args.toArray(new String[args.size()]), getOut() });
    } catch (Throwable t) {
      println(translate("javac.jre", System.getProperty("java.home")));
      return;
    }
    
    // done
    if (Integer.valueOf(0).equals(rc))
      println(translate("javac.success", new Object[]{ ""+sources, reports}));
    else {
      println("---");
      println(translate("javac.error"));
    }
    
  }
  
  /**
   * Look for source files recursively
   */
  private int findSources(File dir, List args) {
    File[] files = dir.listFiles();
    if (files==null||files.length==0)
      return 0;
    int found = 0;
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      if (file.isDirectory()) {
        found += findSources(file, args);
      } else if (file.getName().endsWith(".java")) {
        args.add(file.toString());
        found++;
      }
    }
    return found;
  }
  
}

