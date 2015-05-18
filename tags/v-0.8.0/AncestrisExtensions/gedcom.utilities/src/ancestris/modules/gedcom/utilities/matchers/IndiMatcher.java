package ancestris.modules.gedcom.utilities.matchers;

import genj.gedcom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author lemovice
 */
public class IndiMatcher extends EntityMatcher<Indi, IndiMatcherOptions> {

    private static final Logger log = Logger.getLogger(IndiMatcher.class.getName());

    public IndiMatcher() {
        super();
        this.options = new IndiMatcherOptions();
    }

    @Override
    public int compare(Indi leftIndi, Indi rightIndi) {
        if ((leftIndi.getSex() == rightIndi.getSex())) {
            // Same birth date ?
            if (compareDates(leftIndi.getBirthDate(), rightIndi.getBirthDate()) < options.getDateinterval()) {
                // same death date
                if (compareDates(leftIndi.getDeathDate(), rightIndi.getDeathDate()) < options.getDateinterval()) {
                    // Same last names ?
                    if (compareLastNames(leftIndi, rightIndi)) {
                        // same firt names
                        if (compareFirstNames(leftIndi, rightIndi)) {
                            // Same birth Place ?
                            if (compareBirthPlace(leftIndi, rightIndi)) {
                                if (compareDeathPlace(leftIndi, rightIndi)) {
                                    return 100;
                                } else {
                                    return 80;
                                }
                            } else {
                                return 60;
                            }
                        }
                        return 40;
                    }
                    return 20;
                }
                return 10;
            }
            return 5;
        }
        return 0;
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
                if (((PropertyName) leftName).getLastName().equals(((PropertyName) rightName).getLastName())) {
                    return true;
                } else {
                    return false;
                }
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
                if (count == Math.max(leftFistNames.length, rightFistNames.length)) {
                    return true;
                } else {
                    return false;
                }
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
                    if (options.isEmptyValueValid()) {
                        return options.getDateinterval() - 1;
                    } else {
                        return options.getDateinterval();
                    }
                }
            } else {
                if (options.isEmptyValueValid()) {
                    return options.getDateinterval() - 1;
                } else {
                    return options.getDateinterval();
                }
            }
        } else {
            if (options.isEmptyValueValid()) {
                return options.getDateinterval() - 1;
            } else {
                return options.getDateinterval();
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
                if (rightIndiPropertyPlace.compareTo(leftIndiPropertyPlace) == 0) {
                    return true;
                } else {
                    return false;
                }
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
                if (rightIndiPropertyPlace.compareTo(leftIndiPropertyPlace) == 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    @Override
    protected String[] getKeys(Indi entity) {
        List<String> keys = new ArrayList<String>();
        List<PropertyName> names = entity.getProperties(PropertyName.class);
        for (PropertyName name : names) {
//            for (String firstName : name.getFirstNames(true)) {
//                keys.add(name.getLastName() + firstName);
//            }
            keys.add(name.getLastName());
        }
        return keys.toArray(new String[0]);
    }
}
