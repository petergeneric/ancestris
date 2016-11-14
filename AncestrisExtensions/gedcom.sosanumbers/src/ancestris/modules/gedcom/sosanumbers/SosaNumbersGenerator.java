/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright Ancestris
 *
 * Author: Frederic Lapeyre (frederic-at-ancestris-dot-org). 2006-2016
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org). 2012
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.sosanumbers;

import ancestris.modules.gedcom.utilities.GedcomUtilities;
import genj.gedcom.*;
import genj.util.Registry;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic, dominique
 */
public class SosaNumbersGenerator implements Constants {

    private final static Logger LOG = Logger.getLogger(SosaNumbersGenerator.class.getName(), null);
    private Gedcom gedcom = null;
    private Registry registry = null;
    private int mode = MODE_GENERATE;
    private int numbering = NUMBERING_SOSADABOVILLE;
    private Indi indiDeCujus = null;
    private boolean save = true;
    private Set<Indi> changedIndis = null; // no duplicates
    
    private boolean runBlank = false;
    private int maxCounter = 0;
    private int stoppedCounter = 0;
    
    private ProgressMonitor progressMonitor = null;
    private Task fullTask = null;
    private Runnable task = null;
    

    public void run(final Gedcom gedcom, final Indi indiDeCujus, final String message) {
        
        // Get parameters:
        // ---------------
        this.gedcom = gedcom;
        registry = gedcom.getRegistry();

        // Action : generate or delete
        mode = registry.get(ACTION, MODE_GENERATE);

        // Number : sosa, dabo, sosa_dabo, all
        numbering = registry.get(NUMBERING, NUMBERING_SOSADABOVILLE);

        // Individual : decujus or all
        this.indiDeCujus = indiDeCujus;
        changedIndis = new HashSet<Indi>();

        // Save in gedcom : true of false
        save = registry.get(SAVE, true);
        

        // Prepare the the processing:
        // ---------------------------
        
        // Clean existing numbering if mode is erase and no decujus 
        if (mode == MODE_ERASE && indiDeCujus == null) {
            task = new Runnable() {
                @Override
                public void run() {
                    eraseAll();
                }
            };
        } else if (indiDeCujus == null) {
        // Otherwise, if sosadabo or sosa, go "up" the tree by first erasing and then, if mode is generate, generating numbering. Flag set to guessed along the way if save is false
        } else if (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSADABOVILLE || numbering == NUMBERING_SOSA) {
            task = new Runnable() {
                @Override
                public void run() {
                    numberUp();
                }
            };
        // Otherwise if dabo, go "down" the tree by by first erasing and then, if mode is generate, generating numbering. Flag set to guessed along the way if save is false
        } else if (numbering == NUMBERING_DABOVILLE) {
            task = new Runnable() {
                @Override
                public void run() {
                    numberDown(indiDeCujus, BigInteger.ZERO);
                }
            };
        }
        
        // Calculates number of expected changes 
        // -------------------------------------
        runBlank = true;
        changedIndis.clear();
        maxCounter = 0;
        task.run();
        runBlank = false;
        if (maxCounter == 0) {
            maxCounter = changedIndis.size();
        }
        changedIndis.clear();
        stoppedCounter = 0;
        
        // Run main task while displaying progress bar
        // -------------------------------------------
        progressMonitor = new ProgressMonitor(null, NbBundle.getMessage(getClass(), "SosaNumbersGenerator.task"), "", 0, maxCounter);
        progressMonitor.setProgress(0);
        fullTask = new Task(progressMonitor, maxCounter) {
            @Override
            public Void doInBackground() {
                commit(task);
                String msg = "<html>";
                if (stoppedCounter == 0) {
                    msg +=  message + "<br>" + NbBundle.getMessage(getClass(), "SosaNumbersGenerator.changes", maxCounter) + "</html>";
                } else {
                    msg +=  NbBundle.getMessage(getClass(), "SosaNumbersGenerator.stopped", stoppedCounter) + "</html>"; 
                }
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE));
                System.gc();
            return null;
            }
        };
        fullTask.execute();
        
        // done
    }


    public boolean setProgress(int progress) {
        if (progress % 10 != 0 && progress != maxCounter) {
            return true;
        }
        String message = String.format(NbBundle.getMessage(getClass(), "SosaNumbersGenerator.progress", "%d"), progress);
        progressMonitor.setNote(message);
        progressMonitor.setProgress(progress);
        if (progressMonitor.isCanceled() || fullTask.isDone()) {
            if (progressMonitor.isCanceled()) {
                //fullTask.cancel(true);
                stoppedCounter = progress;
                return false;
            }
        }
        return true;
    }
    
    private void eraseAll() {
        int counter = 0;
        if (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSADABOVILLE) {
            counter += gedcom.getPropertyCount(SOSADABOVILLE_TAG);
            if (!runBlank) {
                GedcomUtilities.deleteTags(gedcom, SOSADABOVILLE_TAG, GedcomUtilities.ENT_INDI);
                if (!setProgress(counter)) return;
            }
        }
        if (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSA) {
            counter += gedcom.getPropertyCount(SOSA_TAG);
            if (!runBlank) {
                GedcomUtilities.deleteTags(gedcom, SOSA_TAG, GedcomUtilities.ENT_INDI);
                if (!setProgress(counter)) return;
            }
        }
        if (numbering == NUMBERING_ALL || numbering == NUMBERING_DABOVILLE) {
            counter += gedcom.getPropertyCount(DABOVILLE_TAG);
            if (!runBlank) {
                GedcomUtilities.deleteTags(gedcom, DABOVILLE_TAG, GedcomUtilities.ENT_INDI);
                if (!setProgress(counter)) return;
            }
        }
        maxCounter = counter;
    }
    
    /**
     * @param indiDeCujus
     * @param sosaValue
     */
    private boolean numberUp() {

        final List<Pair> sosaList = new ArrayList<Pair>();   // list used to store and iterate up the tree
        Pair pair;
        Indi wife, husband;
        BigInteger sosaCounter = BigInteger.ONE;
        Fam famc;
        ListIterator<Pair> listIter = sosaList.listIterator();


        // Iterate on the list to go up the tree.
        updateIndi(indiDeCujus, sosaCounter, listIter, sosaList, null);
        // Sosa d'Aboville generation from this sosa
        if (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSADABOVILLE) {
            numberDown(indiDeCujus, sosaCounter);
        }
        while (listIter.hasNext()) {
            pair = listIter.next();
            sosaCounter = new BigInteger(pair.value);
            // Get father and mother
            famc = pair.indi.getFamilyWhereBiologicalChild();
            if (famc != null) {
                husband = famc.getHusband();
                BigInteger sosa = sosaCounter.shiftLeft(1);
                if (husband != null) {
                    if (!updateIndi(husband, sosa, listIter, sosaList, null)) {
                        return false;
                    }
                    // Sosa d'Aboville generation from this sosa
                    if (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSADABOVILLE) {
                        if (!numberDown(husband, sosa)) {
                           return false; 
                        }
                    }

                }
                wife = famc.getWife();
                sosa = sosaCounter.shiftLeft(1).add(BigInteger.ONE);
                if (wife != null) {
                    if (!updateIndi(wife, sosa, listIter, sosaList, null)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * @param indiDeCujus
     * @param sosaValue
     */
    private boolean numberDown(Indi indiFrom, BigInteger sosaValue) {
        final List<Pair> dabovilleList = new ArrayList<Pair>();
        Pair pair;
        String daboCounter;

        // Iterate on the list to go down the tree.
        ListIterator<Pair> listIter = null;
        if (numbering == NUMBERING_ALL || numbering == NUMBERING_DABOVILLE) {
            listIter = dabovilleList.listIterator();
            updateIndi(indiFrom, sosaValue, listIter, dabovilleList, "1");
        } else {
            dabovilleList.add(new Pair(indiFrom, sosaValue == BigInteger.ZERO ? "1" : ""));
            listIter = dabovilleList.listIterator();
        }
        while (listIter.hasNext()) {
            pair = listIter.next();
            Character suffix = 'a';
            daboCounter = pair.value;

            Fam[] families = pair.indi.getFamiliesWhereSpouse();
            for (Fam family : families) {
                int childOrder = 0;
                for (Indi child : family.getChildren(true)) {
                    childOrder++;
                    // Do not duplicate daboville numbers
                    if (numbering == NUMBERING_DABOVILLE && child.getProperty(DABOVILLE_TAG) != null && contains(dabovilleList, child)) {
                        continue;
                    }
                    // Do not duplicate sosadaboville, but overwrite if sosa lowest than current one
                    if (numbering == NUMBERING_SOSADABOVILLE) {
                        if (contains(dabovilleList, child)) {
                            continue;
                        }
                        if (changedIndis.contains(child)) {
                            // We have already already sosadaboville-tagged this person. Skip if sosa greater than already tagged value
                            String sosa = child.getPropertyDisplayValue(SOSADABOVILLE_TAG);
                            if (!sosa.isEmpty()) {
                                int k = sosa.indexOf("-");
                                if (k == -1) { // sosa child
                                    continue;
                                } else { //daboville child
                                    sosa = sosa.substring(0, k);
                                    BigInteger childSosaValue = new BigInteger(sosa);
                                    if (sosaValue.compareTo(childSosaValue) > 0) {
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                    String counter = daboCounter + (families.length > 1 ? suffix.toString() : "");
                    counter += counter.length() > 0 ? ".":"";
                    counter += childOrder;
                    if (!updateIndi(child, sosaValue, listIter, dabovilleList, counter)) {
                        return false;
                    }
                }
                suffix++;
            }
        }
        return true;
    }

    
    private boolean updateIndi(Indi indi, BigInteger sosaNumber, ListIterator<Pair> listIter, List<Pair> list, String daboValue) {
            
        // Check if indi is new in the list
        boolean isNew = !contains(list, indi);
            
        if (!runBlank) {
            // Clean numbering all or one numbering if individual not in list yet
            if (isNew && (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSADABOVILLE)) {
                indi.delProperties(SOSADABOVILLE_TAG);
            }
            if (isNew && (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSA)) {
                indi.delProperties(SOSA_TAG);
            }
            if (isNew && (numbering == NUMBERING_ALL || numbering == NUMBERING_DABOVILLE)) {
                indi.delProperties(DABOVILLE_TAG);
            }
        }
        
        // Update list to keep going up the tree
        listIter.add(new Pair(indi, daboValue == null ? nbToString(sosaNumber) : daboValue));
        listIter.previous();

        // Erase/Generate numbering for siblings in case of SOSA numbering
        if (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSA) {
            flagSibling(indi.getOlderSiblings(), nbToString(sosaNumber, "+", true), isNew);
            flagSibling(indi.getYoungerSiblings(), nbToString(sosaNumber, "-", true), isNew);
        }
        

        // Quit if just erasing
        if (mode == MODE_ERASE) {
            return true;
        }
        
        // 3. Generate one numbering
        changedIndis.add(indi);
        if (runBlank) {
            return true;
        }
        Property prop = null;
        try {
            String value = nbToString(sosaNumber, "", true, daboValue);
            if (numbering == NUMBERING_SOSADABOVILLE) {
                prop = indi.addProperty(SOSADABOVILLE_TAG, value, setPropertyPosition(indi, SOSADABOVILLE_TAG));
            } else if (numbering == NUMBERING_SOSA) {
                prop = indi.addProperty(SOSA_TAG, value, setPropertyPosition(indi, SOSA_TAG));
            } else if (numbering == NUMBERING_DABOVILLE) {
                prop = indi.addProperty(DABOVILLE_TAG, value, setPropertyPosition(indi, DABOVILLE_TAG));
            }
            LOG.log(Level.FINE, "{0} -> {1}", new Object[]{indi.toString(true), value});
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        if (prop != null) {
            prop.setGuessed(!save);
        }
        if (!setProgress(changedIndis.size())) {
            return false;
        }
        
        return true;
        // done
    }

    private boolean flagSibling(Indi[] siblings, String value, boolean isNew) {
        
        // Flag siblings
        Iterator<Indi> listSibling = Arrays.asList(siblings).iterator();
        while (listSibling.hasNext()) {
            Indi indi = (Indi) listSibling.next();
            
            if (!runBlank) {
                // Clean if new
                if (isNew && (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSA)) {
                    indi.delProperties(SOSA_TAG);
                }
            }
            
            // Continue if just erasing
            if (mode == MODE_ERASE) {
                continue;
            }
            
            changedIndis.add(indi);
            if (runBlank) {
                return true;
            }
            
            try {
                indi.addProperty(SOSA_TAG, value, setPropertyPosition(indi, SOSA_TAG));
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            LOG.log(Level.FINE, "{0} -> {1}", new Object[]{indi.toString(true), value});
            if (!setProgress(changedIndis.size())) return false;
        }
        return true;
    }

    
    private boolean contains(List<Pair> list, Indi indi) {
        for (Pair pair : list) {
            if (pair.indi == indi) {
                return true;
            }
        }
        return false;
    }

    
    
    /**
     * Set Property position
     *
     * @param prop
     *
     * @return
     */
    private int setPropertyPosition(Property prop, String tag) {
        if (prop == null) {
            return 1;
        }
        // Put after last tag
        int k = 0;
        Property[] props = prop.getProperties(tag);
        if (props != null && props.length != 0) {
            return prop.getPropertyPosition(props[props.length-1]) + 1;
        }
        
        // Else after name
        Property pName = prop.getProperty("NAME");
        if (pName != null) {
            return prop.getPropertyPosition(pName) + 1;
        }
        
        // Else first
        return 1;
    }

    /**
     *
     * @param sosa
     *
     * @return String
     */
    protected String nbToString(BigInteger sosa) {
        return nbToString(sosa, "", false);
    }
    
    protected String nbToString(BigInteger sosa, String suffix, boolean gen) {
        return nbToString(sosa, suffix, gen, "");
    }

    protected String nbToString(BigInteger sosa, String suffix, boolean gen, String daboValue) {
        // Sosa bit
        DecimalFormat formatNbrs = new DecimalFormat("0");
        String sosaStr = (sosa != BigInteger.ZERO) ? formatNbrs.format(sosa) : "";
        
        // Suffix is + or - for sosa numbering of siblings
        
        // Dabo is descendants
        String dabo = daboValue == null || daboValue.isEmpty() ? "" : (sosa != BigInteger.ZERO ? "-" + daboValue : daboValue);
        
        // Generation from the decujus
        int genInt = sosa.bitLength();
        if (!dabo.isEmpty()) { // Reduce generation number by the number of dots in the dabo string
            int i = 1;
            for (int k = 0 ; k < dabo.length() ; k++) {
                if (dabo.charAt(k) == '.') {
                    i++;
                }
            }
            genInt -= i;
        }
        String genStr = gen && sosa != BigInteger.ZERO ? " G" + genInt : "";
        
        // return
        return sosaStr + suffix + dabo + genStr;
    }


    
    
    private void commit(final Runnable change) {
        try {
            if (gedcom.isWriteLocked()) {
                change.run();
            } else {
                gedcom.doUnitOfWork(new UnitOfWork() {
                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        change.run();
                    }
                });
            }

        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error committing sosa generator", t);
        } finally {
        }
    }


    
    
    
    
    private class Pair {

        Indi indi;
        String value;

        public Pair(Indi indi, String value) {
            this.indi = indi;
            this.value = value;
        }
    }

    
    private class Task extends SwingWorker<Void, Void> {
        
        private ProgressMonitor progressMonitor;
        private int maxProgress = 0;
        
        
        public Task(ProgressMonitor progressMonitor, int maxProgress) {
            this.progressMonitor = progressMonitor;
            this.maxProgress = maxProgress;
        }
        
        @Override
        public Void doInBackground() {
            int progress = 0;
            setProgress(0);
            setProgress(progress);
            return null;
        }
 
        @Override
        public void done() {
            progressMonitor.setProgress(maxProgress);
        }
    }
    
    
}
