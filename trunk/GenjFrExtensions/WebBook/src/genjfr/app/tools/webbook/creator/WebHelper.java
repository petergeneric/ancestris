package genjfr.app.tools.webbook.creator;

import genj.gedcom.Indi;
import genj.gedcom.Fam;
import genj.gedcom.Source;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertySource;
import genj.gedcom.time.PointInTime;
import genj.gedcom.GedcomException;

import java.io.*;


import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.image.BufferedImage;

import com.sun.image.codec.jpeg.*;

import genj.gedcom.PrivacyPolicy;
import genjfr.app.App;
import genjfr.app.tools.webbook.Log;
import genjfr.app.tools.webbook.WebBookParams;
import genjfr.app.tools.webbook.transfer.FTPRegister;
import java.nio.charset.Charset;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

/**
 * Ancestris
 *
 * Tools for WebBook:
 * - file and directory manipulation
 * - gedcom sets
 * - misc
 *
 *
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 0.1
 */
public class WebHelper {

    public Gedcom gedcom;
    public Log log;
    public WebBookParams wp;
    //
    public Indi indiDeCujus = null;
    private List individualsList = null;
    private List sourcesList = null;
    public PrivacyPolicy privacyPolicy = null;
    private FTPRegister uploadRegister = null;
    /**
     * Variables
     */
    //
    //
    public final String SOSA_TAG = "_SOSA";
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
    private List<Ancestor> listOfAncestors = new ArrayList<Ancestor>();
    private boolean initAncestors = false;
    private Set<Indi> listOfCousins = new HashSet<Indi>();
    private boolean initCousins = false;

    /***************************************************************************
     * CONSTRUCTOR
     */
    public WebHelper(Gedcom gedcom, Log log, WebBookParams wp) {
        this.gedcom = gedcom;
        this.log = log;
        this.wp = wp;
    }

