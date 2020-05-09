/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Zurga (zurga@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * TextArea with undo/redo capabilities.
 *
 * @author Zurga
 */
public class UndoTextArea extends JTextArea implements UndoableEditListener, FocusListener,
        KeyListener {

    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    private UndoManager myUndoManager;

    public UndoTextArea() {
        this(new String());
    }

    public UndoTextArea(String newText) {
        super(newText);
        getDocument().addUndoableEditListener(this);
        this.addKeyListener(this);
        this.addFocusListener(this);
    }

    private void createUndoMananger() {
        myUndoManager = new UndoManager();
        myUndoManager.setLimit(10);
    }

    private void removeUndoMananger() {
        myUndoManager.end();
    }

    @Override
    public void focusGained(FocusEvent fe) {
        createUndoMananger();
    }

    @Override
    public void focusLost(FocusEvent fe) {
        removeUndoMananger();
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        if (myUndoManager != null) {
            myUndoManager.addEdit(e.getEdit());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_Z) && (e.isControlDown())) {
            try {
                myUndoManager.undo();
            } catch (CannotUndoException cue) {
                LOG.log(Level.FINE, "Nothing to undo", cue);
            }
        }

        if ((e.getKeyCode() == KeyEvent.VK_Y) && (e.isControlDown())) {
            try {
                myUndoManager.redo();
            } catch (CannotRedoException cue) {
                LOG.log(Level.FINE, "Nothing to redo", cue);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
