/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.openide.util.Lookup;

/**
 * ClassLoad for Reports
 */
public class ReportLoader {

    /**
     * reports we have
     */
    private List<Report> instances = new ArrayList<Report>(10);
    /**
     * a singleton
     */
    private volatile static ReportLoader singleton;

    /**
     * Clears the report loader's state effectively forcing a reload
     */
    /*
     * package
     */ static void clear() {
        singleton = null;
    }

    /**
     * Access
     */
    public static ReportLoader getInstance() {

        // not known yet?
        if (singleton == null) {
            synchronized (ReportLoader.class) {
                if (singleton == null) {
                    singleton = new ReportLoader();
                }
            }
        }

        // done
        return singleton;

    }

    /**
     * Report by class name
     */
    public Report getReportByName(String classname) {
        for (Report report : instances) {
            if (report.getClass().getName().equals(classname)) {
                return report;
            }
        }
        return null;

    }

    /**
     * Constructor
     */
    private ReportLoader() {
        instances = new ArrayList<Report>(Lookup.getDefault().lookupAll(Report.class));
        // sort 'em
        Collections.sort(instances, new Comparator<Report>() {

            public int compare(Report a, Report b) {
                // 20063008 this can actually fail if the report is bad
                try {
                    return a.getName().compareTo(b.getName());
                } catch (Throwable t) {
                    return 0;
                }
            }
        });

        // done
    }

    /**
     * Which reports do we have
     */
    public Report[] getReports() {
        return getReports(true);
    }

    /**
     * Which reports do we have
     */
    public Report[] getReports(boolean showHidden) {
        List<Report> result = new ArrayList<Report>(instances.size());
        for (Report r : instances) {
            if (!r.isHidden()) {
                result.add(r);
            }
        }
        return result.toArray(new Report[result.size()]);
    }

    /**
     * Save options of all visible reports if a report is hidden (isHidden()
     * returns true) this report is a (special) report whose options must be
     * handled in an optionPanel
     *
     * Those hodden reports extend Report class and can contribute to context
     * menu.
     *
     * XXX: we will have to use netbean context actions intead of using the old
     * API
     */
    // XXX: later all reports will be handled that way
  /*
     * package
     */ void saveOptions() {
        Report[] rs = getReports();
        for (int r = 0; r < rs.length; r++) {
            if (!rs[r].isHidden()) {
                rs[r].saveOptions();
            }
        }
    }
} //ReportLoader
