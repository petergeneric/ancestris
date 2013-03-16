package ancestris.modules.gedcom.searchduplicates;

import ancestris.core.pluginservice.AncestrisPlugin;
import static ancestris.modules.gedcom.searchduplicates.Bundle.*;
import ancestris.modules.gedcom.utilities.matchers.*;
import genj.gedcom.*;
import java.awt.Dialog;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
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
@NbBundle.Messages({"SearchDuplicatesPlugin.duplicateIndexLabel.text=Duplicate {0} of {1} estimate matching {2}",
    "SearchDuplicatesPlugin.nextButton=Next",
    "SearchDuplicatesPlugin.previousButton=Previous",
    "SearchDuplicatesPlugin.mergeButton=Merge",
    "SearchDuplicatesPlugin.noSelectedProperties=You havent selected any properties to merge\nMerge anyway ?"})
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
        }
    };
    private final List<String> entities2Ckeck;
    Map<String, ? extends MatcherOptions> selectedOptions;

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
        final LinkedList<PotentialMatch<? extends Entity>> matchesLinkedList = new LinkedList<PotentialMatch<? extends Entity>>();
        final HashMap<String, Integer> duplicatesHashMap = new HashMap<String, Integer>();
        if (gedcom == null) {
            return;
        }
        try {
            for (String tag : entities2Ckeck) {
                List<? extends Entity> entities = new ArrayList(gedcom.getEntities(tag));

                log.log(Level.INFO, "Checking: {0}", tag);
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
                }
                List<PotentialMatch<? extends Entity>> potentialMatches = (entitiesMatchers.get(tag)).getPotentialMatches(entities);
                Collections.sort(potentialMatches, new Comparator<PotentialMatch>() {
                    @Override
                    public int compare(PotentialMatch e1, PotentialMatch e2) {
                        return e2.getCertainty() - e1.getCertainty();
                    }
                });
                matchesLinkedList.addAll(potentialMatches);
                duplicatesHashMap.put(tag, potentialMatches.size());
            }

            SwingUtilities.invokeLater(new Runnable() {
                SearchDuplicatesResultPanel entityViewPanel = new SearchDuplicatesResultPanel();
                DialogDescriptor checkDuplicatePanelDescriptor;
                int linkedListIndex = -1;
                final JButton nextButton = new JButton();
                final JButton previousButton = new JButton();
                final JButton mergeButton = new JButton();

                @Override
                public void run() {
                    previousButton.setText(SearchDuplicatesPlugin_previousButton()); // NOI18N
                    previousButton.setEnabled(false);
                    previousButton.setDefaultCapable(true);
                    previousButton.putClientProperty("defaultButton", Boolean.FALSE); //NOI18N
                    previousButton.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            previousButtonActionPerformed(evt);
                        }
                    });
                    nextButton.setDefaultCapable(true);
                    nextButton.setText(SearchDuplicatesPlugin_nextButton()); // NOI18N
                    nextButton.setEnabled(false);
                    nextButton.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            nextButtonActionPerformed(evt);
                        }
                    });
                    mergeButton.setDefaultCapable(true);
                    mergeButton.setText(SearchDuplicatesPlugin_mergeButton()); // NOI18N
                    mergeButton.setEnabled(true);
                    mergeButton.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            mergeButtonActionPerformed(evt);
                        }
                    });
                    // There is duplicates let displaying them
                    if (matchesLinkedList.size() > 0) {
                        checkDuplicatePanelDescriptor = new DialogDescriptor(
                                entityViewPanel,
                                NbBundle.getMessage(SearchDuplicatesPlugin.class, "CheckDuplicatePanelDescriptor.title"),
                                false,
                                new Object[]{previousButton, mergeButton, nextButton, DialogDescriptor.CLOSED_OPTION},
                                DialogDescriptor.CLOSED_OPTION,
                                DialogDescriptor.DEFAULT_ALIGN,
                                null,
                                null);

                        if (matchesLinkedList.size() == 1) {
                            checkDuplicatePanelDescriptor.setClosingOptions(new Object[]{DialogDescriptor.CLOSED_OPTION, mergeButton});
                        } else {
                            checkDuplicatePanelDescriptor.setClosingOptions(new Object[]{DialogDescriptor.CLOSED_OPTION});
                        }

                        this.linkedListIndex = 0;
                        if (linkedListIndex < matchesLinkedList.size() - 1) {
                            nextButton.setEnabled(true);
                        }
                        entityViewPanel.setEntities(matchesLinkedList.get(linkedListIndex));
                        SearchDuplicatesPlugin_duplicateIndexLabel_text((linkedListIndex + 1), matchesLinkedList.size(), matchesLinkedList.get(linkedListIndex).getCertainty());
                        checkDuplicatePanelDescriptor.setTitle(NbBundle.getMessage(SearchDuplicatesPlugin.class, "CheckDuplicatePanelDescriptor.title") + " " + SearchDuplicatesPlugin_duplicateIndexLabel_text((linkedListIndex + 1), matchesLinkedList.size(), matchesLinkedList.get(linkedListIndex).getCertainty()));

                        // display Dialog
                        Dialog dialog = DialogDisplayer.getDefault().createDialog(checkDuplicatePanelDescriptor);
                        dialog.setVisible(true);
                        dialog.toFront();
                    } else {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SearchDuplicatesPlugin.class, "CheckDuplicates.noDuplicates"), NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                }

                private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {
                    linkedListIndex -= 1;

                    entityViewPanel.setEntities(matchesLinkedList.get(linkedListIndex));
                    checkDuplicatePanelDescriptor.setTitle(NbBundle.getMessage(SearchDuplicatesPlugin.class, "CheckDuplicatePanelDescriptor.title") + " " + SearchDuplicatesPlugin_duplicateIndexLabel_text((linkedListIndex + 1), matchesLinkedList.size(), matchesLinkedList.get(linkedListIndex).getCertainty()));
                    if (linkedListIndex <= 0) {
                        previousButton.setEnabled(false);
                    }
                    if (linkedListIndex < matchesLinkedList.size() - 1) {
                        nextButton.setEnabled(true);
                    }
                }

                private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {
                    linkedListIndex += 1;

                    entityViewPanel.setEntities(matchesLinkedList.get(linkedListIndex));
                    checkDuplicatePanelDescriptor.setTitle(NbBundle.getMessage(SearchDuplicatesPlugin.class, "CheckDuplicatePanelDescriptor.title") + " " + SearchDuplicatesPlugin_duplicateIndexLabel_text((linkedListIndex + 1), matchesLinkedList.size(), matchesLinkedList.get(linkedListIndex).getCertainty()));

                    if (linkedListIndex >= matchesLinkedList.size() - 1) {
                        nextButton.setEnabled(false);
                    }
                    if (linkedListIndex > 0) {
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
                        try {
                            gedcom.doUnitOfWork(new UnitOfWork() {
                                @Override
                                public void perform(Gedcom gedcom) throws GedcomException {
                                    Entity left = matchesLinkedList.get(linkedListIndex).getLeft();
                                    Entity right = matchesLinkedList.get(linkedListIndex).getRight();

                                    for (Property rightProperty : entityViewPanel.getSelectedProperties()) {
                                        Property leftProperty;
                                        System.out.println(rightProperty.getTag());
                                        try {
                                            if (rightProperty.getMetaProperty().isSingleton()) {
                                                leftProperty = left.getProperty(rightProperty.getTag());
                                                if (leftProperty != null) {
                                                    left.delProperty(leftProperty);
                                                }
                                                leftProperty = left.addProperty(rightProperty.getTag(), rightProperty.getValue());
                                            } else {
                                                leftProperty = left.addProperty(rightProperty.getTag(), rightProperty.getValue());
                                            }

                                            moveProperties(rightProperty, leftProperty);
                                            right.delProperty(rightProperty);
                                            if (leftProperty instanceof PropertyXRef) {
                                                ((PropertyXRef) leftProperty).link();
                                            }
                                        } catch (GedcomException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }

                                    // Update linked entities
                                    for (Entity reference : PropertyXRef.getReferences(right)) {
                                        System.out.println("targetEntity:" + reference.getId());
                                        for (Iterator<PropertyXRef> it = reference.getProperties(PropertyXRef.class).iterator(); it.hasNext();) {
                                            PropertyXRef propertyXRef = it.next();
                                            if (propertyXRef.getTargetEntity().equals(right)) {
                                                propertyXRef.unlink();
                                                propertyXRef.setValue(left.getId());
                                                try {
                                                    propertyXRef.link();
                                                } catch (GedcomException e) {
                                                    log.log(Level.SEVERE, "unexpected", e);
                                                }
                                            }
                                        }
                                    }

                                    // delete merged entity
                                    gedcom.deleteEntity(right);
                                }
                            });
                        } catch (GedcomException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        matchesLinkedList.remove(linkedListIndex);

                        // display next
                        if (matchesLinkedList.size() > 0) {
                            if (matchesLinkedList.size() == 1) {
                                checkDuplicatePanelDescriptor.setClosingOptions(new Object[]{DialogDescriptor.CLOSED_OPTION, mergeButton});
                            }

                            if (linkedListIndex >= matchesLinkedList.size() - 1) {
                                nextButton.setEnabled(false);
                                linkedListIndex = matchesLinkedList.size() - 1;
                            }

                            if (linkedListIndex > 0) {
                                previousButton.setEnabled(true);
                            }

                            entityViewPanel.setEntities(matchesLinkedList.get(linkedListIndex));
                            checkDuplicatePanelDescriptor.setTitle(NbBundle.getMessage(SearchDuplicatesPlugin.class, "CheckDuplicatePanelDescriptor.title") + " " + SearchDuplicatesPlugin_duplicateIndexLabel_text((linkedListIndex + 1), matchesLinkedList.size(), matchesLinkedList.get(linkedListIndex).getCertainty()));
                        }
                    }
                }

                public void moveProperties(Property src, Property dest) throws GedcomException {
                    // loop over children of prop
                    for (Property child : src.getProperties()) {
                        // create copy for prop?
                        Property copy = dest.getProperty(child.getTag(), false);
                        if (copy != null) {
                            dest.delProperty(copy);
                        }
                        copy = dest.addProperty(child.getTag(), child.getValue());
                        moveProperties(child, copy);
                        src.delProperty(child);
                        if (copy instanceof PropertyXRef) {
                            ((PropertyXRef) copy).link();
                        }
                    }
                }
            });
        } catch (InterruptedException ex) {
            log.log(Level.INFO, "the task was CANCELLED");
        }
    }
}
