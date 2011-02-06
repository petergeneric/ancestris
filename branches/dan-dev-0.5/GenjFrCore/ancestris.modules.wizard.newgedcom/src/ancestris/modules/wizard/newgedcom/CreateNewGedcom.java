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
import genj.gedcom.UnitOfWork;
import genjfr.util.GedcomDirectory;
import org.openide.util.Exceptions;

/**
 *
 * @author daniel
 */
public class CreateNewGedcom implements INewGedcomProvider {

    private Context context = null;
    private Indi first = null;

    public CreateNewGedcom() {
        Gedcom gedcom = new Gedcom();
        try {
            // note: dan ce cas pas besoin de memoriser dans le undo history mais cela
            // permet de positionner le gedcom dans l'etat change
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    gedcom.setName(org.openide.util.NbBundle.getMessage(CreateNewGedcom.class, "newgedcom.name"));
                    gedcom.createEntity(Gedcom.SUBM);

                    // Create place format
                    // FIXME: mettre ici l'appel a l'option
                    gedcom.setPlaceFormat("Lieudit,Commune,Code_INSEE,Département,Région,Pays");
                }
            });

            // remember
            context = new Context(gedcom);
            GedcomDirectory.getInstance().registerGedcom(context);
//            openDefaultViews(context);
//            SelectionSink.Dispatcher.fireSelection((Component) null, new Context(context.getGedcom().getFirstEntity(Gedcom.INDI)), true);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Indi getFirst() {
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
