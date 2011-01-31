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
import genj.util.Origin;
import genjfr.util.GedcomDirectory;
import org.openide.util.Exceptions;

/**
 *
 * @author daniel
 */
public class CreateNewGedcom {

    private static Gedcom gedcom = null;
    private static Indi first = null;

    static Gedcom getGedcom() {
        if (gedcom == null) {
            gedcom = new Gedcom();
            //FIXME: raise exception?
            if (gedcom == null) {
                return null;
            }
            gedcom.setName("Nouveau Gedcom");
            try {
                gedcom.createEntity(Gedcom.SUBM);

                // Create place format
                gedcom.setPlaceFormat("Lieudit,Commune,Code_INSEE,Département,Région,Pays");

                // remember
                GedcomDirectory.getInstance().registerGedcom(new Context(gedcom));
//            openDefaultViews(context);
//            SelectionSink.Dispatcher.fireSelection((Component) null, new Context(context.getGedcom().getFirstEntity(Gedcom.INDI)), true);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
        return gedcom;
    }

    static Indi getFirst() {
        if (first == null) {
            try {
                first = (Indi) getGedcom().createEntity(Gedcom.INDI);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return first;
    }
}
