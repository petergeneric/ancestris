/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015-2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.gedcomcompare.tools;

import ancestris.core.actions.AbstractAncestrisAction;
import genj.view.Images;
import java.awt.event.ActionEvent;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class SettingsAction extends AbstractAncestrisAction {
  
    /**
     * Constructor
     */
    public SettingsAction() {
        setImage(Images.imgSettings);
        setTip(NbBundle.getMessage(SettingsAction.class, "TIP_Settings"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        displayOptionsPanel();
    }
    
    public void displayOptionsPanel() {
        OptionsDisplayer.getDefault().open("Extensions/GedcomCompareOptions");
    }

}

