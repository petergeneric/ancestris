/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyFamilyChild;
import genj.gedcom.TagPath;
import genj.view.ViewContext;
import java.util.List;
import java.util.ListIterator;
import org.openide.util.NbBundle;

/**
 * Test for dupes in information about being biological child
 */
@SuppressWarnings("unchecked")
public class TestBiologicalChild extends Test {

    /**
     * Constructor
     */
    /*package*/ TestBiologicalChild() {
        // delegate to super
        super("INDI", Indi.class);
    }

    /**
     * Test individual(s)'s being-child associations
     */
    @Override
    /*package*/ void test(Property prop, TagPath trigger, List<ViewContext> issues, GedcomValidate report) {

        // loop over all famc
        List<PropertyFamilyChild> famcs = prop.getProperties(PropertyFamilyChild.class);
        for (ListIterator<PropertyFamilyChild> it = famcs.listIterator(); it.hasNext();) {
            PropertyFamilyChild famc = it.next();
            if (famc.isValid() && Boolean.FALSE.equals(famc.isBiological())) {
                it.remove();
            }
        }

        // more than one?
        if (famcs.size() > 1) {
            Property famc = famcs.iterator().next();
            issues.add(new ViewContext(famc).setText(NbBundle.getMessage(this.getClass(), "warn.famc.biological")).setCode(getCode()));
        }

        // done
    }

    @Override
    String getCode() {
        return "02";
    }

} //TestBiologicalChild
