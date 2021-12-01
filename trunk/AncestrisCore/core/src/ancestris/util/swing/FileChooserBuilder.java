/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2014-2016 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * Author: Frederic Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

 /*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package ancestris.util.swing;

/**
 * This is a modified version of openide FileChooserBuilder:
 * <ul><li/>fix FileDialog position (partially)
 * <li/>Fix approve text (now if set, JFileCooser is of type Custom)
 * <li/>Add setParent API
 * <li/>Add 'overwrite' warning
 *
 * </ul>
 */
import ancestris.core.resources.Images;
import genj.util.Registry;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.*;

/**
 * Utility class for working with JFileChoosers. In particular, remembering the
 * last-used directory for a given file is made transparent. You pass an ad-hoc
 * string key to the constructor (the fully qualified name of the calling class
 * is good for uniqueness, and there is a constructor that takes a
 * <code>Class</code> object as an argument for this purpose). That key is used
 * to look up the most recently-used directory from any previous invocations
 * with the same key. This makes it easy to have your user interface
 * &ldquo;remember&rdquo; where the user keeps particular types of files, and
 * saves the user from having to navigate through the same set of directories
 * every time they need to locate a file from a particular place.
 * <p>
 * </p>
 * <code>FileChooserBuilder</code>'s methods each return <code>this</code>, so
 * it is possible to chain invocations to simplify setting up a file chooser.
 * Example usage:
 * <pre>
 *      <font color="gray">//The default dir to use if no value is stored</font>
 *      File home = new File (System.getProperty("user.home") + File.separator + "lib");
 *      <font color="gray">//Now build a file chooser and invoke the dialog in one line of code</font>
 *      <font color="gray">//&quot;libraries-dir&quot; is our unique key</font>
 *      File toAdd = new FileChooserBuilder ("libraries-dir").setTitle("Add Library").
 *              setDefaultWorkingDirectory(home).setApproveText("Add").showOpenDialog();
 *      <font color="gray">//Result will be null if the user clicked cancel or closed the dialog w/o OK</font>
 *      if (toAdd != null) {
 *          //do something
 *      }
 * </pre>
 * <p>
 * </p>
 * Instances of this class are intended to be thrown away after use. Typically
 * you create a builder, set it to create file choosers as you wish, then use it
 * to show a dialog or create a file chooser you then do something with.
 * <p>
 * </p>
 * Supports the most common subset of JFileChooser functionality; if you need to
 * do something exotic with a file chooser, you are probably better off creating
 * your own.
 * <p>
 * </p>
 * <b>Note:</b> If you use the constructor that takes a <code>Class</code>
 * object, please use <code>new FileChooserBuilder(MyClass.class)</code>, not
 * <code>new FileChooserBuilder(getClass())</code>. This avoids unexpected
 * behavior in the case of subclassing.
 *
 * @author Tim Boudreau
 */
public class FileChooserBuilder {

    private boolean dirsOnly;
    private BadgeProvider badger;
    private String title;
    private String approveText;
    private String approveTooltipText = null;
    private final String dirKey;
    private File failoverDir;
    private FileFilter filter;
    private boolean fileHiding;
    private boolean controlButtonsShown = true;
    private String aDescription;
    private boolean filesOnly;
    private static final boolean DONT_STORE_DIRECTORIES = false; // Boolean.getBoolean("forget.recent.dirs");
    private SelectionApprover approver;
    private final List<FileFilter> filters = new ArrayList<>(3);
    private boolean useAcceptAllFileFilter = true;
    private boolean imagePreviewer = false;
    private JComponent accessory;
    private boolean badgeProvider = false;
    private File selectedFile;

    public static String[] gedExtensions = {"ged"};
    public static String[] imgExtensions = {"png", "jpg", "jpeg", "gif", "tiff", "bmp", "svg"};
    public static String[] sndExtensions = {"mp3", "wav", "ogg", "flac"};
    public static String[] vidExtensions = {"mp4", "flv", "ogg", "avi", "mov", "mpeg", "mts", "ts", "wmv", "mkv", "asf"};
    public static String[] pdfExtensions = {"pdf", "ps"};
    public static String[] txtExtensions = {"txt"};
    public static String[] docExtensions = {"txt", "doc", "odt"};
    public static String[] csvExtensions = {"csv"};
    public static String[] tblExtensions = {"txt", "csv"};
    public static String[] htmExtensions = {"html"};
    public static String[] zipExtensions = {"zip"};
    public static String[] almExtensions = {"almanac"};
    public static String[] pngExtensions = {"png"};

