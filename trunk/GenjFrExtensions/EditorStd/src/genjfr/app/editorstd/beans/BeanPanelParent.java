/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.editorstd.beans;

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.UnitOfWork;
import ancestris.gedcom.GedcomDirectory;
import javax.swing.text.JTextComponent;
import org.openide.util.Exceptions;

/**
 * A set of common methods to all beans
 * @author frederic
 */
public class BeanPanelParent extends javax.swing.JPanel {

    public Property parentProperty;

    public void updateField(JTextComponent text, Property prop) {
        if (prop != null) {
            updateField(text, prop.getDisplayValue());
        } else {
            text.setText("");
        }
    }

    public void updateField(JTextComponent text, String newText) {
        text.setText(newText);
    }

    public void updateModified() {
        GedcomDirectory.getInstance().updateModified(parentProperty.getGedcom());
    }

    public boolean hasFieldChanged(Property propToSave, String value) {
        if (propToSave == null || value == null) {
            return false;
        }
        return !(propToSave.getValue().equals(value));
    }

    public void updateGedcom(final Property parentProperty, final Property propToSave, final String PROP_TAG, final String value) {
        if (parentProperty == null || value == null) {
            return;
        }
        try {
            parentProperty.getGedcom().doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    if (propToSave != null) {
                        propToSave.setValue(value);
                        updateModified();
                        return;
                    }
                    if (propToSave == null && !value.isEmpty()) {
                        parentProperty.addProperty(PROP_TAG, value);
                        updateModified();
                        return;
                    }
                }
            });
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
