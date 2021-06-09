/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.core.actions;

import genj.gedcom.Property;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.util.actions.Presenter;

/**
 *FIXME: we must document this interface
 * @author daniel
 */
public interface AncestrisAction extends Action, Presenter.Popup {

    /**
     * accessor - image
     */
    Icon getImage();

    /**
     * accessor - text
     */
    String getText();

    /**
     * accessor - tip
     */
    String getTip();

    boolean isSelected();

    boolean isDefault(Property prop);
    
    /**
     * accessor - image
     */
    AncestrisAction setImage(Icon icon);
    
    /**
     * Set Icon from resource path.
     * @param resource resource path. This resource path is used to fond, possibly
     * localized image in classpath. See also {@link ImageUtilities.loadImage}
     * @return 
     */
    AncestrisAction setImage(String resource);

    boolean setSelected(boolean selected);

    /**
     * accessor - text
     */
    AncestrisAction setText(String txt);

    /**
     * accessor - tip
     */
    AncestrisAction setTip(String tip);
    
}
