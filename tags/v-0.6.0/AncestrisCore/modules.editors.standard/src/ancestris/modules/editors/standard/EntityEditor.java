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
import ancestris.modules.editors.standard.actions.AActions;
import ancestris.modules.editors.standard.actions.ACreateChild;
import ancestris.modules.editors.standard.actions.ACreateParent;
import ancestris.modules.editors.standard.actions.ACreateSpouse;
import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import javax.swing.Action;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = AncestrisEditor.class)
public class EntityEditor extends AncestrisEditor {

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

    @Override
    public boolean canEdit(Property property) {
        return (property instanceof Indi || property instanceof Fam);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean edit(Property property, boolean isNew) {
        if (property instanceof Fam) {
            return editEntity((Fam) property, isNew);
        }
        if (property instanceof Indi) {
            return editEntity((Indi) property, isNew);
        }
        return false;
    }

    @Override
    public Action getCreateParentAction(Indi child, int sex) {
        return AActions.alwaysEnabled(
                new ACreateParent(child, sex, this),
                "",
                org.openide.util.NbBundle.getMessage(EntityEditor.class, "action.createparent.title"),
                "ancestris/modules/editors/standard/images/add-child.png", // NOI18N
                true);
    }

    @Override
    public Action getCreateChildAction(Indi indi) {
        return AActions.alwaysEnabled(
                new ACreateChild(indi, this),
                "",
                org.openide.util.NbBundle.getMessage(EntityEditor.class, "action.createchild.title", indi),
                "ancestris/modules/editors/standard/images/add-child.png", // NOI18N
                true);
    }

    @Override
    public Action getCreateSpouseAction(Indi indi) {
        return AActions.alwaysEnabled(
                new ACreateSpouse(indi, this),
                "",
                org.openide.util.NbBundle.getMessage(EntityEditor.class, "action.addspouse.title"),
                "ancestris/modules/editors/standard/images/add-spouse.png", // NOI18N
                true);
    }
}
