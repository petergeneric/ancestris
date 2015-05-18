/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2011 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.gedcom;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;

/**
 * Workbench callbacks
 */
public interface GedcomFileListener {

    /**
     * notification that commit of edits is requested
     * @param context current context
     */
    public void commitRequested(Context context);

    /**
     * notification that gedcom was closed
     * @param gedcom Gedcom to be closed
     * @return whether to continue with close operation or not
     * XXX: don't return anything... Fix return value or javadoc
     */
    public void gedcomClosed(Gedcom gedcom);

    /**
     * notification that gedcom was opened
     * @param gedcom Gedcom to be closed
     * @return whether to continue with close operation or not
     * XXX: don't return anything... Fix return value or javadoc
     */
    public void gedcomOpened(Gedcom gedcom);
}
