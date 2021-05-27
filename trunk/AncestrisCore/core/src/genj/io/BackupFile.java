/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
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

    public static boolean createBackup(File file) throws GedcomIOException {
        boolean result=false;
        try {
            if ((Options.getNbBackups() != 0) && file.exists()) {
                final Pattern p = Pattern.compile(getBasename(file.getName()) + "_([0-9]{8}-[0-9]{6})" + getExtension(file.getName()));
                final File theFile = file;
                File bak;

                FilenameFilter filter = new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return (dir.equals(theFile.getParentFile()) && p.matcher(name).matches());
                    }
                };
                File parent = file.getParentFile();
                if (parent == null) {
                    throw new GedcomIOException("Couldn't create backup for file " + file.getName() + ", problem accessing parent directory.", -1);
                }
                File[] backups = parent.listFiles(filter);
                Arrays.sort(backups);
                
                // TODO: mettre une options
                if (backups.length >= Options.getNbBackups()) {
                    for (int i = 0; i <= backups.length - Options.getNbBackups(); i++) {
                        if (!backups[i].delete()) {
                            throw new GedcomIOException("Couldn't delete backup file " + backups[0].getName() + ", problem deleting old backup files.", -1);
                        }
                    }
                }
                if (backups.length > 0) {
                    Matcher m = p.matcher(backups[backups.length - 1].getName());
                    if (!m.matches()) {
                        throw new GedcomIOException("Couldn't create backup for file " + file.getName() + ", cannot find previous backups.", -1);
                    }
                    bak = getBackupFile(file);
                } else {
                    bak = getBackupFile(file);
                }
                if (bak.exists()) {
                    throw new GedcomIOException("Couldn't create backup file " + bak.getName() + ", backup file already exists.", -1);
                }
                 try {
                    Thread.sleep(500);
                } catch( InterruptedException e) {
                    //RAF.
                }
                if (!file.renameTo(bak)) {
                    throw new GedcomIOException("Couldn't create backup for " + file.getName() + ", problem renaming backup file.", -1);
                }
            }
            result = true;
        } finally {
//            return result;
        }
        return result;
    }

    private static File getBackupFile(File file) {
        String fileName = file.getAbsolutePath();
        return new File(getBasename(fileName) + "_"
                + ISO_FORMAT.format(new Date())
                + getExtension(fileName));
    }

    // From Util.getExtension
    /** Gets the extension of a specified file name. The extension is
     * everything after the last dot. The extension includes the dot char so that
     * fileName = Basename+Extension
     *
     * @param fileName name of the file
     *
     * @return extension of the file (or
     * <code>""</code> if it had none)
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
     *
     * @return basename of the file (or
     * <code>fileName</code> if it had none)
     */
    private static String getBasename(String fileName) {
        int index = fileName.lastIndexOf("."); // NOI18N

        if (index == -1) {
            return fileName; // NOI18N
        } else {
            return fileName.substring(0, index);
        }
    }
}
