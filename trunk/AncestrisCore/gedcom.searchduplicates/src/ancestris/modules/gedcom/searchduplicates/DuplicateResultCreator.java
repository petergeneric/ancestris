/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.searchduplicates;

import ancestris.util.swing.MergeEntityPanel;
import ancestris.modules.gedcom.matchers.PotentialMatch;
import ancestris.util.GedcomUtilities;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.UnitOfWork;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Create MergeEntityPanel.
 *
 * @author Zurga
 */

public class DuplicateResultCreator implements Runnable {

    private final MergeEntityPanel entityViewPanel ;
    private final Gedcom gedcom;
    private final List<PotentialMatch<? extends Entity>> matchesLinkedList;
    private DialogDescriptor checkDuplicatePanelDescriptor;
    private int linkedListIndex = -1;
    private final JButton firstButton = new JButton();
    private final JButton previousButton = new JButton();
    private final JButton swapButton = new JButton();
    private final JButton nextButton = new JButton();
    private final JButton lastButton = new JButton();
    private final JButton mergeButton = new JButton();
    private final JButton cleanButton = new JButton();
    private final JButton closeButton = new JButton();
    
    public DuplicateResultCreator(Gedcom myGedcom, List<PotentialMatch<? extends Entity>> myMatches ) {
        gedcom = myGedcom;
        entityViewPanel= new MergeEntityPanel(myGedcom);
        matchesLinkedList = myMatches;
        
    }

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
        firstButton.setToolTipText(NbBundle.getMessage(DuplicateResultCreator.class, "SearchDuplicatesPlugin.firstButton")); 
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
        previousButton.setToolTipText(NbBundle.getMessage(DuplicateResultCreator.class, "SearchDuplicatesPlugin.previousButton"));
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
        swapButton.setToolTipText(NbBundle.getMessage(DuplicateResultCreator.class, "SearchDuplicatesPlugin.swapButton"));
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
        nextButton.setToolTipText(NbBundle.getMessage(DuplicateResultCreator.class, "SearchDuplicatesPlugin.nextButton"));
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
        lastButton.setToolTipText(NbBundle.getMessage(DuplicateResultCreator.class, "SearchDuplicatesPlugin.lastButton")); 
        lastButton.setEnabled(false);
        lastButton.putClientProperty("defaultButton", Boolean.FALSE);

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
        mergeButton.setToolTipText(NbBundle.getMessage(DuplicateResultCreator.class, "SearchDuplicatesPlugin.mergeButton"));
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
        cleanButton.setToolTipText(NbBundle.getMessage(DuplicateResultCreator.class, "SearchDuplicatesPlugin.cleanButton"));
        cleanButton.setDefaultCapable(true);
        cleanButton.setEnabled(true);

        closeButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/gedcom/searchduplicates/close.png")));
        closeButton.setToolTipText(NbBundle.getMessage(DuplicateResultCreator.class, "SearchDuplicatesPlugin.closeButton"));
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
            setEntities(matchesLinkedList.get(linkedListIndex));
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

    private void setEntities(PotentialMatch<? extends Entity> match) {
        entityViewPanel.setEntities(match.getLeft(), match.getRight(), match.isMerged());
    }
    
    private void firstButtonActionPerformed(java.awt.event.ActionEvent evt) {
        linkedListIndex = 0;

        mergeButton.setEnabled(!matchesLinkedList.get(linkedListIndex).isMerged());
        setEntities(matchesLinkedList.get(linkedListIndex));
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
        setEntities(matchesLinkedList.get(linkedListIndex));
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
        setEntities(e);
    }

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {
        linkedListIndex += 1;

        mergeButton.setEnabled(!matchesLinkedList.get(linkedListIndex).isMerged());
        setEntities(matchesLinkedList.get(linkedListIndex));
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
        setEntities(matchesLinkedList.get(linkedListIndex));
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
                        GedcomUtilities.MergeEntities(left, right, selectedProperties);
                    }
                });
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }

            matchesLinkedList.get(linkedListIndex).setMerged(true);
            mergeButton.setEnabled(!matchesLinkedList.get(linkedListIndex).isMerged());
            setEntities(matchesLinkedList.get(linkedListIndex));
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
            setEntities(matchesLinkedList.get(linkedListIndex));
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
    
    // Remove merged entities from remaining matches
    private int cleanList(List<PotentialMatch<? extends Entity>> matchesLinkedList, Entity rightEntity, int linkedListIndex) {
        PotentialMatch currentMatch = matchesLinkedList.get(linkedListIndex);
        LinkedList<PotentialMatch<? extends Entity>> matchToRemove = new LinkedList<>();
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
