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
package ancestris.gedcom;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.SubMenuAction;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.io.Filter;
import java.awt.event.ActionEvent;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class ActionSaveViewAsGedcom extends SubMenuAction {

    private Gedcom gedcom;
    private Filter filter;
    private List<? extends Filter> filters;
    private boolean isMulti;
    private AbstractAncestrisAction mainAction = null;
    

    public ActionSaveViewAsGedcom(Gedcom gedcom, Filter filter) {
        super();
        this.isMulti = false;
        this.filter = filter;
        init(gedcom);
        mainAction = createActionFromFilter(filter, true);
    }

    public ActionSaveViewAsGedcom(Gedcom gedcom, List<? extends Filter> filters) {
        super();
        this.isMulti = true;
        this.filters = filters;
        init(gedcom);
        for (Filter f : filters) {
            this.addAction(createActionFromFilter(f, false));
        }
    }
    
    private void init(Gedcom gedcom) {
        this.gedcom = gedcom;
        setImage(ancestris.core.resources.Images.imgExport);
        setTip(NbBundle.getMessage(ActionSaveViewAsGedcom.class, "saveView.tip"));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        if (isMulti) {
            super.actionPerformed(event);
            return;
        }
        mainAction.actionPerformed(event);
    }
    
    private AbstractAncestrisAction createActionFromFilter(Filter f, boolean isIcon) {
        
        AbstractAncestrisAction action = new AbstractAncestrisAction() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                String nbOfEntities = String.valueOf(f.getIndividualsCount());
                String title = NbBundle.getMessage(ActionSaveViewAsGedcom.class, "saveview.msgTitle", nbOfEntities);
                FileObject fo = GedcomDirectory.getDefault().saveViewAsGedcom(new Context(gedcom), f, title);
                if (fo != null) {
                    title = NbBundle.getMessage(ActionSaveViewAsGedcom.class, "saveView.title");
                    String msg = NbBundle.getMessage(ActionSaveViewAsGedcom.class, "saveview.msgTitleSuccess", nbOfEntities, fo.getName() + "." + fo.getExt());
                    if (DialogManager.YES_OPTION == DialogManager.createYesNo(title, msg).show()) {
                        GedcomDirectory.getDefault().openGedcom(fo);
                    }
                }
            }
        };
        if (isIcon) {
            action.setImage(ancestris.core.resources.Images.imgDownload);
        } else {
            action.setText(f.getFilterName());
        }
        return action;
        
    }

    
    
}
