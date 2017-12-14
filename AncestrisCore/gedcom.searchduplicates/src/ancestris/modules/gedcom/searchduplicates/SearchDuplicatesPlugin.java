package ancestris.modules.gedcom.searchduplicates;

import ancestris.core.pluginservice.AncestrisPlugin;
import static ancestris.modules.gedcom.searchduplicates.Bundle.*;
import ancestris.modules.gedcom.utilities.GedcomUtilities;
import ancestris.modules.gedcom.utilities.matchers.*;
import genj.gedcom.*;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lemovice left and right entities could be the same.
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
@NbBundle.Messages({"SearchDuplicatesPlugin.firstButton=Go to first duplicate (Ctrl + Up)",
    "SearchDuplicatesPlugin.previousButton=Go to previous duplicate (Ctrl + Left)",
    "SearchDuplicatesPlugin.swapButton=Swap left and right entities (Ctrl + Backspace)",
    "SearchDuplicatesPlugin.nextButton=Go to next duplicate (Ctrl + Right)",
    "SearchDuplicatesPlugin.lastButton=Go to last duplicate (Ctrl + Down)",
    "SearchDuplicatesPlugin.mergeButton=<html>Merge checked properties of the right<br>into the entity of the left,<br>and deletes the entity on the right (Ctrl + Enter)</html>",
    "SearchDuplicatesPlugin.cleanButton=<html>Remove duplicates including<br>the entity on the right (Ctrl + Space)</html>",
    "SearchDuplicatesPlugin.closeButton=<html>Stop the duplicates merge<br>and close the window (Esc)</html>",
    "SearchDuplicatesPlugin.noSelectedProperties=Nothing is checked on the entity on the right.\nThis will only delete it.\nOK to delete it ?"})
public class SearchDuplicatesPlugin extends AncestrisPlugin implements Runnable {

    private static final Logger log = Logger.getLogger(SearchDuplicatesPlugin.class.getName());
    private Gedcom gedcom;
    private TreeMap<String, EntityMatcher> entitiesMatchers = new TreeMap<String, EntityMatcher>() {
        {
            put(Gedcom.INDI, new IndiMatcher());
            put(Gedcom.FAM, new FamMatcher());
            put(Gedcom.NOTE, new NoteMatcher());
            put(Gedcom.SOUR, new SourceMatcher());
            put(Gedcom.REPO, new RepositoryMatcher());
            put(Gedcom.SUBM, new SubmitterMatcher());
            put(Gedcom.OBJE, new MediaMatcher());
        }
    };
    private final List<String> entities2Ckeck;
    private Map<String, ? extends MatcherOptions> selectedOptions;

    public SearchDuplicatesPlugin() {
        this.gedcom = null;
        this.entities2Ckeck = null;
        this.selectedOptions = null;
    }

    public SearchDuplicatesPlugin(Gedcom leftGedcom, List<String> entities2Ckeck, Map<String, ? extends MatcherOptions> selectedOptions) {
        this.gedcom = leftGedcom;
        this.entities2Ckeck = entities2Ckeck;
        this.selectedOptions = selectedOptions;
    }

