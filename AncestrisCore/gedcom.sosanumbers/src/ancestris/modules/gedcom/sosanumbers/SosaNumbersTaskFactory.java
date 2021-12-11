/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.sosanumbers;

import static ancestris.modules.gedcom.sosanumbers.Constants.ACTION;
import static ancestris.modules.gedcom.sosanumbers.Constants.ALLSOSA;
import static ancestris.modules.gedcom.sosanumbers.Constants.MODE_ERASE;
import static ancestris.modules.gedcom.sosanumbers.Constants.MODE_GENERATE;
import static ancestris.modules.gedcom.sosanumbers.Constants.NUMBERING;
import static ancestris.modules.gedcom.sosanumbers.Constants.NUMBERING_ALL;
import static ancestris.modules.gedcom.sosanumbers.Constants.NUMBERING_DABOVILLE;
import static ancestris.modules.gedcom.sosanumbers.Constants.NUMBERING_SOSA;
import static ancestris.modules.gedcom.sosanumbers.Constants.NUMBERING_SOSADABOVILLE;
import static ancestris.modules.gedcom.sosanumbers.Constants.NUMBERING_SPOUSE;
import static ancestris.modules.gedcom.sosanumbers.Constants.SAVE;
import ancestris.util.TimingUtility;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.util.Registry;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
public class SosaNumbersTaskFactory {

    public static SosaNumbersTask create(Gedcom gedcom, Indi indiDeCujus, String message) {
        return new Impl(gedcom, indiDeCujus, message);
    }

    private static class Impl implements SosaNumbersTask, Constants {

        private final static Logger log = Logger.getLogger(SosaNumbersTaskFactory.class.getName());
        private Gedcom gedcom = null;
        private Registry registry = null;
        private String message = null;
        private Indi indiDeCujus = null;

        private int counter = 0, maxCounter = 1;
        private boolean cancel = false;
        private String taskName = "";
        private String state = "";

        private final static int STOP_COUNTER = 99000;
        private final static Comparator SOSA_COMPARE = new SosaComparator();
        private int mode = MODE_GENERATE;
        private int numbering = NUMBERING_SOSADABOVILLE;
        private boolean allSosa = false;
        private boolean numberSpouse = false;
        private boolean save = true;
        private Set<Indi> changedIndis = null; // no duplicates, hashed
        private boolean runBlank = false;

        private Impl(Gedcom gedcom, Indi indiDeCujus, String message) {
            this.gedcom = gedcom;
            this.registry = gedcom.getRegistry();
            this.message = message;
            this.indiDeCujus = indiDeCujus;
            this.taskName = NbBundle.getMessage(SosaNumbersTaskFactory.class, "ContextSosaAction");
        }

