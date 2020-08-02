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
import java.util.List;
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

        private final static int STOP_COUNTER = 15000;
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
            int progress = 100 * counter / maxCounter;
            if (progress % 5 == 0) {
                log.log(Level.FINE, "Time=" + TimingUtility.getInstance().getTime() + " - state = " + getState() + " - progress = " + progress);
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
            counter = 0;
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

            // Clean existing numbering if mode is erase and no decujus 
            if (mode == MODE_ERASE && indiDeCujus == null) {
                state = NbBundle.getMessage(getClass(), "SosaNumbersGenerator.taskErase");
                task = () -> {
                    eraseAll();
                };
            } else if (indiDeCujus == null) {
                // Otherwise, if sosadabo or sosa, go "up" the tree by first erasing and then, if mode is generate, generating numbering. Flag set to guessed along the way if save is false
                state = "";
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
                state = "";
            }

            // Calculates number of expected changes (blank run)
            // -------------------------------------------------
            changedIndis = new HashSet<>();
            counter = 0;
            runBlank = true;
            task.run();
            runBlank = false;
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
            counter = 0;
            changedIndis.clear();
            String msg = "<html>";
            
            task.run();

            if (counter == 0 || counter == maxCounter) {
                if (message == null) {
                    msg += NbBundle.getMessage(getClass(), "SosaNumbersGenerator.autogen") + "<br>" + NbBundle.getMessage(getClass(), "SosaNumbersGenerator.changes", maxCounter) + "</html>";
                } else {
                    msg += message + "<br>" + NbBundle.getMessage(getClass(), "SosaNumbersGenerator.changes", maxCounter) + "</html>";
                }
            } else {
                msg += NbBundle.getMessage(getClass(), "SosaNumbersGenerator.stopped", counter) + "</html>";
            }
            message = msg;

            return true;
        }

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
         * Number Sosa from de Cujus.
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
         * @param indiDeCujus
         * @param sosaValue
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
                            String counter = daboCounter + (families.length > 1 ? suffix.toString() : "");
                            counter += counter.length() > 0 ? "." : "";
                            counter += childOrder;
                            if (!updateIndi(child, sosaValue, listIter, dabovillePairs, counter)) {
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

        private boolean updateIndi(Indi indi, BigInteger sosaNumber, Queue<Pair> listIter, Map<String, Pair> list, String daboValue) {

            int nbErased = 0;

            // Check if indi is new in the list
            boolean isNew = !list.containsKey(indi.getId());

            // Clean numbering all or one numbering if individual not in list yet
            if (isNew && (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSADABOVILLE)) {
                nbErased += indi.getProperties(Indi.TAG_SOSADABOVILLE).length;
                if (!runBlank) {
                    indi.delProperties(Indi.TAG_SOSADABOVILLE);
                }
            }
            if (isNew && (numbering == NUMBERING_ALL || numbering == NUMBERING_SOSA)) {
                nbErased += indi.getProperties(Indi.TAG_SOSA).length;
                if (!runBlank) {
                    indi.delProperties(Indi.TAG_SOSA);
                }
            }
            if (isNew && (numbering == NUMBERING_ALL || numbering == NUMBERING_DABOVILLE)) {
                nbErased += indi.getProperties(Indi.TAG_DABOVILLE).length;
                if (!runBlank) {
                    indi.delProperties(Indi.TAG_DABOVILLE);
                }
            }

            // Update list to keep going up the tree
            if (isNew || allSosa) {
                final Pair newPair = new Pair(indi, daboValue == null ? nbToString(sosaNumber) : daboValue);
                listIter.offer(newPair);
                if (isNew) {
                    list.put(indi.getId(), newPair);
                } else {
                    // replace SOSA value with smallest one.
                    final Pair current = list.get(indi.getId());
                    if (SOSA_COMPARE.compare(newPair, current) < 0) {
                        list.remove(indi.getId());
                        list.put(indi.getId(), newPair);
                    }
                }
            }

            // 3. Generate one numbering
            if (isNew) {
                changedIndis.add(indi);
                if (mode != MODE_ERASE) {
                    counter++;
                } else {
                    maxCounter += nbErased;
                }
                if (runBlank) {
                    return true;
                }
            }

            // Quit if just erasing
            if (mode != MODE_ERASE && (isNew || allSosa)) {
                Property prop = null;
                try {
                    String value = nbToString(sosaNumber, "", true, daboValue);
                    switch (numbering) {
                        case NUMBERING_SOSADABOVILLE:
                            prop = indi.addProperty(Indi.TAG_SOSADABOVILLE, value, getNumberPosition(indi, Indi.TAG_SOSADABOVILLE, sosaNumber));
                            break;
                        case NUMBERING_SOSA:
                            prop = indi.addProperty(Indi.TAG_SOSA, value, getNumberPosition(indi, Indi.TAG_SOSA, sosaNumber));
                            break;
                        case NUMBERING_DABOVILLE:
                            prop = indi.addProperty(Indi.TAG_DABOVILLE, value, setPropertyPosition(indi, Indi.TAG_DABOVILLE));
                            break;
                        default:
                            break;
                    }
                    log.log(Level.FINER, "{0} -> {1}", new Object[]{indi.toString(true), value});
                } catch (GedcomException ex) {
                    log.log(Level.WARNING, "Error during Numbering.", ex);
                }
                if (prop != null) {
                    prop.setGuessed(!save);
                }
            }
            return !isNew || !cancel;
            // done
        }

        /**
         * Define our own delete method in order to monitor progress and speed up performance. (rather than using GedcomUtilities.deleTags)
         *
         * @param tagToRemove Tag to remove
         */
        public boolean deleteTags(String tagToRemove) {
            if (runBlank) {
                for (Entity entity : gedcom.getIndis()) {
                    List<Property> props = entity.getAllProperties(tagToRemove);
                    counter++;
                }
            } else {
                for (Entity entity : gedcom.getIndis()) {
                    boolean counterPlus = true;
                    List<Property> props = entity.getAllProperties(tagToRemove);
                    for (Property prop : props) {
                        Property parent = prop.getParent();
                        if (parent != null) {
                            log.log(Level.FINER, "Tag {0} {1} deleted for {1}", new Object[]{tagToRemove, prop.getDisplayValue(), entity.toString(true)});
                            parent.delProperty(prop);
                            if (counterPlus) {
                                counter++;
                                counterPlus = false;
                            }
                            if (cancel) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
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