    private static String DIMX = "dimX";
    private static String DIMY = "dimY";
    private static String DIMW = "dimW";
    private static String DIMH = "dimH";

    private JFileChooser activeChooser = null;

    /**
     * Create a new FileChooserBuilder using the name of the passed class as the
     * metadata for looking up a starting directory from previous application
     * sessions or invocations.
     *
     * @param type A non-null class object, typically the calling class
     */
    public FileChooserBuilder(Class type) {
        this(type.getName());
    }

    /**
     * Create a new FileChooserBuilder. The passed key is used as a key into
     * NbPreferences to look up the directory the file chooser should initially
     * be rooted on.
     *
     * @param dirKey A non-null ad-hoc string. If a FileChooser was previously
     * used with the same string as is passed, then the initial directory
     */
    public FileChooserBuilder(String dirKey) {
        Parameters.notNull("dirKey", dirKey);
        this.dirKey = dirKey;
        localizeLabels();
    }

    /**
     * Set whether or not any file choosers created by this builder will show
     * only directories.
     *
     * @param val true if files should not be shown
     * @return this
     */
    public FileChooserBuilder setDirectoriesOnly(boolean val) {
        dirsOnly = val;
        assert !filesOnly : "FilesOnly and DirsOnly are mutually exclusive";
        return this;
    }

    public FileChooserBuilder setFilesOnly(boolean val) {
        filesOnly = val;
        assert !dirsOnly : "FilesOnly and DirsOnly are mutually exclusive";
        return this;
    }

    /**
     * Provide an implementation of BadgeProvider which will "badge" the icons
     * of some files.
     *
     * @param provider A badge provider which will alter the icon of files or
     * folders that may be of particular interest to the user
     * @return this
     */
    public FileChooserBuilder setBadgeProvider(BadgeProvider provider) {
        this.badger = provider;
        return this;
    }

    /**
     * Provide a default implementation of badge provider.
     *
     * @return this
     */
    public FileChooserBuilder setDefaultBadgeProvider() {
        this.badgeProvider = true;
        return this;
    }

    /**
     * Provide an implementation of an image previewer which will show the image
     * of a file when it is selected.
     *
     * @return this
     */
    public FileChooserBuilder setDefaultPreviewer() {
        this.imagePreviewer = true;
        return this;
    }

    /**
     * Provide an implementation of an accessory.
     *
     * @param accessory add accessory
     * @return this
     */
    public FileChooserBuilder setAccessory(JComponent accessory) {
        this.accessory = accessory;
        return this;
    }

    /**
     * Set the dialog title for any JFileChoosers created by this builder.
     *
     * @param val A localized, human-readable title
     * @return this
     */
    public FileChooserBuilder setTitle(String val) {
        title = val;
        return this;
    }

    /**
     * Set default title
     *
     * @return this
     */
    public FileChooserBuilder setDefaultTitle() {
        title = NbBundle.getMessage(getClass(), "TITL_ChooseFile");
        return this;
    }

    /**
     * Set the text on the OK button for any file chooser dialogs produced by
     * this builder.
     *
     * @param val A short, localized, human-readable string
     * @return this
     */
    public FileChooserBuilder setApproveText(String val) {
        approveText = val;
        return this;
    }

    /**
     * Set the tooltip text on the OK button.
     *
     * @param val A short, localized, human-readable string
     * @return this
     */
    public FileChooserBuilder setApproveTooltipText(String val) {
        approveTooltipText = val;
        return this;
    }

    private String defaultExtension = null;

    public FileChooserBuilder setDefaultExtension(String extension) {
        defaultExtension = extension;
        return this;
    }