        @Override
        public void run() {
            if (generateSosaNumbers()) {
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE));
                    }
                });
            }
        }

        @Override
        public void cancelTrackable() {
            cancel = true;
        }

        @Override
        public int getProgress() {
            if (runBlank) {
                return 0;
            }
            int progress = 100 * counter / maxCounter;
            if (progress % 5 == 0) {
                log.log(Level.FINER, "Time=" + TimingUtility.getInstance().getTime() + " - state = " + getState() + " - progress = " + progress);
            }
            return progress;
        }

        @Override
        public String getTaskName() {
            return taskName;
        }

        @Override
        public String getState() {
            return state;
        }

        private boolean generateSosaNumbers() {
            
            TimingUtility.getInstance().reset();
            maxCounter = 1;
            counter = 1;
            state = taskName;

            // Action : generate or delete
            mode = registry.get(ACTION, MODE_GENERATE);

            // Number : sosa, dabo, sosa_dabo, all
            numbering = registry.get(NUMBERING, NUMBERING_SOSADABOVILLE);
            allSosa = registry.get(ALLSOSA, false);
            numberSpouse = registry.get(NUMBERING_SPOUSE, false);

            // Save in gedcom : true of false
            save = registry.get(SAVE, true);

            
            // Prepare the processing:
            // ---------------------------
            Runnable task = null;

            if (mode == MODE_ERASE && indiDeCujus == null) {
                state = NbBundle.getMessage(getClass(), "SosaNumbersGenerator.taskErase");
                task = () -> {
                    eraseAll();
                };
            } else if (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSADABOVILLE || numbering == NUMBERING_SOSA) {
                state = NbBundle.getMessage(getClass(), mode != MODE_ERASE ? "SosaNumbersGenerator.taskNb" : "SosaNumbersGenerator.taskErase");
                task = () -> {
                    numberUp();
                };
                // Otherwise if dabo, go "down" the tree by by first erasing and then, if mode is generate, generating numbering. Flag set to guessed along the way if save is false
            } else if (numbering == NUMBERING_DABOVILLE) {
                state = NbBundle.getMessage(getClass(), mode != MODE_ERASE ? "SosaNumbersGenerator.taskNb" : "SosaNumbersGenerator.taskErase");
                task = () -> {
                    numberDown(indiDeCujus, BigInteger.ZERO);
                };
            } else {
                return false;
            }

            // Calculates number of expected changes (blank run)
            // -------------------------------------------------
            changedIndis = new HashSet<>();
            counter = 1;
            runBlank = true;
            task.run();
            maxCounter = counter;

            if (cancel) {
                return false;
            }
            if (maxCounter >= STOP_COUNTER && DialogManager.YES_OPTION != DialogManager.create(NbBundle.getMessage(getClass(), "SosanNumberGenerator.longOpTitle"), NbBundle.getMessage(getClass(), "SosanNumberGenerator.longOpConfirm", STOP_COUNTER))
                    .setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.YES_NO_OPTION).show()) {
                return false;
            }

            // Run main task (real numbering run)
            // ----------------------------------
            counter = 1;
            runBlank = false;
            changedIndis.clear();
            task.run();

            // Ending message
            String msg = "<html>";
            if (counter == 0 || counter == maxCounter) {
                if (message == null) {
                    msg += NbBundle.getMessage(getClass(), "SosaNumbersGenerator.autogen") + "<br>" + NbBundle.getMessage(getClass(), "SosaNumbersGenerator.changes", maxCounter-1) + "</html>";
                } else {
                    msg += message + "<br>" + NbBundle.getMessage(getClass(), "SosaNumbersGenerator.changes", maxCounter) + "</html>";
                }
            } else {
                msg += NbBundle.getMessage(getClass(), "SosaNumbersGenerator.stopped", counter, maxCounter) + "</html>";
            }
            message = msg;

            return true;
        }

        
        
        
        
        /**
         * Mode = ERASE and no decujus indicated
         *
         */
        private void eraseAll() {
            if (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSADABOVILLE) {
                if (!deleteTags(Indi.TAG_SOSADABOVILLE)) {
                    return;
                }
            }
            if (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSA) {
                if (!deleteTags(Indi.TAG_SOSA)) {
                    return;
                }
            }
            if (numbering == NUMBERING_ALL || numbering == NUMBERING_DABOVILLE) {
                deleteTags(Indi.TAG_DABOVILLE);
            }
        }

        /**
         * Define our own delete method in order to monitor progress and speed up performance. (rather than using GedcomUtilities.deleTags)
         *
         * @param tagToRemove Tag to remove
         */
        public boolean deleteTags(String tagToRemove) {
            if (runBlank) {
                for (Entity entity : gedcom.getIndis()) {
                    Property[] props = entity.getProperties(tagToRemove);
                    counter += props.length;
                }
            } else {
                for (Entity entity : gedcom.getIndis()) {
                    counter += entity.getProperties(tagToRemove).length;
                    entity.delProperties(tagToRemove);
                }
            }
            return true;
        }





        /**
         * Number Sosa from de Cujus. Mode = Number or Erase
         *
         */
        private void numberUp() {
            final Map<String, Pair> sosaPairs = new HashMap<>();   // list used to store and iterate up the tree
            Queue<Pair> listIter = new LinkedList<>();

            // Iterate on the list to go up the tree for the sosa part.
            updateIndi(indiDeCujus, BigInteger.ONE, listIter, sosaPairs, null);
            if (treeup(listIter, sosaPairs)) {
                return;
            }

            // Iterate on ascending sosa for the Sosa d'Aboville part
            if (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSADABOVILLE) {
                TreeSet<Pair> sosaSet = new TreeSet<>(SOSA_COMPARE);
                sosaSet.addAll(sosaPairs.values());
                for (Pair p : sosaSet) {
                    if (!numberDown(p.getIndi(), new BigInteger(p.getValue())) && PropertySex.MALE == p.getIndi().getSex()) {
                        return;
                    }
                }
            }
        }

        private boolean treeup(Queue<Pair> iter, Map<String, Pair> listCurrent) {
            while (!iter.isEmpty()) {
                if (runBlank && iter.size() >= STOP_COUNTER) {
                    return false;
                }
                final Pair pair = iter.poll();
                if (pair != null) {
                    final BigInteger sosa = new BigInteger(pair.getValue());
                    final Fam famc = pair.getIndi().getFamilyWhereBiologicalChild();
                    if (famc != null) {
                        final Indi husband = famc.getHusband();
                        if (husband != null && !updateIndi(husband, sosa.shiftLeft(1), iter, listCurrent, null)) {
                            return true;
                        }
                        final Indi wife = famc.getWife();
                        if (wife != null && !updateIndi(wife, sosa.shiftLeft(1).add(BigInteger.ONE), iter, listCurrent, null)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Number Daboville. Mode = Number or Erase
         *
         */
        private boolean numberDown(Indi indiFrom, BigInteger sosaValue) {
            final Map<String, Pair> dabovillePairs = new HashMap<>();

            // Iterate on the list to go down the tree.
            final Queue<Pair> listIter = new LinkedList<>();
            if (numbering == NUMBERING_ALL || numbering == NUMBERING_DABOVILLE) {
                updateIndi(indiFrom, sosaValue, listIter, dabovillePairs, "1");
            } else {
                final Pair newPair = new Pair(indiFrom, sosaValue == BigInteger.ZERO ? "1" : "");
                listIter.offer(newPair);
                dabovillePairs.put(indiFrom.getId(), newPair);
            }
            return treedown(sosaValue, listIter, dabovillePairs);
        }

        private boolean treedown(BigInteger sosaValue, Queue<Pair> listIter, Map<String, Pair> dabovillePairs) {
            while (!listIter.isEmpty()) {
                if (runBlank && listIter.size() >= STOP_COUNTER) {
                    return false;
                }
                final Pair current = listIter.poll();
                if (current != null) {
                    Character suffix = 'a';
                    final String daboCounter = current.getValue();
                    // Remove spouse not Sosa or d'Aboville
                    if (daboCounter != null && !"".equals(daboCounter) && Character.isLetter(daboCounter.charAt(daboCounter.length() - 1))) {
                        return treedown(sosaValue, listIter, dabovillePairs);
                    }
                    final Fam[] families = current.getIndi().getFamiliesWhereSpouse();
                    for (Fam family : families) {
                        int childOrder = 0;
                        for (Indi child : family.getChildren(true)) {
                            childOrder++;
                            // Do not duplicate (sosa)/daboville numbers
                            if (changedIndis.contains(child)) {
                                continue;
                            }
                            String localCtr = daboCounter + (families.length > 1 ? suffix.toString() : "");
                            localCtr += localCtr.length() > 0 ? "." : "";
                            localCtr += childOrder;
                            if (!updateIndi(child, sosaValue, listIter, dabovillePairs, localCtr)) {
                                return false;
                            }
                        }
                        if (numberSpouse) {
                            // Try to number spouse not numbered.
                            final Indi husband = family.getHusband();
                            if (husband != null && !husband.getId().equals(current.getIndi().getId())) {
                                if (!changedIndis.contains(husband) && !updateIndi(husband, sosaValue, listIter, dabovillePairs, daboCounter + suffix.toString())) {
                                    return false;
                                }
                            } else {
                                final Indi wife = family.getWife();
                                if (wife != null && !changedIndis.contains(wife) && !wife.getId().equals(current.getIndi().getId())
                                        && !updateIndi(wife, sosaValue, listIter, dabovillePairs, daboCounter + suffix.toString())) {
                                    return false;
                                }
                            }
                        }
                        suffix++;
                    }
                }
            }
            return true;
        }

        /**
         * Modify individual tag and progress in the tree
         * @param indi
         * @param sosaNumber
         * @param listIter
         * @param list
         * @param daboValue
         * @return 
         */
        private boolean updateIndi(Indi indi, BigInteger sosaNumber, Queue<Pair> listIter, Map<String, Pair> list, String daboValue) {

            // Check if indi has already been seen
            boolean isNew = !changedIndis.contains(indi);
            if (isNew) {
                changedIndis.add(indi);
            }
            
            // Update list to keep progressing in the tree in case we see the individual for the first time or if multiple ancestors are to be marked
            if (isNew || allSosa) {
                final Pair newPair = new Pair(indi, daboValue == null ? nbToString(sosaNumber) : daboValue);
                listIter.offer(newPair);
                if (isNew) {
                    list.put(indi.getId(), newPair);
                } else {
                    // replace SOSA value with smallest one.
                    final Pair current = list.get(indi.getId());
                    if (current != null && SOSA_COMPARE.compare(newPair, current) < 0) {
                        list.remove(indi.getId());
                        list.put(indi.getId(), newPair);
                    }
                }
            }

            // If Erase all numbers or just one, delete or count only depending on blank run 
            if (mode == MODE_ERASE) {
                if (isNew && (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSADABOVILLE)) {
                    counter += indi.getProperties(Indi.TAG_SOSADABOVILLE).length;
                    if (!runBlank) {
                        indi.delProperties(Indi.TAG_SOSADABOVILLE);
                    }
                }
                if (isNew && (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSA)) {
                    counter += indi.getProperties(Indi.TAG_SOSA).length;
                    if (!runBlank) {
                        indi.delProperties(Indi.TAG_SOSA);
                    }
                }
                if (isNew && (numbering == NUMBERING_ALL || numbering == NUMBERING_DABOVILLE)) {
                    counter += indi.getProperties(Indi.TAG_DABOVILLE).length;
                    if (!runBlank) {
                        indi.delProperties(Indi.TAG_DABOVILLE);
                    }
                }
            }

            // If not erase, add numbers if individual seen for the first time or in case of multiple ancestors 
            if (mode != MODE_ERASE && (isNew || allSosa)) {
                Property prop = null;
                try {
                    String value = nbToString(sosaNumber, "", true, daboValue);
                    switch (numbering) {
                        case NUMBERING_SOSADABOVILLE:
                            if (!runBlank) {
                                prop = addSosaValue(indi, Indi.TAG_SOSADABOVILLE, value, getNumberPosition(indi, Indi.TAG_SOSADABOVILLE, sosaNumber));
                            }
                            counter++;
                            break;
                        case NUMBERING_SOSA:
                            if (!runBlank) {
                                prop = addSosaValue(indi, Indi.TAG_SOSA, value, getNumberPosition(indi, Indi.TAG_SOSA, sosaNumber));
                            }
                            counter++;
                            break;
                        case NUMBERING_DABOVILLE:
                            if (!runBlank) {
                                prop = addSosaValue(indi, Indi.TAG_DABOVILLE, value, setPropertyPosition(indi, Indi.TAG_DABOVILLE));
                            }
                            counter++;
                            break;
                        default:
                            break;
                    }
                    log.log(Level.FINER, "{0} -> {1}", new Object[]{indi.toString(true), value});
                } catch (GedcomException ex) {
                    log.log(Level.WARNING, "Error during Numbering.", ex);
                }
                
                // If number added, save it in gedcom if required, otherwise make it guessed
                if (!runBlank &&  prop != null) {
                    prop.setGuessed(!save);
                }
                
            }
            return !isNew || !cancel;
            // done
        }
        
        private Property addSosaValue(Indi indi, String tag, String value, int pos) throws GedcomException {
            Property oldProp = indi.getProperty(tag);
            if (oldProp != null) {
                String oldValue = oldProp.getValue();
                if (oldValue.equals(value)) {
                    return oldProp;
                }
            }
            return indi.addProperty(tag, value, pos);
        }

        /**
         * Get sosa position order by number
         *
         * @param prop
         *
         * @return
         */
        private int getNumberPosition(Property prop, String tag, BigInteger value) {
            if (prop == null) {
                return 1;
            }
            // Put after tag based on sosa order 
            Property[] props = prop.getProperties(tag);
            if (props != null && props.length != 0) {
                for (Property p : props) {
                    final BigInteger bi = extractNumber(p.getDisplayValue());
                    if (bi.compareTo(value) > 0) {
                        return prop.getPropertyPosition(p);
                    }
                }
                return prop.getPropertyPosition(props[props.length - 1]) + 1;
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
            Property[] props = prop.getProperties(tag);
            if (props != null && props.length != 0) {
                return prop.getPropertyPosition(props[props.length - 1]) + 1;
            }

            // Else after name
            Property pName = prop.getProperty("NAME");
            if (pName != null) {
                return prop.getPropertyPosition(pName) + 1;
            }

            // Else first
            return 1;
        }

        private BigInteger extractNumber(String str) {

            int start = 0;
            while (start <= str.length() - 1 && !Character.isDigit(str.charAt(start))) {
                start++;
            }
            int end = start;
            while ((end <= str.length() - 1) && Character.isDigit(str.charAt(end))) {
                end++;
            }
            if (end == start) {
                return BigInteger.ZERO;
            } else {
                return new BigInteger(str.substring(start, end));
            }
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
                for (int k = 0; k < dabo.length(); k++) {
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

    }

}
