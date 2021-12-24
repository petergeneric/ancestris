/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@ancestris.org>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.webbook.transfer;

import ancestris.modules.webbook.Log;
import ancestris.modules.webbook.WebBook;
import ancestris.util.swing.DialogManager;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.NbBundle;

/**
 * A class to upload webbook files on a server
 *
 * @author Frederic Lapeyre leveraging tuomas angervuori source
 * @version 0.3.0
 * @since JDK1.1
 */
public class FTPUpload {

    /* Debug flag for debugging purposes */
    private boolean debug = false;

    /* FTP Response codes */
    public final static int CONN_OK = 125; //Data connection already open
    public final static int DATA_OK = 150; //Data port OK
    public final static int CFG_OK = 200; //Change OK
    public final static int SIZE = 213; //File size
    public final static int READY = 220; //Connection OK
    public final static int ABOR_OK = 225; //Abort OK
    public final static int TRANSFER_OK = 226; //Transfer complete
    public final static int PASV_OK = 227; //Passive mode OK
    public final static int LOGIN_OK = 230; //Login OK
    public final static int CMD_OK = 250; //Command OK
    public final static int DIR_OK = 257; //Directory created
    public final static int PASSWD_REQ = 331; //Password required
    public final static int REN_REQ = 350; //New filename needed
    public final static int PASSIVEPORT = 256; //Passive port

    /* FTP connection */
    private String host = "";
    private String user = "";
    private String password = "";
    protected int timeout = 60000;
    protected boolean timeOutError = false;

    /* FTP command streams */
    private Socket FTPcmd = null; //Connection to the server
    private BufferedReader cmdInput; //Stream out to write commands to the server
    private PrintStream cmdOutput; //Stream in to read response back
    private String cmdResponse; //Latest response message
    private int cmdResponseCode;  //Latest response code

    /* FTP data streams */
    private Socket FTPdata = null; //Data connection socket
    private DataOutputStream dataOutput; //Data stream to transfer the files
    private boolean dataBusy = false; //Is the data channel in use
    private int dataTransferred = 0; //Bytes transferred

    /* Webbook upload variables */
    private final ProgressHandle progress;
    private boolean taskCancelled;
    private final List<File> localFiles;
    private final String localRoot;
    private final String remoteRoot;
    private final Log log;
    private final FTPRegister uploadRegister;
    private int totalToTransfer = 0;
    private int cpt = 0, cptmem = 0;
    private int totalSize = 0;
    private boolean moreThanOneLoop = false;

