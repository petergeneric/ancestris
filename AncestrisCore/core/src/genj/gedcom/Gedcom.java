/**
 * GenealogyJ - Ancestris
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2003 - 2016 Ancestris  <daniel@ancestris.org>, <frederic@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Revision: 1.138 $ $Author: nmeier $ $Date: 2010-01-28 14:48:13 $
 */
package genj.gedcom;

import ancestris.util.swing.DialogManager;
import genj.util.Origin;
import genj.util.ReferenceSet;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.SafeProxy;
import genj.util.swing.ImageIcon;
import genj.view.ViewContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.commons.lang.WordUtils;

/**
 * The object-representation of a Gedom file
 */
//TODO: enlever la comparaison de deux gedcom
public class Gedcom implements Comparable {

    final static Logger LOG = Logger.getLogger("ancestris.gedcom");
    final static Resources resources = Resources.get(Gedcom.class);

    public final static String PASSWORD_UNKNOWN = "unknown";

    //XXX: replace this by enum?
    public static final String 
        // standard Gedcom encodings 
        UTF8 = "UTF-8",         // Since 5.5.1. (recommanded) Multiple international languages at the same time. Optimised space. Diacritic characters appear clearly in raw gedcom file.
                                // Unicode character encoding over 1 character per byte. With WriteBOM, corresponds to UTF-8.
                                // Accentuation displayed properly ; accentuation in gedcom file are properly readable directly from gedcom
        UNICODE = "UNICODE",    // Multiple international languages at the same time. Larger space. Diacritic characters appear clearly in raw gedcom file.
                                // ***International character set enabling more than 110,000 characters covering 100 scripts
                                //    With WriteBOM, corresponds to UTF-16
        ANSEL = "ANSEL",        // Obsolete since 14-feb-2013, replaced by unicode - extension of latin alphabet. Only one international alphabet at a time. 
                                // ***American National Standard for Extended Latin Alphabet Coded Character Set for Bibliographic Use
                                // => enable accentuation being displayed correctly, but accentuation characters are stored over two bytes. 
                                //    Gedcom file not displaying accentuation properly
                                // 63 characters used with ASCII characters
        // non-standard encodings
        ANSI = "ANSI",          // Obsolete - American National Standard Institute
                                // a.k.a. Windows-1252 (@see http://www.hclrss.demon.co.uk/demos/ansi.html)
        LATIN1 = "LATIN1",      // Obsolete - Western Europe languages (a.k.a ISO-8859-1)
        ASCII = "ASCII";        // No diacritic characters possible. Converted to ISO-8859-1 in Ancestris
                                // we're using ISO-8859-1 actually to make extended characters possible - the spec is grayish on that one

    /** encodings including the non Gedcom-standard encodings LATIN1 and ANSI */
    public static final String[] ENCODINGS = {
        UTF8, UNICODE, ANSEL, LATIN1, ANSI, ASCII
    };

    /** languages as defined by the Gedcom standard */
    public static final String[] LANGUAGES = {
        "Afrikaans", "Albanian", "Amharic", "Anglo-Saxon", "Arabic", "Armenian", "Assamese",
        "Belorusian", "Bengali", "Braj", "Bulgarian", "Burmese",
        "Cantonese", "Catalan", "Catalan_Spn", "Church-Slavic", "Czech",
        "Danish", "Dogri", "Dutch",
        "English", "Esperanto", "Estonian",
        "Faroese", "Finnish", "French",
        "Georgian", "German", "Greek", "Gujarati",
        "Hawaiian", "Hebrew", "Hindi", "Hungarian",
        "Icelandic", "Indonesian", "Italian",
        "Japanese",
        "Kannada", "Khmer", "Konkani", "Korean",
        "Lahnda", "Lao", "Latvian", "Lithuanian",
        "Macedonian", "Maithili", "Malayalam", "Mandrin", "Manipuri", "Marathi", "Mewari",
        "Navaho", "Nepali", "Norwegian",
        "Oriya",
        "Pahari", "Pali", "Panjabi", "Persian", "Polish", "Prakrit", "Pusto", "Portuguese",
        "Rajasthani", "Romanian", "Russian",
        "Sanskrit", "Serb", "Serbo_Croa", "Slovak", "Slovene", "Spanish", "Swedish",
        "Tagalog", "Tamil", "Telugu", "Thai", "Tibetan", "Turkish",
        "Ukrainian", "Urdu",
        "Vietnamese",
        "Wendic",
        "Yiddish"
    };
    
    public static final SortedMap<String, String> TRANSLATED_LANGUAGES = new TreeMap<String, String>(); 

    private static final String[] LOCALES = { 
        "af", "sq", "am", "en", "ar", "hy", "as",
        "be", "bn", "?", "bg", "my",
        "zh", "ca", "?", "cu", "cs",
        "da", "?", "nl",
        "en", "eo", "et",
        "fo", "fi", "fr",
        "ka", "de", "el", "gu",
        "?", "he", "hi", "hu",
        "is", "id", "it",
        "ja",
        "kn", "km", "kok", "ko",
        "lah", "lo", "lv", "lt",
        "mk", "mai", "ml", "?", "mni", "mr", "?",
        "nv", "ne", "no",
        "or",
        "him", "pi", "pa", "fa", "pl", "pra", "ps", "pt",
        "raj", "ro", "ru",
        "sa", "sr", "?", "sk", "sl", "es", "sv",
        "tl", "ta", "te", "th", "bo", "tr",
        "uk", "ur",
        "vi",
        "?",
        "yi"
    };
    
    /** Destinations as defined by the Gedcom standard */
    public static final String DEST_ANY = "ANY";
    public static final String DEST_ANSTFILE = "ANSTFILE";
    public static final String DEST_TEMPLEREADY = "TempleReady";
            
    /** record tags */
    public final static String INDI = "INDI",
            FAM = "FAM",
            OBJE = "OBJE",
            NOTE = "NOTE",
            SOUR = "SOUR",
            SUBM = "SUBM",
            REPO = "REPO";

