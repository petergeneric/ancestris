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
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author daniel
 */
public abstract class EditorPanel extends JPanel {
    private String title = null;
    private Context context;

    public void setContext(Context context){
        this.context = context;
    }

    public Context getContext(){
        return  context;
    }

    public abstract void commit();

    public String getTitle() {
        if (title == null)
            return context==null?"":context.getEntity().toString();
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean showPanel(){
        NotifyDescriptor nd = new NotifyDescriptor(new JScrollPane(this), getTitle(), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(nd);
        if (!nd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
            return false;
        }
        try {
            context.getGedcom().doUnitOfWork(new UnitOfWork() {

                public void perform(Gedcom gedcom) throws GedcomException {
                    commit();
                }
            });
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        return true;

    }
}
