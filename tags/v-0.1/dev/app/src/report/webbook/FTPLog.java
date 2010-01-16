/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package webbook;
  
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
public class FTPLog {

  /** option - Log file */
  private Report report;
  public boolean writeFile = false;
  private File logFile;
  private PrintWriter outFile;

  /** Constructor */
  public FTPLog(Report report, String title, String msg, boolean writeFile) {
     this.report = report;
     this.writeFile = writeFile;

     if (writeFile) {
        File file = report.getFileFromUser(msg, Action2.TXT_OK,true);
        if (file == null) {
           report.println(report.translate("upload_logCancel"));
           this.writeFile = false;
           }
        else {
           try{
              outFile = new PrintWriter(new FileWriter(file));
              write("---------- " + report.translate("upload_logTitle") + " ----------");
              write(report.translate("log_version") + " = " + report.translate("version"));
              timeStamp();
              write("------------------------------------");
              write("---"+title+"---");
              }catch(IOException ioe){
               report.println(report.translate("upload_errorLog"));
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

    if (onScreen)  { report.println(Calendar.getInstance().getTime().toString() + " - " + text);  }
    if (writeFile) { outFile.println(Calendar.getInstance().getTime().toString() + " - " + text); }
    return;
  }

  public void write(String text) {
    write(text, false);
    return;
  }

  public void flush() {
    outFile.flush();
    return;
  }

  public void timeStamp() {
    write(Calendar.getInstance().getTime().toString());
    return;  
  }

  public PrintWriter getOutput() {
    return outFile;
  }

  /**
  * Closing log file
  */
  public void close() {

    timeStamp();
    write("------------------------------------");
    write("           "+ report.translate("upload_log_close"));
    write("------------------------------------");
    if (writeFile) {
       report.println(report.translate("upload_log_closingMsg", logFile.toString()));
       report.println("");
       outFile.flush();
       outFile.close();
       }
    return; 
  }

} // Log object

