/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2006 Nils Meier <nils@meiers.net>
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

import ancestris.util.TimingUtility;
import genj.crypto.Enigma;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Submitter;
import genj.util.EnvironmentChecker;
import genj.util.MeteredInputStream;
import genj.util.Origin;
import genj.util.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 * GedcomReader is a custom reader for Gedcom compatible information. Normally
 * it's used by GenJ's application or applet when trying to open a file or
 * simply reading from a stream. This type can be used by 3rd parties that are
 * interested in reading Gedcom into the GenJ object representation as well.
 */
public class GedcomReaderFactory {

    private final static Resources RESOURCES = Resources.get(GedcomReaderFactory.class);
    private static Logger LOG = Logger.getLogger("ancestris.io");
    /**
     * estimated average byte size of one entity
     */
    private final static int ENTITY_AVG_SIZE = 150;

    /**
     * factory method
     */
    public static GedcomReader createReader(Origin origin, GedcomReaderContext context) throws IOException {
        LOG.info("Initializing reader for " + origin);
        return new Impl(new Gedcom(origin), origin.open(), context != null ? context : new DefaultContext());
    }

    public static GedcomReader createReader(InputStream in, GedcomReaderContext context) throws IOException {
        return new Impl(new Gedcom(), in, context != null ? context : new DefaultContext());
    }

    /**
     * reader implementation
     */
    private static class Impl implements GedcomReader {

        /**
         * status the reader goes through
         */
        private final static int READHEADER = 0, READENTITIES = 1, LINKING = 2;
        /**
         * lots of state we keep during reading
         */
        private Gedcom gedcom;
        private int progress;
        private int entity = 0;
        private int state;
        private int length;
        private List<LazyLink> lazyLinks = new ArrayList<>();
        private String tempSubmitter;
        private boolean cancel = false;
        private EntityReader reader;
        private MeteredInputStream meter;
        private Enigma enigma;
        private GedcomReaderContext context;

        /**
         * Constructor
         */
        private Impl(Gedcom ged, InputStream in, GedcomReaderContext context) throws IOException {

            GedcomEncodingSniffer sniffer = new GedcomEncodingSniffer(in);
            Charset charset = sniffer.getCharset();
            String encoding = sniffer.getEncoding();

            if (!sniffer.isDeterministic()) {
                context.handleWarning(0, RESOURCES.getString("read.warn.nochar"), new Context(ged));
            }

            String charsetName = EnvironmentChecker.getProperty("ancestris.gedcom.charset", null, "checking for forced charset for read of " + ged.getName());
            if (charsetName != null) {
                try {
                    charset = Charset.forName(charsetName);
                    encoding = Gedcom.UTF8;
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "Can't force charset " + charset, t);
                }
            }

            // init some data
            this.length = sniffer.available();
            this.gedcom = ged;
            this.gedcom.setEncoding(encoding);
            this.context = context;
            this.meter = new MeteredInputStream(sniffer);
            this.reader = new EntityReader(new InputStreamReader(meter, charset));

            // Done
        }

        /**
         * Thread-safe cancel of read()
         */
        @Override
        public void cancelTrackable() {
            cancel = true;
        }

        /**
         * Returns progress of save in %
         *
         * @return percent as 0 to 100
         */
        @Override
        public int getProgress() {

            // reading right now?
            if (state == READENTITIES && length > 0) {
                progress = (int) Math.min(80, meter.getCount() * 80 / length);
            }

            // done
            return progress;
        }

        /**
         * Returns current read state as explanatory string
         */
        @Override
        public String getState() {
            switch (state) {
                case READHEADER:
                    return getTaskName() + " : " + RESOURCES.getString("progress.read.header");
                case READENTITIES:
                default:
                    String lStr = NumberFormat.getIntegerInstance().format(reader.getLines());
                    String eStr = NumberFormat.getIntegerInstance().format(entity);
                    return getTaskName() + " : " + RESOURCES.getString("progress.read.entities", "" + lStr, eStr);
                case LINKING:
                    String task = getTaskName();
                    return task.isEmpty() ? "" : task + " : " + RESOURCES.getString("progress.read.linking");
            }
        }

        /**
         * number of lines read
         */
        public int getLines() {
            return reader.getLines();
        }