    /**
     *  Constructor
     */
    public FTPUpload(String host, String user, String password, List<File> localFiles, String localRoot, String remoteRoot, Log log, FTPRegister uploadRegister,
            ProgressHandle progress) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.localFiles = localFiles;
        this.localRoot = localRoot;
        if (remoteRoot.endsWith("/")) {
            this.remoteRoot = remoteRoot;
        } else {
            this.remoteRoot = remoteRoot + "/";
        }
        this.log = log;
        this.uploadRegister = uploadRegister;
        this.progress = progress;
        taskCancelled = false;
    }

    /**
     * Start thread
     */
    public void run() {

        boolean ok = false;
        moreThanOneLoop = false;

        while (!ok) {
            timeOutError = false;
            log.write("   ");
            log.write("===========================");
            log.write(trs("upload_check"));
            ok = calculateWhatToDo(moreThanOneLoop);
            if (!ok) {
                break;
            }

            log.write("   ");
            log.write("===========================");
            log.write(trs("upload_connection"));
            ok = connectToServer();
            if (!ok) {
                break;
            }

            log.write("   ");
            log.write("===========================");
            log.write(trs("upload_transfer"));
            ok = uploadLocalFilesToServer();
            if (!ok) {
                if (timeOutError) {
                    closeServerConnection();
                    moreThanOneLoop = true;
                    if (cptmem == cpt && cptmem != 0) {
                        break;
                    }
                    cptmem = cpt;
                    continue;
                } else {
                    break;
                }
            }

            log.write("   ");
            log.write("===========================");
            log.write(trs("upload_removing"));
            ok = removeServerFiles();
            if (!ok) {
                break;
            }
        }

        log.write("   ");
        log.write("===========================");
        log.write(trs("upload_closing"));
        closeServerConnection();
        //
        log.write("   ");
        if (cpt < totalToTransfer || moreThanOneLoop) {
            log.write(log.ERROR, trs("upload_error"));
        }
        if (cpt == totalToTransfer) {
            log.write(log.NORMAL, trs("upload_alldone"));
        }
    }


    /**
     * Cancels transfer
     */
    void cancel() {
        taskCancelled = true;
    }

    /**
     * Determines which files to upload and remove
     */
    private boolean calculateWhatToDo(boolean moreThanOneLoop) {

        // Calculates which files to remove on the server if necessary
        try {
            uploadRegister.calculate(localFiles);
            if (!moreThanOneLoop) {
                totalToTransfer = uploadRegister.getNbFilesToTransfer();
            }
            log.write(trs("upload_foundlocal", localFiles.size()));
            log.write(trs("upload_foundtrsf", totalToTransfer));
        } catch (Exception e) {
            log.write(log.ERROR, trs("upload_error_check"));
            log.write(log.ERROR, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Connect to a ftp server
     * @return true is could connect and login
     */
    private synchronized boolean connectToServer() {

        if (!checkConnection()) {
            return false;
        }
        
        
        try {
            // Opens connection with server
            debugMsg("Opening socket");
            FTPcmd = new Socket(host, 21);
            debugMsg("FTPcmd=" + FTPcmd);
            FTPcmd.setSoTimeout(timeout);
            cmdInput = new BufferedReader(new InputStreamReader(FTPcmd.getInputStream()));
            debugMsg("cmdInput=" + cmdInput);
            cmdOutput = new PrintStream(FTPcmd.getOutputStream());
            debugMsg("cmdOutput=" + cmdOutput);

            //Check if the connection went fine
            getResponse();
            if (cmdResponseCode != READY) {
                throw new Exception(cmdResponse);
            }

            // Login
            cmdOutput.println("USER " + user);
            debugMsg("==> USER XXXXX");
            getResponse();
            if (cmdResponseCode != LOGIN_OK && cmdResponseCode != PASSWD_REQ) {
                throw new Exception(cmdResponse);
            }
            if (cmdResponseCode == PASSWD_REQ) {
                cmdOutput.println("PASS " + password);
                debugMsg("==> PASS XXXXX");
                getResponse();
                if (cmdResponseCode != LOGIN_OK) {
                    throw new Exception(cmdResponse);
                }
            }

            // Set binary mode
            command("TYPE I");
            if (cmdResponseCode != CFG_OK) {
                throw new Exception(cmdResponse);
            }
        } catch (Exception e) {
            log.write(log.ERROR, trs("upload_errorConn"));
            log.write(log.ERROR, e.getMessage());
            timeOutError = isTimeout(e);
            closeServerConnection();
            return false;
        }

        return true;
    }

    private boolean checkConnection() {
        try {
            new URL("https://www.ancestris.org/").openStream();
        } catch (IOException ex) {
            DialogManager.createError(
                        NbBundle.getMessage(this.getClass(), "fb.title"), 
                         NbBundle.getMessage(this.getClass(), "fb.nointernet") +  "\n" + NbBundle.getMessage(this.getClass(), "fb.msg.senderror"))
                        .show();
            return false;
        }
        return true;
    }

    
    /**
     * Upload files
     */
    private boolean uploadLocalFilesToServer() {

        // Init variables
        String currentLocalDir = "";
        String currentRemoteDir = remoteRoot;
        int index = 0;

        // UPLOAD - Send files across
        progress.switchToDeterminate(totalToTransfer);

        log.write(trs("upload_starting"));
        try {
            for (Iterator<File> it = localFiles.iterator(); it.hasNext();) {
                File file = it.next();
                currentLocalDir = getFileDir(file);

                // Quit if task has been cancelled or is finished
                if (taskCancelled) {
                    abort();
                    break;
                }
                // Do not upload file if not need to be
                if (!uploadRegister.isToTransfer(file)) {
                    if (!moreThanOneLoop) {
                        log.write(trs("upload_noneed", new String[]{currentLocalDir, file.getName()}));
                    }
                    continue;
                }

                // Create directory if it does not exist
                if (currentLocalDir.compareTo(currentRemoteDir) != 0) {
                    logEvent("cd " + remoteRoot);
                    if (!cd(remoteRoot)) {
                        logEvent(trs("upload_dirnotthere"));
                        logEvent("mkdir " + remoteRoot);
                        mkdir(remoteRoot);
                        logEvent("cd " + remoteRoot);
                        if (!cd(remoteRoot)) {
                            logEvent(trs("upload_error_ftp_cd"), true);
                            return false;
                        }
                    }
                    String[] dirBits = currentLocalDir.split(File.separator);
                    for (int i = 0; i < dirBits.length; i++) {
                        String dir = dirBits[i];
                        if (dir.trim().length() == 0) {
                            break;
                        }
                        logEvent("cd " + dir);
                        if (!cd(dir)) {
                            log.write(trs("upload_dirnotthere"));
                            logEvent("mkdir " + dir);
                            mkdir(dir);
                            logEvent("cd " + dir);
                            cd(dir);
                        }
                    }
                }

                // Transfer the file itself
                currentRemoteDir = currentLocalDir;
                String storeName = remoteRoot + ((currentRemoteDir.length() == 0) ? "" :  currentRemoteDir + "/") + file.getName();
                logEvent("put " + storeName);
                if (!put(file, storeName)) {
                    logEvent(trs("upload_error_ftp_put"), true);
                    return false;
                }
                cpt++;
                uploadRegister.setFileTransferred(file);
                totalSize += (dataTransferred / 1024);
                log.write(trs("upload_transferred", new Object[]{currentLocalDir, file.getName(), dataTransferred}));
                log.write(trs("upload_donesofar", new Object[]{cpt, totalToTransfer, totalSize}));
                progress.progress(++index);
                log.write(" ");
            }
            log.write(trs("upload_trsfComplete"));

        } catch (IOException e) {
            log.write(log.ERROR, trs("upload_errorLoop"));
            log.write(log.ERROR, e.getMessage());
            return false;
        }

        return true;

    }

    /**
     * Remove server files
     *
     * REMOVE - removed files that are on the server from previous transfers that are not part of this run
     * (if files have been uploaded outside the WebBook upload process, they will not be taken into account)
     * (to remove file on the server that have been put there by another program, use regular ftp client)
     */
    private boolean removeServerFiles() {

        int totalToRemove = 0;
        int index = 0;
        String currentRemoteDir = remoteRoot;

        List<String> listToRemove = uploadRegister.getListToRemove();
        totalToRemove = uploadRegister.getNbFilesToRemove();
        log.write(trs("upload_remove", totalToRemove));
        String FTP_SYNCHRONISE = NbBundle.getMessage(WebBook.class, "transferType.type3");
        if (totalToRemove == 0) {
            log.write(trs("upload_noremove"));
            return false;
        }
        if (!uploadRegister.uploadType.equals(FTP_SYNCHRONISE)) {
            log.write(trs("upload_usernoremove"));
            return false;
        }

        progress.switchToDeterminate(totalToRemove);
        log.write(trs("upload_startingrm"));
        try {
            Collections.sort(listToRemove);
            String remoteDir = "";
            currentRemoteDir = "";
            String file = "";
            for (Iterator<String> it = listToRemove.iterator(); it.hasNext();) {
                String key = it.next();
                remoteDir = getFileDir(key);
                file = getFile(key);
                if (remoteDir.compareTo(currentRemoteDir) != 0) {
                    currentRemoteDir = remoteDir;
                    logEvent("cd " + remoteRoot);
                    cd(remoteRoot);
                    logEvent("cd " + remoteDir);
                    if (!cd(remoteDir)) {
                        logEvent(trs("upload_nodir"));
                        uploadRegister.setFileRemoved(key);
                        progress.progress(++index);
                        continue;
                    }
                }
                logEvent("rm " + file);
                if (!rm(file)) {
                    logEvent(trs("upload_cannotrm", file));
                } else {
                    logEvent(trs("upload_removed", file));
                }
                uploadRegister.setFileRemoved(key);
                progress.progress(++index);
            }

            log.write(trs("upload_rmComplete"));

        } catch (IOException e) {
            log.write(log.ERROR, trs("upload_errorrm"));
            log.write(log.ERROR, e.getMessage());
            return false;
        }

        return true;

    }

    /**
     * Log out from the server and close the connection.
     */
    private synchronized boolean closeServerConnection() {
        //Logout from the server
        if (FTPcmd != null) {
            try {
                command("QUIT");
            } catch (IOException e) {
                log.write(log.ERROR, trs("upload_errorClose") + e);
            }
        }

        //Closing connections
        try {
            if (dataOutput != null) {
                dataOutput.close();
            }
            if (FTPdata != null) {
                FTPdata.close();
            }
            if (cmdOutput != null) {
                cmdOutput.close();
            }
            if (cmdInput != null) {
                cmdInput.close();
            }
            if (FTPcmd != null) {
                FTPcmd.close();
                FTPcmd = null;
            }
        } catch (IOException e) {
            log.write(log.ERROR, trs("upload_errorClose"));
            log.write(log.ERROR, e.getMessage());
        }

        return (FTPcmd == null);
    }

    /**
     * Execute a command on the server
     */
    private synchronized String command(String command) throws IOException {
        cmdOutput.println(command);
        debugMsg("==> " + command);
        return getResponse();
    }

    /**
     * Change directory
     */
    private synchronized boolean cd(String dir) throws IOException {
        command("CWD " + dir);
        return (cmdResponseCode == CMD_OK);
    }

    /**
     * Create a directory
     */
    private synchronized boolean mkdir(String dir) throws IOException {
        command("MKD " + dir);
        return (cmdResponseCode == DIR_OK);
    }

    /**
     * Delete a file
     */
    private synchronized boolean rm(String file) throws IOException {
        command("DELE " + file);
        return (cmdResponseCode == CMD_OK);
    }

    /**
     * Delete a directory
     */
    private synchronized boolean rmdir(String dir) throws IOException {
        command("RMD " + dir);
        return (cmdResponseCode == CMD_OK);
    }

    /**
     * Performs one transfer
     */
    public boolean put(File fileToTransfer, String storeName) {

        String line;
        // Create a timeout break in case transfert gets stuck in the dataOutput.write statement below
        Timer timer = new Timer(120000, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    log.write(trs("upload_error_timeout"));
                    abort();
                    timeOutError = true;
                    dataOutput.close(); // this will generate a socket closed error in the dataOutput.write statement below
                    dataOutput = null;
                } catch (Exception ex) {
                }
            }
        });

        try {
            dataBusy = true; //Set data channel busy

            //Get data connection port number
            int port = passive();
            if (port < 0) {
                return false;
            }

            //Open data channel to server
            FTPdata = new Socket(host, port);
            dataOutput = new DataOutputStream(FTPdata.getOutputStream());
            FTPdata.setSoTimeout(timeout);

            //Check if server wants to accept transfer
            command("STOR " + storeName);
            if ((cmdResponseCode != DATA_OK) && (cmdResponseCode != CONN_OK)) {
                return false;
            }

            //Performs transfer
            timer.start();
            byte b[] = new byte[16384];
            int amount;
            RandomAccessFile file = new RandomAccessFile(fileToTransfer, "r");
            debugMsg(trs("upload_uponefile"));
            dataTransferred = 0;
            while ((amount = file.read(b)) > 0) {
                dataOutput.write(b, 0, amount);
                dataTransferred += amount;
                //debugMsg(""+dataTransferred);
            }

            //Close file
            file.close();
            timer.stop();

            //Close connection
            debugMsg(trs("upload_storefile"));
            dataOutput.close();
            FTPdata.close();

            //Get response for successful file transfer
            getResponse();
            if (cmdResponseCode == TRANSFER_OK) {
                debugMsg(trs("upload_success"));
            }

            dataBusy = false; //Set data channel vacant
        } catch (Exception e) {
            dataBusy = false;
            timer.stop();
            timeOutError = isTimeout(e);
            log.write(log.ERROR, trs("upload_errorPut"));
            log.write(log.ERROR, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Performs one transfer using URLConnection
     */
    public boolean put2(File fileToTransfer, String storeName) {

        try {
            URL url = new URL("ftp://" + user + ":" + password + "@" + host + storeName + ";type=i");
            URLConnection urlc = url.openConnection();
            OutputStream output = urlc.getOutputStream();
            FileInputStream input = new FileInputStream(fileToTransfer);
            byte[] buf = new byte[65536];
            int amount;
            while (true) {
                amount = input.read(buf);
                if (amount <= 0) {
                    break;
                }
                output.write(buf, 0, amount);
                dataTransferred += amount;
            }
            output.close();
            input.close();
            urlc = null;
        } catch (Exception e) {
            timeOutError = isTimeout(e);
            log.write(log.ERROR, trs("upload_errorPut"));
            log.write(log.ERROR, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Set server to passive mode
     * @return passive port number or -1 if fail
     */
    private synchronized int passive() throws IOException {
        int port;
        command("PASV");
        if (cmdResponseCode == PASV_OK) {
            //227 Entering Passive Mode (h1,h2,h3,h4,p1,p2)
            int[] numbers = new int[6];
            String temp;
            StringTokenizer tempToken;
            //Split the response in two at the "("-sign
            tempToken = new StringTokenizer(cmdResponse, "(");
            //First part of the message is useless...
            temp = tempToken.nextToken();
            //The part inside the colons is important...
            temp = tempToken.nextToken();
            //Remove the part after the ")"-sign
            tempToken = new StringTokenizer(temp, ")");
            //Now we have only the part inside the colons.
            temp = tempToken.nextToken();
            //Split up the fields
            tempToken = new StringTokenizer(temp, ",");

            for (int i = 0; i < 6; i++) {
                numbers[i] = Integer.parseInt(tempToken.nextToken());
            }

            //Define the port that passive ftp uses
            port = (numbers[4] * PASSIVEPORT) + numbers[5];
            return port;

        } else {
            return -1;
        }
    }

    /**
     * Abort file transfer
     */
    private synchronized boolean abort() throws IOException {
        command("ABOR");
        return (cmdResponseCode == ABOR_OK);
    }

    /**
     * Get the server response. Note that if there is no response
     * waiting, this class will jam untill there is a response!
     */
    private synchronized String getResponse() {
        String line = "    ";
        cmdResponse = "";

        try {

            do {
                line = cmdInput.readLine();
                cmdResponseCode = Integer.parseInt(line.substring(0, 3));
                if (cmdResponseCode == LOGIN_OK || cmdResponseCode == PASSWD_REQ) {
                    line = line.replaceAll(user, "XXXXX");
                }
                debugMsg("<== " + line);
                cmdResponse += line + "\n";
                if (line == null || line.trim().length() == 0) {
                    line = "999";
                    break;
                }
                //Last line of the response has a empty space after the response code
            } while (!(Character.isDigit(line.charAt(0))
                    && Character.isDigit(line.charAt(1))
                    && Character.isDigit(line.charAt(2))
                    && line.charAt(3) == ' '));

            //Get the latest response code
            cmdResponseCode = Integer.parseInt(line.substring(0, 3));
        } //If timeout error on socket, break and continue
        //For all other errors, set data channel free and terminate
        catch (Exception e) {
            dataBusy = false;
            timeOutError = isTimeout(e);
            cmdResponseCode = 999;
        }

        return cmdResponse;
    }

    /**
     * Log events
     */
    private void logEvent(String str, boolean err) {
        progress.setDisplayName(str);
        log.write(err ? log.ERROR : log.NORMAL, str);
    }

    private void logEvent(String str) {
        logEvent(str, false);
    }

    /**
     * Get incremental directory from file
     */
    private String getFileDir(File f) {
        // localroot = a/b/c/d/e
        // fullDir = a/b/c/d/e/f/g/h/file.txt
        // ==> currentlocaldir = f/g/h
        String file = f.getName();
        String fullDir = f.getAbsolutePath();
        int a, b;
        a = (localRoot.length() == 0) ? 0 : localRoot.length() + 1;
        b = (fullDir.indexOf(file) < 1) ? 0 : fullDir.indexOf(file) - 1;
        return (b > a) ? fullDir.substring(a, b) : "";
    }

    private String getFileDir(String f) {
        // f = www.s.com/a/b/c/d/e/f/g/h/file.txt
        // remoteroot = a/b/c/d/e
        // ==> currentlocaldir = f/g/h
        int a, b;
        a = f.indexOf(remoteRoot) + remoteRoot.length();
        b = f.lastIndexOf("/");
        return (b > a) ? f.substring(a, b) : "";
    }

    private String getFile(String f) {
        return f.substring(f.lastIndexOf("/") + 1);
    }

    /**
     * Determines if the exception is a timeout error
     */
    private boolean isTimeout(Exception e) {
        if (timeOutError) {
            return true;
        }
        if ((e instanceof SocketException || e instanceof SocketTimeoutException)
                && (e.getMessage().indexOf("timed out") > -1 || e.getMessage().indexOf("Socket closed") > -1)
                && (cpt > 1)) {
            return true;
        }
        return false;
    }

    /**
     * Debug
     */
    private void debugMsg(String str) {
        if (debug) {
            log.write("DEBUG - " + str);
        }
    }

    private String trs(String string) {
        return NbBundle.getMessage(FTPLoader.class, string);
    }

    private String trs(String string, Object obj) {
        return NbBundle.getMessage(FTPLoader.class, string, obj);
    }

    public String trs(String string, Object[] arr) {
        return NbBundle.getMessage(FTPLoader.class, string, arr);
    }

}
