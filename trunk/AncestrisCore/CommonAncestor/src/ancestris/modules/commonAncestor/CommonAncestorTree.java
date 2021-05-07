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
 * @author michel
 */
public class CommonAncestorTree {

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
     * create common ancestor tree
     * @param indi1
     * @param indi2
     * @param ancestor 
     */
    public void createPreview(Indi indi1, Indi indi2, Indi ancestor, boolean displayedId, boolean displayRecentYears, int husband_or_wife_first, PreviewTopComponent previewTopComponent) {
        if (indi1 == null || indi2 == null) {
            previewTopComponent.updatePreView(indi1, indi2, new ArrayList<>(), new ArrayList<>(), displayedId, displayRecentYears, husband_or_wife_first);
        }

        // if the common ancestor exists
        if (ancestor != null) {
            List<Step> firstIndiDirectLinks = new ArrayList<>();
            firstIndiDirectLinks.add(new Step(getLastFamilyWhereSpouse(indi1), indi1, indi1.getSex()));
            getAncestorListBetween(ancestor, indi1, firstIndiDirectLinks);
            Collections.reverse(firstIndiDirectLinks);

            List<Step> secondIndiDirectLinks = new ArrayList<>();

            secondIndiDirectLinks.add(new Step(getLastFamilyWhereSpouse(indi2), indi2, indi2.getSex()));
            getAncestorListBetween(ancestor, indi2, secondIndiDirectLinks);
            Collections.reverse(secondIndiDirectLinks);

            previewTopComponent.updatePreView(indi1, indi2, firstIndiDirectLinks, secondIndiDirectLinks, displayedId, displayRecentYears, husband_or_wife_first);
        } else if (ancestor == null) {
            previewTopComponent.updatePreView(indi1, indi2, new ArrayList<>(), new ArrayList<>(), displayedId, displayRecentYears, husband_or_wife_first);
        }
    }

    /**
     * create common ancestor tree
     * @param indi1
     * @param indi2
     * @param ancestor 
     */
    public void createCommonTree(Indi indi1, Indi indi2, Indi ancestor, File outputFile, boolean displayedId, boolean displayRecentYears, int husband_or_wife_first, String fileTypeName) {
        if (indi1 == null || indi2 == null) {
            return;
        }

        // if the common ancestor exists
        if (ancestor != null) {
            List<Step> firstIndiDirectLinks = new ArrayList<>();
            firstIndiDirectLinks.add(new Step(getLastFamilyWhereSpouse(indi1), indi1, indi1.getSex()));
            getAncestorListBetween(ancestor, indi1, firstIndiDirectLinks);
            Collections.reverse(firstIndiDirectLinks);

            List<Step> secondIndiDirectLinks = new ArrayList<>();

            secondIndiDirectLinks.add(new Step(getLastFamilyWhereSpouse(indi2), indi2, indi2.getSex()));
            getAncestorListBetween(ancestor, indi2, secondIndiDirectLinks);
            Collections.reverse(secondIndiDirectLinks);

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
        return;
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

    /**
     * @param ancestor
     * @param descendant
     * @param directLinks
     */
    private void getAncestorListBetween(Indi ancestor, Indi descendant, List<Step> directLinks) {

        Indi link = getParentInDirectLine(ancestor, descendant);

        // while there are links to be added, we keep going
        if (link != null) {
            directLinks.add(new Step(descendant.getFamilyWhereBiologicalChild(), link, link.getSex()));
            //LOG.fine("found link between indi and ancestor : "+link.getName());
            getAncestorListBetween(ancestor, link, directLinks);
        }

    }

    /**
     * @param ancestor
     * @param child
     * @return
     */
    private Indi getParentInDirectLine(Indi ancestor, Indi child) {
        // check his mom/dad
        Indi father = child.getBiologicalFather();
        if (father != null) {
            if (father.isDescendantOf(ancestor) || father.equals(ancestor)) {
                return father;
            }
        }

        Indi mother = child.getBiologicalMother();
        if (mother != null) {
            if (mother.isDescendantOf(ancestor) || mother.equals(ancestor)) {
                return mother;
            }
        }

        // this case is never to happen as we checked that there is a link
        // between the child and the ancestor, until one of the parent is the
        // famous ancestor
        return null;
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
