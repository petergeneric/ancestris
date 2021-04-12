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
import genj.io.InputSource;
import genj.io.input.FileInput;
import genj.view.ViewContext;
import java.util.List;
import java.util.Optional;
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
    void test(Property prop, TagPath path, List<ViewContext> issues, GedcomValidate report) {

        // assuming PropertyFile
        PropertyFile file = (PropertyFile) prop;

        Optional<InputSource> ois = file.getInput();
        
        boolean isError = false;

        if (ois.isPresent()) {
            InputSource is = ois.get();
            if (is instanceof FileInput) {
                FileInput fi = (FileInput) is;
                if (fi.getFile() == null || !fi.getFile().exists()) {
                    isError = true;
                }
            }
        } else {
            isError = true;
        }
        if (isError) {
            issues.add(new ViewContext(prop).setCode(getCode()).setText(NbBundle.getMessage(this.getClass(), "err.nofile")));
        }
        // check it
    }

    @Override
    String getCode() {
        return "08";
    }
} //TestFiles
