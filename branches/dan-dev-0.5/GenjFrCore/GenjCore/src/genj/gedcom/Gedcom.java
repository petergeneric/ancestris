/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * $Revision: 1.138 $ $Author: nmeier $ $Date: 2010-01-28 14:48:13 $
 */
package genj.gedcom;

import genj.util.Origin;
import genj.util.ReferenceSet;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.SafeProxy;
import genj.util.swing.ImageIcon;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The object-representation of a Gedom file
 */
//TODO: enlever la comparaison de deux gedcom
public class Gedcom implements Comparable {
  
  final static Logger LOG = Logger.getLogger("genj.gedcom");
  final static private Random seed = new Random();
  final static Resources resources = Resources.get(Gedcom.class);

  public final static String PASSWORD_UNKNOWN = "unknown";
  
  public static final String
   // standard Gedcom encodings 
    UNICODE  = "UNICODE", 
    ASCII    = "ASCII",      // we're using ISO-8859-1 actually to make extended characters possible - the spec is grayish on that one
    ANSEL    = "ANSEL",
    UTF8     = "UTF-8", // since 5.5.1
   // non-standard encodings
    LATIN1   = "LATIN1",     // a.k.a ISO-8859-1
    ANSI     = "ANSI";       // a.k.a. Windows-1252 (@see http://www.hclrss.demon.co.uk/demos/ansi.html)
  
  /** encodings including the non Gedcom-standard encodings LATIN1 and ANSI */  
  public static final String[] ENCODINGS = { 
    ANSEL, UNICODE, ASCII, LATIN1, ANSI, UTF8 
  };

  /** languages as defined by the Gedcom standard */  
  public static final String[] LANGUAGES = {
    "Afrikaans","Albanian","Amharic","Anglo-Saxon","Arabic","Armenian","Assamese",
    "Belorusian","Bengali","Braj","Bulgarian","Burmese", 
    "Cantonese","Catalan","Catalan_Spn","Church-Slavic","Czech", 
    "Danish","Dogri","Dutch", 
    "English","Esperanto","Estonian", 
    "Faroese","Finnish","French", 
    "Georgian","German","Greek","Gujarati", 
    "Hawaiian","Hebrew","Hindi","Hungarian", 
    "Icelandic","Indonesian","Italian",
    "Japanese", 
    "Kannada","Khmer","Konkani","Korean",
    "Lahnda","Lao","Latvian","Lithuanian", 
    "Macedonian","Maithili","Malayalam","Mandrin","Manipuri","Marathi","Mewari", 
    "Navaho","Nepali","Norwegian",
    "Oriya", 
    "Pahari","Pali","Panjabi","Persian","Polish","Prakrit","Pusto","Portuguese", 
    "Rajasthani","Romanian","Russian", 
    "Sanskrit","Serb","Serbo_Croa","Slovak","Slovene","Spanish","Swedish", 
    "Tagalog","Tamil","Telugu","Thai","Tibetan","Turkish", 
    "Ukrainian","Urdu", 
    "Vietnamese", 
    "Wendic" ,
    "Yiddish"
  };

  /** record tags */
  public final static String
    INDI = "INDI", 
    FAM  = "FAM" ,
    OBJE = "OBJE", 
    NOTE = "NOTE", 
    SOUR = "SOUR", 
    SUBM = "SUBM", 
    REPO = "REPO";
    
  public final static String[] 
    ENTITIES = { INDI, FAM, OBJE, NOTE, SOUR, SUBM, REPO };      

  private final static Map<String,String>
    E2PREFIX = new HashMap<String,String>();
    static {
      E2PREFIX.put(INDI, "I");
      E2PREFIX.put(FAM , "F");
      E2PREFIX.put(OBJE, "M");
      E2PREFIX.put(NOTE, "N");
      E2PREFIX.put(SOUR, "S");
      E2PREFIX.put(SUBM, "B");
      E2PREFIX.put(REPO, "R");
    }
    
