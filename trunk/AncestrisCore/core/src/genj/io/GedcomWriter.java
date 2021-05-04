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
package genj.io;

import genj.crypto.Enigma;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.time.PointInTime;
import genj.util.Resources;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * GedcomWriter is a custom write for Gedcom compatible information. Normally
 * it's used by GenJ's application when trying to save to a file. This type can
 * be used by 3rd parties that are interested in writing Gedcom from a GenJ
 * object-representation managed outside of GenJ as well.
 */
public class GedcomWriter implements IGedcomWriter {

    private final static Resources RESOURCES = Resources.get(GedcomWriter.class);
    private static Logger LOG = Logger.getLogger("ancestris.io");
    /**
     * lots of state
     */
    private Gedcom gedcom;
    private BufferedWriter out;
    private String file;
    private String date;
    private String time;
    private int total;
    private int line;
    private int entity;
    private boolean cancel = false;
    private Filter filter;
    private boolean hasVetoed = false;
    private Enigma enigma = null;

    /**
     * Constructor for a writer that will write gedcom-formatted output on
     * writeGedcom()
     *
     * @param ged object to write out
     * @param stream the stream to write to
     */
    public GedcomWriter(Gedcom ged, OutputStream stream) throws IOException, GedcomEncodingException {

        Calendar now = Calendar.getInstance();

        // init data
        gedcom = ged;
        file = ged.getOrigin() == null ? "Uknown" : ged.getOrigin().getFileName();
        line = 0;
        date = PointInTime.getNow().getValue();
        time = new SimpleDateFormat("HH:mm:ss").format(now.getTime());
        filter = new Filter.Union(gedcom, Collections.<Filter>emptyList());

        CharsetEncoder encoder = getCharset(stream, ged.getEncoding()).newEncoder();
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        out = new BufferedWriter(new OutputStreamWriter(stream, encoder));

        // Done
    }

    /**
     * Create the charset we're using for out
     */
    private Charset getCharset(OutputStream out, String encoding) throws GedcomEncodingException {

        // Attempt encoding
        try {
            // Unicode
            if (Gedcom.UNICODE.equals(encoding)) {

                try {
                    out.write(GedcomEncodingSniffer.BOM_UTF16BE);
                } catch (Throwable t) {
                    // ignored
                }
                return Charset.forName("UTF-16BE");
            }
            // UTF8
            if (Gedcom.UTF8.equals(encoding)) {

                try {
                    out.write(GedcomEncodingSniffer.BOM_UTF8);
                } catch (Throwable t) {
                    // ignored
                }
                return Charset.forName("UTF-8");
            }
            // ASCII - 20050705 using Latin1 (ISO-8859-1) from now on to preserve extended ASCII characters
            if (Gedcom.ASCII.equals(encoding)) {
                return Charset.forName("ISO-8859-1"); // was ASCII
            }      // Latin1 (ISO-8859-1)
            if (Gedcom.LATIN1.equals(encoding)) {
                return Charset.forName("ISO-8859-1");
            }
            // ANSI (Windows-1252)
            if (Gedcom.ANSI.equals(encoding)) {
                return Charset.forName("Windows-1252");
            }
        } catch (UnsupportedCharsetException e) {
        }

        // ANSEL
        if (Gedcom.ANSEL.equals(encoding)) {
            return new AnselCharset();
        }

        // unknown encoding
        throw new GedcomEncodingException("Can't write with unknown encoding " + encoding);

    }

    /**
     * Thread-safe cancel of writeGedcom()
     */
    public void cancelTrackable() {
        cancel = true;
    }

    /**
     * Returns progress of save in %
     *
     * @return percent as 0 to 100
     */
    public int getProgress() {
        if (entity == 0) {
            return 0;
        }
        return entity * 100 / total;
    }

    /**
     * Returns current write state as string
     */
    public String getState() {
        String lStr = NumberFormat.getIntegerInstance().format(line);
        String eStr = NumberFormat.getIntegerInstance().format(entity);
        return getTaskName() + " : " + RESOURCES.getString("progress.read.entities", "" + lStr, eStr);
    }

    @Override
    public String getTaskName() {
        if (gedcom != null) {
            return RESOURCES.getString("writer.title", gedcom.getName());
        }
        return "";
    }

    /**
     * Sets filters to use for checking whether to write entities/properties or
     * not
     */
    public void setFilters(Collection<Filter> fs) {
        filter = new Filter.Union(gedcom, fs);
    }

    /**
     * Number of lines written
     */
    public int getLines() {
        return line;
    }

    /**
     * Actually writes the gedcom-information
     *
     * @exception GedcomIOException
     */
    public void write() throws GedcomIOException {

        // check state - we pass gedcom only once!
        if (gedcom == null) {
            throw new IllegalStateException("can't call write() twice");
        }

        List<Entity> ents = gedcom.getEntities();
        total = ents.size();

        // Out operation
        try {

            // Data
            writeHeader();
            writeEntities(ents);
            writeTail();

            // Close Output
            out.close();

        } catch (GedcomIOException ioe) {
            throw ioe;
        } catch (UnmappableCharacterException unme) {
            throw new GedcomEncodingException(gedcom.getFirstEntity("HEAD"), gedcom.getEncoding());
        } catch (Exception ex) {
            throw new GedcomIOException("Error while writing / " + ex.getMessage(), line);
        } finally {
            gedcom = null;
        }

        // Done
    }

