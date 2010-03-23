/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package merge;
  
import genj.report.Report;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import genj.util.swing.Action2;
import java.util.Calendar;

/**
 * GenJ - Tools
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
public class Log {

  /** option - Log file */
  private Report report;
  public boolean writeFile = false;
  public File logFile;
  private PrintWriter outFile;

  /** Constructor */
  public Log(Report report, String title, String msg, boolean writeFile) {
     this.report = report;
     this.writeFile = writeFile;
     
     if (writeFile) {
        File file = report.getFileFromUser(msg, Action2.TXT_OK,true);
        if (file == null) {
           report.println("##### "+"Cancelled by user"+" #####");
           this.writeFile = false;
           }
        else {
           try{
              outFile = new PrintWriter(new FileWriter(file));
              outFile.println(report.getName());
              outFile.println("Log file for "+title);
              }catch(IOException ioe){
               report.println("IO Exception!");
              ioe.printStackTrace();
              }
           this.logFile = file;
           }
        }
     }
     
  /**
  * Writing to screen and log file
  */
  public void write(String text, boolean onScreen) {

    if (onScreen) report.println(text);
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
  
