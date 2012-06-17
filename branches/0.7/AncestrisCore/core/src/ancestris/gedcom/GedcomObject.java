/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.gedcom;

import genj.app.Workbench;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.io.IOException;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author daniel
 */
public class GedcomObject {

    private Context context;
    private DummyNode dummyNode;
    private GedcomUndoRedo undoredo;

    GedcomObject(Context context) {
        this.context = context;
        dummyNode = new DummyNode();
        undoredo = new GedcomUndoRedo((context.getGedcom()));
    }

    public DummyNode getDummyNode() {
        return dummyNode;
    }

    public GedcomUndoRedo getUndoRedo() {
        return undoredo;
    }

    boolean hasSameGedcom(Context that) {
        return hasSameGedcom(that.getGedcom());
    }

    boolean hasSameGedcom(Gedcom that) {
        return context.getGedcom().equals(that);
    }

    void setContext(Context context) {
        this.context = context;
    }

    Context getContext() {
        return context;
    }

    public class DummyNode extends AbstractNode {

        private SaveCookieImpl saveImpl;

        public DummyNode() {
            super(Children.LEAF);
            saveImpl = new SaveCookieImpl();
        }

        @Override
        public String getDisplayName() {
            return getContext().getGedcom().getName();
        }

        private class SaveCookieImpl implements SaveCookie {

            @Override
            public void save() throws IOException {
                Workbench.getInstance().saveGedcom(getContext());
                fire(getContext().getGedcom().hasChanged());
            }
        }

        public void fire(boolean modified) {
            if (modified) {
                getCookieSet().assign(SaveCookie.class, saveImpl);
            } else {
                getCookieSet().assign(SaveCookie.class);
            }
        }
    }
}
