/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.gedcom;

import genj.io.FileAssociation;
import genj.io.InputSource;
import genj.io.input.FileInput;
import genj.io.input.URLInput;
import genj.util.swing.ImageIcon;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gedcom Property : FILE
 */
public class PropertyFile extends Property {

    private static final Logger LOG = Logger.getLogger("ancestris.app");

    /**
     * standard image
     */
    public final static ImageIcon DEFAULT_IMAGE = Grammar.V55.getMeta(new TagPath("INDI:OBJE:FILE")).getImage();

    /**
     * the file-name
     */
    private String file;

    /**
     * whether file-name should be relative or absolute
     */
    private boolean forceRelative = true;

    /**
     * Whether the file exists on computer or not
     */
    private boolean isLocal = true;

    /**
     * Whether the file is an URL adress
     */
    private boolean isRemote = false;
    
    private Optional<InputSource> input = Optional.empty();

    /**
     * need tag-argument constructor for all properties
     */
    public PropertyFile(String tag) {
        super(tag);
    }

    public boolean isIsLocal() {
        return isLocal;
    }

    public boolean isIsRemote() {
        return isRemote;
    }

    /**
     * Overriden - file association is easy for a PropertyFile
     */
    public boolean addFile(InputSource file) {
        setValue(file.getLocation(), true);
        return true;
    }

    /**
     * Returns the filepath of this file: - if force relative, make it relative
     * - if not, return as is
     *
     * @return
     */
    @Override
    public String getValue() {

        if (file == null) {
            return "";
        }

        // Force relative value :
        // we're checking the value for relative here because
        // in setValue() the parent might not be set yet so
        // getGedcom() wouldn't work there
        if (forceRelative) {
            Gedcom gedcom = getGedcom();
            if (gedcom != null) {
                String relative = gedcom.getOrigin().calcRelativeLocation(file);
                if (relative != null) {
                    file = relative;
                }
            }
        }
        return file;
    }

    /**
     * Sets this filepath as relative
     * @param value
     */
    @Override
    public synchronized void setValue(String value) { 
        
        String newValue = value;
        
        if (value.startsWith("file://")) {
            newValue = value.substring(7);
        }

        String old = getValue();

        // Remember the value
        file = newValue.replace('\\', '/');
        forceRelative = true; // force recalc of relative path in the getValue()

        // Check if local or remote file
        Gedcom gedcom = getGedcom();
        
        final File fichier = new File(newValue);

        if (fichier.exists()) {
            isLocal = true;
            isRemote = false;
        } else {
            try {
                // Try the URL.
                final URL remote = new URL(newValue);
                isRemote = true;
                isLocal = false;
            } catch (MalformedURLException mfue) {
                LOG.log(Level.FINEST, "URL exception.", mfue.getLocalizedMessage());
                isRemote = false;
                isLocal = true;
            }
        }
        
        forceInput();

        // 20030518 don't automatically update TITL/FORM
        // will be prompted in ProxyFile
        // Remember the change
        propagatePropertyChanged(this, old);

        // done    
    }

    /**
     * Sets this filepath as it is entered (ex: used to keep absolute path)
     */
    public synchronized void setValueAsIs(String value) {

        String old = getValue();
        file = value.replace('\\', '/');
        forceRelative = false; // prevent recalc of relative path in the getValue() called during propagation (this will leave value unchanged)
        propagatePropertyChanged(this, old);
    }

    /**
     * Sets this property's value
     */
    public void setValue(String value, boolean updateMeta) {

        // set value
        setValue(value);

        // check
        Property media = getParent();
        if (!updateMeta || !media.getTag().equals("OBJE")) {
            return;
        }

        // look for right place of FORM
        Property parent = this;
        if (!getMetaProperty().allows("FORM")) {
            if (!media.getMetaProperty().allows("FORM")) {
                return;
            }
            parent = media;
        }

        Property form = parent.getProperty("FORM", false);
        if (form == null) {
            parent.addProperty("FORM", getSuffix());
        } else {
            form.setValue(getSuffix());
        }

        // done  
    }

    /**
     * The files location (if externally accessible)
     */
    private File getFile() {
        Gedcom gedcom = getGedcom();
        return gedcom != null ? gedcom.getOrigin().getFile(file) : null;
    }

    /**
     * GetInputSource from the file.
     *
     * @return InputSource with the File.
     */
    public Optional<InputSource> getInput() {
        if (input.isPresent()) {
            return input;
        }

        forceInput();
        return input;
    }

    private void forceInput() {
         final File fichier = getFile();

         if (fichier != null && fichier.exists()) {
             isLocal = true;
             isRemote = false;
            input = InputSource.get(fichier);
            return;
         }
         if (isRemote) {
             try {
                input = InputSource.get(new URL(file));
                return;
            } catch (MalformedURLException mfue) {
                // Should never happen, already checked at set value
                LOG.log(Level.FINEST, "URL exception.", mfue);
            }
        }
        input = Optional.empty();
    }

    /**
     * Resolve the maximum load (whether to return kb)
     */
    public static int getMaxValueAsIconSize(boolean kb) {
        return (kb ? 1 : 1024) * GedcomOptions.getInstance().getMaxImageFileSizeKB();
    }

    /**
     * Calculate suffix of file (empty string if n/a)
     */
    public String getSuffix() {
        getInput();
        if (input.isPresent()) {
            return input.get().getExtension();
        }
        return getSuffix(file);
    }

    /**
     * Calculate suffix of file (empty string if n/a)
     */
    private static String getSuffix(String value) {
        // check for suffix
        String result = "";
        if (value != null) {
            int i = value.lastIndexOf('.');
            if (i >= 0) {
                result = value.substring(i + 1);
                int j = result.indexOf('?');
                if (j > 0) {
                    result = result.substring(0, j);
                }
            }
        }
        // done
        return result;
    }

    public boolean isOpenable() {
        final Optional<InputSource> oInput = getInput();
        if (!oInput.isPresent()) {
            return false;
        }
        final InputSource inputSource = oInput.get();
        return (inputSource instanceof FileInput) || (inputSource instanceof URLInput);
    }
    
    public void openFile() {
        final Optional<InputSource> oInput = getInput();
        if (!oInput.isPresent()) {
            return;
        }
        final InputSource inputSource = oInput.get();
        if (inputSource instanceof FileInput) {
            FileAssociation.getDefault().execute(((FileInput) inputSource).getFile());
        }
        if (inputSource instanceof URLInput) {
            FileAssociation.getDefault().execute(((URLInput) inputSource).getURL());
        }
    }

} //PropertyFile
