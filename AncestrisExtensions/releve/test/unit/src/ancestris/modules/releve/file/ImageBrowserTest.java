package ancestris.modules.releve.file;

import java.io.File;
import java.io.FileFilter;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Michel
 */
public class ImageBrowserTest {

    /**
     * test splitCSV
     */
    @Test
    public void testFindImage() {

        //File mainImageDirectory = new File( "D:\\Généalogie");
        // File mainImageDirectory = new File( "D:\\Généalogie\\Archives09\\2C-Enregistrement\\Allemans\\Allemans-2c30-Insinuations-1780-1788");
//        File mainImageDirectory = new File( "D:\\");
//
//
//        DirectoryReferenceFilter dirFilter = new DirectoryReferenceFilter("2c30");
//        FileReferenceFilter fileFilter = new FileReferenceFilter("004");
//        File result = getFiles(mainImageDirectory, dirFilter, fileFilter);
//        if ( result != null) {
//            System.out.println( "result= "+ result.getAbsolutePath());
//        } else {
//            System.out.println( "result= "+ result);
//        }
//
//        assertNotNull(result);
        assertTrue("test vide", true);

    }

    private File getFiles(File directory, DirectoryReferenceFilter dirFilter, FileReferenceFilter fileFilter) {
        File result = null;

         if (directory.isDirectory()) {
            // je cherche sous repertoire
            File directories[] = directory.listFiles(dirFilter);
            if (directories != null) {
                for (File subdir : directories) {
                    // je cherche le fichier
                    File files[] = subdir.listFiles(fileFilter);
                    if (files != null && files.length > 0) {
                        result = files[0];
                    }
                }

                if (result == null) {
                    // je cherche sous repertoire
                    for (File subdir : directory.listFiles()) {
                        if (subdir.isDirectory()) {
                            result = getFiles(subdir, dirFilter, fileFilter);
                            if (result != null) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }


    private class DirectoryReferenceFilter implements FileFilter {
        String reference;

        public DirectoryReferenceFilter(String reference) {
            this.reference = reference.toLowerCase();
        }

        public boolean accept(File dir) {
            return dir.isDirectory() && dir.getName().toLowerCase().contains(reference);
        }
    }

    private class FileReferenceFilter implements FileFilter {
        String reference;

        public FileReferenceFilter(String reference) {
            this.reference = reference.toLowerCase();
        }

        public boolean accept(File dir) {
            return dir.isFile() && dir.getName().toLowerCase().contains(reference);
        }
    }
}
