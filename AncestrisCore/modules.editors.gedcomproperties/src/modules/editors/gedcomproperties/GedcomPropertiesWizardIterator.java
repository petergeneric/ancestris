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
package modules.editors.gedcomproperties;

import genj.gedcom.Property;
import genj.gedcom.Submitter;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public final class GedcomPropertiesWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    static public final int CREATION_MODE = 0; 
    static public final int UPDATE_MODE = 1;

    static private int mode;
    static private Property prop_HEAD;
    static private Submitter prop_SUBM;
    

    static public int getMode(){
        return mode;
    }
    
    static Property getHead() {
        return prop_HEAD;
    }

    static Submitter getSubmitter() {
        return prop_SUBM;
    }

    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    /**
     * Constructor
     */
    public GedcomPropertiesWizardIterator(int mode, Property propEditedHeader, Submitter propEditedSubmitter) {
        this.mode = mode;
        this.prop_HEAD = propEditedHeader;
        this.prop_SUBM = propEditedSubmitter;
    }

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
            panels.add(new GedcomPropertiesWizardPanel1());
            panels.add(new GedcomPropertiesWizardPanel2());
            panels.add(new GedcomPropertiesWizardPanel3());
            panels.add(new GedcomPropertiesWizardPanel4());
            if (mode == CREATION_MODE) {
                panels.add(new GedcomPropertiesWizardPanel5());
            }
            panels.add(new GedcomPropertiesWizardPanel6());
            String[] steps = new String[panels.size()];
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                    if (mode == CREATION_MODE) {
                        jc.putClientProperty(WizardDescriptor.PROP_IMAGE, ImageUtilities.loadImage(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "CreateImage")));
                        //jc.putClientProperty("WizardPanel_helpDisplayed", Boolean.TRUE); // Turn on an help tab
                    }
                    if (mode == UPDATE_MODE) {
                        jc.putClientProperty(WizardDescriptor.PROP_IMAGE, ImageUtilities.loadImage(NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "UpdateImage")));
                        //jc.putClientProperty("WizardPanel_helpDisplayed", Boolean.TRUE); // Turn on an help tab
                    }
                }
            }
        }
        return panels;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels().get(index);
    }

    @Override
    public String name() {
        return ""; //index + 1 + " " + NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "LBL_from") + " " + getPanels().size();
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed

}
