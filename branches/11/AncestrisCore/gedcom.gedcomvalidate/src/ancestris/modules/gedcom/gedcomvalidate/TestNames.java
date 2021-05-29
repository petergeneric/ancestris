/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.gedcom.TagPath;
import genj.view.ViewContext;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Test names for inconsistencies
 * 
 * 
 */
public class TestNames extends Test {


    /**
     * Constructor
     */
    public TestNames() {
        super((String[]) null, PropertyName.class);
    }

    /**
     * Do the test
     */
    @Override
    void test(Property prop, TagPath path, List<ViewContext> issues, GedcomValidate report) {

        PropertyName name = (PropertyName) prop;
        
        if (name.hasWarning()) {
            emitIssue(prop, issues, "err.names.warning");
        }
       
        // done
    }

    @Override
    String getCode() {
        return "00-5";
    }
    
    void emitIssue(Property prop, List<ViewContext> issues, String error) {
        String text = NbBundle.getMessage(this.getClass(), error, prop.getPath().toString());
        issues.add(new ViewContext(prop).setImage(prop.getImage()).setCode(getCode()).setText(text));
    }
    
} //TestFiles
