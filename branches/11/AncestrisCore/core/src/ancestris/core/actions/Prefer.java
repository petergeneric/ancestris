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

import ancestris.core.resources.Images;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
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

/**
 *
 * @author frederic
 */
@ActionID(category = "Edit", id = "ancestris.core.actions.Prefer")
@ActionRegistration(displayName = "#xref.preferon",lazy = false)
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position = 725)})
public class Prefer extends AbstractAncestrisContextAction {

    protected final static Resources RESOURCES = Resources.get(Prefer.class);
    protected final static Logger LOG = Logger.getLogger("ancestris.core.actions");

    private static ImageIcon IMAGE;
        
    private Fam fam;
    private boolean isPreferred = false;

    
    @Override
    public void resultChanged(LookupEvent ev) {
        // valid only for context aware action
        if (lkpInfo != null) {
            fam = null;
            for (Property prop : lkpInfo.allInstances()) {
                if (prop instanceof Fam) {
                    fam = (Fam) prop;
                    isPreferred = fam.isPreferred();
                }
            }
            super.resultChanged(ev);
        }
    }

    @Override
    protected void contextChanged() {
        super.contextChanged();
        if (fam != null) {
            IMAGE = fam.getImage(false);
            if (isPreferred) {
                super.setImage(IMAGE.getOverLayed(Images.imgPref));
            } else {
                super.setImage(IMAGE);
            }
        }
        setText(getLabel());
        setTip(RESOURCES.getString("xref.prefer.tip"));
        setEnabled(fam != null);
    }

    
    
    public Prefer() {
        super();
    }
    
    public Prefer(Fam fam) {
        this.fam = fam;
        super.setText(getLabel());
        super.setImage(Images.imgPref);
        super.setTip(RESOURCES.getString("xref.prefer.tip"));
        setEnabled(fam != null);
}

    private String getLabel() {
        return RESOURCES.getString(isPreferred ? "xref.preferoff" : "xref.preferon");
    }
    
    @Override
    public void actionPerformedImpl(ActionEvent event) {

        if (fam == null) {
            return;
        }

        try {
            fam.getGedcom().doUnitOfWork(new UnitOfWork() {
                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    isPreferred = !isPreferred;
                    fam.setPreferred(isPreferred);
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


