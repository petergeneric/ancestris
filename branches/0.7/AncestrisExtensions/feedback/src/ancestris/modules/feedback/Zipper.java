/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package ancestris.modules.feedback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
//import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Model for export/import options. It reads {@code OptionsExport/<category>/<item>}
 * from layers and evaluates whether items are applicable for export/import.
 *
 * @author Jiri Skrivanek
 */
public final class Zipper {

    private static final Logger LOGGER = Logger.getLogger(Zipper.class.getPackage().getName());
    /** Source of export/import (zip file or userdir) */
    private File source;
    /** Cache of paths relative to source root */
    List<String> relativePaths;
    /** Target ZipOutputStream for export. */
    private ZipOutputStream zipOutputStream;
    /** Include patterns. */
    private Set<String> includePatterns = new HashSet<String>();
    /** Exclude patterns. */
    private Set<String> excludePatterns = new HashSet<String>();
    /** Properties currently being copied. */
    private EditableProperties currentProperties;
    /** List of ignored folders in userdir. It speeds up folder scanning. */
    private static final List<String> IGNORED_FOLDERS = Arrays.asList("var/cache");  // NOI18N

    /** Returns instance of export options model.
     * @param source source of export/import. It is either zip file or userdir
     * @return instance of export options model
     */
    public Zipper(File source) {
        this.source = source;
    }

