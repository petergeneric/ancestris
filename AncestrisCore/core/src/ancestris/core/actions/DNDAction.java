/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2021 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.core.actions;

import ancestris.util.swing.DialogManager;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author frederic
 */
public abstract class DNDAction extends AbstractAction {

    private TopComponent tc = null;
    private Gedcom gedcom = null;
    private String name = "";
    private Entity importedEntity = null;
    private Entity newEntity = null;
    private String msgSuccess = "DND_SuccessMessage";
    private String msgFailure = "DND_ErrorMessage";
    
    public DNDAction(boolean isError, String name, Icon icon) {
        super(name, icon);
        setEnabled(!isError);
        this.name = name;
    }
    
    public void init(TopComponent tc, Entity importedEntity, Entity targetEntity) {
        this.tc = tc;
        this.gedcom = targetEntity.getGedcom();
        this.importedEntity = importedEntity;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        // Execute user action
        try {
            gedcom.doUnitOfWork(new UnitOfWork() {
                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    newEntity = dropActionPerformed(e);
                }
            });
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }

        // Display result
        Toolkit.getDefaultToolkit().beep();
        if (newEntity != null) {
            String msg = NbBundle.getMessage(DNDAction.class, msgSuccess, name); 
            StatusDisplayer.getDefault().setStatusText(msg);
            SelectionDispatcher.fireSelection(new Context(newEntity));
            if (tc != null) {
                tc.requestActive();
            }
        } else {
            String msg = NbBundle.getMessage(DNDAction.class, msgFailure, name);
            StatusDisplayer.getDefault().setStatusText(msg);
            Toolkit.getDefaultToolkit().beep();
            DialogManager.create(NbBundle.getMessage(DNDAction.class, "DND_Title"), msg).setMessageType(DialogManager.ERROR_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).show();
        }
    
        
    }
    
    public abstract Entity dropActionPerformed(ActionEvent e);
    
    
}
