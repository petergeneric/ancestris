/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.webbook.creator;

import genjfr.app.tools.webbook.WebBook;
import genjfr.app.tools.webbook.WebBookParams;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.openide.util.Exceptions;

/**
 *
 * @author frederic
 */
public class WebTheme extends WebSection {

    /**
     * Constructor
     */
    public WebTheme(boolean generate, WebBook wb, WebBookParams wp, WebHelper wh) {
        super(generate, wb, wp, wh);
    }

    /**
     * Section's entry point
     */
    @Override
    public void create() {

        // create directory
        File dir = wh.createDir(wh.getDir().getAbsolutePath() + File.separator + themeDir, true);

        // create directory
        wh.emptyDir(dir, false);

        // copy icons and style sheet
        createFiles(dir);

        // done
        wh.log.write(themeDir + trs("EXEC_DONE"));
    }

    /**
     * Helper - Create icons
     */
    public void createFiles(File dir) {

        // Get male and female icons in the dierctory
        String genjImagesDir = "genjfr/app/tools/webbook/img/";
        String toFile = dir.getAbsolutePath() + File.separator;

        try {
            // get resource directory where images are
            URL dirURL = wp.getClass().getClassLoader().getResource(wp.getClass().getName().replace(".", "/") + ".class");
            if (dirURL.getProtocol().equals("jar")) {
                /* A JAR path */
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
                JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
                Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith(genjImagesDir)) { //filter according to the path
                        String entry = name.substring(genjImagesDir.length());
                        int checkSubdir = entry.indexOf("/");
                        if (checkSubdir < 0 && !entry.trim().isEmpty()) {
                            // if it is NOT a subdirectory, it must be an image so copy it
                            result.add(entry);
                        }
                    }
                }

                String[] list = result.toArray(new String[result.size()]);
                for (int i = 0; i < list.length; i++) {
                    String fileName = list[i];
                    wh.copy("img/" + fileName, toFile + fileName);
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            wb.log.write(wb.log.ERROR, "createIcons - " + e.getMessage());
        }
    }
}