    public final static String[] ENTITIES = {INDI, FAM, OBJE, NOTE, SOUR, SUBM, REPO};

    private final static Map<String, String> E2PREFIX = new HashMap<String, String>();

    static {
        E2PREFIX.put(INDI, "I");
        E2PREFIX.put(FAM, "F");
        E2PREFIX.put(OBJE, "M");
        E2PREFIX.put(NOTE, "N");
        E2PREFIX.put(SOUR, "S");
        E2PREFIX.put(SUBM, "B");
        E2PREFIX.put(REPO, "R");
    }

    private final static Map<String, Class<? extends Entity>> E2TYPE = new HashMap<String, Class<? extends Entity>>();

    static {
        E2TYPE.put(INDI, Indi.class);
        E2TYPE.put(FAM, Fam.class);
        E2TYPE.put(OBJE, Media.class);
        E2TYPE.put(NOTE, Note.class);
        E2TYPE.put(SOUR, Source.class);
        E2TYPE.put(SUBM, Submitter.class);
        E2TYPE.put(REPO, Repository.class);
    }

    private final static Map<String, ImageIcon> E2IMAGE = new HashMap<String, ImageIcon>();

    /** image */
    private final static ImageIcon image = new ImageIcon(Gedcom.class, "images/Gedcom");

    /** submitter of this Gedcom */
    private Submitter submitter;

    /** grammar version */
//  private Grammar grammar = Grammar.V551;
    private Grammar grammar = Grammar.V55;

    /** destination */
    private String destination = DEST_ANY;

    /** origin of this Gedcom */
    private Origin origin;

    /** warnings during last opening of this Gedcom for later retrieval in Gedcom verifications */
    private List<ViewContext> warnings;

    /** last change */
    private PropertyChange lastChange = null;

    /** maximum ID length in file */
    private int maxIDLength = 0;

    /** entities */
    private LinkedList<Entity> allEntities = new LinkedList<>();
    private Map<String, Map<String, Entity>> tag2id2entity = new HashMap<>();

    /** currently collected undos and redos */
    private boolean isDirty = false;
    private List<List<Undo>> undoHistory = new ArrayList<>(),
            redoHistory = new ArrayList<>();
    private boolean undoRedoInProgress = false;

    /** a semaphore we're using for syncing */
    private Object writeSemaphore = new Object();

    /** current lock */
    private Lock lock = null;

    /** listeners */
    private List<GedcomListener> listeners = new CopyOnWriteArrayList<>();

    /** mapping tags refence sets */
    private Map<String, ReferenceSet<String, Property>> tags2refsets = new HashMap<>();

    /** mapping tags to counts */
    private Map<String, Integer> propertyTag2valueCount = new HashMap<>();

    /** encoding */
    private String encoding = ENCODINGS[Math.min(ENCODINGS.length - 1, GedcomOptions.getInstance().getDefaultEncoding())];

    /** language */
    private String language = null;

    /** cached locale */
    private Locale cachedLocale = null;

    /** cached collator */
    private Collator cachedCollator = null;

    /** global place format */
    private String placeFormat = "";

    /** password for private information */
    private String password = null;

    /** name for unnamed gedcom */
    private String noName = null;

    /** registry for this gedcom */
    private Registry registry = null;

    /**
     * Gedcom's Constructor
     */
    public Gedcom() {
        this(null);
    }

    /**
     * Gedcom's Constructor
     */
    public Gedcom(Origin origin) {
        // remember
        this.origin = origin;
        // Done
    }

    /**
     * Returns the origin of this gedcom
     */
    public Origin getOrigin() {
        return origin;
    }

    /**
     * Sets the origin of this gedcom
     */
    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    /**
     * Returns the warnings of this gedcom
     */
    public List<ViewContext> getWarnings() {
        return warnings;
    }

    /**
     * Sets the warnings of this gedcom
     */
    public void setWarnings(List<ViewContext> warnings) {
        this.warnings = warnings;
    }

