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

import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.util.swing.EditorHyperlinkSupport;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;

/**
 *
 * @author daniel
 */
/**
 * Text panel to handle text output with entity links enabled
 */
class HyperLinkTextPane extends JEditorPane implements MouseListener, MouseMotionListener {

    /** the currently found entity id */
    private String id = null;
    private Gedcom gedcom = null;

    /** constructor */
    public HyperLinkTextPane() {
        setContentType("text/plain");
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        setEditable(false);
        addHyperlinkListener(new EditorHyperlinkSupport(this));
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    /**
     * Check if user moves mouse above something recognizeable in output
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        // try to find id at location
        id = markIDat(e.getPoint());
        // done
    }

    /**
     * Check if user clicks on marked ID
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (id != null && gedcom != null) {
            Entity entity = gedcom.getEntity(id);
            if (entity != null) {
                SelectionDispatcher.fireSelection(e, new Context(entity));
            }
        }
    }

    /**
     * Tries to find an entity id at given position in output
     */
    private String markIDat(Point loc) {
        try {
            // do we get a position in the model?
            int pos = viewToModel(loc);
            if (pos < 0) {
                return null;
            }
            // scan doc
            javax.swing.text.Document doc = getDocument();
            // find ' ' to the left
            for (int i = 0;; i++) {
                // stop looking after 10
                if (i == 10) {
                    return null;
                }
                // check for starting line or non digit/character
                if (pos == 0 || !Character.isLetterOrDigit(doc.getText(pos - 1, 1).charAt(0))) {
                    break;
                }
                // continue
                pos--;
            }
            // find ' ' to the right
            int len = 0;
            while (true) {
                // stop looking after 10
                if (len == 10) {
                    return null;
                }
                // stop at end of doc
                if (pos + len == doc.getLength()) {
                    break;
                }
                // or non digit/character
                if (!Character.isLetterOrDigit(doc.getText(pos + len, 1).charAt(0))) {
                    break;
                }
                // continue
                len++;
            }
            // check if it's an ID
            if (len < 2) {
                return null;
            }
            String _id = doc.getText(pos, len);
            if (gedcom == null || gedcom.getEntity(_id) == null) {
                return null;
            }
            // mark it
            // requestFocusInWindow();
            setCaretPosition(pos);
            moveCaretPosition(pos + len);
            // return in between
            return _id;
            // done
        } catch (BadLocationException ble) {
        }
        // not found
        return null;
    }

    /**
     * have to implement MouseMotionListener.mouseDragger()
     *
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        // ignored
    }

    /**
     * clear and reset View content
     */
    public void clear() {
        setContentType("text/plain");
        setText("");
    }

    /**
     * Add text to the Panel View
     *
     * @param txt the text to be added
     */
    public void add(String txt) {
        javax.swing.text.Document doc = getDocument();
        try {
            doc.insertString(doc.getLength(), txt, null);
        } catch (Throwable t) {
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    public void setGedcom(Gedcom gedcom) {
        this.gedcom = gedcom;
    }
} // Output