  private final static Map<String, Class<? extends Entity>> 
    E2TYPE = new HashMap<String, Class<? extends Entity>>();
    static {
      E2TYPE.put(INDI, Indi.class);
      E2TYPE.put(FAM , Fam .class);
      E2TYPE.put(OBJE, Media.class);
      E2TYPE.put(NOTE, Note.class);
      E2TYPE.put(SOUR, Source.class);
      E2TYPE.put(SUBM, Submitter.class);
      E2TYPE.put(REPO, Repository.class);
    }
    
  private final static Map<String,ImageIcon>
    E2IMAGE = new HashMap<String,ImageIcon>();

  /** image */
  private final static ImageIcon image = new ImageIcon(Gedcom.class, "images/Gedcom");
  
  /** submitter of this Gedcom */
  private Submitter submitter;
  
  /** grammar version */
  private Grammar grammar = Grammar.V551;

  /** origin of this Gedcom */
  private Origin origin;
  
  /** last change */
  private PropertyChange lastChange = null;
  
  /** maximum ID length in file */
  private int maxIDLength = 0;
  
  /** entities */
  private LinkedList<Entity> allEntities = new LinkedList<Entity>();
  private Map<String, Map<String,Entity>> tag2id2entity = new HashMap<String, Map<String,Entity>>();
  
  /** currently collected undos and redos */
  private boolean isDirty = false;
  private List<List<Undo>> 
    undoHistory = new ArrayList<List<Undo>>(),
    redoHistory = new ArrayList<List<Undo>>();

  /** a semaphore we're using for syncing */
  private Object writeSemaphore = new Object();
  
  /** current lock */
  private Lock lock = null;
  
  /** listeners */
  private List<GedcomListener> listeners = new CopyOnWriteArrayList<GedcomListener>();
  
  /** mapping tags refence sets */
  private Map<String, ReferenceSet<String,Property>> tags2refsets = new HashMap<String, ReferenceSet<String, Property>>();
  
  /** mapping tags to counts */
  private Map<String,Integer> propertyTag2valueCount = new HashMap<String,Integer>();

