package ancestris.modules.gedcom.searchduplicates;

import genj.gedcom.Gedcom;
import java.awt.Component;
import java.util.ArrayList;
import static ancestris.modules.gedcom.searchduplicates.Bundle.*;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
@NbBundle.Messages("SearchDuplicatesWizardIterator.name.text={0} from {1}")
public final class SearchDuplicatesWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    private int index;
    private WizardDescriptor wizardDescriptor;
    private WizardDescriptor.Panel<WizardDescriptor>[] allPanels;
    private WizardDescriptor.Panel<WizardDescriptor>[] choiseSequence;
    private WizardDescriptor.Panel<WizardDescriptor>[] indiOnlySelectedSequence;
    private WizardDescriptor.Panel<WizardDescriptor>[] famOnlySelectedSequence;
    private WizardDescriptor.Panel<WizardDescriptor>[] indiAndFamSelectedSequence;
    private WizardDescriptor.Panel<WizardDescriptor>[] indiNorFamSelectedSequence;
    private WizardDescriptor.Panel<WizardDescriptor>[] currentPanels;
    private String[] choiseIndex;
    private String[] indiOnlySelectedIndex;
    private String[] famOnlySelectedIndex;
    private String[] indiAndFamSelectedIndex;
    private String[] indiNorFamSelectedIndex;
    private EventListenerList listenerList = new EventListenerList();
    private ChangeEvent changeEvent = null;

    SearchDuplicatesWizardIterator() {
    }

    public void initialize(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
        allPanels = new WizardDescriptor.Panel[]{
            new SearchDuplicatesWizardPanel1(),
            new SearchDuplicatesWizardPanel2(),
            new SearchDuplicatesWizardPanel3(),
            new SearchDuplicatesWizardPanel4(),
            new SearchDuplicatesWizardPanel5()
        };
        String[] steps = new String[allPanels.length];
        for (int i = 0; i < allPanels.length; i++) {
            Component c = allPanels[i].getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        choiseSequence = new WizardDescriptor.Panel[]{
            allPanels[0], allPanels[1]
        };
        choiseIndex = new String[]{
            steps[0], steps[1], "...."
        };

        indiOnlySelectedSequence = new WizardDescriptor.Panel[]{
            allPanels[0], allPanels[1], allPanels[2], allPanels[4]
        };
        indiOnlySelectedIndex = new String[]{
            steps[0], steps[1], steps[2], steps[4]
        };

        famOnlySelectedSequence = new WizardDescriptor.Panel[]{
            allPanels[0], allPanels[1], allPanels[3], allPanels[4]
        };
        famOnlySelectedIndex = new String[]{
            steps[0], steps[1], steps[3], steps[4]
        };

        indiAndFamSelectedSequence = new WizardDescriptor.Panel[]{
            allPanels[0], allPanels[1], allPanels[2], allPanels[3], allPanels[4]
        };
        indiAndFamSelectedIndex = new String[]{
            steps[0], steps[1], steps[2], steps[3], steps[4]
        };

        indiNorFamSelectedSequence = new WizardDescriptor.Panel[]{
            allPanels[0], allPanels[1], allPanels[4]
        };
        indiNorFamSelectedIndex = new String[]{
            steps[0], steps[1], steps[4]
        };

        currentPanels = choiseSequence;
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DATA, choiseIndex);
    }

    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        return currentPanels;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels()[index];
    }

    @Override
    public String name() {
        if (index == 0 || index == 1) {
            return SearchDuplicatesWizardIterator_name_text(index + 1, "...");
        } else {
            return SearchDuplicatesWizardIterator_name_text(index , getPanels().length);

        }
    }

    @Override
    public boolean hasNext() {
        if (index == 0 || index == 1) {
            return true;
        } else {
            return index < getPanels().length - 1;
        }
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

        // Current index
        if (index == 1) {
            SearchDuplicatesWizardPanel2 searchDuplicatesWizardPanel2 = (SearchDuplicatesWizardPanel2) allPanels[index];
            ArrayList<String> selectedEntities = searchDuplicatesWizardPanel2.getComponent().getSelectedEntities();

            if (selectedEntities.contains(Gedcom.INDI) == true && selectedEntities.contains(Gedcom.FAM) == true) {
                currentPanels = indiAndFamSelectedSequence;
                wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DATA, indiAndFamSelectedIndex);
            } else if (selectedEntities.contains(Gedcom.INDI) == true) {
                currentPanels = indiOnlySelectedSequence;
                wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DATA, indiOnlySelectedIndex);
            } else if (selectedEntities.contains(Gedcom.FAM) == true) {
                currentPanels = famOnlySelectedSequence;
                wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DATA, famOnlySelectedIndex);
            } else {
                currentPanels = indiNorFamSelectedSequence;
                wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DATA, indiNorFamSelectedIndex);
            }
            fireChangeListener();
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

    @Override
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    protected void fireChangeListener() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Lazily create the event:
        if (changeEvent == null) {
            changeEvent = new ChangeEvent(this);
        }

        // Process the listeners notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }
}
