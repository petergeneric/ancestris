package ancestris.modules.commonAncestor;

import ancestris.modules.commonAncestor.graphics.GraphicsOutputFactory;
import ancestris.modules.commonAncestor.graphics.IGraphicsOutput;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author michel & frédéric
 */
public class CommonAncestorTree {

    private List<Step> firstIndiDirectLinks = new ArrayList<>();
    private List<Step> secondIndiDirectLinks = new ArrayList<>();
    private final GraphicsOutputFactory outputs = new GraphicsOutputFactory();

    public CommonAncestorTree() {
    }

    /**
     * find common ancestor for indi1 and indi2
     * @param indi1
     * @param indi2
     * @return ancestors list
     */
    public Set<Indi> findCommonAncestors(Indi indi1, Indi indi2) {
        try {
            SelectionDispatcher.muteSelection(true);
            Set<Indi> ancestorList = new LinkedHashSet<>();
            if (indi1 == null || indi2 == null) {
                return ancestorList;
            }

            // the list to gather the different ancestors in order to let the user choose which one to display

            // search the common ancestor
            if (indi1.isAncestorOf(indi2)) {
                ancestorList.add(indi1);
            } else if (indi2.isAncestorOf(indi1)) {
                ancestorList.add(indi2);
            } else {
                getCommonAncestor(indi1, indi2, ancestorList);
                getCommonAncestor(indi2, indi1, ancestorList);
                ancestorList = filterAncestors(ancestorList);
                //regroupCoupleMembers(ancestorList);
            }

            return ancestorList;
        } finally {
            SelectionDispatcher.muteSelection(false);
        }
    }

    /**
     * Select lines to retain (2021-08-22 FL : new algorithm)
     * Get ancestor lines and display the two that make more sense (different paths and shortest)
     * Use newly created method rather than original algorithm: indiAncestor.getAncestorLinesWith(indiChild) 
     * @param indi1
     * @param indi2
     * @param ancestor 
     */
    public boolean selectLines(Indi indi1, Indi indi2, Indi ancestor) {

        // Clear lines before returning either true or false
        firstIndiDirectLinks.clear();
        secondIndiDirectLinks.clear();
        
        if (indi1 == null || indi2 == null || ancestor == null) {
            return false;
        }

        // Get ancestors lines for both indi1 and indi2 using ancestor for both
        List<List<Indi>> lines1 = ancestor.getAncestorLinesWith(indi1);
        List<List<Indi>> lines2 = ancestor.getAncestorLinesWith(indi2);

        // If one line is empty, return (should never happen)
        if (lines1.size() + lines2.size() < 2) {
            return false;
        }

        // Get first lines to display in case below loops does not find any possibility (default)
        List<Indi> line1 = lines1.get(0);
        List<Indi> line2 = lines2.get(0);

        // If there is more than one line for either (rare), overwrite with the line that does not include common individuals from the other selected line
        // (choose 2 lines with no common individual, and if more than 1 is possible, select first shortest ones found)
        int maxSize1 = Integer.MAX_VALUE;
        int maxSize2 = Integer.MAX_VALUE;
        if (lines1.size() + lines2.size() > 2) {
            for (List<Indi> firstTreeLine : lines1) {
                for (List<Indi> secondTreeLine : lines2) {
                    // Check if a common individual exist between the two lines
                    boolean existCommonIndi = false;
                    for (Indi firstIndi : firstTreeLine) {
                        if (firstIndi == indi1 || firstIndi == ancestor) {
                            continue;
                        }
                        for (Indi secondIndi : secondTreeLine) {
                            if (secondIndi == indi2 || secondIndi == ancestor) {
                                continue;
                            }
                            if (firstIndi.getId().equals(secondIndi.getId())) {
                                // Not good, check next line
                                existCommonIndi = true;
                                break;
                            }
                        }
                        if (existCommonIndi) {
                            break;
                        }
                    }
                    if (!existCommonIndi) {
                        // firstTreeLine and secondTreeLine is a good match. Take it if shortest
                        if (firstTreeLine.size() < maxSize1 || secondTreeLine.size() < maxSize2) {
                            line1 = firstTreeLine;
                            line2 = secondTreeLine;
                            maxSize1 = line1.size();
                            maxSize2 = line2.size();
                        }
                    } else {
                        existCommonIndi = false;
                    }
                }
            }
        }

        // Convert the selected lines into display components (steps)
        for (Indi indi : line1) {
            firstIndiDirectLinks.add(new Step(getLastFamilyWhereSpouse(indi), indi, indi.getSex()));
        }
        for (Indi indi : line2) {
            secondIndiDirectLinks.add(new Step(getLastFamilyWhereSpouse(indi), indi, indi.getSex()));
        }
        Collections.reverse(firstIndiDirectLinks);
        Collections.reverse(secondIndiDirectLinks);
        
        return true;
    }
    
