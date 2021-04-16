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
import ancestris.modules.gedcomcompare.GedcomCompareTopComponent;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class RearrangeAction extends AbstractAncestrisAction {

    private final GedcomCompareTopComponent owner;
    
    /**
     * Constructor
     */
    public RearrangeAction(GedcomCompareTopComponent tstc) {
        this.owner = tstc;
        setImage(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/rearrange.png")));
        setTip(NbBundle.getMessage(RearrangeAction.class, "TIP_Rearrange"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        owner.rearrangeWindows(false);
    }
  
}
