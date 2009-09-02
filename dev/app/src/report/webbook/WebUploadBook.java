/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package webbook;

import genj.report.Report;

import java.io.*;
import java.net.*;
import java.util.*;


public class WebUploadBook {
    public String localfile;
    public String targetfile;
    private String errorMsg = "";

    private String host = "";
    private String user = "";
    private String password = "";
    private File localdir = null;
    private String targetdir = "";
    private ReportWebBook report = null;
    private FTPRegister uploadRegister = null;
    private FTPProgressStatus progress = null;
    private FTPLog log = null;

    public WebUploadBook(String host, String user, String password, File localdir, String targetdir, ReportWebBook report, FTPRegister uploadRegister) {
       this.host = host;
       this.user = user;
       this.password = password;
       this.localdir = localdir;
       this.targetdir = targetdir;
       this.report = report;
       this.uploadRegister = uploadRegister;
       run();
       }

   /**
    * Main entry point (from webbook main sequence of events)
    */
    protected void run() {

       // Open log
       log = new FTPLog(report, report.translate("upload_message", host), report.translate("upload_chooseLog"), true);
       if (log == null || !log.writeFile) {
          report.println(report.translate("upload_errorLog"));
          return;
          }

       // Collect all files to send across
       List<File> localFiles = getFilesRecursively(localdir);
       Collections.sort(localFiles);

       // Create modal progress window which will allow to track progress and cancel it
       progress = new FTPProgressStatus(report, report.translate("upload_prog_window", host), report.translate("upload_init"), "", localFiles.size(), report.translate("upload_button_stop"), log);

       // Launch a thread to put bulk of files giving the log and the progress window id as reference 
       putFiles(localFiles, log, progress);

       // Create modal progress window which will allow to track progress and cancel it - process will stay there until user clicks Cancel 
       // or until the put thread terminates and sends to progress the close signal 
       progress.open();

       // Close log
       log.close();
       }


   /**
    * Get local files
    */
    protected List<File> getFilesRecursively(File dir) {
       List<File> filesRet = new ArrayList();
       File[] strs = dir.listFiles();
       if (strs == null) return null;
       List<File> files = Arrays.asList(strs);
       for (Iterator it = files.iterator(); it.hasNext();) {
          File file = (File) it.next();
          if (file.isDirectory()) {
             filesRet.addAll(getFilesRecursively(file));
             }
          else {
             filesRet.add(file);
             }
          }
       return filesRet;
       }

   /**
    * Launch put thread
    */
    protected boolean putFiles(List<File> localFiles, FTPLog log, FTPProgressStatus progress) {

       try  {
          FTPUpload ftpThread = new FTPUpload(report, host, user, password, localFiles, localdir.getAbsolutePath(), targetdir, log, progress, uploadRegister);
          ftpThread.start();
          }
       catch(Exception exception) {
          report.println(report.translate("upload_errorTrsf", exception));
          log.write(report.translate("upload_errorTrsf", exception));
          return false;
          }
       return true;
       }

}


