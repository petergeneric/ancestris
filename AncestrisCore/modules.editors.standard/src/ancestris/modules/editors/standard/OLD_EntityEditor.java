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

import ancestris.api.editor.AncestrisEditor;
import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = AncestrisEditor.class,position = 100)
public class OLD_EntityEditor extends AncestrisEditor {

    public static Property editEntity(Fam fam, boolean isNew) {
        final FamPanel bean = new FamPanel();

        if (isNew) {
            bean.setTitle(NbBundle.getMessage(OLD_EntityEditor.class, "dialog.fam.new.title", fam));
        } else {
            bean.setTitle(NbBundle.getMessage(OLD_EntityEditor.class, "dialog.fam.edit.title", fam));
        }
        bean.setContext(new Context(fam));
        return bean.showPanel()?fam:null;
    }

    public static Property editEntity(Indi indi, boolean isNew) {
        final OLD_IndiPanel bean = new OLD_IndiPanel();
        if (indi == null) {
            return null;
        }

        if (isNew) {
            bean.setTitle(NbBundle.getMessage(OLD_EntityEditor.class, "dialog.indi.new.title", indi));
        } else {
            bean.setTitle(NbBundle.getMessage(OLD_EntityEditor.class, "dialog.indi.edit.title", indi));
        }
        bean.setContext(new Context(indi));
        return bean.showPanel()?indi:null;
    }

    @Override
    public boolean canEdit(Property property) {
        return (property instanceof Indi || property instanceof Fam);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public Property edit(Property property, boolean isNew) {
        if (property instanceof Fam) {
            return editEntity((Fam) property, isNew);
        }
        if (property instanceof Indi) {
            return editEntity((Indi) property, isNew);
        }
        return null;
    }

    @Override
    public Property add(Property parent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