        /**
         * Actually writes the gedcom-information
         *
         * @exception GedcomIOException reading failed
         * @exception GedcomFormatException reading Gedcom-data brought up wrong
         * format
         * @exception GedcomEncryptionException encountered encrypted property
         * and password didn't match
         */
        @Override
        public Gedcom read() throws GedcomEncryptionException, GedcomIOException, GedcomFormatException {

            // check state - we pass gedcom only once!
            if (gedcom == null) {
                throw new IllegalStateException("can't call read() twice");
            }

            // try it
            try {
                readGedcom();
                gedcom.setLines(reader.getLines());
                return gedcom;
            } catch (GedcomIOException gex) {
                LOG.log(Level.SEVERE, "Error reading gedcom: {0}", gedcom.getName());
                LOG.log(Level.SEVERE, "Error reading gedcom: {0}", "(line:" + gex.getLine() + ") - " + gex.getLocalizedMessage());
                throw gex;
            } catch (Exception t) {
                // catch anything else bubbling up here
                Exceptions.printStackTrace(t);
                LOG.log(Level.SEVERE, "unexpected exception", t);
                throw new GedcomIOException(t.toString(), reader.getLines());
            } finally {
                // close in
                try {
                    reader.in.close();
                } catch (IOException t) {
                }
                // allow gc to collect gedcom
                lazyLinks.clear();
                gedcom = null;
            }

            // nothing happening here
        }

        /**
         * Read Gedcom as a whole
         *
         */
        private void readGedcom() throws IOException {

            long start = System.currentTimeMillis();

            // Read the Header
            readHeader();
            state++;
            long header = System.currentTimeMillis();

            // Read records after the other
            while (reader.readEntity() != null) {
                if (cancel) {
                    throw new GedcomIOException(RESOURCES.getString("read.warn.cancelled"), getLines());
                }
            }

            long records = System.currentTimeMillis();

            // Next state
            state++;

            // Prepare submitter
            if (tempSubmitter.length() > 0) {
                try {
                    Submitter sub = (Submitter) gedcom.getEntity(Gedcom.SUBM, tempSubmitter.replace('@', ' ').trim());
                    gedcom.setSubmitter(sub);
                } catch (IllegalArgumentException t) {
                    context.handleWarning(0, RESOURCES.getString("read.warn.setsubmitter", tempSubmitter), new Context(gedcom));
                }
            }

            // Link references
            linkReferences();
            long linking = System.currentTimeMillis();

            long total = System.currentTimeMillis();
            LOG.log(Level.INFO, "{0}: {1} loaded in {2}s (header {3}s, records {4}s, linking {5}s ({6}))",
                    new Object[]{
                        TimingUtility.getInstance().getTime(),
                        gedcom.getName(),
                        (total - start) / 1000,
                        (header - start) / 1000,
                        (records - header) / 1000, (linking - records) / 1000,
                        lazyLinks.size()
                    });

            // Done
        }

        /**
         * linkage
         */
        private void linkReferences() throws GedcomIOException {

            // loop over kept references
//            for (int i = 0, n = lazyLinks.size(); i < n; i++) {
//                LazyLink lazyLink = lazyLinks.get(i);
            int n = lazyLinks.size();
            int i = 0;
            for (LazyLink lazyLink : lazyLinks) {
                try {
                    if (lazyLink.xref.getParent() != null && lazyLink.xref.getTarget() == null) {
                        lazyLink.xref.link();
                    }
                    progress = 80 + Math.min(20, (i * (20 * 2) / n));  // 100*2 because Links are probably backref'd
                } catch (GedcomException ex) {
                    context.handleWarning(lazyLink.line, ex.getMessage(), new Context(lazyLink.xref));
                } catch (Throwable t) {
                    Exceptions.printStackTrace(t);
                    throw new GedcomIOException(RESOURCES.getString("read.error.xref", new Object[]{lazyLink.xref.getTag(), lazyLink.xref.getValue()}), lazyLink.line);
                }
                i++;
            }

            // done
        }

