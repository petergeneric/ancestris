/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.webbook.transfer;

import ancestris.modules.webbook.WebBook;
import ancestris.modules.webbook.WebBookParams;
import ancestris.modules.webbook.creator.WebHelper;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.prefs.BackingStoreException;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Ancestris
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class FTPRegister {

    /**
     * Variables
     */
    private final static String regPrefix = "reg_";
    private String node = "";
    //
    private String host = "";         // ftp.mysite.com
    private String targetdir = "";    // /basedir/
    private String localRoot = "";
    private final static String FTP_SYSTEMATIC = NbBundle.getMessage(WebBook.class, "transferType.type1"),
            FTP_INCREMENTAL = NbBundle.getMessage(WebBook.class, "transferType.type2"),
            FTP_SYNCHRONISE = NbBundle.getMessage(WebBook.class, "transferType.type3");
    public String uploadType = "";
    private final static int REG_REMOTEMD5 = 0,
            REG_LOCALMD5 = 1,
            REG_TRSFDATE = 2,
            REG_GENLASTRUN = 3,
            REG_TOBETRSF = 4,
            REG_TOBEREMOVD = 5,
            REG_LOCALPATH = 6;
    private final static int REG_SIZE = 7;
    private final static String SEPARATOR = ",";
    private final static String YES = "1";
    private final static String NO = "0";
    private final static String BLANK = "-";
    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',};
    private int nbFilesToTransfer = 0;
    private int nbFilesToRemove = 0;

    //private InputOutput io =  IOProvider.getDefault().getIO("DEBUG", true);

    /**
     * Constructor
     *
     * This register supports multiple gedcoms to multiple web sites
     * All sites information for one gedcom will be stored in the same register
     * Sites information for two different gedcoms will be stored in two different property files
     */
    public FTPRegister(WebBookParams wp, WebHelper wh) {

        this.node = regPrefix + wh.gedcom.getName();
        this.host = wp.param_FTP_site;
        this.targetdir = wp.param_FTP_dir;
        this.localRoot = wh.getDir().getAbsolutePath();
        this.uploadType = wp.param_FTP_transfertType;
        wh.setUploadRegister(this);

        if (wp.param_FTP_upload.equals("1") && !wp.param_FTP_resetHistory.equals("1")) {
            resetFlags();
        }
    }

    private boolean resetFlags() {
        String[] keys = readKeys();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (!isValid(key)) {
                continue;
            }
            String[] fields = readKey(key);
            fields[REG_GENLASTRUN] = NO;
            fields[REG_TOBETRSF] = NO;
            fields[REG_TOBEREMOVD] = NO;
            writeKey(key, fields);
        }
        return true;
    }

    public boolean update(File file) {
        String key = host + targetdir + getFileDir(file) + file.getName();
        setLocalGen(key, file.getAbsolutePath(), YES);
        return true;
    }

    public boolean calculate(List<File> localFiles) {

        String[] keys = readKeys();
        if (keys.length == 0) {
            return false;
        }
        List<String> toBeRemoved = new ArrayList<String>();
        nbFilesToTransfer = 0;
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];

            // loop on files generated this time with server and base dir that match
            if (!isValid(key)) {
                continue;
            }
            // loop if file not in the local list
            if (!isLocal(key, localFiles)) {
                continue;
            }

            String[] fields = readKey(key);
            fields[REG_TOBETRSF] = NO;
            fields[REG_TOBEREMOVD] = NO;

            // if generated this time then get MD5 and store it
            if (equals(fields[REG_GENLASTRUN], YES)) {
                fields[REG_LOCALMD5] = getMD5(fields[REG_LOCALPATH]);
            }

            // if option = 1 or (option =2 ou 3, md5 local <> md5 remote and generated this time, then set toBeTransferred flag to 1
            if (uploadType.equals(FTP_SYSTEMATIC) && (equals(fields[REG_GENLASTRUN], YES))) {
                fields[REG_TOBETRSF] = YES;
                nbFilesToTransfer++;
            }

            if ((uploadType.equals(FTP_INCREMENTAL)) && (!equals(fields[REG_REMOTEMD5], fields[REG_LOCALMD5])) && (equals(fields[REG_GENLASTRUN], YES))) {
                fields[REG_TOBETRSF] = YES;
                nbFilesToTransfer++;
            }

            if ((uploadType.equals(FTP_SYNCHRONISE)) && (!equals(fields[REG_REMOTEMD5], fields[REG_LOCALMD5])) && (equals(fields[REG_GENLASTRUN], YES))) {
//                io.getOut().println(" ");
                fields[REG_TOBETRSF] = YES;
                nbFilesToTransfer++;
            }

            // if option = 3 and generated = 0 and md5 remote <> 0 then set toBeDeleted flag to 1
            if ((uploadType.equals(FTP_SYNCHRONISE)) && (!equals(fields[REG_REMOTEMD5], "")) && (!equals(fields[REG_GENLASTRUN], YES))) {
                fields[REG_TOBEREMOVD] = YES;
            }
            writeKey(key, fields);

            // if remote md5 = 0 and not generated this time then remove line
            if ((equals(fields[REG_REMOTEMD5], BLANK)) && (!equals(fields[REG_GENLASTRUN], YES))) {
                toBeRemoved.add(key);
            }
        }

        // Remove unused keys now
        for (String key : toBeRemoved) {
            removeKey(key);
        }

        return true;
    }

    public boolean isToTransfer(File file) {
        String key = getKey(file);
        if (key != null) {
            String[] fields = readKey(key);
            if (equals(fields[REG_TOBETRSF], YES)) {
                return true;
            }
        }
        return false;
    }

    public boolean setFileTransferred(File file) {
        String key = getKey(file);
        if (key != null) {
            String[] fields = readKey(key);
            fields[REG_REMOTEMD5] = fields[REG_LOCALMD5];
            fields[REG_TRSFDATE] = Calendar.getInstance().getTime().toString();
            writeKey(key, fields);
        }
        return false;
    }

    public int getNbFilesToTransfer() {
        return nbFilesToTransfer;
    }

    public List<String> getListToRemove() {

        String[] keys = readKeys();
        nbFilesToRemove = 0;
        List<String> listRet = new ArrayList<String>();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];

            // loop on files generated this time with server and base dir that match
            if (!isValid(key)) {
                continue;
            }

            String[] fields = readKey(key);
            if (equals(fields[REG_TOBEREMOVD], YES)) {
                listRet.add(key);
            }
        }
        nbFilesToRemove = listRet.size();
        return listRet;
    }

    public boolean setFileRemoved(String key) {
        if (key != null) {
            String[] fields = readKey(key);
            fields[REG_REMOTEMD5] = BLANK;
            fields[REG_TOBEREMOVD] = NO;
            writeKey(key, fields);
        }
        return false;
    }

    public int getNbFilesToRemove() {
        return nbFilesToRemove;
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
        String[] fields = readKey(key);
        fields[REG_GENLASTRUN] = genflag;
        fields[REG_LOCALPATH] = localpath;
        writeKey(key, fields);
        return true;
    }

    private String[] value2array(String value) {
        String[] fields = new String[REG_SIZE];
        if (value == null) {
            for (int i = 0; i < REG_SIZE; i++) {
                fields[i] = BLANK;
            }
        } else {
            String[] values = value.split(SEPARATOR);
            for (int i = 0; i < REG_SIZE; i++) {
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
        for (int i = 0; i < fields.length; i++) {
            value += fields[i] + (i < fields.length - 1 ? SEPARATOR : "");
        }
        return value;
    }

    private String getMD5(String filename) {

        FileInputStream in = null;
        try {
            // Obtain a message digest object.
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Calculate the digest for the given file.
            in = new FileInputStream(filename);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] raw = md.digest();
            return asHex(raw);
        } catch (Exception e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return "ABCDEFG";
    }

    private static String asHex(byte hash[]) {
        char buf[] = new char[hash.length * 2];
        for (int i = 0, x = 0; i < hash.length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }

    //
    // Tools to read and write keys
    //
    private String[] readKeys() {
        String[] keys = null;
        try {
            keys = NbPreferences.forModule(FTPRegister.class).node(node).keys();
        } catch (BackingStoreException ex) {
        }
        return keys;
    }

    private String[] readKey(String key) {
        return value2array(NbPreferences.forModule(FTPRegister.class).node(node).get(key, ""));
    }

    private void writeKey(String key, String[] fields) {
        NbPreferences.forModule(FTPRegister.class).node(node).put(key, array2value(fields));
    }

    private void removeKey(String key) {
        NbPreferences.forModule(FTPRegister.class).node(node).remove(key);
    }

    private boolean isValid(String key) {
        return key.indexOf(host + targetdir) == 0;
    }

    private boolean isLocal(String key, List<File> localFiles) {
        String strKey = key.substring(host.length() + targetdir.length());
        for (Iterator<File> it = localFiles.iterator(); it.hasNext();) {
            File file = it.next();
            String strLocal = file.getAbsolutePath().substring(localRoot.length() + 1);
            if (strLocal.equals(strKey)) {
                return true;
            }
        }
        return false;
    }
    private String getKey(File file) {
        String strLocal = file.getAbsolutePath().substring(localRoot.length() + 1);
        String[] keys = readKeys();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (isValid(key)) {
                String[] fields = readKey(key);
                String strKey = key.substring(host.length() + targetdir.length());
                if (strLocal.equals(strKey)) {
                    return key;
                }
            }
        }
        return null;
    }

}
