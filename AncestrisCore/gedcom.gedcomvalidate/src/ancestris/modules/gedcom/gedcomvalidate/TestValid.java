/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.view.ViewContext;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Test for property validity
 *
 * @author nmeier
 */
@SuppressWarnings("unchecked")
/*package*/ class TestValid extends Test {

    /**
     * the report
     */
    private final GedcomValidate report;

    /**
     * Constructor
     */
    /*package*/ TestValid(GedcomValidate report) {
        super((String[]) null, Property.class);
        this.report = report;
    }

    /**
     * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath,
     * java.util.List)
     */
    /*package*/
    @Override
    void test(Property prop, TagPath path, List<ViewContext> issues, GedcomValidate report) {

        // always an issue with private
        if (!report.isPrivateValueValid && prop.isPrivate()) {
            // got an issue with that
            issues.add(new ViewContext(prop).setCode(getCode()).setText(NbBundle.getMessage(TestValid.class, "err.private", path.toString())));
        }

        // no issue if isEmptyValid && getValue() is empty
        if (!report.isEmptyValueValid && prop.getValue().length() == 0 && prop.getNoOfProperties() == 0) {
            issues.add(new ViewContext(prop).setCode(getCode()).setText(NbBundle.getMessage(TestValid.class, "err.nullValue", path.toString())));
        }

        // no issue if valid.
        if (prop.isValid()) {
            return;
        }

        // got an issue with that
        issues.add(new ViewContext(prop).setCode(getCode()).setText(NbBundle.getMessage(TestValid.class, "err.notvalid", path.toString())));

        // done
    }

    @Override
    String getCode() {
        return "00-3";
    }

} //TestValid