        /**
         * Read Header
         *
         * @exception GedcomIOException reading from <code>BufferedReader</code>
         * failed
         * @exception GedcomFormatException reading Gedcom-data brought up wrong
         * format
         */
        private boolean readHeader() throws IOException {

            Entity header = reader.readEntity();
            if (header == null || !header.getTag().equals("HEAD")) {
                throw new GedcomFormatException(RESOURCES.getString("read.error.noheader"), 0);
            }

            // check 1 SUBM
            tempSubmitter = header.getPropertyValue("SUBM");
            // NM 20080329 - really GenJ doesn't care whether this is set or not - we should really warn before saving but not
            // when reading - the user is not going to add a submitter to everything he opens so we might as well ignore a
            // missing SUBM
            //    if (tempSubmitter.length()==0)
            //      warnings.add(new Warning(0, RESOURCES.getString("read.warn.nosubmitter"), gedcom));

            // check 1 SOUR
            // String source = header.getPropertyValue("SOUR");
            // NM 20080329 - same here - GenJ doesn't care and is not going to write this on save anyways
            //    if (source.length()==0)
            //      warnings.add(new Warning(0, RESOURCES.getString("read.warn.nosourceid"), gedcom));
            // check for
            // 1 GEDC
            // 2 VERSion and
            // 2 FORMat
            Property vers = header.getPropertyByPath("HEAD:GEDC:VERS");
            Property headForm = header.getPropertyByPath("HEAD:GEDC:FORM");
            if (vers == null) {
                context.handleWarning(0, RESOURCES.getString("read.warn.badgedc"), new Context(gedcom));
            } else {
                String v = vers.getValue();
                if ("5.5".equals(v)) {
                    if (headForm == null) {
                         context.handleWarning(0, RESOURCES.getString("read.warn.badgedc"), new Context(gedcom));
                    }
                    gedcom.setGrammar(Grammar.V55);
                    LOG.info("Found VERS " + v + " - Gedcom version is 5.5");
                } else if ("5.5.1".equals(v)) {
                    if (headForm == null) {
                         context.handleWarning(0, RESOURCES.getString("read.warn.badgedc"), new Context(gedcom));
                    }
                    gedcom.setGrammar(Grammar.V551);
                    LOG.info("Found VERS " + v + " - Gedcom version is 5.5.1");
                } else if (v.contains("7.0")) {
                    gedcom.setGrammar(Grammar.v70);
                    LOG.info("Found VERS " + v + " - Gedcom version is 7.0.x");
                } else {
                    String s = RESOURCES.getString("read.warn.badversion", v, gedcom.getGrammar().getVersion());
                    context.handleWarning(0, RESOURCES.getString("read.warn.badversion", v, gedcom.getGrammar().getVersion()), new Context(gedcom));
                    LOG.warning(s);
                }
            }
            
            // V7 Header doesn't look like others.
            if (Grammar.v70.equals(gedcom.getGrammar())) {
                return readHeader7(header);
            }
            
            // Silently change case of Head:GEDC:FORM
            if (headForm != null && !"LINEAGE-LINKED".equals(headForm.getValue())) {
                headForm.setValue("LINEAGE-LINKED");
            }

            // check 1 LANG
            String lang = header.getPropertyValue("LANG");
            if (lang.length() > 0) {
                gedcom.setLanguage(lang);
                LOG.info("Found LANG " + lang + " - Locale is " + gedcom.getLocale());
            }

            // check 1 DEST
            String dest = header.getPropertyValue("DEST");
            if (dest == null || dest.isEmpty()) {
                context.handleWarning(0, RESOURCES.getString("read.warn.baddest"), new Context(gedcom));
            } else {
                if ("ANY".equals(dest)) {
                    gedcom.setDestination(Gedcom.DEST_ANY);
                    LOG.info("Found DEST " + dest + " - Any");
                } else if ("ANSTFILE".equals(dest)) {
                    gedcom.setDestination(Gedcom.DEST_ANSTFILE);
                    LOG.info("Found DEST " + dest + " - Ancestral File");
                } else if ("TempleReady".equals(dest)) {
                    gedcom.setDestination(Gedcom.DEST_TEMPLEREADY);
                    LOG.info("Found DEST " + dest + " - Temple ordinance clearance");
                } else {
                    String s = RESOURCES.getString("read.warn.baddestination", dest, gedcom.getDestination());
                    context.handleWarning(0, RESOURCES.getString("read.warn.baddestination", dest, gedcom.getDestination()), new Context(gedcom));
                    LOG.warning(s);
                }
            }

            // check 1 CHAR
            String encoding = header.getPropertyValue("CHAR");
            if (encoding.length() > 0) {
                gedcom.setEncoding(encoding);
                if (encoding.equals("ASCII")) {
                    context.handleWarning(0, RESOURCES.getString("read.warn.ascii"), new Context(gedcom));
                }
            }

            managePlacHeader(header);

            // get rid of it for now
            // FIXME: Mark all header properties as readonly: they will 
            // not be editable in gedcom editor
            // TODO: add missing properties in gedcom grammar
            for (Property p : header.getProperties()) {
                recurseMarkRO(p);
            }

            // Done
            return true;
        }

