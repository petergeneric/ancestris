/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.view.ViewContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * Test whether properties adhere to their cardinalities
 */
public class TestCardinality extends Test {

    /**
     * Constructor
     */
    public TestCardinality() {
        super((String[]) null, Property.class);
    }

    /**
     * Do the test
     */
    @Override
    void test(Property prop, TagPath path, List<ViewContext> issues, GedcomValidate report) {

        MetaProperty itsmeta = prop.getMetaProperty();

        // check children that occur more than cardinality
        Map<String, Integer> seen = new HashMap<>();
        for (Property child : prop.getProperties()) {
            String tag = child.getTag();
            MetaProperty meta = itsmeta.getNested(tag, false);
            Integer counter = seen.get(tag);
            if (counter == null) {
                counter = 0;
            }
            counter++;
            seen.put(tag, counter);
            if (counter > meta.getMaxCardinality()) {
                issues.add(new ViewContext(child).setCode(getCode()).setText(NbBundle.getMessage(this.getClass(), "err.cardinality.max", prop.getTag(), tag, prop.getGedcom().getGrammar().getVersion(), meta.getCardinality(), meta.getMaxCardinality())));
            }
        }

        // check children that are missing
        MetaProperty[] metas = prop.getNestedMetaProperties(0);
        for (MetaProperty meta : metas) {
            if (meta.isRequired() && seen.get(meta.getTag()) == null) {
                String txt = NbBundle.getMessage(this.getClass(), "err.cardinality.min", prop.getTag(), meta.getTag(), prop.getGedcom().getGrammar().getVersion(), meta.getCardinality());
                issues.add(new ViewContext(prop).setImage(meta.getImage()).setCode(getCode()).setText(txt));
            }
        }
        // done
    }

    @Override
    String getCode() {
        return "00-2";
    }
} //TestFiles