    /**
     * Set a file filter which filters the list of selectable files.
     *
     * @param filter
     * @return this
     */
    public FileChooserBuilder setFileFilter(FileFilter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Set a file filter from array of Formats
     *
     * @param Format[]
     * @return this
     */
    private Map<String, String> formats = new HashMap<>();   // description, extension

    public FileChooserBuilder setFileFilters(Map<String, String> formats) {
        this.formats = formats;
        for (String key : formats.keySet()) {
            filters.add(new FileNameExtensionFilter(key, formats.get(key)));
        }
        return this;
    }

    /**
     * Force filter to be only the file filter provided while file chooser is
     * already open.
     *
     * @param filter A file filter
     * @return this
     */
    public FileChooserBuilder forceFileFilter(FileFilter filter) {
        if (activeChooser != null) {
            activeChooser.resetChoosableFileFilters();
            activeChooser.setFileFilter(filter);
            activeChooser.setAcceptAllFileFilterUsed(false);
        }
        return this;
    }

    /**
     * Equivalent to calling
     * <code>JFileChooser.addChoosableFileFilter(filter)</code>. Adds another
     * file filter that can be displayed in the file filters combo box in the
     * file chooser.
     *
     * @param filter The file filter to add
     * @return this
     * @since 7.26.0
     */
    public FileChooserBuilder addFileFilter(FileFilter filter) {
        filters.add(filter);
        return this;
    }

    /**
     * Add all default file filters to the file chooser.
     *
     * @see MIMEResolver.Registration#showInFileChooser()
     * @see MIMEResolver.ExtensionRegistration#showInFileChooser()
     * @return this
     * @since 8.1
     */
    public FileChooserBuilder addDefaultFileFilters() {
//        filters.addAll(FileFilterSupport.findRegisteredFileFilters());
        return this;
    }

    /**
     * Determines whether the <code>AcceptAll FileFilter</code> is used as an
     * available choice in the choosable filter list. If false, the
     * <code>AcceptAll</code> file filter is removed from the list of available
     * file filters. If true, the <code>AcceptAll</code> file filter will become
     * the the actively used file filter.
     *
     * @param accept whether the <code>AcceptAll FileFilter</code> is used
     * @return this
     * @since 8.3
     */
    public FileChooserBuilder setAcceptAllFileFilterUsed(boolean accept) {
        useAcceptAllFileFilter = accept;
        return this;
    }

    /**
     * Set the current directory which should be used <b>only if</b>
     * a last-used directory cannot be found for the key string passed into this
     * builder's constructor.
     *
     * @param dir A directory to root any created file choosers on if there is
     * no stored path for this builder's key
     * @return this
     */
    public FileChooserBuilder setDefaultWorkingDirectory(File dir) {
        failoverDir = dir;
        return this;
    }

    /**
     * Set the current directory to the default report directory.
     *
     * @return this
     */
    public FileChooserBuilder setDefaultDirAsReportDirectory() {
        failoverDir = new File(Registry.get(genj.gedcom.GedcomOptions.class).get("reportDir", System.getProperty("user.home")));
        return this;
    }

    private boolean force = false;

    /**
     * Force use of the failover directory - i.e. ignore the directory key
     * passed in.
     *
     * @param val
     * @return this
     */
    public FileChooserBuilder forceUseOfDefaultWorkingDirectory(boolean val) {
        this.force = val;
        return this;
    }

    /**
     * Set selected file.
     *
     * @param selectedFile A file to select
     * @return this
     */
    public FileChooserBuilder setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
        return this;
    }

    /**
     * Force selected file while file chooser is already open.
     *
     * @param selectedFile A file to display in the file box
     * @return this
     */
    public FileChooserBuilder forceSelectedFile(File selectedFile) {
        if (activeChooser != null) {
            activeChooser.setSelectedFile(selectedFile);
        }
        return this;
    }

    /**
     * Enable file hiding in any created file choosers
     *
     * @param fileHiding Whether or not to hide files. Default is no.
     * @return this
     */
    public FileChooserBuilder setFileHiding(boolean fileHiding) {
        this.fileHiding = fileHiding;
        return this;
    }

    /**
     * Show/hide control buttons
     *
     * @param val Whether or not to show OK and Cancel buttons on certain UI.
     * Default is true.
     * @return this
     */
    public FileChooserBuilder setControlButtonsAreShown(boolean val) {
        this.controlButtonsShown = val;
        return this;
    }

    /**
     * Set the accessible description for any file choosers created by this
     * builder
     *
     * @param aDescription The description
     * @return this
     */
    public FileChooserBuilder setAccessibleDescription(String aDescription) {
        this.aDescription = aDescription;
        return this;
    }

    private Component parent = null;