    /**
     * Set grammar
     */
    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
    }

    /**
     * Return grammar
     */
    public Grammar getGrammar() {
        return grammar;
    }

    /**
     * Set destination
     */
    public void setDestination(String dest) {
        this.destination = dest;
    }

    /**
     * Return grammar
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Returns the submitter of this gedcom (might be null)
     */
    public Submitter getSubmitter() {
        if (submitter == null) {
            return (Submitter) getFirstEntity(Gedcom.SUBM);
        }
        return submitter;
    }

    /**
     * Sets the submitter of this gedcom
     */
    public void setSubmitter(Submitter set) {

        // change it
        if (set != null && !getEntityMap(SUBM).containsValue(set)) {
            throw new IllegalArgumentException("Submitter is not part of this gedcom");
        }

        // flip it
        final Submitter old = submitter;
        submitter = set;

        // no lock? we're done
        if (lock != null) // keep undo
        {
            lock.addChange(new Undo() {
                void undo() {
                    setSubmitter(old);
                }
            });
        }

        // let listeners know
        for (GedcomListener listener : listeners) {
            if (listener instanceof GedcomMetaListener) {
                ((GedcomMetaListener) listener).gedcomHeaderChanged(this);
            }
        }

        // done
    }

    /**
     * toString overridden
     */
    public String toString() {
        return getName();
    }

    /**
     * Adds a Listener which will be notified when data changes
     * XXX: convert to pcs
     */
    public void addGedcomListener(GedcomListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        if (!listeners.add(SafeProxy.harden(listener))) {
            throw new IllegalArgumentException("can't add gedcom listener " + listener + "twice");
        }
        LOG.log(Level.FINER, "addGedcomListener() from " + new Throwable().getStackTrace()[1] + " (now " + listeners.size() + ")");

    }

    /**
     * Removes a Listener from receiving notifications
     */
    public void removeGedcomListener(GedcomListener listener) {
    // 20060101 apparently window lifecycle mgmt including removeNotify() can be called multiple times (for windows
        // owning windows for example) .. so down the line the same listener might unregister twice - we'll just ignore that
        // for now
        if (listeners != null) {
            listeners.remove(SafeProxy.harden(listener));
            LOG.log(Level.FINER, "removeGedcomListener() from " + new Throwable().getStackTrace()[1] + " (now " + listeners.size() + ")");
        }
    }

    /**
     * The undo redo process
     * 
     * FL: 2021-01-10
     * Some mecanisms in Ancestris generate gedcom modifications depending of other modifications
     * UNDO/REDO needs to suspend these macanisms otherwise the generations keep happening during UNDOs/REDOs
     * This method is just a flag that these mecanisms need to use to suspend the geneations during UNDOs and REDOs
     */
    public boolean isUndoRedoInProgress() {
        return undoRedoInProgress;
    }
    
    public void setUndoRedoInProgress(boolean set) {
        undoRedoInProgress = set;
    }


    /**
     * Final destination for a change propagation
     */
    protected void propagateXRefLinked(final PropertyXRef property1, final PropertyXRef property2) {

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Property " + property1.getTag() + " and " + property2.getTag() + " linked");
        }

        // no lock? we're done
        if (lock != null) // keep undo
        {
            lock.addChange(new Undo() {
                void undo() {
                    property1.unlink();
                }
            });
        }

        // let listeners know
        for (GedcomListener listener : listeners) {
            listener.gedcomPropertyChanged(this, property1);
            listener.gedcomPropertyChanged(this, property2);
        }

        // done
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagateXRefUnlinked(final PropertyXRef property1, final PropertyXRef property2) {

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Property " + property1.getTag() + " and " + property2.getTag() + " unlinked");
        }

        // no lock? we're done
        if (lock != null) // keep undo
        {
            lock.addChange(new Undo() {
                void undo() {
                    property1.link(property2);
                }
            });
        }

        // let listeners know
        for (GedcomListener listener : listeners) {
            listener.gedcomPropertyChanged(this, property1);
            listener.gedcomPropertyChanged(this, property2);
        }

        // done
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagateEntityAdded(final Entity entity) {

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Entity " + entity.getId() + " added");
        }

        // no lock? we're done
        if (lock != null) // keep undo
        {
            lock.addChange(new Undo() {
                void undo() {
                    deleteEntity(entity);
                }
            });
        }

        // let listeners know
        for (GedcomListener listener : listeners) {
            listener.gedcomEntityAdded(this, entity);
        }

        // done
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagateEntityDeleted(final Entity entity) {

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Entity " + entity.getId() + " deleted");
        }

        // no lock? we're done
        if (lock != null) // keep undo
        {
            lock.addChange(new Undo() {
                void undo() throws GedcomException {
                    addEntity(entity);
                }
            });
        }

        // let listeners know
        for (GedcomListener listener : listeners) {
            listener.gedcomEntityDeleted(this, entity);
        }

        // done
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagatePropertyAdded(Entity entity, final Property container, final int pos, final Property added) {

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Property " + added.getTag() + " added to " + container.getTag() + " at position " + pos + " (entity " + entity.getId() + ")");
        }

        // track counts for value properties (that's none references)
        if (!(added instanceof PropertyXRef)) {
            Integer count = propertyTag2valueCount.get(added.getTag());
            propertyTag2valueCount.put(added.getTag(), count == null ? 1 : count + 1);
        }

        // If there is a lock, add to undo
        if (lock != null) // keep undo
        {
            lock.addChange(new Undo() {
                void undo() {
                    container.delProperty(pos);
                }
            });
        }

        // let listeners know
        // FIXME: Dan not sure whether undo history is always accurate
        // specially when properties are added and remove with mixed mode (with and w/o doUnitOfWork)
        // as Undoable properties are identified by their position index
        for (GedcomListener listener : listeners) {
            listener.gedcomPropertyAdded(this, container, pos, added);
        }

        // done
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagatePropertyDeleted(Entity entity, final Property container, final int pos, final Property deleted) {

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Property " + deleted.getTag() + " deleted from " + container.getTag() + " at position " + pos + " (entity " + entity.getId() + ")");
        }

        // track counts for value properties (that's none references)
        if (!(deleted instanceof PropertyXRef)) {
            propertyTag2valueCount.put(deleted.getTag(), propertyTag2valueCount.get(deleted.getTag()) - 1);
        }

        // no lock? we're done
        if (lock != null) // keep undo
        {
            lock.addChange(new Undo() {
                void undo() {
                    container.addProperty(deleted, pos);
                }
            });
        }

        // let listeners know
        for (GedcomListener listener : listeners) {
            listener.gedcomPropertyDeleted(this, container, pos, deleted);
        }

        // done
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagatePropertyChanged(Entity entity, final Property property, final String oldValue) {

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Property " + property.getTag() + " changed in (entity " + entity.getId() + ")");
        }

        // no lock? we're done
        if (lock != null) // keep undo
        {
            lock.addChange(new Undo() {
                void undo() {
                    property.setValue(oldValue);
                }
            });
        }

        // notify
        for (GedcomListener listener : listeners) {
            listener.gedcomPropertyChanged(this, property);
        }

        // done
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagatePropertyMoved(final Property property, final Property moved, final int from, final int to) {

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Property " + property.getTag() + " moved from " + from + " to " + to + " (entity " + property.getEntity().getId() + ")");
        }

        // no lock? we're done
        if (lock != null) // keep undo
        {
            lock.addChange(new Undo() {
                void undo() {
                    property.moveProperty(moved, from < to ? from : from + 1);
                }
            });
        }

        // notify
        for (GedcomListener listener : listeners) {
            listener.gedcomPropertyDeleted(this, property, from, moved);
            listener.gedcomPropertyAdded(this, property, to, moved);
        }

        // done
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagateWriteLockAqcuired() {
        for (GedcomListener listener : listeners) {
            if (listener instanceof GedcomMetaListener) {
                ((GedcomMetaListener) listener).gedcomWriteLockAcquired(this);
            }
        }
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagateBeforeUnitOfWork() {
        for (GedcomListener listener : listeners) {
            if (listener instanceof GedcomMetaListener) {
                ((GedcomMetaListener) listener).gedcomBeforeUnitOfWork(this);
            }
        }
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagateAfterUnitOfWork() {
        for (GedcomListener listener : listeners) {
            if (listener instanceof GedcomMetaListener) {
                ((GedcomMetaListener) listener).gedcomAfterUnitOfWork(this);
            }
        }
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagateWriteLockReleased() {

        for (GedcomListener listener : listeners) {
            if (listener instanceof GedcomMetaListener) {
                ((GedcomMetaListener) listener).gedcomWriteLockReleased(this);
            }
        }
    }

    /**
     * Final destination for a change propagation
     */
    protected void propagateEntityIDChanged(final Entity entity, final String old) throws GedcomException {

        Map<String, Entity> id2entity = getEntityMap(entity.getTag());

        // known?
        if (!id2entity.containsValue(entity)) {
            throw new GedcomException("Can't change ID of entity not part of this Gedcom instance");
        }

        // valid prefix/id?
        String id = entity.getId();
        if (id == null || id.length() == 0) {
            throw new GedcomException("Need valid ID length");
        }

        // dup?
        if (getEntity(id) != null) {
            throw new GedcomException("Duplicate ID is not allowed");
        }

        // do the housekeeping
        id2entity.remove(old);
        id2entity.put(entity.getId(), entity);

        // remember maximum ID length
        maxIDLength = Math.max(id.length(), maxIDLength);

        // log it
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Entity's ID changed from  " + old + " to " + entity.getId());
        }

        // no lock? we're done
        if (lock != null) // keep undo
        {
            lock.addChange(new Undo() {
                void undo() throws GedcomException {
                    entity.setId(old);
                }
            });
        }

        // notify
        for (GedcomListener listener : listeners) {
            listener.gedcomPropertyChanged(this, entity);
        }

        // done
    }

    /**
     * Add entity
     */
    private void addEntity(Entity entity) throws GedcomException {

        String id = entity.getId();

    // some entities (event definitions for example) don't have an
        // id - we'll keep them in our global list but not mapped id->entity
        if (id.length() > 0) {
            Map<String, Entity> id2entity = getEntityMap(entity.getTag());
            if (id2entity.containsKey(id)) {
                throw new GedcomException(resources.getString("error.entity.dupe", id));
            }

            // remember id2entity
            id2entity.put(id, entity);
        }

        // remember entity
        allEntities.add(entity);

        // notify
        entity.addNotify(this);

    }

    /**
     * Accessor - last change
     *
     * @return change or null
     */
    public PropertyChange getLastChange() {
        return lastChange;
    }

    /**
     * Accessor - last change
     */
    protected void updateLastChange(PropertyChange change) {
        if (lastChange == null || lastChange.compareTo(change) < 0) {
            lastChange = change;
        }
    }

    /**
     * Creates a non-related entity with id
     */
    public Entity createEntity(String tag) throws GedcomException {
        return createEntity(tag, null);
    }

    /**
     * Create a entity by tag
     *
     * @exception GedcomException in case of unknown tag for entity
     */
    public Entity createEntity(String tag, String id) throws GedcomException {

        // generate new id if necessary - otherwise trim it
        if (id == null) {
            id = getNextAvailableID(tag);
        }

        // remember maximum ID length
        maxIDLength = Math.max(id.length(), maxIDLength);

        // lookup a type - all well known types need id
        Class<? extends Entity> clazz = (Class<? extends Entity>) E2TYPE.get(tag);
        if (clazz != null) {
            if (id.length() == 0) {
                throw new GedcomException(resources.getString("error.entity.noid", tag));
            }
        } else {
            clazz = Entity.class;
        }

        // Create entity
        Entity result;
        try {
            result = (Entity) clazz.getDeclaredConstructor(String.class, String.class).newInstance(tag, id);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException t) {
            throw new RuntimeException("Can't instantiate " + clazz, t);
        }
        
        result.setNew();

        // keep it
        addEntity(result);

        // Done
        return result;
    }

    /**
     * Deletes entity
     *@param which : Entity to delete.
     */
    public void deleteEntity(Entity which) {

        // Prevent removal of last individual (causes troubles in a number of modules)
        if (which instanceof Indi && getEntities(INDI).size() == 1) {
            DialogManager.createError(resources.getString("error.entity.cannotdelete.title"), resources.getString("error.entity.cannotdelete.msg"))
                    .setDialogId("error.entity.cannotdelete").show();
            return;
        }
        
        
        // Some entities dont' have ids (event definitions for example) - for
        // all others we check the id once more
        String id = which.getId();
        if (id.length() > 0) {

            // Lookup entity map
            Map<String, Entity> id2entity = getEntityMap(which.getTag());

            // id exists ?
            if (!id2entity.containsKey(id)) {
                throw new IllegalArgumentException("Unknown entity with id " + which.getId());
            }

            // forget id
            id2entity.remove(id);
        }

        // Tell it first
        which.beforeDelNotify();

        // Forget it now
        allEntities.remove(which);

        // was it the submitter?    
        if (submitter == which) {
            submitter = null;
        }

        // Done
    }

    /**
     * Internal entity lookup
     */
    private Map<String, Entity> getEntityMap(String tag) {
        // lookup map of entities for tag
        Map<String, Entity> id2entity = tag2id2entity.get(tag);
        if (id2entity == null) {
            id2entity = new HashMap<>();
            tag2id2entity.put(tag, id2entity);
        }
        // done
        return id2entity;
    }

    /**
     * Returns all properties for given path
     */
    public Property[] getProperties(TagPath path) {
        ArrayList<Property> result = new ArrayList<>(100);
        for (Entity ent : getEntities(path.getFirst())) {
            Property[] props = ent.getProperties(path);
            for (int i = 0; i < props.length; i++) {
                result.add(props[i]);
            }
        }
        return Property.toArray(result);
    }

    /**
     * Returns all properties for given path
     */
    public List<? extends Property> getPropertiesByClass(Class<? extends Property> clazz) {
        List<Property> props = new ArrayList();
        Collection<Entity> entities = getEntities();
        for (Entity ent : entities) {
            props.addAll(((Property) ent).getProperties(clazz));
        }
        return props;
    }
    
    /**
     * Count statistics for property tag
     */
    public int getPropertyCount(String tag) {
        Integer result = propertyTag2valueCount.get(tag);
        return result == null ? 0 : result;
    }

    /**
     * Returns all entities
     */
    public List<Entity> getEntities() {
        return Collections.unmodifiableList(allEntities);
    }

    /**
     * Returns entities of given type
     */
    public Collection<? extends Entity> getEntities(String tag) {
        return Collections.unmodifiableCollection(getEntityMap(tag).values());
    }

    /**
     * Returns entities of given type FAM
     */
    @SuppressWarnings("unchecked")
    public Collection<Fam> getFamilies() {
        return (Collection<Fam>) getEntities(FAM);
    }

    /**
     * Returns entities of given type INDI
     */
    @SuppressWarnings("unchecked")
    public Collection<Indi> getIndis() {
        return (Collection<Indi>) getEntities(INDI);
    }

    /**
     * Returns entities of given type NOTE
     */
    @SuppressWarnings("unchecked")
    public Collection<Note> getNotes() {
        return (Collection<Note>) getEntities(NOTE);
    }

    /**
     * Returns entities of given type SOURCE
     */
    @SuppressWarnings("unchecked")
    public Collection<Source> getSources() {
        return (Collection<Source>) getEntities(SOUR);
    }

    /**
     * Returns entities of given type OBJE
     */
    @SuppressWarnings("unchecked")
    public Collection<Media> getMedias() {
        return (Collection<Media>) getEntities(OBJE);
    }

    /**
     * Returns entities of given type sorted by given path (can be empty or null)
     */
    public Entity[] getEntities(String tag, String sortPath) {
        return getEntities(tag, sortPath != null && sortPath.length() > 0 ? new PropertyComparator(sortPath) : null);
    }

    /**
     * Returns entities of given type sorted by comparator (can be null)
     */
    public Entity[] getEntities(String tag, Comparator<Property> comparator) {
        Collection<Entity> ents = getEntityMap(tag).values();
        Entity[] result = ents.toArray(new Entity[ents.size()]);
        // sort by comparator or entity
        if (comparator != null) {
            Arrays.sort(result, comparator);
        } else {
            Arrays.sort(result);
        }
        // done
        return result;
    }

    /**
     * Returns the entity with given id (or null)
     */
    public Entity getEntity(String id) {
        // loop all types
        for (Map<String, Entity> ents : tag2id2entity.values()) {
            Entity result = ents.get(id);
            if (result != null) {
                return result;
            }
        }

        // not found
        return null;
    }

    /**
     * Returns the entity with given id of given type or null if not exists
     */
    public Entity getEntity(String tag, String id) {
        // check back in appropriate type map
        return getEntityMap(tag).get(id);
    }

    /**
     * Returns the deCujus indi lookin through SOSA tags
     * @return decujus indi
     */
    public Indi getDeCujusIndi() {
        // Get all individuals and stop when sosa 1 is found
        Collection <Indi>entities = (Collection <Indi>) getEntities(Gedcom.INDI);
        Property[] props = null;
        String sosaStr = "";
        for (Indi indi : entities) {
            props = indi.getProperties(Indi.TAG_SOSA);
            if (props != null) {
                for (Property prop : props) {
                    sosaStr = prop.getDisplayValue();
                    if ("1".equals(sosaStr) || "1 G1".equals(sosaStr)) {
                        return indi;
                    }
                }
            }
            props = indi.getProperties(Indi.TAG_SOSADABOVILLE);
            if (props != null) {
                for (Property prop : props) {
                    sosaStr = prop.getDisplayValue();
                    if ("1".equals(sosaStr) || "1 G1".equals(sosaStr)) {
                        return indi;
                    }
                }
            }
        }

        return null;
    }

    

    /**
     * Returns a type for given tag
     */
    public static Class<? extends Entity> getEntityType(String tag) {
        return (Class<? extends Entity>) E2TYPE.get(tag);
    }

    /**
     * Returns any instance of entity with given type if exists
     */
    public Entity getFirstEntity(String tag) {
        // loop over entities and return first of given type
        for (Entity e : allEntities) {
            if (e.getTag().equals(tag)) {
                return e;
            }
        }
        // can't help 
        return null;
    }

    /**
     * Return the next available ID for given type of entity
     */
    public String getNextAvailableID(String entity) {

        // Lookup current entities of type
        Map<String, Entity> id2entity = getEntityMap(entity);

    // Look for an available ID
        // 20080121 if there's no entity yet we start with '1' 
        // 20060124 used to start with id2entity.size()+1 for !isFillGapsInIDs since
        // n people already there should optimistically cover 1..n so we can continue
        // with n+1. IF the user started id'ing with 0 then the covered range is 
        // 0..(n-1) though (Philip reported a file like that). So just for that case 
        // let's start at n and let the loop for checking existing IDs move forward
        // once if necessary
        int id = GedcomOptions.getInstance().isFillGapsInIDs() ? 1 : (id2entity.isEmpty() ? 1 : id2entity.size());

        StringBuilder buf = new StringBuilder(maxIDLength);

        search:
        while (true) {

      // 20050619 back to checking all IDs with max id length padding
            // since we don't want to assign I1 if there's a I01 already - got
            // a file from Anton written by Gramps that has these kinds of
            // 'duplicates' all over
            buf.setLength(0);
            buf.append(getEntityPrefix(entity));
            buf.append(id);

            while (true) {
                if (id2entity.containsKey(buf.toString())) {
                    break;
                }
                if (buf.length() >= maxIDLength) {
                    break search;
                }
                buf.insert(1, '0');
            }

            // try next
            id++;
        }
    // 20050509 not patching IDs with zeros anymore - since we now have alignment
        // in tableview there's not really a need to add leading zeros for readability.
        buf.setLength(0);
        buf.append(id);
        while (buf.length() < idLength(entity)) {
            buf.insert(0, '0');
        }

        return getEntityPrefix(entity) + buf;
    }

    private int idLength(String tag) {
        int length = GedcomOptions.getInstance().getEntityIdLength();
        Entity first = getFirstEntity(tag);
        if (first != null
                && first.getId().matches("[a-zA-Z][0-9]*")) {
            length = first.getId().length() - 1;
        }
        return length;
    }

    /**
     * Has the gedcom unsaved changes ?
     */
    public boolean hasChanged() {
        return isDirty || !undoHistory.isEmpty();
    }

    /**
     * Clears flag for unsaved changes
     */
    public void setUnchanged() {

        // is dirty?
        if (!hasChanged()) {
            return;
        }

        // do it
        undoHistory.clear();
        isDirty = false;

        // no lock? we're done
        if (lock != null) // let listeners know
        {
            for (GedcomListener listener : listeners) {
                if (listener instanceof GedcomMetaListener) {
                    ((GedcomMetaListener) listener).gedcomHeaderChanged(this);
                }
            }
        }

        // done
    }

    /**
     * Test for write lock
     */
    public boolean isWriteLocked() {
        return lock != null;
    }

    /**
     * Perform a unit of work - don't throw any exception as they can't be handled
     */
    public void doMuteUnitOfWork(UnitOfWork uow) {
        try {
            doUnitOfWork(uow);
        } catch (GedcomException e) {
            LOG.log(Level.WARNING, "Unexpected gedcom exception", e);
        }
    }

    /**
     * Starts a transaction
     */
    public void doUnitOfWork(UnitOfWork uow) throws GedcomException {

        PropertyChange.Monitor updater;

        // grab lock
        synchronized (writeSemaphore) {

            if (lock != null) {
                throw new GedcomException("Cannot obtain write lock");
            }
            lock = new Lock();

            // hook up updater for changes
            updater = new PropertyChange.Monitor();
            addGedcomListener(updater);

            // reset redos
            redoHistory.clear();

        }

        // let listeners know
        propagateWriteLockAqcuired();

        // run the runnable
        Throwable rethrow = null;
        try {
            uow.perform(this);
        } catch (Throwable t) {
            rethrow = t;
        }

        synchronized (writeSemaphore) {

            // keep undos (within limits)
            if (!lock.undos.isEmpty()) {
                undoHistory.add(lock.undos);

                while (undoHistory.size() > GedcomOptions.getInstance().getNumberOfUndos()) {
                    undoHistory.remove(0);
                    isDirty = true;
                }
            }

            // let listeners know
            propagateWriteLockReleased();

            // release
            lock = null;

            // unhook updater for changes
            removeGedcomListener(updater);
        }

        // log
        LOG.log(Level.FINE, "End of UOW, property counts " + propertyTag2valueCount);

        // done
        if (rethrow != null) {
            if (rethrow instanceof GedcomException) {
                throw (GedcomException) rethrow;
            }
            throw new RuntimeException(rethrow);
        }
    }

    /**
     * Test for undo
     */
    public boolean canUndo() {
        return !undoHistory.isEmpty();
    }

    /*
     * Get Undo remaining Un-do Action
     */
    public int getUndoNb() {
        return undoHistory.size();
    }

    /**
     * Performs an undo
     */
    public void undoUnitOfWork() {
        undoUnitOfWork(true);
    }

    /**
     * Performs an undo
     */
    public void undoUnitOfWork(boolean keepRedo) {

        // there?
        if (undoHistory.isEmpty()) {
            throw new IllegalArgumentException("undo n/a");
        }

        synchronized (writeSemaphore) {

            if (lock != null) {
                throw new IllegalStateException("Cannot obtain write lock");
            }
            lock = new Lock();

        }

        // let listeners know
        propagateWriteLockAqcuired();

        // run through undos
        setUndoRedoInProgress(true);
        
        List<Undo> todo = undoHistory.remove(undoHistory.size() - 1);
        for (int i = todo.size() - 1; i >= 0; i--) {
            Undo undo = todo.remove(i);
            try {
                undo.undo();
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Unexpected throwable during undo()", t);
            }
        }
        
        setUndoRedoInProgress(false);

        synchronized (writeSemaphore) {

            // keep redos
            if (keepRedo) {
                redoHistory.add(lock.undos);
            }

            // let listeners know
            propagateWriteLockReleased();

            // release
            lock = null;
        }

        // done
    }

    /**
     * Test for redo
     */
    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }

    /**
     * Performs a redo
     */
    public void redoUnitOfWork() {

        // there?
        if (redoHistory.isEmpty()) {
            throw new IllegalArgumentException("redo n/a");
        }

        synchronized (writeSemaphore) {

            if (lock != null) {
                throw new IllegalStateException("Cannot obtain write lock");
            }
            lock = new Lock();

        }

        // let listeners know
        propagateWriteLockAqcuired();

        // run the redos
        setUndoRedoInProgress(true);
        
        List<Undo> todo = redoHistory.remove(redoHistory.size() - 1);
        for (int i = todo.size() - 1; i >= 0; i--) {
            Undo undo = todo.remove(i);
            try {
                undo.undo();
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Unexpected throwable during undo()", t);
            }
        }
        
        setUndoRedoInProgress(false);

        // release
        synchronized (writeSemaphore) {

            // keep undos
            undoHistory.add(lock.undos);

            // let listeners know
            propagateWriteLockReleased();

            // clear
            lock = null;
        }

    // done
    }

    /**
     * Get a reference set for given tag
     * @param tag
     */
    public ReferenceSet<String, Property> getReferenceSet(String tag) {
        // lookup
        ReferenceSet<String, Property> result = tags2refsets.get(tag);
        if (result == null) {
            // .. instantiate if necessary
            result = new ReferenceSet<String, Property>();
            tags2refsets.put(tag, result);
            // .. and pre-fill
            String defaults = Gedcom.resources.getString(tag + ".vals", false);
            if (defaults != null) {
                StringTokenizer tokens = new StringTokenizer(defaults, ",");
                while (tokens.hasMoreElements()) {
                    result.add(tokens.nextToken().trim(), null);
                }
            }
        }
        // done
        return result;
    }

    /**
     * Returns the name of this gedcom or null if unnamed
     */
    public String getName() {
        return origin == null ? noName : origin.getName();
    }

    public void setName(String noName) {
        this.noName = noName;
    }
    
    /**
     * Returns the displayed name of the Gedcom file
     * @return 
     */
    public String getDisplayName() {
        String name = getName();
        if (name == null || name.isEmpty()) {
            return "";
        }
        name = name.replace("_", " ");
        char[] delimiters = {' ', '-'};
        return WordUtils.capitalize(name.substring(0, name.lastIndexOf(".") == -1 ? name.length() : name.lastIndexOf(".")), delimiters);
    }

    /**
     * Complete path with name of the GEDCOM file.
     * @return The complete name with path.
     */
    public String getFilePath() {
        if (origin == null) {
            return noName;
        }
        final File file = origin.getFile();
        if (file != null) {
            try {
                return origin.getFile().getCanonicalPath();
            } catch (IOException e) {
                LOG.log(Level.FINE, "Unexpected IOException during name retrieval", e);
            }
        }
        return noName;
    }

    /**
     * Returns a readable name for the given tag
     */
    public static String getName(String tag) {
        return getName(tag, false);
    }

    /**
     * Returns the readable name for the given tag
     */
    public static String getName(String tag, boolean plural) {
        if (plural) {
            String name = resources.getString(tag + ".s.name", false);
            if (name != null) {
                return name;
            }
        }
        String name = resources.getString(tag + ".name", false);
        return name != null ? name : tag;
    }

    /**
     * XXX: Can we use MetaProperty.getInfo()?
     * Accessor - some explanationary information about a tag
     */
    public static String getInfo(String tag) {
        return resources.getString(tag + ".info", false);
    }

    /**
     * Returns the prefix of the given entity
     */
    public static String getEntityPrefix(String tag) {
        String result = E2PREFIX.get(tag);
        if (result == null) {
            result = "X";
        }
        return result;
    }

    /**
     * Returns an image for Gedcom
     */
    public static ImageIcon getImage() {
        return image;
    }

    /**
     * Returns an image for given entity type
     */
    public static ImageIcon getEntityImage(String tag) {
        ImageIcon result = E2IMAGE.get(tag);
        if (result == null) {
            result = Grammar.V55.getMeta(new TagPath(tag)).getImage();
            E2IMAGE.put(tag, result);
        }
        return result;
    }

    /**
     * Returns the Resources (lazily)
     */
    public static Resources getResources() {
        return resources;
    }

    /**
     * Helper that returns registry for gedcom
     * <p/>
     * @return Registry for this gedcom
     */
     /* TODO: getRegistry(gedcom) a mettre ailleurs
     * le fichier gedcom.properties est maintenant dans le home user dir
     * DAN 20101230: now in PreferencesRoot/gedcoms/settings/...
     * TODO: Attention cela a pour inconvenient de ne par pouvoir ouvrir (dans la vie d'ancestris)
     * TODO: deux fichiers portant le meme nom sans collision des reglages.
     * TODO: dans l'avenir on pourra marquer les proprietes par un id que l'on attachera au
     * TODO: fichier gedcom dans une des proprietes.
     * FIXME: mettre dans ancestrispreferences
    */
    public Registry getRegistry() {
        if (registry == null) {
            registry = Registry.get("gedcoms/settings/" + getName());
        }
        return registry;
    }

    /**
     * Accessor - encoding
     * @return 
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Accessor - encoding
     * @param set char encoding to use
     */
    public void setEncoding(String set) {
        encoding = set;
    }

    /**
     * Accessor - place format
     * @return place format
     */
    public String getPlaceFormat() {
        return placeFormat;
    }

    /**
     * Accessor - place format
     */
    public void setPlaceFormat(String set) {
        placeFormat = set.trim();
    }

    /** show place fomat getter and setter (thru registry) */
    public void setShowJuridictions(Boolean[] showFormat) {
        getRegistry().put(GedcomOptions.SHOW_PLACE_FORMAT, showFormat);
    }

    /** if no show-place-juridiction in gedcom registry then returns null that means show everything */
    public Boolean[] getShowJuridictions() {
        return getRegistry().get(GedcomOptions.SHOW_PLACE_FORMAT, (Boolean[]) null);
    }

    /**
     * Set place sort order. helper method to put it in gedcoms registry.
     *
     * @param placeSortOrder if format 1,0,2 for example which means 2nd jurisdiction, then 1rst and 3rd
     */
    public void setPlaceSortOrder(String placeSortOrder) {
        getRegistry().put(GedcomOptions.PLACE_SORT_ORDER, placeSortOrder);
    }

    /**
     * get Place sort order. Null means default, @see PropertyPlace.getValueStartingWithCity()
     *
     * @return
     */
    public String getPlaceSortOrder() {
        return getRegistry().get(GedcomOptions.PLACE_SORT_ORDER, GedcomOptions.getInstance().getPlaceSortOrder());
    }

    /**
     * set default place display format. @see PropertyPlace.format
     *
     * @param placeDisplayFormat
     */
    public void setPlaceDisplayFormat(String placeDisplayFormat) {
        getRegistry().put(GedcomOptions.PLACE_DISPLAY_FORMAT, placeDisplayFormat);
    }

    /**
     * Get place display format starting with city
     */
    public String getPlaceDisplayFormatStartingWithCity() {
        String displayFormat = "";
        String city = PropertyPlace.getCityTag(this);
        String[] jurisdictions = PropertyPlace.getFormat(this);
        for (int i = 0; i < jurisdictions.length; i++) {
            if (jurisdictions[i].equals(city)) {
                displayFormat += i + PropertyPlace.JURISDICTION_SEPARATOR;
            }
        }
        for (int i = 0; i < jurisdictions.length; i++) {
            if (!jurisdictions[i].equals(city)) {
                displayFormat += i + PropertyPlace.JURISDICTION_SEPARATOR;
            }
        }
        return displayFormat;
    }
    
    
    /**
     * getter. Get Place Display Format option for this gedcom.
     * Defaults to global preferences.
     *
     * @return
     */
    public String getPlaceDisplayFormat() {
        return getRegistry().get(GedcomOptions.PLACE_DISPLAY_FORMAT, 
                GedcomOptions.getInstance().getPlaceDisplayFormat());
    }

    /**
     * Accessor - language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Accessor - encoding
     */
    public void setLanguage(String set) {
        language = set;
    }

    /**
     * Accessor - password
     */
    public void setPassword(String set) {
        password = set;
    }

    /**
     * Accessor - password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Accessor - password
     */
    public boolean hasPassword() {
        return password != null;
    }

    /**
     * Check for containment
     */
    public boolean contains(Entity entity) {
        return getEntityMap(entity.getTag()).containsValue(entity);
    }

    /**
     * Return an appropriate Locale instance
     */
    public Locale getLocale() {

        // not known?
        if (cachedLocale == null) {

            // known language?
            if (language != null) {

                // look for it
                Locale[] locales = Locale.getAvailableLocales();
                for (int i = 0; i < locales.length; i++) {
                    if (locales[i].getDisplayLanguage(Locale.ENGLISH).equalsIgnoreCase(language)) {
                        cachedLocale = new Locale(locales[i].getLanguage(), Locale.getDefault().getCountry());
                        break;
                    }
                }

            }

            // default?
            if (cachedLocale == null) {
                cachedLocale = Locale.getDefault();
            }

        }

        // done
        return cachedLocale;
    }

    /**
     * Return an appropriate Collator instance
     */
    public Collator getCollator() {

        // not known?
        if (cachedCollator == null) {
            cachedCollator = Collator.getInstance(getLocale());

      // 20050505 when comparing gedcom values we really don't want it to be
            // done case sensitive. It surfaces in many places (namely for example
            // in prefix matching in PropertyTableWidget) so I'm restricting comparison
            // criterias to PRIMARY from now on
            cachedCollator.setStrength(Collator.PRIMARY);
        }

        // done
        return cachedCollator;
    }

    /**
     * can be compared by name
     */
    @Override
    public int compareTo(Object other) {
        //FIXME:       throw new UnsupportedOperationException("comparaison de deux ged a enlever");
        Gedcom that = (Gedcom) other;
        if (that == null || that.getName() == null) {
            return 1;
        }
        if (getName() == null) {
            return -1;
        }
        return getName().compareTo(that.getName());
    }

    ;

    public void initLanguages() {
        // Define key map of english name of language (String to store in Gedcom) pointing to language name of language with default language name (String to display)
        String language;
        for (int i = 0; i < Gedcom.LANGUAGES.length; i++) {
            if (LOCALES[i].equals("?")) {
                language = Gedcom.LANGUAGES[i];
            } else {
                Locale loc = new Locale(LOCALES[i]);
                language = loc.getDisplayLanguage(loc);
                if (!loc.getDisplayLanguage(loc).equals(loc.getDisplayLanguage(Locale.getDefault()))) {
                    language += " (" + loc.getDisplayLanguage(Locale.getDefault()) + ")";
                }
            }
            TRANSLATED_LANGUAGES.put(Gedcom.LANGUAGES[i], language);
        }

    }

    /**
     * Free up memory
     */
    public void eraseAll() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                freeUpMemory();
            }
        });
         
    }
    
    private void eraseProperties(Property parent) {
        try {
            for (Property child : parent.getProperties()) {
                eraseProperties(child);
            }
            parent.eraseAll();
            parent = null;
        } catch (Exception e) {
        }
    }

    private void freeUpMemory() {
        
        // LinkedList<Entity> allEntities
        for (Entity ent : allEntities) {
            eraseProperties(ent);
            ent.eraseAll();
        }
        allEntities.clear();
        
        // Map<String, Integer> propertyTag2valueCount  
        for (Iterator<Map.Entry<String, Integer>> it = propertyTag2valueCount.entrySet().iterator(); it.hasNext();) {
            it.next();
            it.remove();
        }

        // Map<String, Map<String, Entity>> tag2id2entity
        for (Iterator<Map.Entry<String, Map<String, Entity>>> it = tag2id2entity.entrySet().iterator(); it.hasNext();) {
            Map<String, Entity> map = it.next().getValue();
            for (Iterator<Map.Entry<String, Entity>> it2 = map.entrySet().iterator(); it2.hasNext();) {
                it2.next();
                it2.remove();
            }
            it.remove();
        }
        
        // Map<String, ReferenceSet<String, Property>> tags2refsets
        for (Iterator<Map.Entry<String, ReferenceSet<String, Property>>> it = tags2refsets.entrySet().iterator(); it.hasNext();) {
            ReferenceSet<String, Property> refset = it.next().getValue();
            refset.eraseAll();
            refset = null;
            it.remove();
        }
        
        // List<List<Undo>> undoHistory  
        for (Iterator<List<Undo>> it = undoHistory.iterator(); it.hasNext();) {
            for (Iterator<Undo> it2 = it.next().iterator(); it2.hasNext();) {
                it2.next();
                it2.remove();
            }
            it.remove();
        }
                
        // List<List<Undo>> redoHistory  
        for (Iterator<List<Undo>> it = redoHistory.iterator(); it.hasNext();) {
            for (Iterator<Undo> it2 = it.next().iterator(); it2.hasNext();) {
                it2.next();
                it2.remove();
            }
            it.remove();
        }
                
    }

    
    
    
  /**
   * Undo
   */
  private abstract class Undo {

        abstract void undo() throws GedcomException;
    }

    /**
     * Our locking mechanism is based on one writer at a time
     */
    private class Lock {

        List<Undo> undos = new ArrayList<Undo>();

        void addChange(Undo run) {
            undos.add(run);
        }

    }

} //Gedcom
