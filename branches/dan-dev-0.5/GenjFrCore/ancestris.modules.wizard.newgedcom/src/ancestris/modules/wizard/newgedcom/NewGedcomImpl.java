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

import ancestris.api.newgedcom.NewGedcom;
import genj.gedcom.Context;
import java.awt.Dialog;
import java.text.MessageFormat;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service=NewGedcom.class)
public class NewGedcomImpl implements NewGedcom{

    @Override
    public Context create() {
        // To invoke this wizard, copy-paste and run the following code, e.g. from
        // SomeAction.performAction():
        WizardDescriptor.Iterator iterator = new NewGedcomWizardIterator();
        WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        //wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(NewGedcomImpl.class, "wizard.title"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            // do something
        }
        return new Context();
//FIXME: mettre ce qu'il faut
//        return null;
    }

}
