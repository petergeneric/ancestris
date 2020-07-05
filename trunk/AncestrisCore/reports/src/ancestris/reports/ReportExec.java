package ancestris.reports;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.report.Report;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * Report to run an external program
 * @author Nils Meier nils@meiers.net
 * @version 0.1
 */
@ServiceProvider(service=Report.class)
public class ReportExec extends Report {

  /**
   * Main method
   */
  public void start(Gedcom gedcom) {

    // get the name of the executable
    String cmd = getValueFromUser( "executables", translate("WhichExecutable"), new String[0]);

    if (cmd==null || cmd.length()==0)
      return;

    // run it
    BufferedReader in = null;
    try {
      Process process = Runtime.getRuntime().exec(cmd);
      in = new BufferedReader(new InputStreamReader(process.getInputStream()));
      while (true) {
        String line = in.readLine();
        if (line==null) break;
        println(line);
      }
    } catch (IOException ioe) {
      println(translate("Error", ioe.getMessage()));
    } finally {
      try { in.close(); } catch (Throwable t) {};
    }

    // done
  }

} //ReportExec
