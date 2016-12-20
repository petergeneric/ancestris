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
package ancestris.modules.wizards.newgedcom;

import ancestris.api.newgedcom.ModifyGedcom;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import java.awt.Dialog;
import java.text.MessageFormat;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service=ModifyGedcom.class)
public class CreateNewGedcom implements INewGedcomProvider,ModifyGedcom {
    
    private static final genj.gedcom.GedcomOptions gedcomOptions = genj.gedcom.GedcomOptions.getInstance();
    private Context context = null;

    @Override
    public Context create() {
        // To invoke this wizard, copy-paste and run the following code, e.g. from
        // SomeAction.performAction():
        WizardDescriptor.Iterator<WizardDescriptor> iterator = new NewGedcomWizardIterator(this);
        WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        //wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(CreateNewGedcom.class, "wizard.title"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            return context;
        }
        return null;
    }

    @Override
    public Context getContext() {
        if (context == null){
            Gedcom gedcom = new Gedcom();
            gedcom.setName(org.openide.util.NbBundle.getMessage(CreateNewGedcom.class, "newgedcom.name"));
//XXX:            GedcomDirectory.getInstance().setDefault(gedcom);

            // remember
            context = new Context(gedcom);
        }
        return context;
    }

    @Override
    public Indi getFirst() {
        if (getContext().getGedcom().getFirstEntity(Gedcom.INDI)==null)
            try {
                getContext().getGedcom().createEntity(Gedcom.INDI);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        return (Indi)getContext().getGedcom().getFirstEntity(Gedcom.INDI);
    }

    public Context update() {
        return null;
    }

    public Context update(Context context) {
        return null;
    }

    public boolean isReady() {
        return false; // module not ready 
    }

}
