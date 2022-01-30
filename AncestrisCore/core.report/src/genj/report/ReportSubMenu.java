/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2022 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.report;

import ancestris.core.actions.SubMenuAction;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.io.Filter;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author frederic
 */
public class ReportSubMenu extends SubMenuAction {

    private Gedcom gedcom;
    private Filter filter;

    public ReportSubMenu(Gedcom gedcom, Filter filter) {
        super();
        this.filter = filter;
        this.gedcom = gedcom;
        setImage(ReportViewFactory.IMG);
        setTip(NbBundle.getMessage(ReportSubMenu.class, "report.submenu.tip"));
    }
    
    @Override
    public void actionPerformed(final ActionEvent event) {
        Indi[] indis = getIndis();
        if (indis.length == 0) {
            return;
        }
        Collection<Action> actions = ReportPlugin.getReportActions(getIndis(), gedcom);
        JPopupMenu menu = Utilities.actionsToPopup(actions.toArray(new Action[]{}), Lookup.EMPTY);
        Component source = (Component) event.getSource();
        menu.show(source, 0, source.getHeight());        
    }
    
    private Indi[] getIndis() {
        List<Indi> indis = new ArrayList<>();
        for (Indi indi : gedcom.getIndis()) {
            if (!filter.veto(indi)) {
                indis.add(indi);
            }
        }
        return indis.toArray(new Indi[indis.size()]);
    }

}


