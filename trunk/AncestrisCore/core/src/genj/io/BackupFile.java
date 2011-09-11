/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genj.io;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author daniel
 */
public class BackupFile {
    static private final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");


    public static void createBackup(File file) throws GedcomIOException {
        if (file.exists()) {
            final Pattern p = Pattern.compile(getBasename(file.getName()) + "_([0-9]{8}-[0-9]{6})" + getExtension(file.getName()));
            final File theFile = file;
            File bak;

            FilenameFilter filter = new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return (dir.equals(theFile.getParentFile()) && p.matcher(name).matches());
                }
            };
            File parent = file.getParentFile();
            File[] backups = parent.listFiles(filter);
            Arrays.sort(backups);

            // TODO: mettre une options
            if (backups.length >= 10) {
                for (int i = 0; i <= backups.length - 10; i++) {
                    if (!backups[i].delete()) {
                        throw new GedcomIOException("Couldn't delete backup file " + backups[0].getName(), -1);
                    }
                }
            }
            if (backups.length > 0) {
                Matcher m = p.matcher(backups[backups.length - 1].getName());
                if (!m.matches()) {
                    throw new GedcomIOException("Couldn't create backup for file " + file.getName(), -1);
                }
                bak = getBackupFile(file);
            } else {
                bak = getBackupFile(file);
            }
            if (bak.exists()) {
                throw new GedcomIOException("Couldn't create backup file " + bak.getName(), -1);
            }
            if (!file.renameTo(bak)) {
                throw new GedcomIOException("Couldn't create backup for " + file.getName(), -1);
            }
        }
    }

    private static String getNextSuffix1(String suffix) {
        return String.format("%04d", Integer.parseInt(suffix) + 1);
    }
    private static File getBackupFile(File file){
        String fileName = file.getAbsolutePath();
        return new File(getBasename(fileName)+"_"+
                ISO_FORMAT.format(new Date())+
                getExtension(fileName));
    }

    // From Util.getExtension
    /** Gets the extension of a specified file name. The extension is
    * everything after the last dot. The extension includes the dot char so that
    * fileName = Basename+Extension
    *
    * @param fileName name of the file
    * @return extension of the file (or <code>""</code> if it had none)
    */
    private static String getExtension(String fileName) {
        int index = fileName.lastIndexOf("."); // NOI18N

        if (index == -1) {
            return ""; // NOI18N
        } else {
            return fileName.substring(index);
        }
    }

    /** Gets the basename of a specified file name. The basename is
    * everything before the last dot.
    *
    * @param fileName name of the file
    * @return basename of the file (or <code>fileName</code> if it had none)
    */
    private static String getBasename(String fileName) {
        int index = fileName.lastIndexOf("."); // NOI18N

        if (index == -1) {
            return fileName; // NOI18N
        } else {
            return fileName.substring(0,index);
        }
    }


}