        // Manage PLAC in header.
        private void managePlacHeader(Entity header) {
            // check
            // 1 PLAC
            // 2 FORM
            Property plac = header.getProperty("PLAC");
            String form = "";
            if (plac != null) {
                form = plac.getPropertyValue("FORM");
                gedcom.setPlaceFormat(form);
                LOG.info("Found Place.Format " + form);
            }
            if (plac == null || form == null || form.isEmpty()) {
                context.handleWarning(0, RESOURCES.getString("read.warn.badplac"), new Context(gedcom));
            }
        }

        private static void recurseMarkRO(Property prop) {
            prop.setReadOnly(true);
            for (Property p : prop.getProperties()) {
                recurseMarkRO(p);
            }
        }
        
        private boolean readHeader7(Entity header) {
            // Always UTF-8
            gedcom.setEncoding("UTF-8");
            managePlacHeader(header);
            // Reconstruct header with correct type of properties.
            Iterator<Property> it = Arrays.asList(header.getProperties()).iterator();
            while (it.hasNext()){
                Property p = it.next();
                try {
                MetaProperty mp = header.getGedcom().getGrammar().getMeta(p.getPath());
                Property np = mp.create(p.getValue());
                if ( !np.getClass().equals(p.getClass())) {
                    np = header.addProperty(p.getTag(), p.getValue());
                    recurseProperties(p, np);
                    header.delProperty(p);
                }
                } catch (GedcomException e) {
                    // Nothing to do, unable to get value so do nothing.
                    LOG.log(Level.FINER, "Unable to get value from :" + p.toString(), e);
                }
            }
            
            for (Property p : header.getProperties()) {
                recurseMarkRO(p);
            }
            return true;
        }
        
        private void recurseProperties(Property oldValue, Property newValue) {
            for (Property p : oldValue.getProperties()){
                Property newChild = newValue.addProperty(p.getTag(), p.getValue());
                recurseProperties(p, newChild);
            }
        }

        @Override
        public String getTaskName() {
            if (gedcom != null && RESOURCES != null) {
                String tname = gedcom.getName();
                if (tname == null) {
                    tname = "gedcom file";
                }
                return RESOURCES.getString("reader.title", tname);
            }
            return "";
        }

        /**
         * our entity reader
         */
        private class EntityReader extends PropertyReader {

            /**
             * constructor
             */
            EntityReader(Reader in) {
                super(in, null, false);
            }

            /**
             * read one entity
             */
            Entity readEntity() throws IOException {

                if (!readLine(true, true)) {
                    throw new GedcomFormatException(RESOURCES.getString("read.error.norecord"), lines);
                }

                if (level != 0) {
                    throw new GedcomFormatException(RESOURCES.getString("read.error.nonumber"), lines);
                }

                // Trailer? we're done
                if (tag.equals("TRLR")) {
                    // consume any trailing blanks
                    if (readLine(true, true)) {
                        throw new GedcomFormatException(RESOURCES.getString("read.error.aftertrlr"), lines);
                    }
                    return null;
                }

                // Create entity and read its properties
                Entity result;
                try {

                    result = gedcom.createEntity(tag, xref);
                    // When entity is read from file, it is old.
                    result.setOld();

                    // warn about missing xref if it's a well known type
                    if (result.getClass() != Entity.class && xref.length() == 0) {
                        context.handleWarning(getLines(), RESOURCES.getString("read.warn.recordnoid", Gedcom.getName(tag)), new Context(result));
                    }

                    // check entity validity
                    if (!result.isValid()) {
                        context.handleWarning(getLines(), RESOURCES.getString("read.warn.invalidrecord", tag, value), new Context(result));
                    }

                    // preserve valeur for those who care
                    result.setValue(value.replaceAll("@@", "@"));

                    // continue into properties
                    readProperties(result, 0, 0);

                } catch (GedcomException ex) {
                    throw new GedcomIOException(ex.getMessage(), lines);
                }

                // the trailer?
                if (!tag.equals("TRLR")) {
                    entity++;
                }

                // Done
                return result;
            }

