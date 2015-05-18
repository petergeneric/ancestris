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
package ancestris.modules.document.view;

import genj.gedcom.Context;
import javax.swing.JComponent;

/**
 * Document view class to display any JComponent in DocumentViewTopComponent window.
 *
 * @author daniel
 */
public class WidgetDocumentView extends AbstractDocumentView {

    /**
     * Create and register the view.
     *
     * @param context Gedcom context for this view
     * @param title   tab title
     * @param tooltip tooltip for tab
     * @param widget  component to be displayed. As any {@link AbstractDocumentView}
     * the component is embedded in a JScollPane.
     */
    public WidgetDocumentView(Context context, String title, String tooltip, JComponent widget) {
        super(context, title, tooltip);
        setView(widget);
    }
}