    /**
     * write line for header and footer
     */
    private void writeLine(String line) throws IOException {
        out.write(line);
        out.newLine();
        this.line++;
    }

    /**
     * Write Header information
     *
     * @exception IOException
     */
    private Entity writeHeader() throws IOException, GedcomException {
        // Header
        Entity header = gedcom.getFirstEntity("HEAD");
        if (header == null) {
            header = gedcom.createEntity("HEAD", "");
        }

        // replace HEAD:SOUR
        Property prop;
        prop = replaceProperties(header, "SOUR", "ANCESTRIS");
        prop.addProperty("VERS", Lookup.getDefault().lookup(ancestris.api.core.Version.class).getVersionString());
        prop.addProperty("NAME", "Ancestris");
        prop.addProperty("CORP", RESOURCES.getString("header.corp", "Ancestris")).addProperty("ADDR", "http://www.ancestris.org");
        replaceProperties(header, "DEST", gedcom.getDestination());

        // Replace HEAD:DATE
        replaceProperties(header, "DATE", date).addProperty("TIME", time);
        if (gedcom.getSubmitter() != null) {
            replaceProperties(header, "SUBM", "@" + gedcom.getSubmitter().getId() + '@');
        }
        replaceProperties(header, "FILE", file);

        prop = replaceProperties(header, "GEDC", "");
        prop.addProperty("VERS", gedcom.getGrammar().getVersion());
        prop.addProperty("FORM", "LINEAGE-LINKED");

        replaceProperties(header, "CHAR", gedcom.getEncoding());

        if (gedcom.getLanguage() != null) {
            replaceProperties(header, "LANG", gedcom.getLanguage());
        }
        // becomes redundant after change of getter in gedcom
//        if (gedcom.getPlaceFormat().length() > 0) {
//            replaceProperties(header, "PLAC", "").addProperty("FORM", PropertyPlace.formatSpaces(gedcom.getPlaceFormat())); 
//        }

        new EntityWriter().write(0, header);
        return header;
        // done
    }

    // FIXME: should we put this in property class?
    private Property replaceProperties(Property prop, String tag, String value) {
        prop.delProperties(tag);
        return prop.addProperty(tag, value);
    }

    /**
     * Write Entities information
     *
     * @exception IOException
     */
    private void writeEntities(List<Entity> entities) throws IOException {

        // Loop through entities
        es:
        for (Entity e : entities) {
            // Don't output header twice
            if ("HEAD".equals(e.getTag())) {
                continue;
            }
            // .. check op
            if (cancel) {
                throw new GedcomIOException("Operation cancelled", line);
            }
            // .. filtered?
            if (filter.veto(e)) {
                hasVetoed = true;
                continue es;
            }
            // .. writing it and its subs
            try {
                line += new EntityWriter().write(0, e);
            } catch (UnmappableCharacterException unme) {
                throw new GedcomEncodingException(e, gedcom.getEncoding());
            }

            // .. track it
            entity++;
        }

        // Done
    }

    /**
     * Write Tail information
     *
     * @exception IOException
     */
    private void writeTail() throws IOException {
        // Tailer
        writeLine("0 TRLR");
    }

    @Override
    public boolean hasFiltersVetoed() {
        return hasVetoed;
    }

    /**
     * our entity writer
     */
    private class EntityWriter extends PropertyWriter {

        /**
         * constructor
         */
        EntityWriter() {
            super(out, false);
        }

        /**
         * intercept prop decoding to check filters
         */
        protected void writeProperty(int level, Property prop) throws IOException {

            // check against filters
            if (!prop.getTag().equalsIgnoreCase("HEAD") && !prop.isTransient() && !prop.isGuessed()) {
                if (filter.veto(prop)) {
                    hasVetoed = true;
                    return;
                }
            }
            // cont
            super.writeProperty(level, prop);
        }

        /**
         * intercept value decoding to facilitate encryption
         */
        protected String getValue(Property prop) throws IOException {
            return prop.isPrivate() ? encrypt(prop.getValue()) : super.getValue(prop);
        }

        /**
         * encrypt a value
         */
        private String encrypt(String value) throws IOException {

            // not necessary for gedcom without password or empty values
            if (gedcom.getPassword() == null || value.length() == 0) {
                return value;
            }

            // Make sure enigma is setup
            if (enigma == null) {

                // no need if password is unknown (data is already/still encrypted)
                if (gedcom.getPassword() == Gedcom.PASSWORD_UNKNOWN) {
                    return value;
                }

                // error if password isn't set
                if (gedcom.getPassword() == null) {
                    throw new IOException("Password not set - needed for encryption");
                }

                // error if can't encrypt
                enigma = Enigma.getInstance(gedcom.getPassword());
                if (enigma == null) {
                    throw new IOException("Encryption not available");
                }

            }

            // encrypt and done
            return enigma.encrypt(value);
        }
    } //EntityDecoder
} //GedcomWriter

