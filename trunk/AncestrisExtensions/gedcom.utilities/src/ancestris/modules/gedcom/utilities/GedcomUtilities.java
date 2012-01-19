/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
/**
 * Extract from GenJ - Report - ReportToolBox
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
package ancestris.modules.gedcom.utilities;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.UnitOfWork;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GedcomUtilities {

    public final static int ENT_ALL = 0;
    public final static int ENT_INDI = 1;
    public final static int ENT_FAM = 2;
    public final static int ENT_NOTE = 3;
    public final static int ENT_SOUR = 4;
    public final static int ENT_SUBM = 5;
    public final static int ENT_REPO = 6;
    public final static String entityTypes[] = {
        "All",
        Gedcom.INDI,
        Gedcom.FAM,
        Gedcom.NOTE,
        Gedcom.SOUR,
        Gedcom.SUBM,
        Gedcom.REPO
    };
    private final static Logger LOG = Logger.getLogger(GedcomUtilities.class.getName(), null);
    private Gedcom gedcom = null;

    public GedcomUtilities(Gedcom gedcom) {
        this.gedcom = gedcom;
    }

    public void deleteTags(final String tagToRemove, final int entityType) {
        LOG.log(Level.INFO, "deleting_tag {0}", tagToRemove);

        // Perform unit of work
        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    Collection entities;
                    Entity entity;
                    int iCounter = 0;

                    if (entityType == ENT_ALL) {
                        entities = gedcom.getEntities();
                    } else {
                        entities = gedcom.getEntities(entityTypes[entityType]);
                    }

                    List propsToDelete = new ArrayList();
                    for (Iterator it = entities.iterator(); it.hasNext();) {
                        entity = (Entity) it.next();
                        getPropertiesRecursively(entity, propsToDelete, tagToRemove);
                        for (Iterator props = propsToDelete.iterator(); props.hasNext();) {
                            Property prop = (Property) props.next();
                            if (prop != null) {
                                Property parent = prop.getParent();
                                if (parent != null) {
                                    String propText = parent.getTag() + " " + tagToRemove + " '" + prop.toString() + "'";
                                    parent.delProperty(prop);
                                    iCounter++;
                                    LOG.log(Level.INFO, "deleting_tag {0} {1} {2}", new Object[]{entity.getTag(), entity.toString(), propText});
                                }
                            }
                        }
                    }
                    LOG.log(Level.INFO, "DeletedNb {0}", iCounter);
                }
            }); // end of doUnitOfWork
        } catch (GedcomException e) {
            LOG.severe(e.getMessage());
        }
    }

    private void getPropertiesRecursively(Property parent, List props, String tag) {
        Property[] children = parent.getProperties();
        for (int c = 0; c < children.length; c++) {
            Property child = children[c];
            if (child.getTag().compareTo(tag) == 0) {
                props.add(child);
            }
            getPropertiesRecursively(child, props, tag);
        }
    }
}
