package ancestris.modules.releve.merge;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author michel
 */


class MergeLogger {

    static final Level ACCEPT = Level.FINE;
    static final Level REFUSE = Level.FINEST;
    
    static private File logFile;
    static private FileHandler fileHandler = null; 
    static final Logger LOG = Logger.getLogger("releve");
    
    static {
        LOG.setLevel(Level.OFF);
    }
    
    static void enable() {
        try {
            if( fileHandler == null) {
                LOG.setUseParentHandlers(false);
                logFile = new File(System.getProperty("java.io.tmpdir"), "relevelog.txt");
                logFile.delete();
                fileHandler = new FileHandler(logFile.getCanonicalPath());
                fileHandler.setEncoding("UTF-8");
                LOG.addHandler(fileHandler);
            } else {
                fileHandler.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        LOG.setLevel(Level.FINEST);
    }

    static void disable() { 
        if(fileHandler!=null ) {
            fileHandler.flush();
            fileHandler.close();
        }
        LOG.removeHandler(fileHandler);
        fileHandler = null;        
    }
    
    static void logAccept(String className, String methodName, String format, Object ... args ) {
        LOG.logp(ACCEPT, className, methodName, String.format("ACCEPT " + format, args ));
    }
    
    /**
     * copie le nom du fichier de trace dans le presse papier
     */
    static void copyFileNameToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection sel = new StringSelection(logFile.getPath());
        clipboard.setContents(sel, sel);
         
    } 
    
    /**
     * affiche le fichier de log avec l'etiteur de texte par défaut du système
     * @throws IOException 
     */
    static void showLog() throws IOException {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        Desktop desktop = Desktop.getDesktop();
         
        desktop.open(logFile);         
    }            

}