            /**
             * override read to get a chance to decrypt values
             */
            @Override
            protected void readProperties(Property prop, int currentLevel, int pos) throws IOException {
                // let super do its thing
                super.readProperties(prop, currentLevel, pos);
                // decrypt lazy
                decryptLazy(prop);
            }

            /**
             * Decrypt a valeur if necessary
             */
            private void decryptLazy(Property prop) throws GedcomIOException {

                // 20060128 an xref is never crypted and getValue() is expensive so we try to avoid this
                if (prop instanceof PropertyXRef) {
                    return;
                }
                // 20060128 a valid date can't need decryption and getValue() is expensive so we try to avoid this
                if ((prop instanceof PropertyDate) && prop.isValid()) {
                    return;
                }

                // no need to do anything if not encrypted valeur
                String valeur = prop.getValue();
                if (!Enigma.isEncrypted(valeur)) {
                    return;
                }

                // set property private
                prop.setPrivate(true, false);

                // no need to do anything for unknown password
                if (Gedcom.PASSWORD_UNKNOWN.equals(gedcom.getPassword())) {
                    return;
                }

                // try to decrypt until we have a good password or bailed
                while (enigma == null) {

                    // ask for it
                    String pwd = context.getPassword();

                    // bail if not provided
                    if (pwd == null) {
                        context.handleWarning(getLines(), RESOURCES.getString("crypt.password.unknown"), new Context(prop));
                        gedcom.setPassword(Gedcom.PASSWORD_UNKNOWN);
                        return;
                    }

                    // try it
                    try {
                        enigma = Enigma.getInstance(pwd);
                        enigma.decrypt(valeur);
                        gedcom.setPassword(pwd);
                    } catch (IOException e) {
                        enigma = null;
                    }

                    // try again if needed
                }

                // have enigma - has to work now
                try {
                    prop.setValue(enigma.decrypt(valeur));
                } catch (IOException e) {
                    throw new GedcomIOException(RESOURCES.getString("crypt.password.invalid"), lines);
                }

                // done
            }

            /**
             * keep track of xrefs - we're going to link them lazily afterwards
             */
            @Override
            protected void link(PropertyXRef xref, int line) {
                // keep as warning
                lazyLinks.add(new LazyLink(xref, line));
            }

            /**
             * keep track of invalid lines
             */
            @Override
            protected void trackInvalidLine(Property prop) {
                context.handleWarning(getLines(), RESOURCES.getString("read.warn.invalidline", prop.getValue()), new Context(prop));
            }

            /**
             * keep track of empty lines
             */
            @Override
            protected void trackEmptyLine() {
                // care about empty lines before TRLR
                if (!"TRLR".equals(tag)) {
                    context.handleWarning(getLines(), RESOURCES.getString("read.error.emptyline"), new Context(gedcom));
                }
            }

            /**
             * keep track of bad levels
             */
            @Override
            protected void trackBadLevel(int level, Property parent) {
                context.handleWarning(getLines(), RESOURCES.getString("read.warn.badlevel", "" + level), new Context(parent));
            }

            /**
             * keep track of bad properties
             */
            @Override
            protected void trackBadProperty(Property property, String message) {
                context.handleWarning(getLines(), message, new Context(property));
            }
        } //EntityReader

        /**
         * A lazy link
         */
        private static class LazyLink {

            private PropertyXRef xref;
            private int line;

            LazyLink(PropertyXRef xref, int line) {
                this.xref = xref;
                this.line = line;
            }
        }
    } //GedcomReaderImpl

    private static class DefaultContext implements GedcomReaderContext {

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public void handleWarning(int line, String warning, Context context) {
        }
    }
}
