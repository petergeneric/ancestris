package ancestris.modules.gedcom.searchduplicates;

import genj.gedcom.Gedcom;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.openide.WizardDescriptor;

public final class SearchDuplicatesWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> allPanels;
    private List<WizardDescriptor.Panel<WizardDescriptor>> currentPanels;
    private EventListenerList listenerList = new EventListenerList();
    private ChangeEvent changeEvent = null;

    SearchDuplicatesWizardIterator() {
        allPanels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        allPanels.add(new SearchDuplicatesWizardPanel1());
        allPanels.add(new SearchDuplicatesWizardPanel2());
        allPanels.add(new SearchDuplicatesWizardPanel3());
        allPanels.add(new SearchDuplicatesWizardPanel4());
        String[] steps = new String[allPanels.size()];
        for (int i = 0; i < allPanels.size(); i++) {
            Component c = allPanels.get(i).getComponent();
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
        SearchDuplicatesWizardPanel2 searchDuplicatesWizardPanel2 = (SearchDuplicatesWizardPanel2) allPanels.get(1);
        ArrayList<String> selectedEntities = searchDuplicatesWizardPanel2.getComponent().getSelectedEntities();
        currentPanels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        currentPanels.add(allPanels.get(0));
        currentPanels.add(allPanels.get(1));
        if (selectedEntities.contains(Gedcom.INDI) == true) {
            currentPanels.add(allPanels.get(2));
        }
        if (selectedEntities.contains(Gedcom.FAM) == true) {
            currentPanels.add(allPanels.get(3));
        }
    }

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        return currentPanels;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels().get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().size();
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

        // Current index
        if (index == 1) {
            SearchDuplicatesWizardPanel2 searchDuplicatesWizardPanel2 = (SearchDuplicatesWizardPanel2) allPanels.get(index);
            ArrayList<String> selectedEntities = searchDuplicatesWizardPanel2.getComponent().getSelectedEntities();

            currentPanels.clear();
            currentPanels.add(allPanels.get(0));
            currentPanels.add(allPanels.get(1));
            if (selectedEntities.contains(Gedcom.INDI) == true) {
                currentPanels.add(allPanels.get(2));
            }
            if (selectedEntities.contains(Gedcom.FAM) == true) {
                currentPanels.add(allPanels.get(3));
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
