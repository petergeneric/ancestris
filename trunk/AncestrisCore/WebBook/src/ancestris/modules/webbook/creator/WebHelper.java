package ancestris.modules.webbook.creator;

import ancestris.gedcom.privacy.PrivacyPolicy;
import ancestris.modules.webbook.Log;
import ancestris.modules.webbook.WebBookParams;
import ancestris.modules.webbook.transfer.FTPRegister;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySource;
import genj.gedcom.Source;
import genj.gedcom.time.PointInTime;
import genj.io.InputSource;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.imageio.ImageIO;
import org.openide.filesystems.FileUtil;

/**
 * Ancestris
 *
 * Tools for WebBook: - file and directory manipulation - gedcom sets - misc
 *
 *
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class WebHelper {

    public Gedcom gedcom;
    public Log log;
    public WebBookParams wp;
    //
    public Indi indiDeCujus = null;
    private List<Indi> individualsList = null;
    private List<WebMedia.Photo> photosList = null;
    private List<Source> sourcesList = null;
    private FTPRegister uploadRegister = null;
    /**
     * Variables
     */
    //
    //
    public final String DEFCHAR = "-";
    //
    private MediaTracker mediaTracker = new MediaTracker(new Container());

    private class Info {

        Integer counter = 0;
        List<Property> props = null;
    }
    private SortedMap<String, Integer> listOfLastnames = null;
    private boolean initLastname = false;
    private SortedMap<String, Info> listOfCities = null;
    private boolean initCity = false;
    private SortedMap<String, Info> listOfDays = null;
    private boolean initDay = false;
    private List<Ancestor> listOfAncestors = new ArrayList<>();
    private boolean initAncestors = false;
    private Set<Indi> listOfCousins = new HashSet<>();
    private boolean initCousins = false;

    /**
     * *************************************************************************
     * CONSTRUCTOR
     */
    public WebHelper(Gedcom gedcom, Log log, WebBookParams wp) {
        this.gedcom = gedcom;
        this.log = log;
        this.wp = wp;
    }

    /**
     * *************************************************************************
     * TOOLS FOR FILE MANIPULATION
     */
    //
    public void setUploadRegister(FTPRegister ulReg) {
        uploadRegister = ulReg;
    }

    /**
     * Returns local home directory
     */
    public File getDir() {
        return new File(wp.param_localWebDir);
    }

    /**
     * Clean Dir
     *
     */
    public void cleanLocalDir() {
        emptyDir(new File(wp.param_localWebDir), false);
    }

    /**
     * Empty Dir
     *
     */
    public boolean emptyDir(File dir, boolean removeDir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String children1 : children) {
                boolean success = emptyDir(new File(dir, children1), true);
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        if (removeDir && !dir.getAbsolutePath().equals(wp.param_logFile)) {
            return dir.delete();
        }
        return true;
    }

    /**
     * Create Dir
     *
     */
    public File createDir(String outfile, boolean create) {

        String parent = (new File(outfile)).getAbsoluteFile().getParent();
        if (parent == null || parent.length() == 0) {
            return null;
        }
        File fp = new File(parent);
        if (!fp.exists()) {
            String[] dirs = parent.split("[" + File.separator + File.separator + "]");
            String absoluteDir = dirs[0] + File.separator;
            for (int i = 1; i < dirs.length; i++) {
                String dir = dirs[i];
                absoluteDir += dir + File.separator;
                File ad = new File(absoluteDir);
                if (!ad.exists()) {
                    ad.mkdir();
                    putSecurityFile(ad);
                }
            }
        }
        File f = new File(outfile);
        if (create) {
            f.mkdir();
            putSecurityFile(f);
        }
        return (f);
    }

    /**
     * In case of PHP site, ensure we have an index.php file in the directory in
     * case surfer goes direct to the directory
     *
     */
    private void putSecurityFile(File dir) {
        if (wp.param_PHP_Support.equals("1")) {
            File file = getFileForName(dir, "index.php");
            try (PrintWriter out = getWriter(file, Charset.forName("UTF-8"))) {
                out.println("<?php header('Location: ../index.php'); die; ?>");
            }
        }
    }

    /**
     * Generates file name
     */
    public File getFileForName(File dir, String fileName) {
        return new File(dir, fileName);
    }

    /**
     * Helper - Create a PrintWriter wrapper for output stream
     */
    public PrintWriter getWriter(File file, Charset cs) {

        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), cs));
            // Update register
            if (uploadRegister != null) {
                uploadRegister.update(file);
            }

        } catch (IOException e) {
            //e.printStackTrace();
            log.write(log.ERROR, "getWriter - " + e.getMessage());
        }
        return pw;
    }

    /**
     * Clean file name from drive letters (windows), colon ":", starting file
     * separators and spaces
     */
    public String getCleanFileName(String input, String defchar) {

        // Eliminate drive letter by starting after ":"
        String str = input.substring(Math.max(0, input.lastIndexOf(":") + 1));

        // Eliminate back-slashes in case of windows or any other case
        while (str.startsWith("\\")) {
            str = str.substring(1);
        }

        // Eliminate slashes
        while (str.startsWith("/")) {
            str = str.substring(1);
        }

        // Eliminate blank spaces
        String temp = str.replaceAll("\\s", "_");
        
        // Convert path to avoid windows difficulties.
        temp = temp.replaceAll("\\\\", "/");

        // Remove anything web parameters
        int i = temp.indexOf('?');
        if (i > 0) {
            temp = temp.substring(0, i);
        }

        // Eliminate accents
        String cleanName = fileNameConvert(temp, defchar);

        return cleanName;
    }

    public String fileNameConvert(String filename, String defchar) {
        if (filename == null) {
            return "null";
        }
        String text = filename.toLowerCase();
        char[] charInput = text.toCharArray();
        StringBuilder strOutput = new StringBuilder(1000);
        for (int i = 0; i < charInput.length; i++) {
            strOutput.append(convertChar(charInput[i], false, defchar));
        }
        return strOutput.toString();
    }

    public void copyFiles(String imagesDir, String toFile, String fromDir) {
        try {
            // get resource directory where images are
            URL dirURL = wp.getClass().getClassLoader().getResource(wp.getClass().getName().replace(".", "/") + ".class");
            if (dirURL.getProtocol().equals("jar")) {
                /*
                 * A JAR path
                 */
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
                JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
                Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith(imagesDir)) { //filter according to the path
                        String entry = name.substring(imagesDir.length());
                        int checkSubdir = entry.indexOf("/");
                        if (checkSubdir < 0 && !entry.trim().isEmpty()) {
                            // if it is NOT a subdirectory, it must be an image so copy it
                            result.add(entry);
                        }
                    }
                }

                String[] list = result.toArray(new String[result.size()]);
                for (String fileName : list) {
                    copy(fromDir + fileName, toFile + fileName);
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
            log.write(log.ERROR, "copyFiles - " + e.getMessage());
        }
    }

    public String convertChar(char c, boolean isAnchor, String defchar) {
        String str;
        switch (c) {
            case 'à':
                str = "a";
                break;
            case 'á':
                str = "a";
                break;
            case 'â':
                str = "a";
                break;
            case 'ã':
                str = "a";
                break;
            case 'ä':
                str = "a";
                break;
            case 'å':
                str = "a";
                break;
            case 'æ':
                str = "ae";
                break;
            case 'ç':
                str = "c";
                break;
            case 'è':
                str = "e";
                break;
            case 'é':
                str = "e";
                break;
            case 'ê':
                str = "e";
                break;
            case 'ë':
                str = "e";
                break;
            case 'ì':
                str = "i";
                break;
            case 'í':
                str = "i";
                break;
            case 'î':
                str = "i";
                break;
            case 'ï':
                str = "i";
                break;
            case 'ð':
                str = "o";
                break;
            case 'ñ':
                str = "n";
                break;
            case 'ò':
                str = "o";
                break;
            case 'ó':
                str = "o";
                break;
            case 'ô':
                str = "o";
                break;
            case 'õ':
                str = "o";
                break;
            case 'ö':
                str = "o";
                break;
            case 'ø':
                str = "o";
                break;
            case 'ù':
                str = "u";
                break;
            case 'ú':
                str = "u";
                break;
            case 'û':
                str = "u";
                break;
            case 'ü':
                str = "u";
                break;
            case 'ý':
                str = "y";
                break;
            case 'þ':
                str = "p";
                break;
            case 'ÿ':
                str = "y";
                break;
            case 'ß':
                str = "ss";
                break;
            default:
                str = String.valueOf(c);
                if (str.matches("[a-zA-Z0-9-]")) {
                    return str;
                } else if (str.compareTo(".") == 0) {
                    return (isAnchor ? defchar : str);
                } else if (str.compareTo("/") == 0) {
                    return (isAnchor ? defchar : str);
                } else if (str.compareTo("\\") == 0) {
                    return (isAnchor ? defchar : str);
                } else {
                    return defchar;
                }
        }
        return str;
    }

    /**
     * Memorise photos list
     */
    public void setPhotos(List<WebMedia.Photo> photos) {
        photosList = photos;
    }

    /**
     * Get photos for an entity
     */
    public List<WebMedia.Photo> getPhoto(Entity entity) {
        List<WebMedia.Photo> ret = new ArrayList<>();
        for (WebMedia.Photo photo : photosList) {
            if (photo.getEntity().equals(entity)) {
                ret.add(photo);
            }
        }
        return ret;
    }

    /**
     * Get title of a media
     */
    public String getTitle(PropertyFile media, String defchar) {
        String str = "";
        // Get TITL in case of 5.5.1 OBJE record
        Property ptitle = media.getProperty("TITL");
        if (ptitle != null) {
            str = ptitle.getDisplayValue().trim();
            if (!str.isEmpty()) {
                return str;
            }
        }

        // Else get TITL in case of link OBJE
        ptitle = media.getParent().getProperty("TITL");
        if (ptitle != null) {
            str = ptitle.getDisplayValue().trim();
            if (!str.isEmpty()) {
                return str;
            }
        }

        // Else, use filename
        InputSource file = media.getInput().orElse(null);
        if (file != null) {
            String filename = file.getName();
            return getCleanFileName(filename, defchar);
        }
        str = media.toString();
        return str.substring(str.lastIndexOf(File.separator) + 1);
    }

    /**
     * Copy method for resource copy to file given by string
     */
    public void copy(String from_name, String to_name) throws IOException {
        File to_file = new File(to_name);
        try (FileOutputStream fos = new FileOutputStream(to_file)) {
            FileUtil.copy(wp.getClass().getResourceAsStream(from_name), fos);
            if (uploadRegister != null) {
                uploadRegister.update(to_file);
            }
        }
    }

    /**
     * ReadStream method from resource to string
     */
    public String readStream(String from_name) throws IOException {
        byte[] bytes;
        try (InputStream is = wp.getClass().getResourceAsStream(from_name)) {
            bytes = new byte[is.available()];
            is.read(bytes);
        }
        return new String(bytes, "UTF8");
    }

    /**
     * Copy method for file to file copy
     */
    public void copy(String from_name, String to_name, boolean linkOnly) throws IOException {
        copy(from_name, to_name, linkOnly, true);
    }

    /**
     * Copy method for file to file copy
     */
    public void copy(String from_name, String to_name, boolean linkOnly, boolean force) throws IOException {

        File from_file = new File(from_name);  // Get File objects from Strings
        File to_file = new File(to_name);

        // if file exists and force is false, exit as there is no need to recreate an existing file (optimisation of performance for user)
        if (to_file.exists() && !force) {
            if (uploadRegister != null) {
                uploadRegister.update(to_file);
            }
            return;
        }

        // First make sure the source file exists, is a file, and is readable.
        if (!from_file.exists()) {
            abort("FileCopy: no such source file: " + from_name);
        }
        if (!from_file.isFile()) {
            abort("FileCopy: can't copy directory: " + from_name);
        }
        if (!from_file.canRead()) {
            abort("FileCopy: source file is unreadable: " + from_name);
        }

        // If the destination is a directory, use the source file name as the destination file name
        if (to_file.isDirectory()) {
            to_file = new File(to_file, from_file.getName());
        }

        // If the destination exists, make sure it is a writeable file and ask before overwriting it.
        // If the destination doesn't exist, make sure the directory exists and is writeable.
        if (to_file.exists()) {
            if (!to_file.canWrite()) {
                abort("FileCopy: destination file is unwriteable: " + to_name);
            }
        } else {
            // if file doesn't exist, check if directory exists and is writeable.
            // If getParent() returns null, then the directory is the current dir.
            // so look up the user.dir system property to find out what that is.
            String parent = to_file.getParent();  // Get the destination directory
            if (parent == null) {
                parent = System.getProperty("user.dir"); // or CWD
            }
            File dir = new File(parent);          // Convert it to a file.
            if (!dir.exists()) {
                createDir(to_name, false);
                // abort("FileCopy: destination directory doesn't exist: " + parent);
            }
            if (dir.isFile()) {
                abort("FileCopy: destination is not a directory: " + parent);
            }
            if (!dir.canWrite()) {
                abort("FileCopy: destination directory is unwriteable: " + parent);
            }
        }

        // If we've gotten this far, then everything is okay.
        // Only use link for linux, force copy for other systems.
        String OS_NAME = System.getProperty("os.name");
        if (OS_NAME == null) {
            OS_NAME = "";
        }
        boolean IS_OS2 = OS_NAME.startsWith("OS/2");
        boolean IS_MAC = OS_NAME.startsWith("Mac");
        boolean IS_WINDOWS = OS_NAME.startsWith("Windows");
        boolean IS_UNIX = !IS_OS2 && !IS_WINDOWS && !IS_MAC;

        if (!IS_UNIX) {
            linkOnly = false;
        }
        // Copy symbolic link if that is the option....
        try {
            if (linkOnly) { // will only work for linux
                String[] command = {"ln", "-s", from_name, to_name};
                Runtime.getRuntime().exec(command);
            } else {
                // ...Otherwise copy the file, a buffer of bytes at a time.
                //String[] command = { "cp", from_name, to_name }; (cp for linux, copy for dos
                //Runtime.getRuntime().exec(command);
                FileInputStream from = null;  // Stream to read from source
                FileOutputStream to = null;   // Stream to write to destination
                try {
                    from = new FileInputStream(from_file);  // Create input stream
                    to = new FileOutputStream(to_file);     // Create output stream
                    byte[] buffer = new byte[4096];         // A buffer to hold file contents
                    int bytes_read;                         // How many bytes in buffer
                    // Read a chunk of bytes into the buffer, then write them out,
                    // looping until we reach the end of the file (when read() returns -1).
                    // Note the combination of assignment and comparison in this while
                    // loop.  This is a common I/O programming idiom.
                    while ((bytes_read = from.read(buffer)) != -1) { // Read bytes until EOF
                        to.write(buffer, 0, bytes_read);             //   write bytes
                    }
                } // Always close the streams, even if exceptions were thrown
                finally {
                    if (from != null) {
                        try {
                            from.close();
                        } catch (IOException e) {
                            //e.printStackTrace();
                            log.write(log.ERROR, "copy (from) - " + e.getMessage());
                        }
                    }
                    if (to != null) {
                        try {
                            to.close();
                        } catch (IOException e) {
                            //e.printStackTrace();
                            log.write(log.ERROR, "copy (to) - " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.write(log.ERROR, "copy - " + e.getMessage());
        }

        // Update register
        if (uploadRegister != null) {
            uploadRegister.update(to_file);
        }

    }

    /**
     * A convenience method to throw an exception
     */
    private void abort(String msg) throws IOException {
        log.write(log.ERROR, "abort - " + msg);
        throw new IOException(msg);
    }

    /**
     * Detect image
     */
    public boolean isImage(String infile) {
        if (infile == null) {
            return false;
        }
        return (infile.toLowerCase().endsWith(".jpg")
                || infile.toLowerCase().endsWith(".png")
                || infile.toLowerCase().endsWith(".bmp")
                || infile.toLowerCase().endsWith(".gif"));
    }

    /**
     * Get image size
     */
    String getImageSize(String absoluteFile, String quote) {
        Image image = Toolkit.getDefaultToolkit().getImage(absoluteFile);
        mediaTracker.addImage(image, 0);

        try {
            mediaTracker.waitForID(0);
        } catch (InterruptedException e) {
            return "100', '100";
        }

        int imageHeight = image.getHeight(null);
        int imageWidth = image.getWidth(null);
        mediaTracker.removeImage(image);
        image.flush();
        return "" + imageWidth + "', '" + imageHeight;
    }

    public boolean scaleImage(String infile, String outfile, int width, int height, int quality, boolean force) {

        // if file exists and force is false, exit as there is no need to recreate an existing file (optimisation of performance for user)
        File out_file = new File(outfile);
        if (out_file.exists() && !force) {
            if (uploadRegister != null) {
                uploadRegister.update(out_file);
            }
            return true;
        }

        boolean result = false;
        Image image = Toolkit.getDefaultToolkit().getImage(infile);
        mediaTracker.addImage(image, 0);

        try {
            mediaTracker.waitForID(0);
        } catch (InterruptedException e) {
            log.write(log.ERROR, "scaleImage (mediaTracker) - " + e.getMessage());
        }

        // determine thumbnail size from WIDTH and HEIGHT
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        if ((imageWidth <= 0) || (imageHeight <= 0)) {
            mediaTracker.removeImage(image);
            image.flush();
            return false;   // a non picture file will have size <=0 (sound for instance can be -1)
        }
        double imageRatio = (double) imageWidth / (double) imageHeight;
        int thumbWidth = width;
        int thumbHeight = height;
        if (width == 0) {
            thumbWidth = (int) (thumbHeight * imageRatio);
        }
        if (height == 0) {
            thumbHeight = (int) (thumbWidth / imageRatio);
        }
        double thumbRatio = (double) thumbWidth / (double) thumbHeight;
        if (thumbRatio < imageRatio) {
            thumbHeight = (int) (thumbWidth / imageRatio);
        } else {
            thumbWidth = (int) (thumbHeight * imageRatio);
        }

        try {
            // draw original image to thumbnail image object and
            // scale it to the new size on-the-fly
            BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = thumbImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

            createDir(outfile, false);

            // save thumbnail image to OUTFILE
            // XXX: previously used com.sun.image.codec.jpeg.* with jpeg
            // XXX:quality 100. Should we use png images instead?
            ImageIO.write(thumbImage, "jpeg", out_file);
            result = true;
        } catch (IOException e) {
            log.write(log.ERROR, "scaleImage (encoding) - " + e.getMessage());
        } 

        // Update register
        if (uploadRegister != null) {
            uploadRegister.update(out_file);
        }

        // Flush resources and exit
        mediaTracker.removeImage(image);
        image.flush();
        return result;
    }

    /**
     * Read a file into a string
     *
     */
    public String readFile(String filename) throws IOException {

        if ((filename == null) || (filename.length() == 0)) {
            return "";
        }
        File f;
        FileInputStream in = null;
        StringBuilder sb = new StringBuilder("");

        try {
            f = new File(filename);
            in = new FileInputStream(f);
            int size = (int) f.length();
            byte[] data = new byte[size];
            int chars_read = 0;
            while (chars_read < size) {
                chars_read += in.read(data, chars_read, size - chars_read);
            }
            sb.append(new String(data, "UTF8"));
        } catch (IOException e) {
            log.write(log.ERROR, "readFile (read) - " + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                log.write(log.ERROR, "readFile (finally) - " + e.getMessage());
            }
        }

        return sb.toString();
    }

    /**
     * Writes a file from a string
     *
     */
    public boolean writeFile(String filename, String text) throws IOException {
        if ((filename == null) || (filename.length() == 0)) {
            return false;
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter(filename))) {
                out.write(text);
        } catch (IOException e) {
            log.write(log.ERROR, "writeFile - " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * *************************************************************************
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     * TOOLS FOR GEDCOM SETS
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */
    //
    /**
     * Returns nb of individuals
     *///USED
    public int getNbIndis() {
        return gedcom.getIndis().size();
    }

    /**
     * Get sosa if available
     */
    Indi getIndiDeCujus(String str) {
        //
        if (indiDeCujus != null) {
            return indiDeCujus;
        }
        Entity[] indis = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
        for (Entity indi1 : indis) {
            Indi indi = (Indi) indi1;
            if (indi.toString().equals(str)) {
                indiDeCujus = indi;
                break;
            }
        }
        return indiDeCujus;
    }

    /**
     * Get sosa if available
     */
    public String getSosa(Indi indi) {
        if (indi == null) {
            return "";
        }
        return indi.getSosaString();
    }

    /**
     * Return sorted list of lastnames of Gedcom file Do NOT use getLastNames()
     * function of genj because it returns all lastnames found and we only want
     * one last name per person here (Genj itself only accesses individuals
     * using the first lastname found for them)
     */
    @SuppressWarnings("unchecked")
    public List<String> getLastNames(String defchar, Comparator<String> sortLastnames) {
        if (!initLastname) {
            initLastname = buildLastnamesList(gedcom, defchar, sortLastnames);
        }
        return (List<String>) new ArrayList<>(listOfLastnames.keySet());
    }

    public int getTotalNamesCount() {
        if (!initLastname) {
            initLastname = buildLastnamesList(gedcom, DEFCHAR, new Comparator<String>() {

                @Override
                public int compare(String t1, String t2) {
                    return (t1.compareTo(t2));
                }
            });
        }
        return listOfLastnames.size();
    }

    //USED
    public int getLastNameCount(String lastname, String defchar) {
        String str = lastname;
        if (str == null) {
            str = defchar;
        }
        if (listOfLastnames.get(str) == null) {
            return -1;
        }
        return (int) listOfLastnames.get(str);
    }

    @SuppressWarnings("unchecked")
    private boolean buildLastnamesList(Gedcom gedcom, String defchar, Comparator<String> sortLastnames) {
        listOfLastnames = new TreeMap<>(sortLastnames);
        Collection<Indi> indis = (Collection<Indi>) gedcom.getEntities(Gedcom.INDI);
        for (Indi indi : indis) {
            String str = getLastName(indi, defchar);
            Integer counter = listOfLastnames.get(str);
            if (counter == null) {
                counter = 1;
            } else {
                counter++;
            }
            listOfLastnames.put(str, counter);
        }
        return true;
    }

    public String getLastName(Indi indi, String defchar) {
        if (indi == null) {
            return defchar;
        }
        String str = indi.getLastName();
        return (str == null ? defchar : str.isEmpty() ? defchar : str);
    }

    /**
     * Return sorted list of individuals (Indi) of Gedcom file Lastnames are
     * sorted according to their anchor-compatible equivallent strings (A-Z a-z
     * '-' characters only)
     */
    @SuppressWarnings("unchecked")
    public List<Indi> getIndividuals(Gedcom gedcom, Comparator<Indi> sort) {
        Comparator<Indi> sortIndividuals = sort;
        if (sortIndividuals == null) {
            sortIndividuals = new Comparator<Indi>() {

                @Override
                public int compare(Indi t1, Indi t2) {
                    return (t1.compareTo(t2));
                }
            };
        }
        if (individualsList == null) {
            individualsList = new ArrayList<>((Collection<Indi>) gedcom.getEntities(Gedcom.INDI));
            Collections.sort(individualsList, sortIndividuals);
        }
        return individualsList;
    }

    /**
     * Return sorted list of sources of Gedcom file Sources are sorted by codes
     */
    @SuppressWarnings("unchecked")
    public List<Source> getSources(Gedcom gedcom) {
        if (sourcesList == null) {
            sourcesList = new ArrayList<>((Collection<Source>) gedcom.getEntities(Gedcom.SOUR));
            Collections.sort(sourcesList, sortSources);
        }
        return sourcesList;
    }

    /**
     * Return sorted list of sources of entity Sources are sorted by codes
     */
    @SuppressWarnings("unchecked")
    public List<Source> getSources(Indi indi) {
        // get sources of individual
        List<Property> sources = new ArrayList<>();
        getPropertiesRecursively((Property) indi, sources, PropertySource.class);

        // get sources of the associated families
        Fam[] families = indi.getFamiliesWhereSpouse();
        for (int i = 0; families != null && i < families.length; i++) {
            Fam family = families[i];
            getPropertiesRecursively((Property) family, sources, PropertySource.class);
        }

        List<Source> sourcesOutput = new ArrayList<>();
        for (Property propSrc : sources) {
            if (propSrc instanceof PropertySource) {
                PropertySource pSource = (PropertySource) propSrc;
                if (pSource.getTargetEntity() != null) {
                    Source src = (Source) pSource.getTargetEntity();
                    if (!sourcesOutput.contains(src)) {
                        sourcesOutput.add(src);
                    }
                }
            }
        }

        Collections.sort(sourcesOutput, sortSources);
        return sourcesOutput;
    }

    /**
     * Extract the first number bit in the string going from left to right
     */
    public int extractNumber(String str) {

        int start = 0, end = 0;
        while (start <= end && !Character.isDigit(str.charAt(start))) {
            start++;
        }
        end = start;
        while ((end <= str.length() - 1) && Character.isDigit(str.charAt(end))) {
            end++;
        }
        if (end == start) {
            return 0;
        } else {
            return Integer.parseInt(str.substring(start, end));
        }
    }
    /**
     * Comparator to sort Individuals
     */
    public Comparator<Source> sortSources = new Comparator<Source>() {

        @Override
        public int compare(Source o1, Source o2) {
            return (extractNumber(o1.getId()) - extractNumber(o2.getId()));
        }
    };

    /**
     * Return sorted list of cities of Gedcom file Cities are sorted according
     * to their anchor-compatible equivallent strings (A-Z a-z '-' characters
     * only)
     */
    @SuppressWarnings("unchecked")
    public List<String> getCities(Gedcom gedcom) {
        if (!initCity) {
            initCity = buildCitiesList(gedcom, new Comparator<String>() {

                @Override
                public int compare(String t1, String t2) {
                    return (t1.compareTo(t2));
                }
            });
        }
        return new ArrayList<>((Collection) listOfCities.keySet());
    }

    public int getTotalCitiesCount() {
        if (!initCity) {
            initCity = buildCitiesList(gedcom, new Comparator<String>() {

                @Override
                public int compare(String t1, String t2) {
                    return (t1.compareTo(t2));
                }
            });
        }
        return listOfCities.size();
    }

    public int getCitiesCount(String city) {
        if (listOfCities.get(city) == null) {
            return -1;
        }
        Info infoCity = listOfCities.get(city);
        return (int) infoCity.counter;
    }

    @SuppressWarnings("unchecked")
    public List<Property> getCitiesProps(String city) {
        if (listOfCities.get(city) == null) {
            return null;
        }
        Info infoCity = listOfCities.get(city);
        return infoCity.props;
    }

    private boolean buildCitiesList(Gedcom gedcom, Comparator<String> sortStrings) {

        Collection<Entity> entities = gedcom.getEntities();
        List<Property> placesProps = new ArrayList<>();
        for (Entity ent : entities) {
            getPropertiesRecursively(ent, placesProps, PropertyPlace.class);
        }

        listOfCities = new TreeMap<>(sortStrings);
        String juridic = "";
        for (Property prop : placesProps) {
            if (prop instanceof PropertyPlace) {
                juridic = Normalizer.normalize(((PropertyPlace) prop).getCity().trim(), Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");  // convert accents
            } else {
                break;
            }
            if (juridic != null && juridic.length() > 0) {
                juridic = juridic.toLowerCase(); // Don't distinguish upper and lower case.
                Integer val = null;
                List<Property> listProps = null;
                Info infoCity = listOfCities.get(juridic);
                if (infoCity == null) {
                    val = 0;
                    listProps = new ArrayList<>();
                    infoCity = new Info();
                } else {
                    val = infoCity.counter;
                    listProps = infoCity.props;
                }
                val += 1;
                listProps.add(prop);
                infoCity.counter = val;
                infoCity.props = listProps;
                listOfCities.put(juridic, infoCity);
            }
        }
        return true;
    }

    public <P extends Property> void getPropertiesRecursively(Property parent, List<Property> props, Class<P> clazz) {
        Property[] children = parent.getProperties();
        for (Property child : children) {
            props.addAll(child.getProperties(clazz));
            getPropertiesRecursively(child, props, clazz);
        }
    }

    /**
     * Return sorted list of days in the year for Gedcom file Days are sorted
     * according to their anchor-compatible equivallent strings (A-Z a-z '-'
     * characters only)
     */
    @SuppressWarnings("unchecked")
    public List<String> getDays(Gedcom gedcom) {
        if (!initDay) {
            initDay = buildDaysList(gedcom);
        }
        return new ArrayList<>((Collection) listOfDays.keySet());
    }

    public int getDaysCount(String day) {
        if (listOfDays.get(day) == null) {
            return -1;
        }
        Info infoDay = listOfDays.get(day);
        return (int) infoDay.counter;
    }

    public List<Property> getDaysProps(String day) {
        if (listOfDays.get(day) == null) {
            return null;
        }
        Info infoDay = listOfDays.get(day);
        return infoDay.props;
    }

    private boolean buildDaysList(Gedcom gedcom) {

        listOfDays = new TreeMap<>();
        Collection<Entity> entities = gedcom.getEntities();
        List<Property> datesProps = new ArrayList<>();
        for (Entity ent : entities) {
            getPropertiesRecursively(ent, datesProps, PropertyDate.class);
        }

        String day = "";
        for (Property prop : datesProps) {
            day = getDay(prop);
            if (day != null) {
                Integer val = null;
                List<Property> listProps = null;
                Info infoDay = listOfDays.get(day);
                if (infoDay == null) {
                    val = 0;
                    listProps = new ArrayList<>();
                    infoDay = new Info();
                } else {
                    val = infoDay.counter;
                    listProps = infoDay.props;
                }
                val += 1;
                listProps.add(prop);
                infoDay.counter = val;
                infoDay.props = listProps;
                listOfDays.put(day, infoDay);
            }
        }
        return true;
    }

    public String getDay(Property prop) {
        if (!(prop instanceof PropertyDate)) {
            return null;
        }
        PropertyDate date = (PropertyDate) prop;
        if (!date.isValid() || date.isRange()) {
            return null;
        }
        PointInTime pit = null;
        try {
            pit = date.getStart().getPointInTime(PointInTime.GREGORIAN);
            if (pit.getMonth() < 0 || pit.getMonth() > 11) {
                return null;
            }
            if (pit.getDay() < 0 || pit.getDay() > 30) {
                return null;
            }
            return String.format("%02d", pit.getMonth() + 1) + String.format("%02d", pit.getDay() + 1);
        } catch (GedcomException e) {
            // e.printStackTrace();
            //log.write(log.ERROR, "getDay - " + e.getMessage());
        }
        return null;
    }

    /**
     * Get Ancestors
     */
    public List<Ancestor> getAncestorsList(Indi rootIndi) {
        if (!initAncestors) {
            initAncestors = buildAncestors(rootIndi);
        }
        return listOfAncestors;
    }

    public Set<Indi> getAncestors(Indi rootIndi) {
        if (!initAncestors) {
            initAncestors = buildAncestors(rootIndi);
        }
        Set<Indi> list = new HashSet<>();
        for (Ancestor ancestor : listOfAncestors) {
            list.add(ancestor.indi);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private boolean buildAncestors(Indi rootIndi) {
        // Depending on option, start at sosa number 1 or if option says 0, the one of the individual selected
        BigInteger startSosa = BigInteger.ONE;

        // Run recursion
        List<Ancestor> list = new ArrayList<>(1);
        list.add(new Ancestor(startSosa, rootIndi, 1));
        recursion(list, 1);

        Collections.sort(listOfAncestors, sortAncestors);
        return true;
    }

    /**
     * Recurse over a generation list up to the maximum number of generations
     *
     * @param generation the current generation (sosa,indi) - the list of all
     * individuals in that generation
     * @param gen the current generation
     */
    @SuppressWarnings("unchecked")
    void recursion(List<Ancestor> generation, int gen) {

        // Build next generation (scan individuals in that generation and build next one)
        List<Ancestor> nextGeneration = new ArrayList<>();

        for (Ancestor ances : generation) {
            // next biplet
            BigInteger sosa = ances.sosa;
            Indi indi = ances.indi;

            // grab father and mother
            Fam famc = indi.getFamilyWhereBiologicalChild();
            if (famc != null) {
                Indi father = famc.getHusband();
                if (father != null) {
                    nextGeneration.add(new Ancestor(sosa.shiftLeft(1), father, gen + 1));
                }
                Indi mother = famc.getWife();
                if (mother != null) {
                    Ancestor aMother = new Ancestor(sosa.shiftLeft(1).add(BigInteger.ONE), mother, gen + 1);
                    nextGeneration.add(aMother);
                }
            }
            listOfAncestors.add(ances);
        }

        // Recurse into next generation
        if (!nextGeneration.isEmpty()) {
            recursion(nextGeneration, gen + 1);
        }
    }
    Comparator<Ancestor> sortAncestors = new Comparator<Ancestor>() {

        @Override
        public int compare(Ancestor a1, Ancestor a2) {
            return a1.sosa.compareTo(a2.sosa);
        }
    };

    /**
     * Get Cousins
     */
    public Set<Indi> getCousins(Indi rootIndi) {
        if (!initCousins) {
            initCousins = buildCousins(rootIndi);
        }
        return listOfCousins;
    }

    @SuppressWarnings("unchecked")
    private boolean buildCousins(Indi rootIndi) {
        // declarations
        Collection<Indi> indis = (Collection<Indi>) rootIndi.getGedcom().getEntities(Gedcom.INDI);
        Set<Indi> ancestors = getAncestors(rootIndi);
        Set<Indi> otherIndis = new HashSet<>();

        // get all non ancestors
        for (Indi indi : indis) {
            if (!ancestors.contains(indi)) {
                otherIndis.add(indi);
            }
        }

        // Get cousins now by flaging all non ancestors that are descendants of ancestors
        for (Iterator<Indi> it = ancestors.iterator(); it.hasNext();) {
            Indi ancestor = it.next();
            Set<Indi> descendants = new HashSet<Indi>();
            getDescendants(ancestor, otherIndis, descendants);
            listOfCousins.addAll(descendants);
            otherIndis.removeAll(descendants);
        }
        return true;
    }

    private void getDescendants(Indi ancestor, Set<Indi> inSet, Set<Indi> descendants) {
        Indi[] children = ancestor.getChildren();
        for (int i = 0; i < children.length; i++) {
            Indi indi = children[i];
            if (!inSet.contains(indi)) {
                continue;
            }
            descendants.add(indi);
            inSet.remove(indi);
            getDescendants(indi, inSet, descendants);
        }
        return;
    }
    // Privacy policy
    private PrivacyPolicy privacyPolicy = null;
    //

    public PrivacyPolicy getPrivacyPolicy() {
        if (privacyPolicy == null) {
            privacyPolicy = PrivacyPolicy.getDefault();
        }
        return privacyPolicy;
    }

    /**
     * Check if entity is private Individual : see above Family : private if
     * either husband or wife is private
     */
    public boolean isPrivate(Entity ent) {
        if (ent instanceof Indi) {
            return isPrivate((Property) ent);
        }
        if (ent instanceof Fam) {
            Fam famRel = (Fam) ent;
            Indi husband = famRel.getHusband();
            Indi wife = famRel.getWife();
            return isPrivate((Property) husband) || isPrivate((Property) wife);
        }
        return false;
    }

    /**
     * Check if property is private
     */
    public boolean isPrivate(Property prop) {
        return ((prop != null) && (getPrivacyPolicy().isPrivate(prop)));
    }

    public String getPrivDisplay() {
        return getPrivacyPolicy().getPrivateMask();
    }
} // End_of_Report

