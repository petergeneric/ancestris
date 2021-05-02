package ancestris.modules.gedcom.matchers;

import genj.gedcom.Fam;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author lemovice
 */
public class IndiMatcher extends EntityMatcher<Indi, IndiMatcherOptions> {

    private static final Logger LOG = Logger.getLogger(IndiMatcher.class.getName());

    public IndiMatcher() {
        super();
        this.options = new IndiMatcherOptions();
    }

    @Override
    public int compare(Indi leftIndi, Indi rightIndi) {
        
        int score = 0;
        
        // Exclude empty names
        if (options.isExcludeEmptyNames()) {
            if (isEmptyName(leftIndi) || isEmptyName(rightIndi)) {
                return -1;
            }
        }
        
        // Exclude parent and child relationships
        if (options.isExcludeSameFamily()) {
            if (leftIndi.getParents().contains(rightIndi) || rightIndi.getParents().contains(leftIndi)) {
                return 0;
            }
        }
        
        // Same sex
        if ((leftIndi.getSex() == rightIndi.getSex())) {
            score += 5;
        }
        
        // Same last names ?
        boolean sameLastnames = compareLastNames(leftIndi, rightIndi);
        if (sameLastnames) {
            score += 5;
        }

        // Same firt names ?
        boolean sameFirstnames = compareFirstNames(leftIndi, rightIndi);

        // Eliminate solution if all names or all firstnames must be equal
        if (options.isAllFirstNamesEquals() && !sameFirstnames) {
            return 0;
        }
        if (options.isCheckAllNames() && (!sameLastnames || !sameFirstnames)) {
            return 0;
        }
        if (sameFirstnames) {
            score += 10;
        }
        
        // Same birth date ?
        if (compareDates(leftIndi.getBirthDate(), rightIndi.getBirthDate()) < options.getDateinterval()) {
            score += (options.isEmptyValueInvalid() ? 20 : 10);
        }
        
        // Same sex, same name, same firstname, same birth date : Push the value
        if (score >=30) {
            score +=20;
        }

        // Same birth Place ?
        if (compareBirthPlace(leftIndi, rightIndi)) {
            score += 20;
        }

        // same death date ?
        if (compareDates(leftIndi.getDeathDate(), rightIndi.getDeathDate()) < options.getDateinterval()) {
            score += (options.isEmptyValueInvalid() ? 20 : 10);
        }

        // same death place ?
        if (compareDeathPlace(leftIndi, rightIndi)) {
            score += 20;
        }

        // same spouse and wedding date ?
        score += 20 * compareMarriage(leftIndi, rightIndi);

        return score>100 ? 100 : score;
    }

    private boolean isEmptyName(Indi indi) {
        Property[] names = indi.getProperties("NAME");
        for (Property name : names) {
            String nameStr = ((PropertyName) name).getLastName().trim();
            if (nameStr.isEmpty() || nameStr.equals("?")) {
                return true;
            }
            nameStr = ((PropertyName) name).getFirstName().trim();
            if (nameStr.isEmpty() || nameStr.equals("?")) {
                return true;
            }
        }
        return false;
    }

    private boolean compareLastNames(Indi leftIndi, Indi rightIndi) {
        if (options.isCheckAllNames()) {
            Property[] leftNames = leftIndi.getProperties("NAME");
            Property[] rightNames = rightIndi.getProperties("NAME");
            double count = 0;
            for (Property leftName : leftNames) {
                for (Property rightName : rightNames) {
                    if (((PropertyName) leftName).getLastName().equals(((PropertyName) rightName).getLastName())) {
                        count++;
                        break;
                    }
                }
            }
            return count > 0;
        } else {
            Property leftName = leftIndi.getProperty("NAME");
            Property rightName = rightIndi.getProperty("NAME");
            if (leftName != null && rightName != null) {
                return ((PropertyName) leftName).getLastName().equals(((PropertyName) rightName).getLastName());
            } else {
                return false;
            }
        }
    }

