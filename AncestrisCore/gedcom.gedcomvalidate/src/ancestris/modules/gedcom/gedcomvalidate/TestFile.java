/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;
import genj.view.ViewContext;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Test whether files that are pointed to actually exist
 */
@SuppressWarnings("unchecked")
public class TestFile extends Test {

    /**
     * Constructor
     */
    public TestFile() {
        super((String[]) null, PropertyFile.class);
    }

    /**
     * Do the test
     */
    @Override
    void test(Property prop, TagPath path, List<ViewContext> issues, GedcomValidate report) {

        // assuming PropertyFile
        PropertyFile file = (PropertyFile) prop;

        if (!file.isValid()) {
            issues.add(new ViewContext(prop).setCode(getCode()).setText(NbBundle.getMessage(this.getClass(), "err.nofile")));
        }
        // check it
    }

    @Override
    String getCode() {
        return "08";
    }
} //TestFiles
