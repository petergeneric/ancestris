/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.wizard.newgedcom;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genjfr.util.GedcomDirectory;
import org.openide.util.Exceptions;

/**
 *
 * @author daniel
 */
public class CreateNewGedcom {

    private Context context = null;
    private Indi first = null;

    public CreateNewGedcom() {
            Gedcom gedcom = new Gedcom();
            gedcom.setName(org.openide.util.NbBundle.getMessage(CreateNewGedcom.class, "newgedcom.name"));
            try {
                gedcom.createEntity(Gedcom.SUBM);

                // Create place format
                // FIXME: mettre ici l'appel a l'option
                gedcom.setPlaceFormat("Lieudit,Commune,Code_INSEE,Département,Région,Pays");

                // remember
                context = new Context(gedcom);
                GedcomDirectory.getInstance().registerGedcom(context);
//            openDefaultViews(context);
//            SelectionSink.Dispatcher.fireSelection((Component) null, new Context(context.getGedcom().getFirstEntity(Gedcom.INDI)), true);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
    }

    Context getContext() {
        return context;
    }

    Indi getFirst() {
        if (first == null) {
            try {
                first = (Indi) getContext().getGedcom().createEntity(Gedcom.INDI);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return first;
    }
}