    @Override
    public void run() {
        //final LinkedList<PotentialMatch<? extends Entity>> matchesLinkedList = new LinkedList<PotentialMatch<? extends Entity>>();
        final LinkedList<PotentialMatch<? extends Entity>> matchesLinkedList = new LinkedList<PotentialMatch<? extends Entity>>();
        final HashMap<String, Integer> duplicatesHashMap = new HashMap<String, Integer>();
        if (gedcom == null) {
            return;
        }
        try {
            
            // Get matches by block of entity type
            for (String tag : entities2Ckeck) {
                List<? extends Entity> entities = new ArrayList<Entity>(gedcom.getEntities(tag));

                log.log(Level.FINE, "Checking: {0}", tag);
                if (tag.equals(Gedcom.INDI)) {
                    (entitiesMatchers.get(tag)).setOptions((IndiMatcherOptions) selectedOptions.get(Gedcom.INDI));
                } else if (tag.equals(Gedcom.FAM)) {
                    (entitiesMatchers.get(tag)).setOptions((FamMatcherOptions) selectedOptions.get(Gedcom.FAM));
                } else if (tag.equals(Gedcom.NOTE)) {
                    (entitiesMatchers.get(tag)).setOptions((NoteMatcherOptions) selectedOptions.get(Gedcom.NOTE));
                } else if (tag.equals(Gedcom.REPO)) {
                    (entitiesMatchers.get(tag)).setOptions((RepositoryMatcherOptions) selectedOptions.get(Gedcom.REPO));
                } else if (tag.equals(Gedcom.SOUR)) {
                    (entitiesMatchers.get(tag)).setOptions((SourceMatcherOptions) selectedOptions.get(Gedcom.SOUR));
                } else if (tag.equals(Gedcom.SUBM)) {
                    (entitiesMatchers.get(tag)).setOptions((SubmitterMatcherOptions) selectedOptions.get(Gedcom.SUBM));
                } else if (tag.equals(Gedcom.OBJE)) {
                    (entitiesMatchers.get(tag)).setOptions((MediaMatcherOptions) selectedOptions.get(Gedcom.OBJE));
                }
                // Get block
                List<PotentialMatch<? extends Entity>> potentialMatches = (entitiesMatchers.get(tag)).getPotentialMatches(entities);
                
                // Swap matches so that left entity is with smaller id
                for (PotentialMatch<? extends Entity> e : potentialMatches) {
                    String idLeft = e.getLeft().getId();
                    String idRight = e.getRight().getId();
                    if (idLeft.compareToIgnoreCase(idRight) > 0) {
                        e.swap();
                    }
                }
                
                // Sort matches by certainty then entity name
                Collections.sort(potentialMatches, new Comparator<PotentialMatch<? extends Entity>>() {
                    @Override
                    public int compare(PotentialMatch<? extends Entity> e1, PotentialMatch<? extends Entity> e2) {
                        if (e2.getCertainty() - e1.getCertainty() != 0) {
                            return e2.getCertainty() - e1.getCertainty();
                        }
                        return e1.getLeft().toString(true).toLowerCase().compareTo(e2.getLeft().toString(true).toLowerCase());
                    }
                });
                matchesLinkedList.addAll(potentialMatches);
                duplicatesHashMap.put(tag, potentialMatches.size());
            }
            
            // Display them
            SwingUtilities.invokeLater(new Runnable() {
                ResultPanel entityViewPanel = new ResultPanel(gedcom);
                DialogDescriptor checkDuplicatePanelDescriptor;
                int linkedListIndex = -1;
                final JButton firstButton = new JButton();
                final JButton previousButton = new JButton();
                final JButton swapButton = new JButton();
                final JButton nextButton = new JButton();
                final JButton lastButton = new JButton();
                final JButton mergeButton = new JButton();
                final JButton cleanButton = new JButton();
                final JButton closeButton = new JButton();

                @Override
                public void run() {
                    Action doFirst = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (firstButton.isEnabled()) {
                                firstButtonActionPerformed(e);
                            }
                        }
                    };
                    firstButton.setAction(doFirst);
                    firstButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/gedcom/searchduplicates/first.png")));
                    firstButton.setToolTipText(SearchDuplicatesPlugin_firstButton()); // NOI18N
                    firstButton.setEnabled(false);
                    firstButton.setDefaultCapable(true);
                    firstButton.putClientProperty("defaultButton", Boolean.FALSE); //NOI18N
                    
                    Action doPrevious = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (previousButton.isEnabled()) {
                                previousButtonActionPerformed(e);
                            }
                        }
                    };
                    previousButton.setAction(doPrevious);
                    previousButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/gedcom/searchduplicates/previous.png")));
                    previousButton.setToolTipText(SearchDuplicatesPlugin_previousButton()); // NOI18N
                    previousButton.setEnabled(false);
                    previousButton.setDefaultCapable(true);
                    previousButton.putClientProperty("defaultButton", Boolean.FALSE); //NOI18N
                    
                    Action doSwap = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (swapButton.isEnabled()) {
                                swapButtonActionPerformed(e);
                            }
                        }
                    };
                    swapButton.setAction(doSwap);
                    swapButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/gedcom/searchduplicates/swap.png")));
                    swapButton.setToolTipText(SearchDuplicatesPlugin_swapButton()); // NOI18N
                    swapButton.setEnabled(true);
                    swapButton.setDefaultCapable(true);
                    swapButton.putClientProperty("defaultButton", Boolean.FALSE); //NOI18N
                    
                    Action doNext = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (nextButton.isEnabled()) {
                                nextButtonActionPerformed(e);
                            }
                        }
                    };
                    nextButton.setAction(doNext);
                    nextButton.setDefaultCapable(true);
                    nextButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/gedcom/searchduplicates/next.png")));
                    nextButton.setToolTipText(SearchDuplicatesPlugin_nextButton()); // NOI18N
                    nextButton.setEnabled(false);
                    
                    Action doLast = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (lastButton.isEnabled()) {
                                lastButtonActionPerformed(e);
                            }
                        }
                    };
                    lastButton.setAction(doLast);
                    lastButton.setDefaultCapable(true);
                    lastButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/gedcom/searchduplicates/last.png")));
                    lastButton.setToolTipText(SearchDuplicatesPlugin_lastButton()); // NOI18N
                    lastButton.setEnabled(false);
                    lastButton.putClientProperty("defaultButton", Boolean.FALSE); //NOI18N
                    
                    Action doMerge = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (mergeButton.isEnabled()) {
                                mergeButtonActionPerformed(e);
                            }
                        }
                    };
                    mergeButton.setAction(doMerge);
                    mergeButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/gedcom/searchduplicates/merge.png")));
                    mergeButton.setToolTipText(SearchDuplicatesPlugin_mergeButton()); // NOI18N
                    mergeButton.setDefaultCapable(true);
                    mergeButton.setEnabled(true);
                    
                    Action doClean = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (cleanButton.isEnabled()) {
                                cleanButtonActionPerformed(e);
                            }
                        }
                    };
                    cleanButton.setAction(doClean);
                    cleanButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/gedcom/searchduplicates/clean.png")));
                    cleanButton.setToolTipText(SearchDuplicatesPlugin_cleanButton()); // NOI18N
                    cleanButton.setDefaultCapable(true);
                    cleanButton.setEnabled(true);
                    
                    closeButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/gedcom/searchduplicates/close.png")));
                    closeButton.setToolTipText(SearchDuplicatesPlugin_closeButton()); // NOI18N
                    closeButton.setDefaultCapable(true);
                    closeButton.setEnabled(true);
                    
                    // There are duplicates let's display them
                    if (matchesLinkedList.size() > 0) {
                        checkDuplicatePanelDescriptor = new DialogDescriptor(
                                entityViewPanel,
                                "",
                                false,
                                new Object[]{firstButton, previousButton, swapButton, nextButton, lastButton, mergeButton, cleanButton, closeButton},
                                mergeButton,
                                DialogDescriptor.DEFAULT_ALIGN,
                                null,
                                null);

                        checkDuplicatePanelDescriptor.setClosingOptions(new Object[]{closeButton});

                        this.linkedListIndex = 0;
                        if (linkedListIndex < matchesLinkedList.size() - 1) {
                            nextButton.setEnabled(true);
                            lastButton.setEnabled(true);
                        }
                        entityViewPanel.setEntities(matchesLinkedList.get(linkedListIndex));
                        setTitle();

                        // Display Dialog
                        Dialog dialog = DialogDisplayer.getDefault().createDialog(checkDuplicatePanelDescriptor);
                        entityViewPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), "doFirst");
                        entityViewPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), "doPrevious");
                        entityViewPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK), "doSwap");
                        entityViewPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK), "doNext");
                        entityViewPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), "doLast");
                        entityViewPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "doMerge");
                        entityViewPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK), "doClean");
                        entityViewPanel.getActionMap().put("doFirst", doFirst);
                        entityViewPanel.getActionMap().put("doPrevious", doPrevious);
                        entityViewPanel.getActionMap().put("doSwap", doSwap);
                        entityViewPanel.getActionMap().put("doNext", doNext);
                        entityViewPanel.getActionMap().put("doLast", doLast);
                        entityViewPanel.getActionMap().put("doMerge", doMerge);
                        entityViewPanel.getActionMap().put("doClean", doClean);
                        dialog.setModal(true);
                        dialog.setVisible(true);
                        dialog.toFront();
                    } else {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SearchDuplicatesPlugin.class, "CheckDuplicates.noDuplicates"), NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                }

                private void firstButtonActionPerformed(java.awt.event.ActionEvent evt) {
                    linkedListIndex = 0;

                    mergeButton.setEnabled(!matchesLinkedList.get(linkedListIndex).isMerged());
                    entityViewPanel.setEntities(matchesLinkedList.get(linkedListIndex));
                    setTitle();
                    if (linkedListIndex <= 0) {
                        firstButton.setEnabled(false);
                        previousButton.setEnabled(false);
                    }
                    if (linkedListIndex < matchesLinkedList.size() - 1) {
                        nextButton.setEnabled(true);
                        lastButton.setEnabled(true);
                    }
                }

                private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {
                    linkedListIndex -= 1;

                    mergeButton.setEnabled(!matchesLinkedList.get(linkedListIndex).isMerged());
                    entityViewPanel.setEntities(matchesLinkedList.get(linkedListIndex));
                    setTitle();
                    if (linkedListIndex <= 0) {
                        firstButton.setEnabled(false);
                        previousButton.setEnabled(false);
                    }
                    if (linkedListIndex < matchesLinkedList.size() - 1) {
                        nextButton.setEnabled(true);
                        lastButton.setEnabled(true);
                    }
                }

                private void swapButtonActionPerformed(java.awt.event.ActionEvent evt) {
                    PotentialMatch<? extends Entity> e = matchesLinkedList.get(linkedListIndex);
                    e.swap();
                    entityViewPanel.setEntities(e);
                }

                private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {
                    linkedListIndex += 1;

                    mergeButton.setEnabled(!matchesLinkedList.get(linkedListIndex).isMerged());
                    entityViewPanel.setEntities(matchesLinkedList.get(linkedListIndex));
                    setTitle();

                    if (linkedListIndex >= matchesLinkedList.size() - 1) {
                        nextButton.setEnabled(false);
                        lastButton.setEnabled(false);
                    }
                    if (linkedListIndex > 0) {
                        firstButton.setEnabled(true);
                        previousButton.setEnabled(true);
                    }
                }

                private void lastButtonActionPerformed(java.awt.event.ActionEvent evt) {
                    linkedListIndex = matchesLinkedList.size() - 1;

                    mergeButton.setEnabled(!matchesLinkedList.get(linkedListIndex).isMerged());
                    entityViewPanel.setEntities(matchesLinkedList.get(linkedListIndex));
                    setTitle();

                    if (linkedListIndex >= matchesLinkedList.size() - 1) {
                        nextButton.setEnabled(false);
                        lastButton.setEnabled(false);
                    }
                    if (linkedListIndex > 0) {
                        firstButton.setEnabled(true);
                        previousButton.setEnabled(true);
                    }
                }

                private void mergeButtonActionPerformed(java.awt.event.ActionEvent evt) {
                    boolean merge = false;
                    if (entityViewPanel.getSelectedProperties().isEmpty() == true) {
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(SearchDuplicatesPlugin.class, "SearchDuplicatesPlugin.noSelectedProperties"), NotifyDescriptor.OK_CANCEL_OPTION);
                        DialogDisplayer.getDefault().notify(nd);
                        if (nd.getValue() == NotifyDescriptor.OK_OPTION) {
                            merge = true;
                        }
                    } else {
                        merge = true;
                    }
                    
                    if (merge == true) {
                        final Entity left = matchesLinkedList.get(linkedListIndex).getLeft();
                        final Entity right = matchesLinkedList.get(linkedListIndex).getRight();
                        final List<Property> selectedProperties = entityViewPanel.getSelectedProperties();
                        try {
                            gedcom.doUnitOfWork(new UnitOfWork() {
                                @Override
                                public void perform(Gedcom gedcom) throws GedcomException {
                                    GedcomUtilities.MergeEntities(gedcom, left, right, selectedProperties);
                                }
                            });
                        } catch (GedcomException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        matchesLinkedList.get(linkedListIndex).setMerged(true);
                        mergeButton.setEnabled(!matchesLinkedList.get(linkedListIndex).isMerged());
                        entityViewPanel.setEntities(matchesLinkedList.get(linkedListIndex));
                        setTitle();
                    }

                }
                
                private void cleanButtonActionPerformed(java.awt.event.ActionEvent evt) {
                    Entity right = matchesLinkedList.get(linkedListIndex).getRight();
                    linkedListIndex = cleanList(matchesLinkedList, right, linkedListIndex);
                    matchesLinkedList.remove(linkedListIndex);

                    // display next
                    if (matchesLinkedList.size() > 0) {

                        if (linkedListIndex >= matchesLinkedList.size() - 1) {
                            nextButton.setEnabled(false);
                            lastButton.setEnabled(false);
                            linkedListIndex = matchesLinkedList.size() - 1;
                        }

                        if (linkedListIndex <= 0) {
                            firstButton.setEnabled(false);
                            previousButton.setEnabled(false);
                            linkedListIndex = 0;
                        }

                        if (linkedListIndex > 0) {
                            firstButton.setEnabled(true);
                            previousButton.setEnabled(true);
                        }

                        if (linkedListIndex < matchesLinkedList.size() - 1) {
                            nextButton.setEnabled(true);
                            lastButton.setEnabled(true);
                        }

                        mergeButton.setEnabled(!matchesLinkedList.get(linkedListIndex).isMerged());
                        entityViewPanel.setEntities(matchesLinkedList.get(linkedListIndex));
                        setTitle();
                    } else {
                        closeButton.doClick();
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SearchDuplicatesPlugin.class, "CheckDuplicates.mergeCompleted"), NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                }

                
                
                // end runnable

                // Set title
                //SearchDuplicatesPlugin.title.part1=Probability : {0}%
                //SearchDuplicatesPlugin.title.part2=Duplicate {0} of {1}
                private void setTitle() {
                    String part1 = "";
                    if (matchesLinkedList.get(linkedListIndex).isMerged()) {
                        part1 = NbBundle.getMessage(getClass(), "SearchDuplicatesPlugin.title.part1b");
                    } else {
                        part1 = NbBundle.getMessage(getClass(), "SearchDuplicatesPlugin.title.part1a", matchesLinkedList.get(linkedListIndex).getCertainty());
                    }
                    String part2 = NbBundle.getMessage(getClass(), "SearchDuplicatesPlugin.title.part2", (linkedListIndex + 1), matchesLinkedList.size());
                    checkDuplicatePanelDescriptor.setTitle(part1 + " - " + part2);
                }
                
            });
        } catch (InterruptedException ex) {
            log.log(Level.FINE, "the task was CANCELLED");
        }
    }
    
    // Remove merged entities from remaining matches
    private int cleanList(LinkedList<PotentialMatch<? extends Entity>> matchesLinkedList, Entity rightEntity, int linkedListIndex) {
        PotentialMatch currentMatch = matchesLinkedList.get(linkedListIndex);
        LinkedList<PotentialMatch<? extends Entity>> matchToRemove = new LinkedList<PotentialMatch<? extends Entity>>();
        for (PotentialMatch match : matchesLinkedList) {
            if (!match.equals(currentMatch) && (match.getLeft().equals(rightEntity) || match.getRight().equals(rightEntity))) {
                matchToRemove.add(match);
            }
        }
        matchesLinkedList.removeAll(matchToRemove);
        int index = 0;
        for (PotentialMatch match : matchesLinkedList) {
            if (match.equals(currentMatch)) {
                return index;
            }
            index++;
        }
        return 0;
    }
    
}
