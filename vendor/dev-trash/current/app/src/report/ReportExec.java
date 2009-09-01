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

/**
 * GenJ - Report to run an external program
 * @author Nils Meier nils@meiers.net
 * @version 0.1
 */
public class ReportExec extends Report {

  /**
   * Main method
   */
  public void start(Gedcom gedcom) {

    // get the name of the executable
    String cmd = getValueFromUser( "executables", translate("WhichExecutable"), new String[0]);

    if(cmd == null)
      return;

    // run it
    try {
      Process process = Runtime.getRuntime().exec(cmd);
      BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
      while (true) {
        String line = in.readLine();
        if (line==null) break;
        println(line);
      }
    } catch (IOException ioe) {
      println(translate("Error")+ioe.getMessage());
    }

    // done
  }

  /**
   * @see genj.report.Report#usesStandardOut()
   */
  public boolean usesStandardOut() {
    return true;
  }

} //ReportExec
