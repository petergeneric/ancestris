package ancestris.startup.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

public class Util {

    /**
     *
     *  @param fromFile     From which path to load the properties file
     *  @param fromClasspath true, If the path is relative to classpath
     *
     */
    public static Properties loadProperties(String fromFile, boolean fromClasspath) {
        Properties _props = null;
        if (!fromClasspath) {
            File _fromFile = new File(fromFile);
            if (!_fromFile.exists() || !_fromFile.isFile() || !_fromFile.canRead()) {
                return null;
            }
        }
        InputStream fileIn = null;
        _props = new PropertiesLike();
        try {
            if (fromClasspath) {
                fileIn = Util.class.getResourceAsStream(fromFile);
            } else {
                fileIn = new FileInputStream(fromFile);
            }
            _props.load(fileIn);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            close(fileIn);
        }
        return _props;
    }

    public static Properties loadProperties(String fromFile) {
        return loadProperties(fromFile, false);
    }

    public static Properties loadProperties(File fromFile) {
        return loadProperties(fromFile.getAbsolutePath(), false);
    }

    /**
     * Create file and parent dir if non existent
     * @param file
     * @return same as createNewFile
     */
    public static boolean createRecursively(File file) throws IOException {
        File fileDir = file.getParentFile();
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return file.createNewFile();
    }

    public static boolean copy(File from, File to) {
        // test if files has permissions to read and write
        if (!from.exists() || !from.isFile() || !from.canRead()) {
            return false;
        }
        File toDir = to.getParentFile();
        if (!toDir.exists()) {
            toDir.mkdirs();
        }
        boolean copied = false;
        FileInputStream fileIn = null;
        FileOutputStream fileOut = null;
        try {
            fileIn = new FileInputStream(from);
            fileOut = new FileOutputStream(to);
            FileUtil.copy(fileIn, fileOut);
            copied = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            close(fileOut);
            close(fileIn);
        }
        return copied;
    }

    public static boolean copy(String from, String to) {
        return copy(new File(from), new File(to));
    }

    public static String getNBInstallPath() {
        return InstalledFileLocator.getDefault().locate(".", null, false).getPath();
    }

    // <editor-fold defaultstate="collapsed" desc=" close(out) ">
    public static void close(OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ex) {
            }
        }
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" close(in) ">
    public static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
    }// </editor-fold>

    static void close(Reader in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
    }

    static void close(Writer out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ex) {
            }
        }
    }
}
