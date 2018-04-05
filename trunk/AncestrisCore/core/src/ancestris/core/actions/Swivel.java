/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.core.actions;

import ancestris.util.swing.DialogManager;
import ancestris.util.swing.SelectEntityPanel;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.UnitOfWork;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
@ActionID(category = "Edit", id = "ancestris.core.actions.Swivel")
@ActionRegistration(displayName = "Swivel",lazy = false)
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position = 1105)})
@NbBundle.Messages({"xref.swivel=Swivel..."})
public class Swivel extends AbstractAncestrisContextAction {

    protected final static Resources RESOURCES = Resources.get(Swivel.class);
    protected final static Logger LOG = Logger.getLogger("ancestris.edit.beans");
    private static ImageIcon IMAGE;
    private PropertyXRef xref;

    
    @Override
    public void resultChanged(LookupEvent ev) {
        // valid only for context aware action
        if (lkpInfo != null) {
            xref = null;
            for (Property prop : lkpInfo.allInstances()) {
                if (prop instanceof PropertyXRef) {
                    xref = (PropertyXRef) prop;
                }
            }
            super.resultChanged(ev);
        }
    }

    @Override
    protected void contextChanged() {
        super.contextChanged();
        if (xref != null && xref.getTargetEntity() != null) {
            IMAGE = xref.getTargetEntity().getImage(false);
            super.setImage(IMAGE.getOverLayed(MetaProperty.IMG_LINK));
        }
        setText(RESOURCES.getString("xref.swivel"));
        setEnabled(xref != null);
    }

    
    
    public Swivel() {
        super();
    }
    
    public Swivel(PropertyXRef xref) {
        this.xref = xref;
        super.setText(RESOURCES.getString("xref.swivel"));
        super.setImage(MetaProperty.IMG_LINK);
        setEnabled(xref != null);
}

    @Override
    public void actionPerformedImpl(ActionEvent event) {

        if (xref == null) {
            return;
        }

        SelectEntityPanel select = new SelectEntityPanel(xref.getGedcom(), xref.getTargetType(), NbBundle.getMessage(this.getClass(), "xrefbean.swivel.askentity"), null);
        if (DialogManager.OK_OPTION != DialogManager.create(getText(), select)
                .setMessageType(DialogManager.QUESTION_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).setDialogId("xrefbean.swivel").show()) {
            return;
        }
        final Entity newTarget = select.getSelection();

        if (xref.getTarget() != null) {
            LOG.fine("Swiveling " + xref.getEntity().getId() + "." + xref.getPath() + " from " + xref.getTarget().getEntity().getId() + " to " + newTarget.getId());
        } else {
            LOG.fine("Swiveling " + xref.getEntity().getId() + "." + xref.getPath() + " to " + newTarget.getId());
        }

        try {
            xref.getGedcom().doUnitOfWork(new UnitOfWork() {
                public void perform(Gedcom gedcom) throws GedcomException {
                    Property backpointer = xref.getTarget();
                    if (backpointer != null) {
                        xref.unlink();
                        backpointer.getParent().delProperty(backpointer);
                    }
                    xref.setValue("@" + newTarget.getId() + "@");
                    xref.link();
                }
            });
        } catch (GedcomException ge) {
            DialogManager.create(getText(), ge.getMessage())
                    .setMessageType(DialogManager.WARNING_MESSAGE).show();
            LOG.log(Level.FINER, ge.getMessage(), ge);
        }

        // done
    }

}


