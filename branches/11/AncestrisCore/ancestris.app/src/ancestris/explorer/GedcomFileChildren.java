/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.explorer;

import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomDirectory.GedcomRegistryListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author daniel
 */
class GedcomFileChildren extends Children.Keys<Gedcom> implements GedcomRegistryListener {

    @Override
    protected Node[] createNodes(Gedcom key) {
        return new Node[]{new GedcomFileNode(key)};
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        GedcomDirectory.getDefault().addListener(this);
        updateGedcoms();
    }

    @Override
    protected void removeNotify() {
        GedcomDirectory.getDefault().removeListener(this);
        super.removeNotify();
    }

    void updateGedcoms() {
        List<Context> contexts = GedcomDirectory.getDefault().getContexts();
        List<Gedcom> gedcoms = new ArrayList<Gedcom>();
        for (Context context : contexts) {
            gedcoms.add(context.getGedcom());
        }
        setKeys(gedcoms);
    }

    @Override
    public void gedcomRegistered(Context context) {
        updateGedcoms();
    }

    @Override
    public void gedcomUnregistered(Context context) {
        updateGedcoms();
    }
}