    /***************************************************************************
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
     * Create Dir
     **///USED
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
                }
            }
        }
        File f = new File(outfile);
        if (create) {
            f.mkdir();
        }
        return (f);
    }

    /**
     * Generates file name
     *///USED
    public File getFileForName(File dir, String fileName) {
        return new File(dir, fileName);
    }

    /**
     * Helper - Create a PrintWriter wrapper for output stream
     *///USED
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
     * Empty Dir
     **/
    public boolean emptyDir(File dir, boolean removeDir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = emptyDir(new File(dir, children[i]), true);
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        if (removeDir) {
            return dir.delete();
        }
        return true;
    }

    /**
     * Clean file name from drive letters (windows), colon ":", starting file separators and spaces
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
        StringBuffer strOutput = new StringBuffer(1000);
        for (int i = 0; i < charInput.length; i++) {
            strOutput.append(convertChar(charInput[i], false, defchar));
        }
        return strOutput.toString();
    }

    public String convertChar(char c, boolean isAnchor, String defchar) {
        String str = null;
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
                if (str.matches("[a-zA-Z0-9]")) {
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
     * Get title of a media
     */
    public String getTitle(PropertyFile media, String defchar) {
        Property ptitle = media.getParent().getProperty("TITL");
        if (ptitle != null && ptitle.toString().length() != 0) {
            return ptitle.toString();
        }
        File file = media.getFile();
        if (file != null) {
            String filename = file.getName();
            return getCleanFileName(filename, defchar);
        }
        String str = media.toString();
        return str.substring(str.lastIndexOf(File.separator) + 1);
    }

    /**
     * Copy method for resource copy to file given by string
     */
    public void copy(String from_name, String to_name) throws IOException {
        File to_file = new File(to_name);
        FileOutputStream fos = new FileOutputStream(to_file);
        FileUtil.copy(wp.getClass().getResourceAsStream(from_name), fos);
        if (uploadRegister != null) {
            uploadRegister.update(to_file);
        }
        fos.close();
    }

    /**
     * ReadStream method from resource to string
     */
    public String readStream(String from_name) throws IOException {
        InputStream is = wp.getClass().getResourceAsStream(from_name);
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        is.close();
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
        boolean IS_OSX = OS_NAME.startsWith("mac os x");
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
        } catch (Exception e) {
            //e.printStackTrace();
            log.write(log.ERROR, "copy - " + e.getMessage());
        }

        // Update register
        if (uploadRegister != null) {
            uploadRegister.update(to_file);
        }


    }

    /** A convenience method to throw an exception */
    private void abort(String msg) throws IOException {
        log.write(log.ERROR, "abort - " + msg);
        throw new IOException(msg);
    }

    /** Detect image */
    public boolean isImage(String infile) {
        if (infile == null) {
            return false;
        }
        return (infile.toLowerCase().endsWith(".jpg") || infile.toLowerCase().endsWith(".png") || infile.toLowerCase().endsWith(".gif"));
    }

    /** Get image size */
    String getImageSize(String absoluteFile) {
        Image image = Toolkit.getDefaultToolkit().getImage(absoluteFile);
        mediaTracker.addImage(image, 0);

        try {
            mediaTracker.waitForID(0);
        } catch (Exception e) {
            //e.printStackTrace();
            return "100','100";
        }

        int imageHeight = image.getHeight(null);
        int imageWidth = image.getWidth(null);
        mediaTracker.removeImage(image);
        image.flush();
        return "" + imageWidth + "','" + imageHeight;
    }

    /** Scale image */
    public boolean scaleImage(String infile, String outfile, int width, int height, int quality) {
        return scaleImage(infile, outfile, width, height, quality, true);
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
        } catch (Exception e) {
            //e.printStackTrace();
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

        BufferedOutputStream out = null;
        try {
            // draw original image to thumbnail image object and
            // scale it to the new size on-the-fly
            BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = thumbImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

            createDir(outfile, false);

            // save thumbnail image to OUTFILE
            out = new BufferedOutputStream(new FileOutputStream(outfile));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
            int qual = Math.max(0, Math.min(quality, 100));
            param.setQuality((float) qual / 100.0f, false);
            encoder.setJPEGEncodeParam(param);
            encoder.encode(thumbImage);
            out.close();
            result = true;
        } catch (Exception e) {
            // e.printStackTrace();
            log.write(log.ERROR, "scaleImage (encoding) - " + e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.write(log.ERROR, "scaleImage (out) - " + e.getMessage());
                }
            }
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
     **/
    public String readFile(String filename) throws IOException {

        if ((filename == null) || (filename.length() == 0)) {
            return "";
        }
        File f;
        FileInputStream in = null;
        StringBuffer sb = new StringBuffer("");

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
     **/
    public boolean writeFile(String filename, String text) throws IOException {
        if ((filename == null) || (filename.length() == 0)) {
            return false;
        }
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            out.write(text);
            out.close();
        } catch (IOException e) {
            log.write(log.ERROR, "writeFile - " + e.getMessage());
            return false;
        }
        return true;
    }

    /***************************************************************************
     * TOOLS FOR GEDCOM SETS
     */
    //
    /**
     * Returns nb of individuals
     *///USED
    public int getNbIndis() {
        return gedcom.getEntities(Gedcom.INDI, "INDI:NAME").length;
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
        for (int i = 0; i < indis.length; i++) {
            Indi indi = (Indi) indis[i];
            if (indi.toString().equals(str)) {
                indiDeCujus = indi;
                break;
            }
        }
        return indiDeCujus;
    }

    /**
     * Privacy policy
     */
    public PrivacyPolicy getPrivacyPolicy() {
        if (privacyPolicy == null) {
            privacyPolicy = new PrivacyPolicy(
                    NbPreferences.forModule(App.class).get("privAlive", "").equals("true"), // boolean
                    new Integer(NbPreferences.forModule(App.class).get("privYears", "")), //int
                    NbPreferences.forModule(App.class).get("privFlag", ""));
        }
        return privacyPolicy;
    }

    /**
     * Get sosa if available
     */
    public String getSosa(Indi indi) {
        if (indi == null) {
            return "";
        }
        Property prop = indi.getProperty(SOSA_TAG);
        if (prop == null) {
            return "";
        }
        String str = prop.getDisplayValue();
        while (str.startsWith("0")) {
            str = str.substring(1);
        }
        return str;
    }

    /**
     * Return sorted list of lastnames of Gedcom file
     * Do NOT use getLastNames() function of genj because it returns all lastnames found and we only want one last name per person here
     * (Genj itself only accesses individuals using the first lastname found for them)
     */
    @SuppressWarnings("unchecked")
    public List<String> getLastNames(String defchar, Comparator sortLastnames) {
        if (!initLastname) {
            initLastname = buildLastnamesList(gedcom, defchar, sortLastnames);
        }
        return (List) new ArrayList((Collection) listOfLastnames.keySet());
    }

    public int getTotalNamesCount() {
        if (!initLastname) {
            initLastname = buildLastnamesList(gedcom, DEFCHAR, new Comparator() {

                public int compare(Object t1, Object t2) {
                    return ((String) t1).compareTo(((String) t2));
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
    private boolean buildLastnamesList(Gedcom gedcom, String defchar, Comparator sortLastnames) {
        listOfLastnames = new TreeMap<String, Integer>(sortLastnames);
        List indis = new ArrayList(gedcom.getEntities(Gedcom.INDI));
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
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
        return (str == null ? defchar : str.length() == 0 ? defchar : str);
    }

    /**
     * Return sorted list of individuals (Indi) of Gedcom file
     * Lastnames are sorted according to their anchor-compatible equivallent strings (A-Z a-z '-' characters only)
     */
    @SuppressWarnings("unchecked")
    public List<Indi> getIndividuals(Gedcom gedcom, Comparator sort) {
        Comparator sortIndividuals = sort;
        if (sortIndividuals == null) {
            sortIndividuals = new Comparator() {

                public int compare(Object t1, Object t2) {
                    return ((Indi) t1).compareTo(((Indi) t2));
                }
            };
        }
        if (individualsList == null) {
            individualsList = new ArrayList<Indi>(gedcom.getEntities(Gedcom.INDI));
            Collections.sort(individualsList, sortIndividuals);
        }
        return individualsList;
    }

    /**
     * Return sorted list of sources of Gedcom file
     * Sources are sorted by codes
     */
    @SuppressWarnings("unchecked")
    public List<Entity> getSources(Gedcom gedcom) {
        if (sourcesList == null) {
            sourcesList = new ArrayList(gedcom.getEntities(Gedcom.SOUR));
            Collections.sort(sourcesList, sortSources);
        }
        return sourcesList;
    }

    /**
     * Return sorted list of sources of entity
     * Sources are sorted by codes
     */
    @SuppressWarnings("unchecked")
    public List<Source> getSources(Indi indi) {
        // get sources of individual
        List<Property> sources = new ArrayList<Property>();
        getPropertiesRecursively((Property) indi, sources, PropertySource.class);

        // get sources of the associated families
        Fam[] families = indi.getFamiliesWhereSpouse();
        for (int i = 0; families != null && i < families.length; i++) {
            Fam family = families[i];
            getPropertiesRecursively((Property) family, sources, PropertySource.class);
        }

        List<Source> sourcesOutput = new ArrayList<Source>();
        for (Iterator<Property> s = sources.iterator(); s.hasNext();) {
            Property propSrc = s.next();
            if (propSrc instanceof PropertySource) {
                PropertySource pSource = (PropertySource) propSrc;
                if (pSource != null && pSource.getTargetEntity() != null) {
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
    public Comparator sortSources = new Comparator() {

        public int compare(Object o1, Object o2) {
            Source src1 = (Source) o1;
            Source src2 = (Source) o2;
            return (extractNumber(src1.getId()) - extractNumber(src2.getId()));
        }
    };

    /**
     * Return sorted list of cities of Gedcom file
     * Cities are sorted according to their anchor-compatible equivallent strings (A-Z a-z '-' characters only)
     */
    @SuppressWarnings("unchecked")
    public List<String> getCities(Gedcom gedcom) {
        if (!initCity) {
            initCity = buildCitiesList(gedcom, new Comparator() {

                public int compare(Object t1, Object t2) {
                    return ((String) t1).compareTo(((String) t2));
                }
            });
        }
        return (List) new ArrayList((Collection) listOfCities.keySet());
    }

    public int getTotalCitiesCount() {
        if (!initCity) {
            initCity = buildCitiesList(gedcom, new Comparator() {

                public int compare(Object t1, Object t2) {
                    return ((String) t1).compareTo(((String) t2));
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

    @SuppressWarnings("unchecked")
    private boolean buildCitiesList(Gedcom gedcom, Comparator sortStrings) {

        Collection entities = gedcom.getEntities();
        List<PropertyPlace> placesProps = new ArrayList<PropertyPlace>();
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Entity ent = (Entity) it.next();
            getPropertiesRecursively((Property) ent, placesProps, PropertyPlace.class);
        }

        listOfCities = new TreeMap<String, Info>(sortStrings);
        String juridic = "";
        for (Iterator it = placesProps.iterator(); it.hasNext();) {
            Property prop = (Property) it.next();
            if (prop instanceof PropertyPlace) {
                juridic = ((PropertyPlace) prop).getCity().trim();
            } else {
                break;
            }
            if (juridic != null && juridic.length() > 0) {
                Integer val = null;
                List<Property> listProps = null;
                Info infoCity = listOfCities.get(juridic);
                if (infoCity == null) {
                    val = 0;
                    listProps = new ArrayList<Property>();
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

    @SuppressWarnings("unchecked")
    public void getPropertiesRecursively(Property parent, List props, Class clazz) {
        Property[] children = parent.getProperties();
        for (int c = 0; c < children.length; c++) {
            Property child = children[c];
            props.addAll(child.getProperties(clazz));
            getPropertiesRecursively(child, props, clazz);
        }
    }

    /**
     * Return sorted list of days in the year for Gedcom file
     * Days are sorted according to their anchor-compatible equivallent strings (A-Z a-z '-' characters only)
     */
    @SuppressWarnings("unchecked")
    public List<String> getDays(Gedcom gedcom) {
        if (!initDay) {
            initDay = buildDaysList(gedcom);
        }
        return (List<String>) new ArrayList((Collection) listOfDays.keySet());
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

        listOfDays = new TreeMap<String, Info>();
        Collection entities = gedcom.getEntities();
        List<Property> datesProps = new ArrayList<Property>();
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Entity ent = (Entity) it.next();
            getPropertiesRecursively((Property) ent, datesProps, PropertyDate.class);
        }

        String day = "";
        for (Iterator it = datesProps.iterator(); it.hasNext();) {
            Property prop = (Property) it.next();
            day = getDay(prop);
            if (day != null) {
                Integer val = null;
                List<Property> listProps = null;
                Info infoDay = listOfDays.get(day);
                if (infoDay == null) {
                    val = 0;
                    listProps = new ArrayList<Property>();
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
        Set<Indi> list = new HashSet<Indi>();
        for (Iterator it = listOfAncestors.iterator(); it.hasNext();) {
            Ancestor ancestor = (Ancestor) it.next();
            list.add(ancestor.indi);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private boolean buildAncestors(Indi rootIndi) {
        // Depending on option, start at sosa number 1 or if option says 0, the one of the individual selected
        int startSosa = 1;

        // Run recursion
        List list = new ArrayList(3);
        list.add(new Integer(startSosa));
        list.add(rootIndi);
        Fam[] fams = rootIndi.getFamiliesWhereSpouse();
        if ((fams != null) && (fams.length > 0)) {
            list.add(fams[0]);
        } else {
            list.add(null);
        }
        recursion(list, 1);

        Collections.sort(listOfAncestors, sortAncestors);
        return true;
    }

    /**
     * Recurse over a generation list up to the maximum number of generations
     * @param generation the current generation (sosa,indi,fam) - the list of all individuals in that generation
     * @param gen the current generation
     */
    @SuppressWarnings("unchecked")
    void recursion(List generation, int gen) {

        // Build mext generation (scan individuals in that generation and build next one)
        List nextGeneration = new ArrayList();

        for (int i = 0; i < generation.size();) {
            // next triplet
            int sosa = ((Integer) generation.get(i++)).intValue();
            Indi indi = (Indi) generation.get(i++);
            Fam fam = (Fam) generation.get(i++);

            // grab father and mother
            Fam famc = indi.getFamilyWhereBiologicalChild();
            if (famc != null) {
                Indi father = famc.getHusband();
                if (father != null) {
                    nextGeneration.add(new Integer(sosa * 2));
                    nextGeneration.add(father);
                    nextGeneration.add(famc);
                }
                Indi mother = famc.getWife();
                if (mother != null) {
                    nextGeneration.add(new Integer(sosa * 2 + 1));
                    nextGeneration.add(mother);
                    nextGeneration.add(famc);
                }
            }
        }

        // store ancestor information
        for (int i = 0; i < generation.size();) {
            int sosa = ((Integer) generation.get(i++)).intValue();
            Indi indi = (Indi) generation.get(i++);
            i++;
            Ancestor ancestor = new Ancestor();
            ancestor.sosa = sosa;
            ancestor.gen = gen;
            ancestor.indi = indi;
            listOfAncestors.add(ancestor);
        }

        // Recurse into next generation
        if (!nextGeneration.isEmpty()) {
            recursion(nextGeneration, gen + 1);
        }
    }
    Comparator sortAncestors = new Comparator() {

        public int compare(Object o1, Object o2) {
            Ancestor a1 = (Ancestor) o1;
            Ancestor a2 = (Ancestor) o2;
            return a1.sosa - a2.sosa;
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
        List indis = new ArrayList(rootIndi.getGedcom().getEntities(Gedcom.INDI));
        Set ancestors = getAncestors(rootIndi);
        Set otherIndis = new HashSet();

        // get all non ancestors
        for (Iterator it = indis.iterator(); it.hasNext();) {
            Indi indi = (Indi) it.next();
            if (!ancestors.contains(indi)) {
                otherIndis.add(indi);
            }
        }

        // Get cousins now by flaging all non ancestors that are descendants of ancestors
        for (Iterator it = ancestors.iterator(); it.hasNext();) {
            Indi ancestor = (Indi) it.next();
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

    /**
     * Check if individual or entity
     */
    public boolean isPrivate(Indi indi) {
        return ((indi != null) && (indi.getBirthDate() != null) && (getPrivacyPolicy().isPrivate(indi)));
    }

    public boolean isPrivate(Entity ent) {
        if (ent instanceof Indi) {
            return isPrivate((Indi) ent);
        }
        if (ent instanceof Fam) {
            Fam famRel = (Fam) ent;
            Indi husband = famRel.getHusband();
            Indi wife = famRel.getWife();
            return isPrivate(husband) || isPrivate(wife);
        }
        return false;
    }
} // End_of_Report



