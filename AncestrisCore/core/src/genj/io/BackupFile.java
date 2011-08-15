/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genj.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author daniel
 */
public class BackupFile {

    public static void createBackup(File file) throws GedcomIOException {
        if (file.exists()) {
            final Pattern p = Pattern.compile(file.getName() + "_([0-9]{4})_");
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
                bak = new File(file.getAbsolutePath() + "_"
                        + getNextSuffix(m.group(1))
                        + "_");
            } else {
                bak = new File(file.getAbsolutePath() + "_"
                        + getNextSuffix("0")
                        + "_");
            }
            if (bak.exists()) {
                throw new GedcomIOException("Couldn't create backup file " + bak.getName(), -1);
            }
            if (!file.renameTo(bak)) {
                throw new GedcomIOException("Couldn't create backup for " + file.getName(), -1);
            }
        }
    }

    private static String getNextSuffix(String suffix) {
        return String.format("%04d", Integer.parseInt(suffix) + 1);
    }
}
