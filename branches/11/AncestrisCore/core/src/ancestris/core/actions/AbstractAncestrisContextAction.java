/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.core.actions;

import ancestris.gedcom.GedcomDirectory;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.SwingUtilities;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * ActionChange - change the gedcom information
 */
public abstract class AbstractAncestrisContextAction extends AbstractAncestrisAction
        implements LookupListener {

    /**
     * Lookup Context
     */
    protected Lookup context;
    /**
     * Lookup.Result to get properties from lookup for resultChange in default
     * implementation
     */
    protected Lookup.Result<Property> lkpInfo;
    /**
     * Properties in lookup
     */
    protected List<Property> contextProperties = new ArrayList<Property>(5);

    /**
     * Constructor
     */
    public AbstractAncestrisContextAction() {
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        this.context = Utilities.actionsGlobalContext();
    }

    /**
     * Constructor
     */
    public AbstractAncestrisContextAction(Gedcom ged, ImageIcon img, String text) {
        this();
        setImageText(img, text);
    }

    /**
     * Convenient shortcut
     *
     * @param img
     * @param text
     */
    public final void setImageText(ImageIcon img, String text) {
        super.setImage(img);
        super.setText(text);
        super.setTip(text);
    }

    public Gedcom getGedcom() {
        Gedcom gedcom = ancestris.util.Utilities.getGedcomFromContext(context);
        if (gedcom == null) {
            Context dc = getDefaultContext();
            if (dc != null) {
                gedcom = dc.getGedcom();
            }
        }
        return gedcom;
    }

    protected Context getDefaultContext() {
        List<Context> gedcontexts = GedcomDirectory.getDefault().getContexts();
        if (gedcontexts.size() >= 1) {
            return gedcontexts.get(0);
        }
        return null;
    }

    protected Context getContext() {
        if (contextProperties.isEmpty()) {
            return null;
        }
        Property p = contextProperties.get(0);
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(p.getEntity());

        return new Context(p.getGedcom(), entities, contextProperties);
    }

    /**
     * helper to set whole context
     *
     * @param props
     */
    protected void setContextProperties(Collection<? extends Property> props) {
        contextProperties.clear();
        contextProperties.addAll(props);
    }

    protected void setContextProperties(Property prop) {
        contextProperties.clear();
        contextProperties.add(prop);
    }

    @Override
    public boolean isEnabled() {
        initLookupListner();
        return super.isEnabled();
    }

    /**
     * Setup Lookup change listener on Property object. This is the default
     * implementation and may be overiden to listen to other object changes in
     * Lookup (ie Entity).
     */
    protected void initLookupListner() {
        assert SwingUtilities.isEventDispatchThread() : "this shall be called just from AWT thread";

        if (context == null) {
            return;
        }
        if (lkpInfo != null) {
            return;
        }

        //The thing we want to listen for the presence or absence of
        //on the global selection
        lkpInfo = context.lookupResult(Property.class);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }

    /**
     * callback for Lookup Result change. This can be overidden to get
     * properties from LookupResult on which this action should apply.
     * contextChanged is then called to change text, tip or image based on new
     * context.
     *
     * @param ev
     */
    @Override
    public void resultChanged(LookupEvent ev) {
        if (lkpInfo != null) {
            contextProperties.clear();
            contextProperties.addAll(lkpInfo.allInstances());
        }
        /* If contextProperties is empty (no appropriate context found 
         * in global lookup) we try to find a context based on gedcom files openned.
         * If there is only one gedcom openned, use it. That way, even if
         * A top component with no context is shown (eg welcome screen)
         * we can enable menu entries to increase useability.
         */
        if (contextProperties.isEmpty()) {
            Context dc = getDefaultContext();
            if (dc != null) {
                List<? extends Property> props = dc.getProperties();
                if (props.isEmpty()) {
                    props = dc.getEntities();
                }
                contextProperties.addAll(props);
            }
        }
        contextChanged();
    }

    /**
     * Called upon context change in Lookup to update text, tip or image
     * representation for this action
     */
    protected void contextChanged() {
    }

    /**
     * @see genj.util.swing.AbstractAncestrisAction#execute()
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        if (getGedcom() == null) {
            return;
        }

        initLookupListner();

        actionPerformedImpl(event);

        // Propagate changes in lookup too
        resultChanged(null);

        // done
    }

    protected abstract void actionPerformedImpl(final ActionEvent event);
}
