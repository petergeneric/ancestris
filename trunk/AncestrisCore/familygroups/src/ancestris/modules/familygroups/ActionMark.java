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

package ancestris.modules.familygroups;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.SubMenuAction;
import ancestris.gedcom.GedcomDirectory;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.io.Filter;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.ImageIcon;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author frederic
 */
public class ActionMark extends SubMenuAction {

    private static ImageIcon imgMark = new javax.swing.ImageIcon(ActionMark.class.getResource("MarkingIcon.png")); // NOI18N
    private static ImageIcon imgUnmark = new javax.swing.ImageIcon(ActionMark.class.getResource("Clean.png")); // NOI18N
    private static String TAG = "_FAMILY_GROUP";
    private Gedcom gedcom;
    private List<? extends Filter> filters;
    

    public ActionMark(Gedcom gedcom, List<? extends Filter> filters) {
        super();
        this.gedcom = gedcom;
        this.filters = filters;
        setImage(imgMark);
        setTip(NbBundle.getMessage(ActionMark.class, "markgroup.tip"));
        this.addAction(createMarkAction());
        this.addAction(createUnmarkAction());
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        super.actionPerformed(event);
    }
    
    private AbstractAncestrisAction createMarkAction() {
        // Scan all individuals, check which group it belongs to, and mark it
        AbstractAncestrisAction action = new AbstractAncestrisAction() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                commit(new Runnable() {
                    @Override
                    public void run() {
                        for (Indi indi : gedcom.getIndis()) {
                            for (Filter f:filters) {
                                if (!f.veto(indi)) {
                                    String value = f.getFilterName();
                                    Property prop = indi.getProperty(TAG);
                                    if (prop == null) {
                                        prop = indi.addProperty(TAG, "");
                                    }
                                    prop.setValue(value);
                                    break;
                                }
                            }
                        }
                        Context ctx = Utilities.actionsGlobalContext().lookup(Context.class);
                        GedcomDirectory.getDefault().activateTopComponent(ctx);
                    }
                    
                });
            }
        };
        action.setImage(imgMark);
        action.setText(NbBundle.getMessage(ActionMark.class, "markgroup.mark"));
        return action;
    }

    private AbstractAncestrisAction createUnmarkAction() {
        // Scan all individuals, remove tag
        AbstractAncestrisAction action = new AbstractAncestrisAction() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                commit(new Runnable() {
                    @Override
                    public void run() {
                        for (Indi indi : gedcom.getIndis()) {
                            for (Property prop : indi.getProperties(TAG)) {
                                indi.delProperty(prop);
                            }
                        }
                        Context ctx = Utilities.actionsGlobalContext().lookup(Context.class);
                        GedcomDirectory.getDefault().activateTopComponent(ctx);
                    }
                    
                });
            }
        };
        action.setImage(imgUnmark);
        action.setText(NbBundle.getMessage(ActionMark.class, "markgroup.unmark"));
        return action;
    }
    
    private void commit(final Runnable task) {
        try {
            if (gedcom.isWriteLocked()) {
                task.run();
            } else {
                gedcom.doUnitOfWork((Gedcom localGedcom) -> {
                    task.run();
                });
            }
        } catch (GedcomException ge) {
            Exceptions.printStackTrace(ge);
        } finally {
        }
    }
    
    
    
}