    private boolean compareFirstNames(Indi leftIndi, Indi rightIndi) {
        if (options.isCheckAllNames()) {
            Property[] leftNames = leftIndi.getProperties("NAME");
            Property[] rightNames = rightIndi.getProperties("NAME");

            if (options.isAllFirstNamesEquals()) {
                for (Property leftName : leftNames) {
                    double count = 0;
                    String[] leftFistNames = ((PropertyName) leftName).getFirstName().split(" ");
                    String[] rightFistNames = new String[]{};
                    for (Property rightName : rightNames) {
                        rightFistNames = ((PropertyName) rightName).getFirstName().split(" ");
                        for (String leftFistName : leftFistNames) {
                            for (String rightFistName : rightFistNames) {
                                if (leftFistName.equals(rightFistName)) {
                                    count++;
                                }
                            }
                        }
                    }
                    if (count == Math.max(leftFistNames.length, rightFistNames.length)) {
                        return true;
                    }
                }
                return false;
            } else {
                for (Property leftName : leftNames) {
                    String[] leftFistNames = ((PropertyName) leftName).getFirstName().split(" ");
                    for (Property rightName : rightNames) {
                        String[] rightFistNames = ((PropertyName) rightName).getFirstName().split(" ");
                        for (String leftFistName : leftFistNames) {
                            for (String rightFistName : rightFistNames) {
                                if (leftFistName.equals(rightFistName)) {
                                    // One maching first name it's ok
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
            }
        } else {
            Property leftName = leftIndi.getProperty("NAME");
            Property rightName = rightIndi.getProperty("NAME");
            if (leftName == null || rightName == null) {
                return false;
            }
            if (options.isAllFirstNamesEquals()) {
                double count = 0;
                String[] leftFistNames = ((PropertyName) leftName).getFirstName().split(" ");
                String[] rightFistNames = ((PropertyName) rightName).getFirstName().split(" ");
                for (String leftFistName : leftFistNames) {
                    for (String rightFistName : rightFistNames) {
                        if (leftFistName.equals(rightFistName)) {
                            count++;
                        }
                    }
                }
                return count == Math.max(leftFistNames.length, rightFistNames.length);
            } else {
                String[] leftFistNames = ((PropertyName) leftName).getFirstName().split(" ");
                String[] rightFistNames = ((PropertyName) rightName).getFirstName().split(" ");
                for (String leftFistName : leftFistNames) {
                    for (String rightFistName : rightFistNames) {
                        if (leftFistName.equals(rightFistName)) {
                            // One maching first name it's ok
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }

    private int compareDates(PropertyDate leftDate, PropertyDate rightDate) {
        if (leftDate != null && rightDate != null) {
            if (leftDate.isValid() && rightDate.isValid()) {
                try {
                    return Math.abs(leftDate.getStart().getJulianDay() - rightDate.getStart().getJulianDay());
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                    if (options.isEmptyValueInvalid()) {
                        return options.getDateinterval();
                    } else {
                        return options.getDateinterval() - 1;
                    }
                }
            } else {
                if (options.isEmptyValueInvalid()) {
                    return options.getDateinterval();
                } else {
                    return options.getDateinterval() - 1;
                }
            }
        } else {
            if (options.isEmptyValueInvalid()) {
                return options.getDateinterval();
            } else {
                return options.getDateinterval() - 1;
            }
        }
    }

    private boolean compareBirthPlace(Indi leftIndi, Indi rightIndi) {
        Property leftIndiBirthProperty = leftIndi.getProperty("BIRT");
        Property rightIndiBirthProperty = rightIndi.getProperty("BIRT");

        if (leftIndiBirthProperty != null && rightIndiBirthProperty != null) {
            PropertyPlace rightIndiPropertyPlace = (PropertyPlace) rightIndiBirthProperty.getProperty("PLAC");
            PropertyPlace leftIndiPropertyPlace = (PropertyPlace) leftIndiBirthProperty.getProperty("PLAC");
            if (rightIndiPropertyPlace != null && leftIndiPropertyPlace != null) {
                return rightIndiPropertyPlace.compareTo(leftIndiPropertyPlace) == 0;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean compareDeathPlace(Indi leftIndi, Indi rightIndi) {
        Property leftIndiDeathProperty = leftIndi.getProperty("DEAT");
        Property rightIndiDeathProperty = rightIndi.getProperty("DEAT");
        if (leftIndiDeathProperty != null && rightIndiDeathProperty != null) {
            PropertyPlace rightIndiPropertyPlace = (PropertyPlace) rightIndiDeathProperty.getProperty("PLAC");
            PropertyPlace leftIndiPropertyPlace = (PropertyPlace) leftIndiDeathProperty.getProperty("PLAC");
            if (rightIndiPropertyPlace != null && leftIndiPropertyPlace != null) {
                return rightIndiPropertyPlace.compareTo(leftIndiPropertyPlace) == 0;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    // Compare fams of each
    private int compareMarriage(Indi leftIndi, Indi rightIndi) {
        Fam[] leftFams = leftIndi.getFamiliesWhereSpouse();
        Fam[] rightFams = rightIndi.getFamiliesWhereSpouse();
        // Let's see if wa can find one match (same spouse or same spouse lastname, same date)
        int match = 0;
        boolean sameMarrDate;
        for (Fam leftFam : leftFams) {
            for (Fam rightFam : rightFams) {
                match = 0;
                sameMarrDate = false;
                Indi leftSpouse = leftFam.getOtherSpouse(leftIndi);
                Indi rightSpouse = rightFam.getOtherSpouse(rightIndi);
                if (leftSpouse != null && rightSpouse != null) {
                    if (leftSpouse.equals(rightSpouse)) {
                        match += 3;
                    } else {
                        boolean sameSpouse = compareLastNames(leftSpouse, rightSpouse);
                        if (sameSpouse) {
                            match++;
                        }
                        sameSpouse = compareFirstNames(leftSpouse, rightSpouse);
                        if (sameSpouse) {
                            match++;
                        }
                    }
                }
                PropertyDate leftDate = leftFam.getMarriageDate();
                PropertyDate rightDate = rightFam.getMarriageDate();
                if (leftDate != null && rightDate != null) {
                    sameMarrDate = compareDates(leftDate, rightDate) < options.getDateinterval();
                }
                if (sameMarrDate) {
                    match++;
                }
                if (match > 1) {
                    return match;
                }
            }
        }

        return match;

    }

    @Override
    protected String[] getKeys(Indi entity) {
        List<String> keys = new ArrayList<>();
        List<PropertyName> names = entity.getProperties(PropertyName.class);
        names.forEach((name) -> {
            keys.add(name.getLastName());
        });
        return keys.toArray(new String[0]);
    }
}
