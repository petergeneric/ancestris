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

import genj.io.InputSource;
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
     */
    public synchronized void setValue(String value) {

        String old = getValue();

        // Remember the value
        file = value.replace('\\', '/');
        forceRelative = true; // force recalc of relative path in the getValue()

        // Check if local or remote file
        
        Gedcom gedcom = getGedcom();
       
        final File fichier = new File(value);
        if (fichier.exists()) {
            isLocal = true;
            isRemote = false;
        } else {
            isLocal = false;
            try {
                final URL remote = new URL(value);
                isRemote = true;
            } catch (MalformedURLException mfue) {
                LOG.log(Level.FINE, "URL exception.", mfue);
                isRemote = false;
            }
        }

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

        Property form = parent.getProperty("FORM");
        if (form == null) {
            parent.addProperty("FORM", PropertyFile.getSuffix(file));
        } else {
            form.setValue(PropertyFile.getSuffix(file));
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
        final File fichier = getFile();
        
        if (fichier.exists()) {
            isLocal = true;
            isRemote = false;
            return InputSource.get(fichier);
        }
        if (isRemote) {
            try {
            return InputSource.get(new URL(file));
            } catch (MalformedURLException mfue) {
                // Should never happen, already checked at set value
                LOG.log(Level.FINE, "URL exception.", mfue);
            }
        }
        return Optional.empty(); // Create URL.
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
        return getSuffix(file);
    }

    /**
     * Calculate suffix of file (empty string if n/a)
     */
    public static String getSuffix(String value) {
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

} //PropertyFile
