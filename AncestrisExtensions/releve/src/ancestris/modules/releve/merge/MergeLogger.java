package ancestris.modules.releve.merge;

import genj.gedcom.Entity;
import genj.gedcom.PropertyDate;
import genj.io.FileAssociation;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author michel
 */


class MergeLogger {

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
        LOG.setLevel(Level.OFF);
    }

    static final Level ACCEPT = Level.FINE;
    static final Level REFUSE = Level.FINEST;

    static MergeInfo.InfoFormatter infoFormater = new MergeInfo.InfoFormatter() {
        @Override
        public Object format(Object arg) {
            if (arg == null) {
                return "null";
            } else if (arg instanceof Entity) {
                return ((Entity) arg).getId();
            } else if (arg instanceof PropertyDate) {
                return ((PropertyDate) arg).getValue();
            } else {
                return arg;
            }
        }
    };

    static LogRecord getAccept( String logFormat, Object ... logArgs ) {
        return new LogRecord(ACCEPT, "ACCEPT " + new MergeInfo(logFormat, logArgs).toString(infoFormater) );
    }

    static LogRecord getAccept(MergeInfo mergeInfo ) {
        return new LogRecord(ACCEPT, "ACCEPT " + mergeInfo.toString(infoFormater) );
    }

    static LogRecord getRefuse( String logFormat, Object ... logArgs ) {
        return new LogRecord(REFUSE, "REFUSE " + new MergeInfo(logFormat, logArgs).toString(infoFormater)) ;
    }

    static LogRecord getRefuse( MergeInfo mergeInfo ) {
        return new LogRecord(REFUSE, "REFUSE " + mergeInfo.toString(infoFormater) );
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
    static void showLog() throws Exception {
        
        if( System.getProperty("os.name").toLowerCase().contains("win") ) {
            FileAssociation.getDefault().execute(logFile.getAbsolutePath());
        } else {
            FileAssociation.getDefault().execute(new File(new URI("file://" + logFile.getPath()).getPath()).getAbsolutePath());
        }
    }

}
