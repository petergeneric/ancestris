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

import java.net.URL;
import org.openide.WizardDescriptor;

/**
 *
 * @author daniel
 */
public abstract class NewGedcomWizardPanel implements WizardDescriptor.Panel<WizardDescriptor>{
    abstract URL getHelpUrl();
    abstract void applyNext();
}
