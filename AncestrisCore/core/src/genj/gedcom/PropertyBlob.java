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
import genj.util.Base64;
import genj.util.ByteArray;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gedcom Property : BLOB
 */
public class PropertyBlob extends Property implements MultiLineProperty {
    
    private static final Logger LOG = Logger.getLogger("ancestris.app");

    /**
     * the content (either base64 or raw bytes)
     */
    private Object content = "";

    public PropertyBlob() {
        super("BLOB");
    }

    /**
     * need tag-argument constructor for all properties
     */
    public PropertyBlob(String tag) {
        super(tag);
    }

    /**
     * Returns the data of this Blob
     */
    private byte[] getBlobData() {

        // Already present ?
        if (content instanceof byte[]) {
            return (byte[]) content;
        }

        // Decode Base64
        try {
            content = Base64.decode(content.toString());
        } catch (IllegalArgumentException e) {
            Gedcom.LOG.log(Level.WARNING, "Cannot convert blob base64 in " + getGedcom().getName() + "/" + getEntity() + "/" + getPath() + " into bytes (" + e.getMessage() + ")");
            return new byte[0];
        }

        return (byte[]) content;
    }

    /**
     * GetData as InputSource.
     *
     * @return inputSource with data.
     */
    public Optional<InputSource> getInput() {
        return InputSource.get(getGedcom().getName() + "/" + getEntity() + "/" + getPath(), getBlobData());
    }

    /**
     * Title of Blob
     */
    public String getTitle() {
        Entity e = getEntity();
        return (e instanceof Media) ? ((Media) e).getTitle() : getTag();
    }

    /**
     * Returns the property value line
     */
    public String getValue() {

        // Raw-Data existing ?
        if (content instanceof byte[]) {
            return ((byte[]) content).length + " Raw Bytes";
        }

        // gotta be base64 string
        return content.toString().length() + " Base64 Bytes";
    }

    /**
     * @see genj.gedcom.MultiLineProperty#getLineCollector()
     */
    public Collector getLineCollector() {
        return new BlobCollector();
    }

    /**
     * Returns an Iterator which can be used to iterate through several lines of
     * this blob's value
     */
    public MultiLineProperty.Iterator getLineIterator() {

        // raw?
        if (content instanceof byte[]) {
            return new BlobIterator(Base64.encode((byte[]) content));
        }

        // string!
        return new BlobIterator(content.toString());
    }

    /**
     * Sets a property value line
     */
    public synchronized void setValue(String value) {

        String old = getValue();

        // Successfull new information
        content = value;

        // Remember changed property
        propagatePropertyChanged(this, old);

        // Done
    }

    /**
     * Overridden - special file association handling
     */
    public boolean addFile(InputSource is) {
        return load(is, true);
    }

    /**
     * Sets this property's value
     */
    public boolean load(InputSource file, boolean updateMeta) {

        String old = getValue();

            try (InputStream in = file.open()) {
                byte[] newContent = new ByteArray(in, in.available(), false).getBytes();
                content = newContent;
            } catch (Throwable t) {
                LOG.log(Level.FINE, "Unable to get the contents.", t);
                return false;
            }

        // Remember changed property
        propagatePropertyChanged(this, old);

        // check
        Property media = getParent();
        if (!updateMeta || !(media instanceof PropertyMedia || media instanceof Media)) {
            return true;
        }

        // format? this is all gedcom 5.5. style
        Property format = media.getProperty("FORM");
        if (format == null) {
            format = media.addProperty(new PropertySimpleValue("FORM"));
        }
        format.setValue(file.getExtension());

        // done  
        return true;
    }

    /**
     * A continuation for gathering blob data
     *
     */
    private class BlobCollector implements MultiLineProperty.Collector {

        /**
         * current state
         */
        private StringBuffer buffer;

        /**
         * Constructor
         */
        private BlobCollector() {
            buffer = new StringBuffer(1024);
            if (content instanceof String) {
                buffer.append(content);
            }
        }

        /**
         * @see genj.gedcom.MultiLineSupport.Continuation#append(int,
         * java.lang.String, java.lang.String)
         */
        public boolean append(int indent, String tag, String value) {

            // only level 1 (direct children)
            if (indent != 1) {
                return false;
            }

            // gotta be CONT 
            if (!"CONT".equals(tag)) {
                return false;
            }

            // grab it
            buffer.append(value.trim());

            // accepted
            return true;
        }

        /**
         * @see genj.gedcom.MultiLineProperty.Collector#getValue()
         */
        public String getValue() {
            return buffer.toString();
        }

    } //MyContinuation

    /**
     * Member class for iterating through adress' lines of base64-encoded data
     */
    private static class BlobIterator implements MultiLineProperty.Iterator {

        /**
         * the base64 string
         */
        private String base64;

        /**
         * the offset in the string
         */
        private int offset;

        /**
         * the break line value
         */
        private final int LINE = 72;

        /**
         * Constructor
         */
        public BlobIterator(String base64) {
            this.base64 = base64;
            this.offset = 0;
        }

        /**
         * @see genj.gedcom.MultiLineProperty.Iterator#setValue()
         */
        public void setValue(String base64) {
            // this is only called by PropertyWriter on read for encryption - ignored as the blob
            // data cannot be encrypted / overwritten
        }

        /**
         * @see genj.gedcom.MultiLineSupport.LineIterator#getIndent()
         */
        public int getIndent() {
            return offset == 0 ? 0 : 1;
        }

        /**
         * current tag
         */
        public String getTag() {
            return offset == 0 ? getTag() : "CONT";
        }

        /**
         * Returns the next line of this iterator
         */
        public String getValue() {
            return base64.substring(offset, Math.min(offset + LINE, base64.length()));
        }

        /**
         * set to next
         */
        public boolean next() {
            if (offset + LINE >= base64.length()) {
                return false;
            }
            offset += LINE;
            return true;
        }

    } //Base64Iterator

} //PropertyBlob
