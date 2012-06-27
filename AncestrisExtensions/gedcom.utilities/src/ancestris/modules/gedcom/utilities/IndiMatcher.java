package ancestris.modules.gedcom.utilities;

import genj.gedcom.*;
import java.util.logging.Logger;

/**
 *
 * @author lemovice
 */
public class IndiMatcher extends EntityMatcher<Indi> {

    private static final Logger log = Logger.getLogger(IndiMatcher.class.getName());

    @Override
    public int compareEntities(Indi leftIndi, Indi rightIndi) {
        if ((leftIndi.getSex() == rightIndi.getSex())) {
            // Compare LastName
            int lastNameScore = compareLastNames(leftIndi, rightIndi);
            if (lastNameScore > 80) {
                // Compare FistNames
                int firstNameScore = compareFirstNames(leftIndi, rightIndi);
                if (firstNameScore > 80) {
                    if (compareBirthPlace(leftIndi, rightIndi)) {
                        if (compareBirthDate(leftIndi, rightIndi) < 4000) {
                            if (compareDeathPlace(leftIndi, rightIndi)) {
                                if (compareDeathDate(leftIndi, rightIndi) < 4000) {
                                    return 100;
                                } else {
                                    return 80;
                                }
                            } else {
                                return 75;
                            }
                        } else {
                            return 50;
                        }
                    } else {
                        return 25;
                    }

                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private int compareLastNames(Indi leftIndi, Indi rightIndi) {
        Property[] leftNames = leftIndi.getProperties("NAME");
        Property[] rightNames = rightIndi.getProperties("NAME");
        double count = 0;
        for (Property leftName : leftNames) {
            for (Property rightName : rightNames) {
                if (((PropertyName)leftName).getLastName().equals(((PropertyName)rightName).getLastName())) {
                    count++;
                    break;
                }
            }
        }
        return (int) ((count / leftNames.length) * 100);
    }

    private int compareFirstNames(Indi leftIndi, Indi rightIndi) {
        Property[] leftNames = leftIndi.getProperties("NAME");
        Property[] rightNames = rightIndi.getProperties("NAME");
        double count = 0;

        for (Property leftName : leftNames) {
            for (Property rightName : rightNames) {
                if (((PropertyName)leftName).getFirstName().equals(((PropertyName)rightName).getFirstName())) {
                    count++;
                    break;
                }
            }
        }
        return (int) ((count / leftNames.length) * 100);
    }

    private int compareBirthDate(Indi leftIndi, Indi rightIndi) {
        PropertyDate leftIndiBirthDate = leftIndi.getBirthDate();
        PropertyDate rightIndiBirthDate = rightIndi.getBirthDate();

        return leftIndiBirthDate.getStart().compareTo(rightIndiBirthDate.getStart());
    }

    private boolean compareBirthPlace(Indi leftIndi, Indi rightIndi) {
        Property leftIndiBirthProperty = leftIndi.getProperty("BIRT");
        Property rightIndiBirthProperty = rightIndi.getProperty("BIRT");
        PropertyPlace rightIndiPropertyPlace = (PropertyPlace) rightIndiBirthProperty.getProperty("PLAC");
        PropertyPlace leftIndiPropertyPlace = (PropertyPlace) leftIndiBirthProperty.getProperty("PLAC");
        if (rightIndiPropertyPlace.compareTo(leftIndiPropertyPlace) == 0) {
            return true;
        } else {
            return false;
        }
    }

    private int compareDeathDate(Indi leftIndi, Indi rightIndi) {
        PropertyDate leftIndiDeathDate = leftIndi.getDeathDate();
        PropertyDate rightIndiDeathDate = rightIndi.getDeathDate();

        return leftIndiDeathDate.getStart().compareTo(rightIndiDeathDate.getStart());
    }

    private boolean compareDeathPlace(Indi leftIndi, Indi rightIndi) {
        Property leftIndiDeathProperty = leftIndi.getProperty("DEAT");
        Property rightIndiDeathProperty = rightIndi.getProperty("DEAT");
        PropertyPlace rightIndiPropertyPlace = (PropertyPlace) rightIndiDeathProperty.getProperty("PLAC");
        PropertyPlace leftIndiPropertyPlace = (PropertyPlace) leftIndiDeathProperty.getProperty("PLAC");
        if (rightIndiPropertyPlace.compareTo(leftIndiPropertyPlace) == 0) {
            return true;
        } else {
            return false;
        }
    }
}
