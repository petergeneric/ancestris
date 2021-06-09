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
import genj.gedcom.Property;
import genj.util.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * A base class for Ancestris Action (simplified Action2 Class).
 *
 * @author daniel
 */
//XXX: we must do some cleanup
public class AbstractAncestrisAction extends AbstractAction implements AncestrisAction {

    private final static String KEY_TEXT = Action.NAME,
            KEY_OLDTEXT = Action.NAME + ".old",
            KEY_SHORT_TEXT = "shortname",
            KEY_TIP = Action.SHORT_DESCRIPTION,
            KEY_ENABLED = "enabled",
            KEY_MNEMONIC = Action.MNEMONIC_KEY,
            KEY_ICON = Action.SMALL_ICON, KEY_SELECTED = "SwingSelectedKey"; // XXX: maybe we can remove this by using BooleanStateAction
    private final static Logger LOG = Logger.getLogger("ancestris.actions");
    /** predefined strings */
    public final static String TXT_YES = NbBundle.getMessage(GedcomDirectory.class, "cc.button.yes"),
            TXT_NO = NbBundle.getMessage(GedcomDirectory.class, "cc.button.no"),
            TXT_OK = NbBundle.getMessage(GedcomDirectory.class, "cc.button.ok"),
            TXT_CANCEL = NbBundle.getMessage(GedcomDirectory.class, "cc.button.cancel");

    /** constructor */
    public AbstractAncestrisAction() {
    }

    /** constructor */
    public AbstractAncestrisAction(String text) {
        setText(text);
    }

    /** constructor */
    public AbstractAncestrisAction(String text, Icon icon) {
        setText(text);
        setImage(icon);
    }

    /** default noop implementation of action invocation */
    @Override
    public void actionPerformed(ActionEvent e) {
    }

    /**
     * Intercepted super class getter - delegate calls to getters so they can be overridden. this
     * method is not supposed to be called from sub-types to avoid a potential endless-loop
     */
    @Override
    public Object getValue(String key) {
        if (KEY_TEXT.equals(key)) {
            return getText();
        }
        if (KEY_ICON.equals(key)) {
            return getImage();
        }
        if (KEY_TIP.equals(key)) {
            return getTip();
        }
        return super.getValue(key);
    }

    /**
     * accessor - image
     */
    @Override
    public final AncestrisAction setImage(Icon icon) {
        super.putValue(KEY_ICON, icon);
        return this;
    }

    @Override
    public final AncestrisAction setImage(String resource) {
        return setImage(new ImageIcon(ImageUtilities.loadImage(resource, true)));
    }
    

    /**
     * accessor - text
     */
    @Override
    public final AncestrisAction setText(String txt) {
        super.putValue(KEY_TEXT, txt);

        return this;
    }

    /**
     * accessor - text
     */
    @Override
    public String getText() {
        return (String) super.getValue(KEY_TEXT);
    }

    /**
     * accessor - tip
     */
    @Override
    public AbstractAncestrisAction setTip(String tip) {
        super.putValue(KEY_TIP, tip);
        return this;
    }

    /**
     * accessor - tip
     */
    @Override
    public String getTip() {
        return (String) super.getValue(KEY_TIP);
    }

    /**
     * accessor - image
     */
    @Override
    public Icon getImage() {
        return (Icon) super.getValue(KEY_ICON);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem m;
        m = new Actions.MenuItem(this, true);
        m.setToolTipText(getTip());
        return m;
    }

//  public AbstractAncestrisAction install(JComponent component, String shortcut) {
//    return install(component,shortcut,JComponent.WHEN_IN_FOCUSED_WINDOW);
//  }
//  
//  public AbstractAncestrisAction install(JComponent component, String shortcut, int condition) {
//    InputMap inputs = component.getInputMap(condition);
//    inputs.put(KeyStroke.getKeyStroke(shortcut), this);
//    component.getActionMap().put(this, this);
//    return this;
//  }
//  
//  public static void uninstall(JComponent component, String shortcut) {
//    uninstall(component, component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), shortcut);
//    uninstall(component, component.getInputMap(JComponent.WHEN_FOCUSED), shortcut);
//    uninstall(component, component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT), shortcut);
//  }
//  
//  private static void uninstall(JComponent component, InputMap map, String shortcut) {
//    KeyStroke key = KeyStroke.getKeyStroke(shortcut);
//    Object o = map.get(key);
//    if (o instanceof AbstractAncestrisAction) {
//      map.put(key, null);
//      component.getActionMap().remove(o);
//    }
//  }
//
 // XXX: maybe we can remove this by using BooleanStateAction
    @Override
    public boolean isSelected() {
        return Boolean.TRUE.equals((Boolean) getValue(KEY_SELECTED));
    }

    @Override
    public boolean setSelected(boolean selected) {
        boolean old = isSelected();
        putValue(KEY_SELECTED, selected ? Boolean.TRUE : Boolean.FALSE);
        return old;
    }

    @Override
    public boolean isDefault(Property prop) {
        return false;
    }
}
