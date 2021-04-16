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
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class SearchAction extends AbstractAncestrisAction {

    private final GedcomCompareTopComponent owner;
    
    private final ImageIcon SEARCH_ICON  = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/search.png"));
    private final ImageIcon ROTATING_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/rotating16.gif"));

    /**
     * Constructor
     */
    public SearchAction(GedcomCompareTopComponent tstc) {
        this.owner = tstc;
        setImage(SEARCH_ICON);
        setTip(NbBundle.getMessage(RearrangeAction.class, "TIP_Search"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        owner.launchMapsSearch();
    }
    
    public void setOn() {
        setImage(SEARCH_ICON);
        setTip(NbBundle.getMessage(RearrangeAction.class, "TIP_Search"));
        setEnabled(true);
    }
  
    public void setOff() {
        setImage(SEARCH_ICON);
        setTip(NbBundle.getMessage(RearrangeAction.class, "TIP_Search"));
        setEnabled(false);
    }
  
    public void setSearching() {
        setImage(ROTATING_ICON);
        setTip(NbBundle.getMessage(RearrangeAction.class, "TIP_SearchInProgress"));
        setEnabled(true);
    }
  
}
