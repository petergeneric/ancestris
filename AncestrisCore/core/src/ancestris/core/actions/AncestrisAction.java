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

    /**
     * accessor - image
     */
    AncestrisAction setImage(Icon icon);

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
