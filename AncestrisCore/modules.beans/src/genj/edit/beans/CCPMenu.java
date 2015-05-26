/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package genj.edit.beans;

import ancestris.core.resources.Images;
import genj.gedcom.Gedcom;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class CCPMenu extends JPopupMenu {

    JTextComponent text = null;

    public CCPMenu(JComponent jc) {
        Component[] jcs = jc.getComponents();
        for (int i = 0; i < jcs.length; i++) {
            Component jc1 = jcs[i];
            if (jc1 instanceof JTextComponent) {
                JTextComponent jtc = (JTextComponent) jc1;
                jtc.setComponentPopupMenu(new CCPMenu(jtc));
            }
            if (jc1 instanceof JComboBox) {
                JComboBox jcb = (JComboBox) jc1;
                JTextComponent jtc = (JTextComponent) jcb.getEditor().getEditorComponent();
                jtc.setComponentPopupMenu(new CCPMenu(jtc));
            }
        }
    }
        
    public CCPMenu(JTextComponent c) {
        this.text = c;

        JMenuItem cut = new JMenuItem(NbBundle.getMessage(CCPMenu.class, "action.cut"));
        cut.setIcon(Images.imgCut);
        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                text.cut();
            }
        });
        add(cut);

        JMenuItem copy = new JMenuItem(NbBundle.getMessage(CCPMenu.class, "action.copy"));
        copy.setIcon(Images.imgCopy);
        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                text.copy();
            }
        });
        add(copy);

        JMenuItem paste = new JMenuItem(NbBundle.getMessage(CCPMenu.class, "action.paste"));
        paste.setIcon(Images.imgPaste);
        paste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                text.paste();
            }
        });
        add(paste);
    }

}
