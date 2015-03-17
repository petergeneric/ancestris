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

import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import java.awt.event.ActionEvent;

/**
 *
 * @author daniel
 */
//XXX: write javadoc
public class JumpToEntityAction extends AbstractAncestrisAction {

    private Entity entity;

    public JumpToEntityAction(Entity entity) {
        this.entity = entity;
        setImage(entity.getImage());
        setText(entity.toString());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SelectionDispatcher.fireSelection(null, new Context(entity));
    }
}
