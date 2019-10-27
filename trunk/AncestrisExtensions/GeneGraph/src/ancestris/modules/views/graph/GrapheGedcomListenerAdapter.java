/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.graph;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Property;

/**
 * Gedcom listener for graphe.
 *
 * @author Zurga
 */
public class GrapheGedcomListenerAdapter extends GedcomListenerAdapter {

    private final ModifEntity entities = new ModifEntity();
    private final GraphTopComponent graph;

    public GrapheGedcomListenerAdapter(GraphTopComponent graphe) {
        super();
        graph = graphe;
    }

    @Override
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
        entities.clear();
    }

    @Override
    public void gedcomWriteLockReleased(Gedcom gedcom) {
        graph.changeDisplay(entities);
    }

    @Override
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        if (checkGedcom(gedcom)) {
            entities.addEntity(entity);
        }
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        if (checkGedcom(gedcom)) {
            entities.deleteEntity(entity);
        }
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        if (checkGedcom(gedcom)) {
            Entity entity = property.getEntity();
            if (entity != null) {
                entities.modifyEntity(entity);
            }
        }
    }

    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        if (checkGedcom(gedcom)) {
            Entity entity = property.getEntity();
            if (entity != null) {
                entities.modifyEntity(entity);
            }
        }
    }

    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
        if (checkGedcom(gedcom)) {
            Entity entity = property.getEntity();
            if (entity != null) {
                entities.modifyEntity(entity);
            }
        }
    }

    private boolean checkGedcom(Gedcom gedcom) {
        Gedcom graphGedcom = graph.getGedcom();
        return graphGedcom != null && graphGedcom.equals(gedcom);
    }

}
