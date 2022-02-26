/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2014 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.gedcomvalidate;

import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.view.ViewContext;

import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Test for date validity against a specified range.
 */
/*package*/ class TestDateRangeValid extends Test {

    /** the report */
    private final GedcomValidate report;

    /**
     * Constructor
     */
    TestDateRangeValid(GedcomValidate report) {
        super((String[]) null, Property.class);
        this.report = report;
    }

    /**
     * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath, java.util.List)
     */
    /*package*/
    @Override
    void test(Property prop, TagPath path, List<ViewContext> issues, GedcomValidate report) {

        // not an issue if prop is not a date
        if (!(prop instanceof PropertyDate)) {
            return;
        }

        PropertyDate propDate = ((PropertyDate) prop);
        if (checkPointInTime(propDate.getStart(), report.minYear, report.maxYear) && checkPointInTime(propDate.getEnd(), report.minYear, report.maxYear)) {
            return;
        }   
        // got an issue with that
        issues.add(new ViewContext(prop).setCode(getCode()).setText(
                NbBundle.getMessage(this.getClass(), "warn.year.range", propDate.getDisplayValue(), report.minYear.getYear(), report.maxYear.getYear(), path)));
        // done
    }

    /**
     * Check whether a PointInTime is in provided range. If pit is null return true;
     *
     * @param pit
     * @param min
     * @param max
     *
     * @return
     */
    private boolean checkPointInTime(PointInTime pit, PointInTime min, PointInTime max) {
        if (pit == null) {
            return true;
        }
        /* invalid PIT are considered in range as PropertyDate.getStart or getEnd may
         * return invalid PIT if not set
        */
        if (!pit.isValid()){
            return true;
        }
        Parameters.notNull("min", min);
        Parameters.notNull("max", max);
        return pit.compareTo(min) >= 0 && pit.compareTo(max) <= 0;
    }

    @Override
    String getCode() {
        return "05";
    }

} //TestValid