  /** encoding */
  private String encoding = ENCODINGS[Math.min(ENCODINGS.length-1, Options.getInstance().defaultEncoding)];
    
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
   * Returns the origin of this gedcom
   */
  public void setOrigin(Origin origin) {
    this.origin = origin;
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
   * Returns the submitter of this gedcom (might be null)
   */
  public Submitter getSubmitter() {
    if (submitter==null)
      return (Submitter)getFirstEntity(Gedcom.SUBM);
    return submitter;
  }
  
  /** 
   * Sets the submitter of this gedcom
   */
  public void setSubmitter(Submitter set) {
    
    // change it
    if (set!=null&&!getEntityMap(SUBM).containsValue(set))
      throw new IllegalArgumentException("Submitter is not part of this gedcom");

    // flip it
    final Submitter old = submitter;
    submitter = set;
    
    // no lock? we're done
    if (lock==null) 
      return;
      
    // keep undo
    lock.addChange(new Undo() {
      void undo() {
          setSubmitter(old);
      }
    });
    
    // let listeners know
    for (GedcomListener listener : listeners) {
      if (listener instanceof GedcomMetaListener)
        ((GedcomMetaListener)listener).gedcomHeaderChanged(this);
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
   */
  public void addGedcomListener(GedcomListener listener) {
    if (listener==null)
      throw new IllegalArgumentException("listener can't be null");
    if (!listeners.add(SafeProxy.harden(listener)))
      throw new IllegalArgumentException("can't add gedcom listener "+listener+"twice");
    LOG.log(Level.FINER, "addGedcomListener() from "+new Throwable().getStackTrace()[1]+" (now "+listeners.size()+")");
    
  }

  /**
   * Removes a Listener from receiving notifications
   */
  public void removeGedcomListener(GedcomListener listener) {
    // 20060101 apparently window lifecycle mgmt including removeNotify() can be called multiple times (for windows
    // owning windows for example) .. so down the line the same listener might unregister twice - we'll just ignore that
    // for now
    listeners.remove(SafeProxy.harden(listener));
    LOG.log(Level.FINER, "removeGedcomListener() from "+new Throwable().getStackTrace()[1]+" (now "+listeners.size()+")");
  }
  
  /**
   * the current undo set
   */
  private List<Undo> getCurrentUndoSet() {
    return undoHistory.get(undoHistory.size()-1);
  }
  
  /**
   * Final destination for a change propagation
   */
  protected void propagateXRefLinked(final PropertyXRef property1, final PropertyXRef property2) {
    
    if (LOG.isLoggable(Level.FINER))
      LOG.finer("Property "+property1.getTag()+" and "+property2.getTag()+" linked");
    
    // no lock? we're done
    if (lock==null) 
      return;
      
    // keep undo
    lock.addChange(new Undo() {
      void undo() {
        property1.unlink();
      }
    });
    
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
    
    if (LOG.isLoggable(Level.FINER))
      LOG.finer("Property "+property1.getTag()+" and "+property2.getTag()+" unlinked");
    
    // no lock? we're done
    if (lock==null) 
      return;
      
    // keep undo
    lock.addChange(new Undo() {
        void undo() {
          property1.link(property2);
        }
      });
    
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
    
    if (LOG.isLoggable(Level.FINER))
      LOG.finer("Entity "+entity.getId()+" added");
    
    // no lock? we're done
    if (lock==null) 
      return;
      
    // keep undo
    lock.addChange(new Undo() {
      void undo() {
        deleteEntity(entity);
      }
    });
    
    // let listeners know
    for (GedcomListener listener : listeners) 
      listener.gedcomEntityAdded(this, entity);

    // done
  }
  
  /**
   * Final destination for a change propagation
   */
  protected void propagateEntityDeleted(final Entity entity) {
    
    if (LOG.isLoggable(Level.FINER))
      LOG.finer("Entity "+entity.getId()+" deleted");
    
    // no lock? we're done
    if (lock==null) 
      return;
    
    // keep undo
    lock.addChange(new Undo() {
        void undo() throws GedcomException  {
          addEntity(entity);
        }
      });
    
    // let listeners know
    for (GedcomListener listener : listeners) 
      listener.gedcomEntityDeleted(this, entity);

    // done
  }
  
  /**
   * Final destination for a change propagation
   */
  protected void propagatePropertyAdded(Entity entity, final Property container, final int pos, Property added) {
    
    if (LOG.isLoggable(Level.FINER))
      LOG.finer("Property "+added.getTag()+" added to "+container.getTag()+" at position "+pos+" (entity "+entity.getId()+")");
    
    // track counts for value properties (that's none references)
    if (!(added instanceof PropertyXRef)) {
      Integer count = propertyTag2valueCount.get(added.getTag());
      propertyTag2valueCount.put(added.getTag(), count==null ? 1 : count+1);
    }
    
    // no lock? we're done
    if (lock==null) 
      return;
      
    // keep undo
    lock.addChange(new Undo() {
        void undo() {
          container.delProperty(pos);
        }
      });
    
    // let listeners know
    for (GedcomListener listener : listeners) 
      listener.gedcomPropertyAdded(this, container, pos, added);

    // done
  }
  
  /**
   * Final destination for a change propagation
   */
  protected void propagatePropertyDeleted(Entity entity, final Property container, final int pos, final Property deleted) {
    
    if (LOG.isLoggable(Level.FINER))
      LOG.finer("Property "+deleted.getTag()+" deleted from "+container.getTag()+" at position "+pos+" (entity "+entity.getId()+")");
    
    // track counts for value properties (that's none references)
    if (!(deleted instanceof PropertyXRef)) {
      propertyTag2valueCount.put(deleted.getTag(), propertyTag2valueCount.get(deleted.getTag())-1 );
    } 
    
    // no lock? we're done
    if (lock==null) 
      return;
      
    // keep undo
    lock.addChange(new Undo() {
        void undo() {
          container.addProperty(deleted, pos);
        }
      });
    
    // let listeners know
    for (GedcomListener listener : listeners) 
      listener.gedcomPropertyDeleted(this, container, pos, deleted);
    
    // done
  }
  
  /**
   * Final destination for a change propagation
   */
  protected void propagatePropertyChanged(Entity entity, final Property property, final String oldValue) {
    
    if (LOG.isLoggable(Level.FINER))
      LOG.finer("Property "+property.getTag()+" changed in (entity "+entity.getId()+")");
    
    // no lock? we're done
    if (lock==null) 
      return;
      
    // keep undo
    lock.addChange(new Undo() {
        void undo() {
          property.setValue(oldValue);
        }
      });
    
    // notify
    for (GedcomListener listener : listeners) 
      listener.gedcomPropertyChanged(this, property);

    // done
  }

  /**
   * Final destination for a change propagation
   */
  protected void propagatePropertyMoved(final Property property, final Property moved, final int from, final int to) {
    
    if (LOG.isLoggable(Level.FINER))
      LOG.finer("Property "+property.getTag()+" moved from "+from+" to "+to+" (entity "+property.getEntity().getId()+")");
    
    // no lock? we're done
    if (lock==null) 
      return;
      
    // keep undo
    lock.addChange(new Undo() {
        void undo() {
          property.moveProperty(moved, from<to ? from : from+1);
        }
      });
    
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
      if (listener instanceof GedcomMetaListener)
        ((GedcomMetaListener)listener).gedcomWriteLockAcquired(this);
    }
  }
  
  /**
   * Final destination for a change propagation
   */
  protected void propagateBeforeUnitOfWork() {
    for (GedcomListener listener : listeners) {
      if (listener instanceof GedcomMetaListener)
        ((GedcomMetaListener)listener).gedcomBeforeUnitOfWork(this);
    }
  }
  
  /**
   * Final destination for a change propagation
   */
  protected void propagateAfterUnitOfWork() {
    for (GedcomListener listener : listeners) {
      if (listener instanceof GedcomMetaListener) 
        ((GedcomMetaListener)listener).gedcomAfterUnitOfWork(this);
    }
  }
  
  /**
   * Final destination for a change propagation
   */
  protected void propagateWriteLockReleased() {
    
    for (GedcomListener listener : listeners) {
      if (listener instanceof GedcomMetaListener) 
        ((GedcomMetaListener)listener).gedcomWriteLockReleased(this);
    }
  }  
  
  /**
   * Final destination for a change propagation
   */
  protected void propagateEntityIDChanged(final Entity entity, final String old) throws GedcomException {
    
    Map<String, Entity> id2entity = getEntityMap(entity.getTag());
    
    // known?
    if (!id2entity.containsValue(entity))
      throw new GedcomException("Can't change ID of entity not part of this Gedcom instance");
    
    // valid prefix/id?
    String id = entity.getId();
    if (id==null||id.length()==0)
      throw new GedcomException("Need valid ID length");
    
    // dup?
    if (getEntity(id)!=null)
      throw new GedcomException("Duplicate ID is not allowed");

    // do the housekeeping
    id2entity.remove(old);
    id2entity.put(entity.getId(), entity);
    
    // remember maximum ID length
    maxIDLength = Math.max(id.length(), maxIDLength);
    
    // log it
    if (LOG.isLoggable(Level.FINER))
      LOG.finer("Entity's ID changed from  "+old+" to "+entity.getId());
    
    // no lock? we're done
    if (lock==null) 
      return;
      
    // keep undo
    lock.addChange(new Undo() {
        void undo() throws GedcomException {
          entity.setId(old);
        }
      });
    
    // notify
    for (GedcomListener listener : listeners) 
      listener.gedcomPropertyChanged(this, entity);

    // done
  }

  /**
   * Add entity 
   */
  private void addEntity(Entity entity) throws GedcomException {
    
    String id = entity.getId();
    
    // some entities (event definitions for example) don't have an
    // id - we'll keep them in our global list but not mapped id->entity
    if (id.length()>0) {
      Map<String, Entity> id2entity = getEntityMap(entity.getTag());
      if (id2entity.containsKey(id))
        throw new GedcomException(resources.getString("error.entity.dupe", id));
      
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
   * @return change or null
   */
  public PropertyChange getLastChange() {
    return lastChange;
  }
  
  /**
   * Accessor - last change
   */
  protected void updateLastChange(PropertyChange change) {
    if (lastChange==null || lastChange.compareTo(change)<0)
      lastChange = change;
  }

  /**
   * Creates a non-related entity with id
   */
  public Entity createEntity(String tag) throws GedcomException {
    return createEntity(tag, null);
  }    
    
  /**
   * Create a entity by tag
   * @exception GedcomException in case of unknown tag for entity
   */
  public Entity createEntity(String tag, String id) throws GedcomException {
    
    // generate new id if necessary - otherwise trim it
    if (id==null)
      id = getNextAvailableID(tag);
    
    // remember maximum ID length
    maxIDLength = Math.max(id.length(), maxIDLength);

    // lookup a type - all well known types need id
    Class<? extends Entity> clazz = (Class<? extends Entity>)E2TYPE.get(tag);
    if (clazz!=null) {
      if (id.length()==0)
        throw new GedcomException(resources.getString("entity.error.noid", tag));
    } else {
      clazz = Entity.class;
    }
    
    // Create entity
    Entity result; 
    try {
      result = (Entity)clazz.getDeclaredConstructor(String.class, String.class).newInstance(tag, id);
    } catch (Throwable t) {
      throw new RuntimeException("Can't instantiate "+clazz, t);
    }

    // keep it
    addEntity(result);

    // Done
    return result;
  }  

  /**
   * Deletes entity
   * @exception GedcomException in case unknown type of entity
   */
  public void deleteEntity(Entity which) {

    // Some entities dont' have ids (event definitions for example) - for
    // all others we check the id once more
    String id = which.getId();
    if (id.length()>0) {
      
      // Lookup entity map
      Map<String,Entity> id2entity = getEntityMap(which.getTag());
  
      // id exists ?
      if (!id2entity.containsKey(id))
        throw new IllegalArgumentException("Unknown entity with id "+which.getId());

      // forget id
      id2entity.remove(id);
    }
    
    // Tell it first
    which.beforeDelNotify();

    // Forget it now
    allEntities.remove(which);

    // was it the submitter?    
    if (submitter==which) submitter = null;

    // Done
  }

  /**
   * Internal entity lookup
   */
  private Map<String,Entity> getEntityMap(String tag) {
    // lookup map of entities for tag
    Map<String,Entity> id2entity = tag2id2entity.get(tag);
    if (id2entity==null) {
      id2entity = new HashMap<String,Entity>();
      tag2id2entity.put(tag, id2entity);
    }
    // done
    return id2entity;
  }
  
  /**
   * Returns all properties for given path
   */
  public Property[] getProperties(TagPath path) {
    ArrayList<Property> result = new ArrayList<Property>(100);
    for (Entity ent : getEntities(path.getFirst())) {
      Property[] props = ent.getProperties(path);
      for (int i = 0; i < props.length; i++) result.add(props[i]);
    }
    return Property.toArray(result);
  }
  
  /**
   * Count statistics for property tag
   */
  public int getPropertyCount(String tag) {
    Integer result = propertyTag2valueCount.get(tag);
    return result==null ? 0 : result;
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
   * Returns entities of given type sorted by given path (can be empty or null)
   */
  public Entity[] getEntities(String tag, String sortPath) {
    return getEntities(tag, sortPath!=null&&sortPath.length()>0 ? new PropertyComparator(sortPath) : null);
  }

  /**
   * Returns entities of given type sorted by comparator (can be null)
   */
  public Entity[] getEntities(String tag, Comparator<Property> comparator) {
    Collection<Entity> ents = getEntityMap(tag).values();
    Entity[] result = (Entity[])ents.toArray(new Entity[ents.size()]);
    // sort by comparator or entity
    if (comparator!=null) 
      Arrays.sort(result, comparator);
    else
      Arrays.sort(result);
    // done
    return result;
  }

  /**
   * Returns the entity with given id (or null)
   */
  public Entity getEntity(String id) {
    // loop all types
    for (Map<String,Entity> ents : tag2id2entity.values()) {
      Entity result = ents.get(id);
      if (result!=null)
        return result;
    }
    
    // not found
    return null;
  }

  /**
   * Returns the entity with given id of given type or null if not exists
   */
  public Entity getEntity(String tag, String id) {
    // check back in appropriate type map
    return (Entity)getEntityMap(tag).get(id);
  }
  
  /**
   * Returns a type for given tag
   */
  public static Class<? extends Entity> getEntityType(String tag) {
    Class<? extends Entity> result = (Class<? extends Entity>)E2TYPE.get(tag);
    if (result==null)
      throw new IllegalArgumentException("no such type");
    return result;
  }
  
  /**
   * Returns any instance of entity with given type if exists
   */
  public Entity getFirstEntity(String tag) {
    // loop over entities and return first of given type
    for (Entity e : allEntities) {
      if (e.getTag().equals(tag))
        return e;
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
    int id = Options.getInstance().isFillGapsInIDs ? 1 : (id2entity.isEmpty() ? 1 : id2entity.size());
    
    StringBuffer buf = new StringBuffer(maxIDLength);
    
    search: while (true) {
      
      // 20050619 back to checking all IDs with max id length padding
      // since we don't want to assign I1 if there's a I01 already - got
      // a file from Anton written by Gramps that has these kinds of
      // 'duplicates' all over
      buf.setLength(0);
      buf.append(getEntityPrefix(entity));
      buf.append(id);
      
      while (true) {
        if (id2entity.containsKey(buf.toString())) break;
        if (buf.length()>=maxIDLength) break search;
        buf.insert(1, '0');
      } 
      
      // try next
      id++;
    }
    
    // 20050509 not patching IDs with zeros anymore - since we now have alignment
    // in tableview there's not really a need to add leading zeros for readability.
    return getEntityPrefix(entity) + id;
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
    if (!hasChanged())
      return;
    
    // do it
    undoHistory.clear();
    isDirty = false;
    
    // no lock? we're done
    if (lock==null)
      return;
    
    // let listeners know
    for (GedcomListener listener : listeners) {
      if (listener instanceof GedcomMetaListener)
        ((GedcomMetaListener)listener).gedcomHeaderChanged(this);
    }

    // done
  }
  
  /**
   * Test for write lock
   */
  public boolean isWriteLocked() {
    return lock!=null;
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
      
      if (lock!=null)
        throw new GedcomException("Cannot obtain write lock");
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
        
        while (undoHistory.size()>Options.getInstance().getNumberOfUndos()) {
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
    LOG.log(Level.FINE, "End of UOW, property counts "+propertyTag2valueCount);

    // done
    if (rethrow!=null) {
      if (rethrow instanceof GedcomException)
        throw (GedcomException)rethrow;
      throw new RuntimeException(rethrow);
    }
  }

  /**
   * Test for undo
   */
  public boolean canUndo() {
    return !undoHistory.isEmpty();
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
    if (undoHistory.isEmpty())
      throw new IllegalArgumentException("undo n/a");

    synchronized (writeSemaphore) {
      
      if (lock!=null)
        throw new IllegalStateException("Cannot obtain write lock");
      lock = new Lock();
  
    }
    
    // let listeners know
    propagateWriteLockAqcuired();
    
    // run through undos
    List<Undo> todo = undoHistory.remove(undoHistory.size()-1);
    for (int i=todo.size()-1;i>=0;i--) {
      Undo undo = (Undo)todo.remove(i);
      try {
        undo.undo();
      } catch (Throwable t) {
        LOG.log(Level.SEVERE, "Unexpected throwable during undo()", t);
      }
    }
    
    synchronized (writeSemaphore) {

      // keep redos
      if (keepRedo)
        redoHistory.add(lock.undos);
      
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
    if (redoHistory.isEmpty())
      throw new IllegalArgumentException("redo n/a");

    synchronized (writeSemaphore) {
      
      if (lock!=null)
        throw new IllegalStateException("Cannot obtain write lock");
      lock = new Lock();
  
    }
    
    // let listeners know
    propagateWriteLockAqcuired();
    
    // run the redos
    List<Undo> todo = redoHistory.remove(redoHistory.size()-1);
    for (int i=todo.size()-1;i>=0;i--) {
      Undo undo = (Undo)todo.remove(i);
      try {
        undo.undo();
      } catch (Throwable t) {
        LOG.log(Level.SEVERE, "Unexpected throwable during undo()", t);
      }
    }
    
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
   */
  /*package*/ ReferenceSet<String,Property> getReferenceSet(String tag) {
    // lookup
    ReferenceSet<String,Property> result = tags2refsets.get(tag);
    if (result==null) {
      // .. instantiate if necessary
      result = new ReferenceSet<String, Property>();
      tags2refsets.put(tag, result);
      // .. and pre-fill
      String defaults = Gedcom.resources.getString(tag+".vals",false);
      if (defaults!=null) {
        StringTokenizer tokens = new StringTokenizer(defaults,",");
        while (tokens.hasMoreElements()) result.add(tokens.nextToken().trim(), null);
      }
    }
    // done
    return result;
  }

  /**
   * Returns the name of this gedcom or null if unnamed
   */
  public String getName() {
    return origin==null ? null : origin.getName();
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
      String name = resources.getString(tag+".s.name", false);
      if (name!=null)
        return name;
    }
    String name = resources.getString(tag+".name", false);
    return name!=null ? name : tag;
  }

  /**
   * Returns the prefix of the given entity
   */
  public static String getEntityPrefix(String tag) {
    String result = (String)E2PREFIX.get(tag);
    if (result==null)
      result = "X";
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
    ImageIcon result = (ImageIcon)E2IMAGE.get(tag);
    if (result==null) {
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
   * TODO: getRegistry(gedcom) a mettre ailleurs
   * le fichier gedcom.properties est maintenant dans le home user dir
   * DAN 20101230: now in PreferencesRoot/gedcoms/settings/...
   * TODO: Attention cela a pour inconvenient de ne par pouvoir ouvrir (dans la vie d'ancestris)
   * TODO: deux fichiers portant le meme nom sans collision des reglages.
   * TODO: dans l'avenir on pourra marquer les proprietes par un id que l'on attachera au
   * TODO: fichier gedcom dans une des proprietes.
   * FIXME: mettre dans ancestrispreferences
   */
  public Registry getRegistry(){
    return Registry.get("gedcoms/settings/"+getName());
  }
  /**
   * Accessor - encoding
   */
  public String getEncoding() {
    return encoding;
  }
  
  /**
   * Accessor - encoding
   */
  public void setEncoding(String set) {
     encoding = set;
  }
  
  /**
   * Accessor - place format
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
    return password!=null;
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
    if (cachedLocale==null) {
      
      // known language?
      if (language!=null) {
        
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
      if (cachedLocale==null)
        cachedLocale = Locale.getDefault();
      
    }
    
    // done
    return cachedLocale;
  }
  
  /**
   * Return an appropriate Collator instance
   */
  public Collator getCollator() {
    
    // not known?
    if (cachedCollator==null) {
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
  public int compareTo(Object other) {
//FIXME:       throw new UnsupportedOperationException("comparaison de deux ged a enlever");
    Gedcom that = (Gedcom)other;
    return getName().compareTo(that.getName());
  };
  
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
