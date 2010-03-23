/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genjfr.app.tools.merge;
  
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Calendar;
import javax.swing.JFileChooser;
import org.openide.windows.WindowManager;

/**
 * GenJ - Tools
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
public class Log {

  /** option - Log file */
  private tmpMerge report;
  public boolean writeFile = false;
  public File logFile;
  private PrintWriter outFile;

  /** Constructor */
  public Log(tmpMerge report, String title, String msg, boolean writeFile) {
     this.report = report;
     this.writeFile = writeFile;
     
     if (writeFile) {
         System.out.println("ici 1");
        File file = getFileFromUser("logMerge");
         System.out.println("ici 2");
        if (file == null) {
           System.out.println("##### "+"Cancelled by user"+" #####");
           this.writeFile = false;
           }
        else {
           try{
              outFile = new PrintWriter(new FileWriter(file));
              outFile.println("Log file for "+title);
              }catch(IOException ioe){
               System.out.println("IO Exception!");
              ioe.printStackTrace();
              }
           this.logFile = file;
           }
        }
     }

      public File getFileFromUser(String title) {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(title);
        int rc = chooser.showOpenDialog(WindowManager.getDefault().findTopComponent("GenjViewTopComponent"));
        File result = chooser.getSelectedFile();
        if (rc != JFileChooser.APPROVE_OPTION || result == null) {
            return null;
        }
        return result;
    }

  /**
  * Writing to screen and log file
  */
  public void write(String text, boolean onScreen) {

    if (onScreen) System.out.println(text);
    if (writeFile) outFile.println(text);
    return;  
  }

  public void write(String text) {
    write(text, true);
    return;  
  }
  
  public void write(int level, int indent, String str, int length, String text) {
    String line = "";
    for (int i=1;i<=length;i++) line += str; 

    String indentSpaces = "";
    for (int i=1;i<=indent;i++) indentSpaces += " "; 

    if (level == 0) {
       write (indentSpaces+text);
       }
    if (level == 1) {
       write (" ");
       write (line);
       write (indentSpaces+text);
       write (line);
       }
    if (level == 2) {
       write (line);
       write (indentSpaces+text);
       write (" ");
       }
    if (level == 9) {
       write (" ");
       write (line);
       write (indentSpaces+"##### "+text+" #####");
       write (line);
       write (" ");
       }
    return;
  }

  public void timeStamp() {
    write(Calendar.getInstance().getTime().toString(), true);
    return;
  }

  public void timeStamp(int indent, String str) {
    write(0, indent, "", 0, str+Calendar.getInstance().getTime().toString());
    return;
  }

  public String getLogName() {
    return logFile.toString();
  }

  /**
  * Closing log file
  */
  public void close() {

    if (writeFile) {
       outFile.flush();
       outFile.close();
       }
    return;  
  }

} // Log object
  
