
package ancestris.modules.releve.editor;

/**
 *
 * @author michel
 */

import ancestris.modules.releve.model.Record;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class EditorConfigGroupDialog {
    
    
    static public void  showEditorConfigGroupDialog(final Record.RecordType recordType, final EditorBeanGroup group, final MouseEvent evt) {
        final JPopupMenu popup = new JPopupMenu();
    
       //je cree lees items du popup menu     
       for(EditorBeanField field : group.getFields() ) {
           if (field.isUsed()) {
               JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(field.getLabel(), field.isVisible());
               menuItem.addActionListener(new FieldActionListener(recordType, field));
               popup.add(menuItem);
           }
       }
       popup.show(evt.getComponent(), evt.getX(), evt.getY());
    }
    
    static class FieldActionListener implements ActionListener {
        private final Record.RecordType recordType;
        private final EditorBeanField field;
        FieldActionListener( Record.RecordType recordType, EditorBeanField field) {
            this.recordType = recordType;
            this.field = field;
        }
        @Override
        public void actionPerformed(ActionEvent evt) {
            JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) evt.getSource();
            // je met à jour la visibilité du champs
            field.setVisible(menuItem.isSelected());
            // j'enregistre les modifcations
            EditorBeanGroup.savePreferences();
            fireEditorConfigListener( recordType);
        }        
    }
    
    ///////////////////////////////////////////////////////////////////////////
    /**
     * gestion des listener pour propager la modifcation de la configuration 
     * à toutes les instances de l'editeur. 
     */
    static protected interface EditorConfigListener {
        void onEditorConfigChanged(Record.RecordType recordType);
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

    static public void fireEditorConfigListener (final Record.RecordType recordType) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (EditorConfigListener listener : editorConfigListeners) {
                    listener.onEditorConfigChanged(recordType);
                }
            }
        });
    }
}
