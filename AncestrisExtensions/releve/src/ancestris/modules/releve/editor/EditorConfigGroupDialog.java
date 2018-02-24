
package ancestris.modules.releve.editor;

/**
 *
 * @author michel
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

public class EditorConfigGroupDialog {
    
    
    static public void  showEditorConfigGroupDialog2(EditorBeanGroup group, MouseEvent evt) {
        final JPopupMenu popup = new JPopupMenu();
    
       //je cree le popupmenu        
       for(EditorBeanField field : group.getFields() ) {
           if (field.isUsed()) {
               StayOpenCheckBoxMenuItem menuItem = new StayOpenCheckBoxMenuItem(field.getLabel());
               menuItem.setState(field.isVisible());
               menuItem.addActionListener(new FieldActionListener(field));
               popup.add(menuItem);
           }
       }
       popup.show(evt.getComponent(), evt.getX(), evt.getY());
    }
    
    void initData(EditorBeanGroup group, MouseEvent evt) {
    }
    
    
    static class FieldActionListener implements ActionListener {
        private final EditorBeanField field;
        FieldActionListener( EditorBeanField field) {
            this.field = field;
        }
        @Override
        public void actionPerformed(ActionEvent evt) {
            JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) evt.getSource();
            // je met à jour la visibilité du champs
            field.setVisible(menuItem.isSelected());
            // j'enregistre les modifcations
            EditorBeanGroup.savePreferences();
            fireEditorConfigListener();
        }        
    }
    
    
    
    /**
     * extension de  JCheckBoxMenuItem pour mainteni le menu popup
     * affiché après avoir cliqué sur un item
     */
    static public class StayOpenCheckBoxMenuItem extends JCheckBoxMenuItem {

        public StayOpenCheckBoxMenuItem(String text) {
            super(text);
        }

        @Override
        public void updateUI() {
            super.updateUI();
            setUI(new BasicCheckBoxMenuItemUI() {
                @Override
                protected void doClick(MenuSelectionManager msm) {
                    //super.doClick(msm);
                    menuItem.doClick(0);
                }
            });
        }

    }
    
    ///////////////////////////////////////////////////////////////////////////
    /**
     * gestion des listener pour propages la modifcation de la configuration 
     * à toutes les instances de l'editeur. 
     */
    static protected interface EditorConfigListener {
        void onEditorConfigChanged();
    }
    
    static private final ArrayList<EditorConfigListener> editorConfigListeners = new ArrayList<EditorConfigListener>(1);
    
       /**
     * @param validationListeners the validationListeners to set
     */
    static public void addEditorConfigListener(EditorConfigListener listener) {
        editorConfigListeners.add(listener);
    }

    /**
     * @param validationListeners the validationListeners to set
     */
    static public void removeEditorConfigListener(EditorConfigListener listener) {
        editorConfigListeners.remove(listener);
    }

    static public void fireEditorConfigListener () {
          for (EditorConfigListener listener : editorConfigListeners) {
            listener.onEditorConfigChanged();
        }
    }
}
