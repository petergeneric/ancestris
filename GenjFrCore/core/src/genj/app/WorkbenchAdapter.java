/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.app;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Trackable;
import genj.view.View;

public class WorkbenchAdapter implements WorkbenchListener {

    @Override
    public void commitRequested(Context context) {
    }

    @Override
    public void gedcomClosed(Gedcom gedcom) {
    }

    @Override
    public void gedcomOpened(Gedcom gedcom) {
    }

    @Override
    public void selectionChanged(Context context, boolean isActionPerformed) {
    }

    @Override
    public void viewClosed(View view) {
    }

    @Override
    public void viewOpened(View view) {
    }

    @Override
    public void workbenchClosing() {
    }

    @Override
    public void processStarted(Trackable process) {
    }

    @Override
    public void processStopped(Trackable process) {
    }
}
