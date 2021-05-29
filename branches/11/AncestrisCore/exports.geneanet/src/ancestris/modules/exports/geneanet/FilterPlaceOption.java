/*
 * Copyright (C) 2019 Zurga
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ancestris.modules.exports.geneanet;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.io.Filter;
import org.openide.util.NbBundle;

/**
 * Filter to allow to change place in Geneanet Export. Nothing to do, just
 * select in option
 *
 * @author Zurga
 */
public class FilterPlaceOption implements Filter {

    /**
     * callback
     */
    @Override
    public boolean veto(Entity indi) {
        // fine
        return false;
    }

    @Override
    public String getFilterName() {
        return NbBundle.getMessage(GeneanetExportAction.class, "FilterPlaceName");
    }

    @Override
    public boolean veto(Property property) {
        return false;
    }

    @Override
    public boolean canApplyTo(Gedcom gedcom) {
        return true;
    }
}