    /**
     * @param parent
     * @return
     */
    public FileChooserBuilder setParent(Component parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Create a JFileChooser that conforms to the parameters set in this
     * builder.
     *
     * @return A file chooser
     */
    public JFileChooser createFileChooser() {
        JFileChooser result = new SavedDirFileChooser(dirKey, failoverDir,
                force, approver);
        prepareFileChooser(result);
        return result;
    }

    /**
     * Set a selection approver which can display an &quot;Overwrite file?&quot;
     * or similar dialog if necessary, when the user presses the accept button
     * in the file chooser dialog.
     *
     * @param approver A SelectionApprover which will determine if the selection
     * is valid
     * @return this
     * @since 7.26.0
     */
    public FileChooserBuilder setSelectionApprover(SelectionApprover approver) {
        this.approver = approver;
        return this;
    }

    ////////// SHOW-ERS //////////////////////////////
    /**
     * Show an open dialog that allows multiple selection.
     *
     * @return An array of files, or null if the user cancelled the dialog
     */
    public File[] showMultiOpenDialog() {
        JFileChooser chooser = createFileChooser();
        chooser.setMultiSelectionEnabled(true);
        int result = chooser.showOpenDialog(findDialogParent());
        if (JFileChooser.APPROVE_OPTION == result) {
            saveDialogSize(chooser);
            File[] files = chooser.getSelectedFiles();
            return files == null ? new File[0] : files;
        } else {
            saveDialogSize(chooser);
            return null;
        }
    }

    /**
     * Show an open dialog with a file chooser set up according to the
     * parameters of this builder.
     *
     * @return A file if the user clicks the accept button and a file or folder
     * was selected at the time the user clicked cancel.
     */
    public File showOpenDialog() {
        JFileChooser chooser = createFileChooser();
        if (Boolean.getBoolean("nb.native.filechooser")) { //NOI18N
            FileDialog fileDialog = createFileDialog(chooser.getCurrentDirectory());
            if (null != fileDialog) {
                return showFileDialog(fileDialog, FileDialog.LOAD);
            }
        }
        chooser.setMultiSelectionEnabled(false);
        int dlgResult = chooser.showOpenDialog(findDialogParent());
        if (JFileChooser.APPROVE_OPTION == dlgResult) {
            saveDialogSize(chooser);
            File result = chooser.getSelectedFile();
            if (result != null && !result.exists()) {
                result = null;
            }
            return result;
        } else {
            saveDialogSize(chooser);
            return null;
        }

    }

    /**
     * Show a save dialog with the file chooser set up according to the
     * parameters of this builder.
     *
     * @return A file if the user clicks the accept button and a file or folder
     * was selected at the time the user clicked cancel.
     */
    public File showSaveDialog() {
        return showSaveDialog(true);
    }

    public File showSaveDialog(boolean askForOverwrite) {
        JFileChooser chooser = createFileChooser();
        if (Boolean.getBoolean("nb.native.filechooser")) { //NOI18N
            FileDialog fileDialog = createFileDialog(chooser.getCurrentDirectory());
            if (null != fileDialog) {
                return showFileDialog(fileDialog, FileDialog.SAVE);
            }
        }
        File file = null;
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(findDialogParent())) {
            file = chooser.getSelectedFile();
        }
        saveDialogSize(chooser);
        if (file == null) {
            return null;
        }
        // add filter extension if file has none
        if (file.getName().indexOf('.') == -1) {
            String extension = getExtensionFromFilter(chooser.getFileFilter());
            if (extension != null && !extension.isEmpty()) {
                defaultExtension = extension;
            }
            if (defaultExtension != null && !defaultExtension.isEmpty()) {
                file = new File(file.getAbsolutePath() + "." + defaultExtension);
            }
        }
        if (askForOverwrite && file.exists()) {
            if (DialogManager.YES_OPTION != DialogManager.createYesNo(
                    NbBundle.getMessage(FileChooserBuilder.class, "TITL_fileOverwrite"),
                    NbBundle.getMessage(FileChooserBuilder.class, "MSG_fileOverwrite", file.getName())).
                    setMessageType(DialogManager.WARNING_MESSAGE).show()) {
                file = null;
            }
        }
        return file;
    }

