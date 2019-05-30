/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.geoplace;

import ancestris.api.editor.AncestrisEditor;
import ancestris.util.swing.DialogManager.ADialog;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.UnitOfWork;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.jxmapviewer.viewer.GeoPosition;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel & frederic
 */
@ServiceProvider(service = AncestrisEditor.class,position = 100)
public class PlaceEditor extends AncestrisEditor {
    private PlaceEditorPanel editorPanel = null;
    private static ImageIcon editorIcon = new ImageIcon(PlaceEditor.class.getResource("resources/geo.png")); // NOI18N

    public PlaceEditor() {
    }

    @Override
    public boolean canEdit(Property property) {
        return property instanceof PropertyPlace;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    
    
    
    @Override
    public Property edit(Property property, boolean isNew) {
        return edit(property, null);
    }

    @Override
    public Property add(Property parent) {
        return edit(null, parent);
    }

    
    
    
    
    public Property edit(Property place, Object arg) {
        if (place == null) {
            return null;
        }
        Gedcom gedcom = place.getGedcom();
        GeoPosition geoPoint = null;
        if (arg != null) {
            if (arg instanceof GeoPosition) {
                geoPoint = (GeoPosition) arg;
            }
        }
        if (gedcom != null && place instanceof PropertyPlace) {
            if (editorPanel == null) {
                editorPanel = new PlaceEditorPanel();
            }
            editorPanel.set(gedcom, (PropertyPlace) place, true);
            editorPanel.setGeoPoint(geoPoint);
            String title = NbBundle.getMessage(getClass(), "PlaceEditorPanel.edit.all", place.getDisplayValue());
            ADialog dialog = new ADialog(title, editorPanel);
            if (dialog.show() == ADialog.OK_OPTION) {
                // Add dow:
                    try {
                        gedcom.doUnitOfWork(new UnitOfWork() {
                            @Override
                            public void perform(Gedcom gedcom) throws GedcomException {
                                editorPanel.commit();
                            }
                        });
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                return editorPanel.get();
            }
        }
        return null;
    }

    @Override
    public String getName(boolean canonical) {
        if (canonical) {
            return getClass().getCanonicalName();
        } else {
            return NbBundle.getMessage(PlaceEditor.class, "OpenIDE-Module-Name");
        }
    }

    @Override
    public ImageIcon getIcon() {
        return editorIcon; // default
    }
        
    @Override
    public String toString() {
        return getName(false);
    }

    @Override
    public Action getCreateParentAction(Indi indi, int sex) {
        return getDefaultAction(indi);
    }

    @Override
    public Action getCreateSpouseAction(Indi indi) {
        return getDefaultAction(indi);
    }
    

}