    /** Creates zip file according to current state of model, i.e. only
     * include/exclude patterns from enabled items are copied from source userdir.
     * @param targetZipFile target zip file
     */
    void doExport(File targetZipFile) {
        try {
            ensureParent(targetZipFile);
            // Create the ZIP file
            zipOutputStream = new ZipOutputStream(new FileOutputStream(targetZipFile));
            copyFiles();
            createProductInfo(zipOutputStream);
            // Complete the ZIP file
            zipOutputStream.close();
        } catch (IOException ex) {
            Exceptions.attachLocalizedMessage(ex,
                    NbBundle.getMessage(Zipper.class, "OptionsExportModel.export.zip.error", targetZipFile));
            Exceptions.printStackTrace(ex);
        } finally {
            if (zipOutputStream != null) {
                try {
                    zipOutputStream.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    private static enum ParserState {

        START,
        IN_KEY_PATTERN,
        AFTER_KEY_PATTERN,
        IN_BLOCK
    }

    /** Parses given compound string pattern into set of single patterns.
     * @param pattern compound pattern in form filePattern1#keyPattern1#|filePattern2#keyPattern2#|filePattern3
     * @return set of single patterns containing just one # (e.g. [filePattern1#keyPattern1, filePattern2#keyPattern2, filePattern3])
     */
    static Set<String> parsePattern(String pattern) {
        Set<String> patterns = new HashSet<String>();
        if (pattern.contains("#")) {  //NOI18N
            StringBuilder partPattern = new StringBuilder();
            ParserState state = ParserState.START;
            int blockLevel = 0;
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                switch(state) {
                    case START:
                        if (c == '#') {
                            state = ParserState.IN_KEY_PATTERN;
                            partPattern.append(c);
                        } else if (c == '(') {
                            state = ParserState.IN_BLOCK;
                            blockLevel++;
                            partPattern.append(c);
                        } else if (c == '|') {
                            patterns.add(partPattern.toString());
                            partPattern = new StringBuilder();
                        } else {
                            partPattern.append(c);
                        }
                        break;
                    case IN_KEY_PATTERN:
                        if (c == '#') {
                            state = ParserState.AFTER_KEY_PATTERN;
                        } else {
                            partPattern.append(c);
                        }
                        break;
                    case AFTER_KEY_PATTERN:
                        if (c == '|') {
                            state = ParserState.START;
                            patterns.add(partPattern.toString());
                            partPattern = new StringBuilder();
                        } else {
                            assert false : "Wrong OptionsExport pattern " + pattern + ". Only format like filePattern1#keyPattern#|filePattern2 is supported.";  //NOI18N
                        }
                        break;
                    case IN_BLOCK:
                        partPattern.append(c);
                        if (c == ')') {
                            blockLevel--;
                            if (blockLevel == 0) {
                                state = ParserState.START;
                            }
                        }
                        break;
                }
            }
            patterns.add(partPattern.toString());
        } else {
            patterns.add(pattern);
        }
        return patterns;
    }

    public void addIncludePatterns(String include){
        if (includePatterns == null) {
            includePatterns = new HashSet<String>();
        }
        includePatterns.addAll(parsePattern(include));
    }

    public void addExcludePatterns(String exclude){
        if (excludePatterns == null) {
            excludePatterns = new HashSet<String>();
        }
        excludePatterns.addAll(parsePattern(exclude));
    }


    /** Just for debugging. */
    @Override
    public String toString() {
        return getClass().getName() + " source=" + source;  //NOI18N
    }


    /** Copy files from source (zip or userdir) into target userdir or fip file
     * according to current state of model. i.e. only include/exclude patterns from
     * enabled items are considered.
     * @throws IOException if copying fails
     */
    private void copyFiles() throws IOException {
        if (source.isDirectory()) {
            // userdir
            copyFolder(source);
        } else {
            copyFile(getRelativePath(source, source));
        }
    }

    /** Copy given folder to target userdir or zip file obeying include/exclude patterns.
     * @param file folder to copy
     * @throws IOException if copying fails
     */
    private void copyFolder(File file) throws IOException {
        String relativePath = getRelativePath(source, file);
        if (IGNORED_FOLDERS.contains(relativePath)) {
            return;
        }
        File[] children = file.listFiles();
        if (children == null || children.length == 0) {
                copyFile(getRelativePath(source, file));
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                copyFolder(child);
            } else {
                copyFile(getRelativePath(source, child));
            }
        }
    }

    /** Returns list of file path relative to given source root. It scans
     * sub folders recursively.
     * @param sourceRoot source root
     * @return list of file path relative to given source root
     */
    static List<String> getRelativePaths(File sourceRoot) {
        return getRelativePaths(sourceRoot, sourceRoot);
    }

    private static List<String> getRelativePaths(File root, File file) {
        String relativePath = getRelativePath(root, file);
        List<String> result = new ArrayList<String>();
        if (file.isDirectory()) {
            if (IGNORED_FOLDERS.contains(relativePath)) {
                return result;
            }
            File[] children = file.listFiles();
            if (children == null) {
                return Collections.emptyList();
            }
            for (File child : children) {
                result.addAll(getRelativePaths(root, child));
            }
        } else {
            result.add(relativePath);
        }
        return result;
    }

    /** Returns slash separated path relative to given root. */
    private static String getRelativePath(File root, File file) {
        String result = file.getAbsolutePath().substring(root.getAbsolutePath().length());
        result = result.replace('\\', '/');  //NOI18N
        if (result.startsWith("/") && !result.startsWith("//")) {  //NOI18N
            result = result.substring(1);
        }
        return result;
    }

    /** Returns set of keys matching given pattern.
     * @param relativePath path relative to sourceRoot
     * @param propertiesPattern pattern like file.properties#keyPattern
     * @return set of matching keys, never null
     * @throws IOException if properties cannot be loaded
     */
    private Set<String> matchingKeys(String relativePath, String propertiesPattern) throws IOException {
        Set<String> matchingKeys = new HashSet<String>();
        String[] patterns = propertiesPattern.split("#", 2);
        String filePattern = patterns[0];
        String keyPattern = patterns[1];
        if (relativePath.matches(filePattern)) {
            if (currentProperties == null) {
                currentProperties = getProperties(relativePath);
            }
            for (String key : currentProperties.keySet()) {
                if (key.matches(keyPattern)) {
                    matchingKeys.add(key);
                }
            }
        }
        return matchingKeys;
    }

    /** Copy file given by relative path from source zip or userdir to target
     * userdir or zip file. It creates necessary sub folders.
     * @param relativePath relative path
     * @throws java.io.IOException if copying fails
     */
    private void copyFile(String relativePath) throws IOException {
        currentProperties = null;
        boolean includeFile = false;  // include? entire file
        Set<String> includeKeys = new HashSet<String>();
        Set<String> excludeKeys = new HashSet<String>();
        for (String pattern : includePatterns) {
            if (pattern.contains("#")) {  //NOI18N
                includeKeys.addAll(matchingKeys(relativePath, pattern));
            } else {
                if (relativePath.matches(pattern)) {
                    includeFile = true;
                    includeKeys.clear();  // include entire file
                    break;
                }
            }
        }
        if (includeFile || !includeKeys.isEmpty()) {
            // check excludes
            for (String pattern : excludePatterns) {
                if (pattern.contains("#")) {  //NOI18N
                    excludeKeys.addAll(matchingKeys(relativePath, pattern));
                } else {
                    if (relativePath.matches(pattern)) {
                        includeFile = false;
                        includeKeys.clear();  // exclude entire file
                        break;
                    }
                }
            }
        }
        LOGGER.log(Level.FINEST, "{0}, includeFile={1}, includeKeys={2}, excludeKeys={3}", new Object[]{relativePath, includeFile, includeKeys, excludeKeys});  //NOI18N
        if (!includeFile && includeKeys.isEmpty()) {
            // nothing matches
            return;
        }

        if (zipOutputStream != null) {  // export to zip
            LOGGER.log(Level.FINE, "Adding to zip: {0}", relativePath);  //NOI18N
            if ((new File(source,relativePath)).isFile()) {
                // Add ZIP entry to output stream.
                zipOutputStream.putNextEntry(new ZipEntry(relativePath));
                // Transfer bytes from the file to the ZIP file
                copyFileOrProperties(relativePath, includeKeys, excludeKeys, zipOutputStream);
            } else {
                // Add ZIP entry to output stream.
                zipOutputStream.putNextEntry(new ZipEntry(relativePath+"/.empty"));
            }
            // Complete the entry
            zipOutputStream.closeEntry();
        }
    }

    /** Copy file from relative path in zip file or userdir to target OutputStream.
     * It copies either entire file or just selected properties.
     * @param relativePath relative path
     * @param includeKeys keys to include
     * @param excludeKeys keys to exclude
     * @param out output stream
     * @throws IOException if coping fails
     */
    private void copyFileOrProperties(String relativePath, Set<String> includeKeys, Set<String> excludeKeys, OutputStream out) throws IOException {
        if (includeKeys.isEmpty() && excludeKeys.isEmpty()) {
            // copy entire file
            copyFile(relativePath, out);
        } else {
            if (!includeKeys.isEmpty()) {
                currentProperties.keySet().retainAll(includeKeys);
            }
            currentProperties.keySet().removeAll(excludeKeys);
            // copy just selected properties
            LOGGER.log(Level.FINE, "  Only keys: {0}", currentProperties.keySet());
            currentProperties.store(out);
        }
    }

    /** Returns properties from relative path in zip or userdir.
     * @param relativePath relative path
     * @return properties from relative path in zip or userdir.
     * @throws IOException if cannot open stream
     */
    private EditableProperties getProperties(String relativePath) throws IOException {
        EditableProperties properties = new EditableProperties(false);
        InputStream in = null;
        try {
            in = getInputStream(relativePath);
            properties.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return properties;
    }

    /** Returns InputStream from relative path in zip file or userdir.
     * @param relativePath relative path
     * @return InputStream from relative path in zip file or userdir.
     * @throws IOException if stream cannot be open
     */
    private InputStream getInputStream(String relativePath) throws IOException {
        if (source.isFile()) {
            //zip file
            ZipFile zipFile = new ZipFile(source);
            ZipEntry zipEntry = zipFile.getEntry(relativePath);
            return zipFile.getInputStream(zipEntry);
        } else {
            // userdir
            return new FileInputStream(new File(source, relativePath));
        }
    }

    /** Copy file from relative path in zip file or userdir to target OutputStream.
     * @param relativePath relative path
     * @param out output stream
     * @throws java.io.IOException if copying fails
     */
    private void copyFile(String relativePath, OutputStream out) throws IOException {
        InputStream in = null;
        try {
            in = getInputStream(relativePath);
            FileUtil.copy(in, out);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /** Creates parent of given file, if doesn't exist. */
    private static void ensureParent(File file) throws IOException {
        final File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Cannot create folder: " + parent.getAbsolutePath());  //NOI18N
            }
        }
    }

    /** Creates zip file containing only selected files from given source dir.
     * @param targetFile target zip file
     * @param sourceDir source dir
     * @param relativePaths paths to be added to zip file
     * @throws java.io.IOException
     */
    static void createZipFile(File targetFile, File sourceDir, List<String> relativePaths) throws IOException {
        ensureParent(targetFile);
        ZipOutputStream out = null;
        try {
            // Create the ZIP file
            out = new ZipOutputStream(new FileOutputStream(targetFile));
            // Compress the files
            for (String relativePath : relativePaths) {
                LOGGER.finest("Adding to zip: " + relativePath);  //NOI18N
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(relativePath));
                // Transfer bytes from the file to the ZIP file
                FileInputStream in = null;
                try {
                    in = new FileInputStream(new File(sourceDir, relativePath));
                    FileUtil.copy(in, out);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
                // Complete the entry
                out.closeEntry();
            }
            createProductInfo(out);
            // Complete the ZIP file
            out.close();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /** Adds build.info file with product, os, java version to zip file. */
    private static void createProductInfo(ZipOutputStream out) throws IOException {
        String productVersion = MessageFormat.format(
                NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"), //NOI18N
                new Object[]{System.getProperty("netbeans.buildnumber")}); //NOI18N
        String os = System.getProperty("os.name", "unknown") + ", " + //NOI18N
                System.getProperty("os.version", "unknown") + ", " + //NOI18N
                System.getProperty("os.arch", "unknown"); //NOI18N
        String java = System.getProperty("java.version", "unknown") + ", " + //NOI18N
                System.getProperty("java.vm.name", "unknown") + ", " + //NOI18N
                System.getProperty("java.vm.version", ""); //NOI18N
        out.putNextEntry(new ZipEntry("build.info"));  //NOI18N
        PrintWriter writer = new PrintWriter(out);
        writer.println("ProductVersion=" + productVersion); //NOI18N
        writer.println("OS=" + os); //NOI18N
        writer.println("Java=" + java); //NOI18Nv
        writer.println("Userdir=" + System.getProperty("netbeans.user")); //NOI18N
        writer.flush();
        // Complete the entry
        out.closeEntry();
    }
}
