/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genjfr.app.tools.webbook.transfer;

import genjfr.app.tools.webbook.WebBook;
import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.openide.util.NbBundle;

/**
 * GenJ - Report creating a web Book or reports
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class FTPRegister {

  /**
   * Variables
   */
  private Properties properties = null;
  private String filename = "";
  private String host = "";         // ftp.mysite.com
  private String targetdir = "";    // /basedir/
  private String localRoot = "";

  private final static String
    FTP_SYSTEMATIC = NbBundle.getMessage(WebBook.class, "transferType.type1"),
    FTP_INCREMENTAL = NbBundle.getMessage(WebBook.class, "transferType.type2"),
    FTP_SYNCHRONISE = NbBundle.getMessage(WebBook.class, "transferType.type3");
  private String uploadType = FTP_INCREMENTAL;

  private final static int
    REG_REMOTEMD5  = 0,
    REG_LOCALMD5   = 1,
    REG_TRSFDATE   = 2,
    REG_GENLASTRUN = 3,
    REG_TOBETRSF   = 4,
    REG_TOBEREMOVD = 5,
    REG_LOCALPATH  = 6;
  private final static int REG_SIZE = 7;
  private final static String SEPARATOR = ",";
  private final static String YES = "1";
  private final static String NO  = "0";
  private final static String BLANK  = "-";

  private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',};

  private int nbFilesToTransfer = 0;
  private int nbFilesToRemove = 0;


  /**
   * Constructor
   */
  public FTPRegister(String filename, String host, String targetdir, File localRoot, String uploadType, boolean reset) {

    this.filename = filename;
    this.host = host;
    this.targetdir = targetdir;
    this.localRoot = localRoot.getAbsolutePath();
    this.uploadType = uploadType;

    File file = new File(filename);
    if (!reset && file.exists()) {
       open();
       reset();
       write();
       }
    else {
       properties = new Properties();
       write();
       }
    }

  /**
   * Public methods
   */
  public boolean close() {
    return write();
    }

  public boolean update(File file) {
    String key = host + targetdir + getFileDir(file) + file.getName();
    setLocalGen(key, file.getAbsolutePath(), YES);
    return true;
    }

  public boolean calculate(String uploadType) {

    List<String> toBeRemoved = new ArrayList<String>();
    nbFilesToTransfer = 0;
    for (Iterator it = properties.keySet().iterator(); it.hasNext(); ) {
      String key = (String)it.next();

      // loop on files generated this time with server and base dir that match
      if (key.indexOf(host + targetdir) < 0) {
         continue;
         }

      String[] fields = value2array((String)properties.get(key));

      // if generated this time then get MD5 and store it
      if (equals(fields[REG_GENLASTRUN], YES)) { 
         fields[REG_LOCALMD5] = getMD5(fields[REG_LOCALPATH]);
         }

      // if option = 1 or (option =2 ou 3, md5 local <> md5 remote and generated this time, then set toBeTransferred flag to 1
      fields[REG_TOBETRSF] = NO;
      fields[REG_TOBEREMOVD] = NO;
      if (uploadType.equals(FTP_SYSTEMATIC)) {
         fields[REG_TOBETRSF] = YES;
         nbFilesToTransfer++;
         }

      if ((uploadType.equals(FTP_INCREMENTAL)) && (!equals(fields[REG_REMOTEMD5], fields[REG_LOCALMD5])) && (equals(fields[REG_GENLASTRUN], YES))) {
         fields[REG_TOBETRSF] = YES;
         nbFilesToTransfer++;
         }

      if ((uploadType.equals(FTP_SYNCHRONISE)) && (!equals(fields[REG_REMOTEMD5], fields[REG_LOCALMD5])) && (equals(fields[REG_GENLASTRUN], YES))) {
         fields[REG_TOBETRSF] = YES;
         nbFilesToTransfer++;
         }

      // if option = 3 and generated = 0 and md5 remote <> 0 then set toBeDeleted flag to 1
      if ((uploadType.equals(FTP_SYNCHRONISE)) && (!equals(fields[REG_REMOTEMD5], "")) && (!equals(fields[REG_GENLASTRUN], YES))) {
         fields[REG_TOBEREMOVD] = YES;
         }
      properties.setProperty(key, array2value(fields));

      // if remote md5 = 0 and not generated this time then remove line
      if ((equals(fields[REG_REMOTEMD5], BLANK)) && (!equals(fields[REG_GENLASTRUN], YES))) {
         toBeRemoved.add(key);
         }
      }

    // Remove unused keys now
    for (Iterator it = toBeRemoved.iterator(); it.hasNext(); ) {
      String key = (String)it.next();
      properties.remove(key);
      }

    return true;
    }

  public boolean isToTransfer(File file) {
    String key = getKey(file);
    if (key != null) {
       String[] fields = value2array((String)properties.get(key));
       if (equals(fields[REG_TOBETRSF], YES)) return true;
       }
    return false;
    }

  public boolean setFileTransferred(File file) {
    String key = getKey(file);
    if (key != null) {
       String[] fields = value2array((String)properties.get(key));
       fields[REG_REMOTEMD5] = fields[REG_LOCALMD5];
       fields[REG_TRSFDATE] = Calendar.getInstance().getTime().toString();
       properties.setProperty(key, array2value(fields));
       }
    return false;
    }

  public int getNbFilesToTransfer() {
    return nbFilesToTransfer;
    }

  public List<String> getListToRemove() {

    nbFilesToRemove = 0;
    List<String> listRet = new ArrayList<String>();
    for (Iterator it = properties.keySet().iterator(); it.hasNext(); ) {
      String key = (String)it.next();

      // loop on files generated this time with server and base dir that match
      if (key.indexOf(host + targetdir) < 0) {
         continue;
         }

      String[] fields = value2array((String)properties.get(key));
      if (equals(fields[REG_TOBEREMOVD], YES)) { 
         listRet.add(key);
         }
      }
    nbFilesToRemove = listRet.size();
    return listRet;
    }

  public boolean setFileRemoved(String key) {
    if (key != null) {
       String[] fields = value2array((String)properties.get(key));
       fields[REG_REMOTEMD5] = BLANK;
       fields[REG_TOBEREMOVD] = NO;
       properties.setProperty(key, array2value(fields));
       }
    return false;
    }

  public int getNbFilesToRemove() {
    return nbFilesToRemove;
    }

  public boolean save() {
    return write();
    }


  /**
   * Private methods
   */
  private boolean open() {
     try {
        InputStream in = new FileInputStream(filename);
        properties = new Properties();
        if (in != null) {
           properties.loadFromXML(in);
           in.close();
           }
        } catch (IOException e) {
           return false;
        }
     return true;
    }

  private boolean write() {
    try {
       OutputStream out = new FileOutputStream(filename);
       if (out != null) {
          properties.storeToXML(out, "Webbok upload register " + Calendar.getInstance().getTime().toString());
          out.close();
          }
       } catch (IOException e) { 
          return false;
       }
     return true;
    }

  private boolean reset() {
    for (Iterator it = properties.keySet().iterator(); it.hasNext(); ) {
      String key = (String)it.next();
      if (key.indexOf(host + targetdir) < 0) {
         continue;
         }
      String[] fields = value2array((String)properties.get(key));
      fields[REG_GENLASTRUN] = NO;
      fields[REG_TOBETRSF] = NO;
      fields[REG_TOBEREMOVD] = NO;
      properties.setProperty(key, array2value(fields));
      }
    return true;
   }

  private String getFileDir(File f) {
    // localroot = a/b/c/d/e
    // fullDir = a/b/c/d/e/f/g/h/file.txt
    // ==> currentlocaldir = f/g/h
    String file = f.getName();
    String fullDir = f.getAbsolutePath();
    int a, b;
    a = (localRoot.length() == 0) ? 0 : localRoot.length() + 1;
    b = (fullDir.indexOf(file) < 1) ? 0 : fullDir.indexOf(file) - 1; 
    return (b > a) ? fullDir.substring(a, b) + File.separator : "";
    }

  private boolean equals(String str1, String str2) {
    return (str1.compareTo(str2) == 0);
    }

  private boolean setLocalGen(String key, String localpath, String genflag) {
    String[] fields = value2array(properties.getProperty(key));
    fields[REG_GENLASTRUN] = genflag;
    fields[REG_LOCALPATH] = localpath;
    properties.setProperty(key, array2value(fields));
    return true;
    }

  private String[] value2array(String value) {
    String[] fields = new String[REG_SIZE];
    if (value == null) {
      for (int i = 0 ; i < REG_SIZE ; i++) {
         fields[i] = BLANK;
         }
      }
    else {
      String [] values = value.split(SEPARATOR);
      for (int i = 0 ; i < REG_SIZE ; i++) {
         fields[i] = BLANK;
         if (i < values.length && values[i].trim().length() != 0) {
            fields[i] = values[i];
            }
         }
      }
    return fields;
    }

  private String array2value(String[] fields) {
    String value = "";
    for (int i = 0 ; i < fields.length ; i++) {
       value += fields[i] + (i < fields.length-1 ? SEPARATOR : "");
       }
    return value;
    }

  private String getKey(File file) {
    String path = file.getAbsolutePath();
    for (Iterator it = properties.keySet().iterator(); it.hasNext(); ) {
      String key = (String)it.next();
      String value = (String)properties.get(key);
      if (key.indexOf(host + targetdir) != -1) {
         String[] fields = value2array(value);
         if (equals(fields[REG_LOCALPATH], path)) return key;
         }
      }
    return null;
    }

  private String getMD5(String filename) {

    try {
       // Obtain a message digest object.
       MessageDigest md = MessageDigest.getInstance("MD5");
       // Calculate the digest for the given file.
       FileInputStream in = new FileInputStream(filename);
       byte[] buffer = new byte[8192];
       int length;
       while ((length = in.read(buffer)) != -1)
           md.update(buffer, 0, length);
       byte[] raw = md.digest();
       return asHex(raw);
       }
    catch(Exception e) {
       }
    return "ABCDEFG";
    }

  private static String asHex (byte hash[]) {
    char buf[] = new char[hash.length * 2];
    for (int i = 0, x = 0; i < hash.length; i++) {
      buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
      buf[x++] = HEX_CHARS[hash[i] & 0xf];
    }
    return new String(buf);
  }



}