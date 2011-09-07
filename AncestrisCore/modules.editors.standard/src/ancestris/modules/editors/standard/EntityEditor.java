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

package ancestris.modules.editors.standard;

import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import org.openide.util.NbBundle;

/**
 *
 * @author daniel
 */
public class EntityEditor {
    public static boolean editEntity(Fam fam, boolean isNew) {
        final FamPanel bean = new FamPanel();

        if (isNew) {
            bean.setTitle(NbBundle.getMessage(EntityEditor.class, "dialog.fam.new.title", fam));
        } else {
            bean.setTitle(NbBundle.getMessage(EntityEditor.class, "dialog.fam.edit.title", fam));
        }
        bean.setContext(new Context(fam));
        return bean.showPanel();
    }

    public static boolean editEntity(Indi indi, boolean isNew) {
        final IndiPanel bean = new IndiPanel();
        if (indi == null) {
            return false;
        }

        if (isNew) {
            bean.setTitle(NbBundle.getMessage(EntityEditor.class, "dialog.indi.new.title", indi));
        } else {
            bean.setTitle(NbBundle.getMessage(EntityEditor.class, "dialog.indi.edit.title", indi));
        }
        bean.setContext(new Context(indi));
        return bean.showPanel();
    }

}
