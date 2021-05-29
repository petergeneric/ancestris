/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.gedcomcompare.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class LocalGedcomsPopup extends JPopupMenu  {
    
    private List<LocalGedcomFrame> listOfGedcoms;
    private ButtonGroup buttonGroup;
            
    public LocalGedcomsPopup(List<LocalGedcomFrame> list) {
        
        buttonGroup = new ButtonGroup();
        listOfGedcoms = list;
        updateItems();
        
    }    
    
    public void updateItems() {

        // clear buttons
        while (buttonGroup.getElements().hasMoreElements()) {
            buttonGroup.remove(buttonGroup.getElements().nextElement());
        }
        removeAll();
        
        // Quit if empty
        if (listOfGedcoms.isEmpty()) {
            return;
        }
        
        // Recreate otherwise
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel label = new JLabel(NbBundle.getMessage(getClass(), "LocalGedcomsPopup_Choose_Main"));
        add(label);
        
        listOfGedcoms.stream().sorted(
                (gedcom1, gedcom2) -> gedcom1.getGedcom().getDisplayName().toUpperCase().compareTo(gedcom2.getGedcom().getDisplayName().toUpperCase())
        ).forEachOrdered((gedcom) -> {
            JRadioButton button = new JRadioButton(gedcom.getGedcom().getDisplayName()+"   ");
            button.setSelected(gedcom.isMain());
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    gedcom.setMain(true);
                }
            });
            buttonGroup.add(button);
            add(button);
        });
        
        // Select first if none selected
        boolean isSelected = false;
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            isSelected |= buttons.nextElement().isSelected();
        }
        
        if (!isSelected) {
            JRadioButton button = (JRadioButton) buttonGroup.getElements().nextElement();
            //button.setSelected(true);
            button.doClick();
        }
        
        
    }

}