    /**
     * create common ancestor tree
     * @param indi1
     * @param indi2
     * @param ancestor 
     */
    public void createPreview(Indi indi1, Indi indi2, Indi ancestor, boolean displayedId, boolean displayRecentYears, int husband_or_wife_first, PreviewTopComponent previewTopComponent) {

        selectLines(indi1, indi2, ancestor);
        previewTopComponent.updatePreView(indi1, indi2, firstIndiDirectLinks, secondIndiDirectLinks, displayedId, displayRecentYears, husband_or_wife_first);
    }

    /**
     * create common ancestor tree
     * @param indi1
     * @param indi2
     * @param ancestor 
     */
    public void createCommonTree(Indi indi1, Indi indi2, Indi ancestor, File outputFile, boolean displayedId, boolean displayRecentYears, int husband_or_wife_first, String fileTypeName) {

        if (!selectLines(indi1, indi2, ancestor)) {
            return;
        }

        // If the common ancestor exists, get ancestor lines and display the two that make more sense (different paths and shortest)
        IGraphicsOutput output = outputs.createOutput(outputFile, fileTypeName);
        if (output == null) {
            // report canceled 
            return;
        }
        try {
            output.output(new Renderer(indi1, indi2, firstIndiDirectLinks, secondIndiDirectLinks, displayedId, displayRecentYears, husband_or_wife_first));
            //return output.result(this);
        } catch (IOException e) {
            System.out.println(e.toString());
            return;
        }
    }

    private Set<Indi> filterAncestors(Set<Indi> ancestorList) {
        Set<Indi> filteredList = new LinkedHashSet<>();
        boolean found = false;
        for (Indi ancestor : ancestorList) {
            found = false;
            Fam[] families = ancestor.getFamiliesWhereSpouse();
            // for each of the families the ancestor belonged to,
            // look if one of the other ancestors did not belong to it
            for (Fam familie : families) {
                Indi otherSpouse = familie.getOtherSpouse(ancestor);
                if (filteredList.contains(otherSpouse)) {
                    found = true;
                }
            }
            if (found == false) {
                filteredList.add(ancestor);
            }
        }
        return filteredList;
    }

    /** finds the most recent family of the given Indi
     * @param indi the Indi whom family we're looking for
     * @return his last family
     */
    private Fam getLastFamilyWhereSpouse(Indi indi) {
        Fam[] fams = indi.getFamiliesWhereSpouse();
        if (fams == null || fams.length == 0) {
            return null;
        }
        return fams[fams.length - 1];
    }

    /**
     * check biological father and mother of "indi" to see of one is "other"'s ancestor.
     * @param indi
     * @param other
     * @return
     */
    private void getCommonAncestor(Indi firstIndi, Indi secondIndi, Set<Indi> ancestorList) {
        //FIXME non symmetrical algorithm :

        Indi father = firstIndi.getBiologicalFather();
        if (father != null) {
            if (father.isAncestorOf(secondIndi)) {
                ancestorList.add(father);
            } else {
                getCommonAncestor(father, secondIndi, ancestorList);
            }
        }

        Indi mother = firstIndi.getBiologicalMother();
        if (mother != null) {
            if (mother.isAncestorOf(secondIndi)) {
                ancestorList.add(mother);
            } else {
                getCommonAncestor(mother, secondIndi, ancestorList);
            }
        }
    }

    public Map<String, IGraphicsOutput> getOutputList() {
        return outputs.getOutputList();
    }

    /**
     * @return file type names ready for use ((jpeg, png, pdf ...)
     */
    public List<String> getFileTypeNames() {
        return outputs.getFileTypeNames();
    }
}