    ////////// public tools ////////////////////
    /*
     * Get the extension of a file Name.
     */
    public static String getExtension(String s) {
        String ext = "";
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
            // Remove potential parameters after name (URL name).
            int j = ext.indexOf('?');
            if (j > 0 && j <= ext.length()) {
                ext = ext.substring(0, j);
            }
        }

        return ext;
    }

    /*
     * Get the extension icon of a file.
     */
    public static Icon getIconFromFileExtension(File file) {
        String extension = getExtension(file.getName());
        Icon icon = null;

        if (Arrays.asList(gedExtensions).contains(extension)) {
            icon = Images.imgGedcom;
        } else if (Arrays.asList(imgExtensions).contains(extension)) {
            icon = Images.imgImage;
        } else if (Arrays.asList(sndExtensions).contains(extension)) {
            icon = Images.imgSound;
        } else if (Arrays.asList(vidExtensions).contains(extension)) {
            icon = Images.imgVideo;
        } else if (Arrays.asList(pdfExtensions).contains(extension)) {
            icon = Images.imgPDF;
        } else if (Arrays.asList(txtExtensions).contains(extension)) {
            icon = Images.imgText;
        } else if (Arrays.asList(zipExtensions).contains(extension)) {
            icon = Images.imgZip;
        } else if (Arrays.asList(almExtensions).contains(extension)) {
            icon = Images.imgAlm;
        } // add more at will !

        return icon;
    }

    /*
     * Get the type extensions
     */
    // Define this accept all as a filter in order to be able to put "All types" at the botomm of the list.         
    // With setAcceptAllFileFilterUsed(true), it would appear at the top.
    public static FileFilter getAllFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(FileChooserBuilder.class, "Filter_ALLTYPES");
            }
        };
    }

    public static FileNameExtensionFilter getGedcomFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_GEDCOM"), gedExtensions);
    }

    public static FileNameExtensionFilter getTextFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_TEXT"), txtExtensions);
    }

    public static FileNameExtensionFilter getTableFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_TABLE"), tblExtensions);
    }

    public static FileNameExtensionFilter getPdfFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_PDF"), pdfExtensions);
    }

    public static FileNameExtensionFilter getHtmlFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_HTML"), htmExtensions);
    }

    public static FileNameExtensionFilter getCSVFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_CSV"), csvExtensions);
    }

    public static FileNameExtensionFilter getImageFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_Images"), imgExtensions);
    }

    public static FileNameExtensionFilter getSoundFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_Sounds"), sndExtensions);
    }

    public static FileNameExtensionFilter getVideoFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_Videos"), vidExtensions);
    }

    public static FileNameExtensionFilter getZipFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_Zip"), zipExtensions);
    }

    public static FileNameExtensionFilter getAlmanacFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_Almanac"), almExtensions);
    }
    
    public static FileNameExtensionFilter getPngFilter() {
        return new FileNameExtensionFilter(NbBundle.getMessage(FileChooserBuilder.class, "Filter_PNG"), pngExtensions);
    }

    // Private methods //////////////////////////////////////////////////////////////////////////////////////
    /**
     * Tries to find an appropriate component to parent the file chooser to when
     * showing a dialog.
     *
     * @return this
     */
    private Component findDialogParent() {
        // Force display in the center
        if (true) {
            return null;
        }
        // Forget all that follows, this is a bit strange to thte user to see this window at different positions everytime the time
        Component parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (parent == null) {
            parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        }
        if (parent == null) {
            Frame[] f = Frame.getFrames();
            parent = f.length == 0 ? null : f[f.length - 1];
        }
        return parent;
    }

    private File showFileDialog(FileDialog fileDialog, int mode) {
        String oldFileDialogProp = System.getProperty("apple.awt.fileDialogForDirectories"); //NOI18N
        if (dirsOnly) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true"); //NOI18N
        }
        fileDialog.setMode(mode);
        fileDialog.setVisible(true);
        if (dirsOnly) {
            if (null != oldFileDialogProp) {
                System.setProperty("apple.awt.fileDialogForDirectories", oldFileDialogProp); //NOI18N
            } else {
                System.clearProperty("apple.awt.fileDialogForDirectories"); //NOI18N
            }
        }
        if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            String selFile = fileDialog.getFile();
            File dir = new File(fileDialog.getDirectory());
            return new File(dir, selFile);
        }
        return null;
    }

    private void prepareFileChooser(JFileChooser chooser) {
        setDialogSize(chooser);

        chooser.setFileSelectionMode(dirsOnly ? JFileChooser.DIRECTORIES_ONLY
                : filesOnly ? JFileChooser.FILES_ONLY
                        : JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileHidingEnabled(fileHiding);  // default to !ancestris.core.CoreOptions.getInstance().getShowHidden()   ?

        chooser.setControlButtonsAreShown(controlButtonsShown);
        chooser.setAcceptAllFileFilterUsed(useAcceptAllFileFilter);
        if (title != null) {
            chooser.setDialogTitle(title);
        }
        if (approveText != null) {
            chooser.setApproveButtonText(approveText);
        }
        if (badgeProvider) {
            badger = new DefaultBadgeProvider();
        }
        if (badger != null) {
            chooser.setFileView(new CustomFileView(new BadgeIconProvider(badger),
                    chooser.getFileSystemView()));
        }
        if (filter != null) {
            chooser.setFileFilter(filter);
        }
        if (selectedFile != null) {
            chooser.setSelectedFile(selectedFile);
        }
        if (aDescription != null) {
            chooser.getAccessibleContext().setAccessibleDescription(aDescription);
        }
        if (!filters.isEmpty()) {
            for (FileFilter f : filters) {
                chooser.addChoosableFileFilter(f);
            }
        }
        if (imagePreviewer) {
            chooser.setAccessory(new DefaultImagePreviewer(chooser));
        } else if (accessory != null) {
            chooser.setAccessory(accessory);
        }

        removeButtonTooltips(chooser);
        if (approveTooltipText != null) {
            chooser.setApproveButtonToolTipText(approveTooltipText);
        }

        activeChooser = chooser;
    }

    private FileDialog createFileDialog(File currentDirectory) {
        if (badger != null) {
            return null;
        }
        if (!Boolean.getBoolean("nb.native.filechooser")) {
            return null;
        }
        if (dirsOnly && !Utilities.isMac()) {
            return null;
        }
        Component parentComponent = findDialogParent();
        Frame parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parentComponent);

        FileDialog fileDialog = new FileDialog(parentFrame);
        if (title != null) {
            fileDialog.setTitle(title);
        }
        if (null != currentDirectory) {
            fileDialog.setDirectory(currentDirectory.getAbsolutePath());
        }
//        fileDialog.setLocationRelativeTo(null); 
//        fileDialog.setAlwaysOnTop(true);
        return fileDialog;
    }

    private void setDialogSize(JFileChooser chooser) {
        int x = Integer.valueOf(NbPreferences.forModule(FileChooserBuilder.class).get(DIMX, "-1"));
        int y = Integer.valueOf(NbPreferences.forModule(FileChooserBuilder.class).get(DIMY, "-1"));
        int w = Integer.valueOf(NbPreferences.forModule(FileChooserBuilder.class).get(DIMW, "650"));
        int h = Integer.valueOf(NbPreferences.forModule(FileChooserBuilder.class).get(DIMH, "400"));
        if (x >= 0 && y >= 0) {
            chooser.setLocation(x, y);
        }
        chooser.setPreferredSize(new Dimension(w, h));
    }

    private void saveDialogSize(JFileChooser chooser) {
        int x = chooser.getLocation().x;
        int y = chooser.getLocation().y;
        NbPreferences.forModule(FileChooserBuilder.class).put(DIMX, "" + x);
        NbPreferences.forModule(FileChooserBuilder.class).put(DIMY, "" + y);

        int w = chooser.getSize().width;
        int h = chooser.getSize().height;
        NbPreferences.forModule(FileChooserBuilder.class).put(DIMW, "" + w);
        NbPreferences.forModule(FileChooserBuilder.class).put(DIMH, "" + h);
    }

    private void removeButtonTooltips(Container container) {

        Component[] jc = container.getComponents();
        for (int i = 0; i < jc.length; i++) {
            Component c = jc[i];
            if (c instanceof JButton) {
                JButton j = (JButton) c;
                if (j.getText() != null && !j.getText().isEmpty()) {
                    j.setToolTipText(null);
                }
            } else if (c instanceof Container) {
                removeButtonTooltips((Container) c);
            }
        }

    }

    private String getExtensionFromFilter(FileFilter fileFilter) {
        return formats.get(fileFilter.getDescription());
    }

    private void localizeLabels() {
        String[] KEYS = {
            "acceptAllFileFilterText",
            "lookInLabelText",
            "cancelButtonText",
            "cancelButtonToolTipText",
            "openButtonText",
            "openButtonToolTipText",
            "filesOfTypeLabelText",
            "fileNameLabelText",
            "folderNameLabelText",
            "listViewButtonToolTipText",
            "listViewButtonAccessibleName",
            "detailsViewButtonToolTipText",
            "detailsViewButtonAccessibleName",
            "upFolderToolTipText",
            "upFolderAccessibleName",
            "homeFolderToolTipText",
            "homeFolderAccessibleName",
            "fileNameHeaderText",
            "fileSizeHeaderText",
            "fileTypeHeaderText",
            "fileDateHeaderText",
            "fileAttrHeaderText",
            "openDialogTitleText",
            "newFolderToolTipText",
            "saveButtonText",
            "updateButtonText",
            "helpButtonText",
            "saveButtonToolTipText",
            "updateButtonToolTipText",
            "helpButtonToolTipText"
        };

        for (String key : KEYS) {
            UIManager.put("FileChooser." + key, NbBundle.getMessage(FileChooserBuilder.class, "FileChooser." + key));
        }

    }

    /**
     * Object which can approve the selection (enabling the OK button or
     * equivalent) in a JFileChooser. Equivalent to overriding
     * <code>JFileChooser.approveSelection()</code>
     *
     * @since 7.26.0
     */
    public interface SelectionApprover {

        /**
         * Approve the selection, enabling the dialog to be closed. Called by
         * the JFileChooser's <code>approveSelection()</code> method. Use this
         * interface if you want to, for example, show a dialog asking
         * &quot;Overwrite File X?&quot; or similar.
         *
         * @param selection The selected file(s) at the time the user presses
         * the Open, Save or OK button
         * @return true if the selection is accepted, false if it is not and the
         * dialog should not be closed
         */
        public boolean approve(File[] selection);
    }

    private static final class SavedDirFileChooser extends JFileChooser {

        private final String dirKey;
        private final SelectionApprover approver;

        SavedDirFileChooser(String dirKey, File failoverDir, boolean force, SelectionApprover approver) {
            this.dirKey = dirKey;
            this.approver = approver;
            if (force && failoverDir != null && failoverDir.exists() && failoverDir.isDirectory()) {
                setCurrentDirectory(failoverDir);
            } else {
                String path = DONT_STORE_DIRECTORIES ? null
                        : NbPreferences.forModule(FileChooserBuilder.class).get(dirKey, null);
                if (path != null) {
                    File f = new File(path);
                    if (f.exists() && f.isDirectory()) {
                        setCurrentDirectory(f);
                    } else if (failoverDir != null) {
                        setCurrentDirectory(failoverDir);
                    }
                } else if (failoverDir != null) {
                    setCurrentDirectory(failoverDir);
                }
            }
        }

        @Override
        public void approveSelection() {
            if (approver != null) {
                File[] selected = getSelectedFiles();
                final File sf = getSelectedFile();
                if ((selected == null || selected.length == 0) && sf != null) {
                    selected = new File[]{sf};
                }
                boolean approved = approver.approve(selected);
                if (approved) {
                    super.approveSelection();
                }
            } else {
                super.approveSelection();
            }
        }

        @Override
        public int showOpenDialog(Component parent) throws HeadlessException {
            int result = super.showOpenDialog(parent);
            if (result == APPROVE_OPTION) {
                saveCurrentDir();
            }
            return result;
        }

        @Override
        public int showSaveDialog(Component parent) throws HeadlessException {
            int result;
            if (getApproveButtonText() == null) {
                result = super.showSaveDialog(parent);
            } else {
                result = super.showDialog(parent, getApproveButtonText());
            }
            if (result == APPROVE_OPTION) {
                saveCurrentDir();
            }
            return result;
        }

        private void saveCurrentDir() {
            File dir = super.getCurrentDirectory();
            if (!DONT_STORE_DIRECTORIES && dir != null && dir.exists() && dir.isDirectory()) {
                NbPreferences.forModule(FileChooserBuilder.class).put(dirKey, dir.getPath());
            }
        }
    }

    //Can open this API later if there is a use-case
    interface IconProvider {

        public Icon getIcon(File file, Icon orig);
    }

    /**
     * Provides "badges" for icons that indicate files or folders of particular
     * interest to the user.
     *
     * @see FileChooserBuilder#setBadgeProvider
     */
    public interface BadgeProvider {

        /**
         * Get the badge the passed file should use.  <b>Note:</b> this method is
         * called for every visible file. The negative test (deciding
         * <i>not</i> to badge a file) should be very, very fast and immediately
         * return null.
         *
         * @param file The file in question
         * @return an icon or null if no change to the appearance of the file is
         * needed
         */
        public Icon getBadge(File file);

        /**
         * Get the x offset for badges produced by this provider. This is the
         * location of the badge icon relative to the real icon for the file.
         *
         * @return a rightward pixel offset
         */
        public int getXOffset();

        /**
         * Get the y offset for badges produced by this provider. This is the
         * location of the badge icon relative to the real icon for the file.
         *
         * @return a downward pixel offset
         */
        public int getYOffset();
    }

    private static final class BadgeIconProvider implements IconProvider {

        private final BadgeProvider badger;

        public BadgeIconProvider(BadgeProvider badger) {
            this.badger = badger;
        }

        @Override
        public Icon getIcon(File file, Icon orig) {
            Icon badge = badger.getBadge(file);
            if (badge != null && orig != null) {
                return new MergedIcon(orig, badge, badger.getXOffset(),
                        badger.getYOffset());
            }
            return orig;
        }
    }

    private static final class CustomFileView extends FileView {

        private final IconProvider provider;
        private final FileSystemView view;

        CustomFileView(IconProvider provider, FileSystemView view) {
            this.provider = provider;
            this.view = view;
        }

        @Override
        public Icon getIcon(File f) {
            Icon result = view.getSystemIcon(f);
            result = provider.getIcon(f, result);
            return result;
        }
    }

    private static class MergedIcon implements Icon {

        private final Icon icon1;
        private final Icon icon2;
        private final int xMerge;
        private final int yMerge;

        MergedIcon(Icon icon1, Icon icon2, int xMerge, int yMerge) {
            assert icon1 != null;
            assert icon2 != null;
            this.icon1 = icon1;
            this.icon2 = icon2;

            if (xMerge == -1) {
                xMerge = icon1.getIconWidth() - icon2.getIconWidth();
            }

            if (yMerge == -1) {
                yMerge = icon1.getIconHeight() - icon2.getIconHeight();
            }

            this.xMerge = xMerge;
            this.yMerge = yMerge;
        }

        @Override
        public int getIconHeight() {
            return Math.max(icon1.getIconHeight(), yMerge + icon2.getIconHeight());
        }

        @Override
        public int getIconWidth() {
            return Math.max(icon1.getIconWidth(), yMerge + icon2.getIconWidth());
        }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            icon1.paintIcon(c, g, x, y);
            icon2.paintIcon(c, g, x + xMerge, y + yMerge);
        }
    }

    /**
     * Image previewer as an option
     */
    private static final class DefaultImagePreviewer extends JLabel {

        public DefaultImagePreviewer(JFileChooser chooser) {
            final int size = 120;
            setText("");
            setPreferredSize(new Dimension(size, size));
            setBorder(BorderFactory.createEtchedBorder());
            setHorizontalAlignment(JLabel.CENTER);
            setHorizontalTextPosition(JLabel.CENTER);
            chooser.addPropertyChangeListener((PropertyChangeEvent evt) -> {
                if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    File f = (File) evt.getNewValue();
                    if (f != null) {
                        ImageIcon icon = new ImageIcon(f.getPath());
                        if (icon.getIconWidth() > size) {
                            icon = new ImageIcon(icon.getImage().getScaledInstance(size, -1, Image.SCALE_DEFAULT));
                        }
                        setIcon(icon);
                    }
                }
            });
        }
    }

    /**
     * Default badge provider as an option
     */
    private static final class DefaultBadgeProvider implements BadgeProvider {

        public DefaultBadgeProvider() {
        }

        @Override
        public Icon getBadge(File file) {
            return getIconFromFileExtension(file);
        }

        @Override
        public int getXOffset() {
            return 5;
        }

        @Override
        public int getYOffset() {
            return 5;
        }

    }

}
