/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.nav;

import ancestris.api.editor.AncestrisEditor;
import ancestris.awt.FilteredMouseAdapter;
import ancestris.modules.beans.ABluePrintBeans;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import org.openide.awt.MouseUtils;

/**
 *
 * @author zurga
 */
class ABeanHandler extends FilteredMouseAdapter {

    final FamilyPanel fPanel;

    public ABeanHandler(FamilyPanel fPane) {
        super();
        fPanel = fPane;
    }

    @Override
    public void mousePressed(MouseEvent evt) {

        // Return if no source
        Object src = evt.getSource();
        if (src == null) {
            return;
        }
        // Get bean clicked
        ABluePrintBeans bean = null;
        if (src instanceof ABluePrintBeans) {
            bean = (ABluePrintBeans) src;
            if (bean == null) {
                return;
            }
        }
        
        // RIGHT CLICK = SELECT STICKY
        if (evt != null && evt.getButton() == MouseEvent.BUTTON3) {
            Property prop = bean.getProperty();
            if (prop != null) {
                fPanel.setSticky(true);
                fPanel.setSelectedPanel(bean.getParent());
                SelectionDispatcher.fireSelection(evt, new Context(prop));
            }

        // DOUBLE CLICK = EDIT STICKY
        } else if (MouseUtils.isDoubleClick(evt)) {
            try {
                fPanel.setSticky(true);
                // Double click on someone = edit it (with an AncestrisEditor, not an Editor)
                if (bean != null && bean.getProperty() != null) {
                    AncestrisEditor editor = AncestrisEditor.findEditor(bean.getProperty());
                    if (editor != null) {
                        editor.edit(bean.getProperty());
                    }
                } else {
                    // Double click on empty = create person (with an AncestrisEditor, not an Editor)
                    getCreateAction().actionPerformed(new ActionEvent(evt.getSource(), 0, ""));
                }
                fPanel.refresh();
            } finally {
                fPanel.setSticky(false);
            }

        // SIMPLE CLICK = SELECT NOT STICKY
        } else if (evt.getClickCount() == 1 && bean != null && bean.getProperty() != null) {
            Container c = bean.getParent();
            if (c != null) {
                Property prop = bean.getProperty();
                if (prop instanceof Entity) {
                    // In case of selection of another spouse, change context back to main indi and only change spouse index
                    if (c.equals(fPanel.getoFamsPanel()) && prop instanceof Indi) {
                        Indi spouse = (Indi) prop; // other spouse clicked
                        Fam[] fams = fPanel.getFocusIndi().getFamiliesWhereSpouse();
                        for (int idx = 0; idx < fams.length; idx++) {
                            Indi os = fams[idx].getOtherSpouse(fPanel.getFocusIndi());
                            if (os != null && os.equals(spouse)) {
                                fPanel.setFamIndex(idx);
                                fPanel.refresh();
                                return;
                            }
                        }
                    }
                }
                fPanel.setSticky(false);
                SelectionDispatcher.fireSelection(new Context(prop));
            }
        }

    }


    public ActionListener getCreateAction() {
        return (ActionEvent e) -> {/* Nothing to do */        };
    }

}
