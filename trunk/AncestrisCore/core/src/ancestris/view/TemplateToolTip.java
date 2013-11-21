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
package ancestris.view;

import ancestris.renderer.Renderer;
import genj.gedcom.Entity;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.LookAndFeel;
import javax.swing.ToolTipManager;
import javax.swing.plaf.ToolTipUI;
import javax.swing.text.html.HTMLDocument;
import org.openide.util.Exceptions;

/**
 * A rendering component showing the currently selected entity
 * via html
 */
public class TemplateToolTip extends JToolTip {

    private Entity entity;
    private final JEditorPane theEditorPane;
    private final JScrollPane pane;
    private int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
    private final static int dismissDelayMinutes = (int) TimeUnit.MINUTES.toMillis(10); // 10 minutes

    @Override
    public void addNotify() {
        super.addNotify();
        defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
        ToolTipManager.sharedInstance().setDismissDelay(dismissDelayMinutes);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
    }

    /**
     * Constructor
     */
    public TemplateToolTip() {
        super();
        setLayout(new BorderLayout());
        LookAndFeel.installBorder(this, "ToolTip.border");
        LookAndFeel.installColors(this, "ToolTip.background", "ToolTip.foreground");
        theEditorPane = new JEditorPane();
        theEditorPane.setContentType("text/html");
        theEditorPane.setEditable(false);
        pane = new JScrollPane(theEditorPane);
        add(pane);
    }
    Entity prevEntity = null;
    String prevTT = null;

    @Override
    public void setTipText(String tipText) {
        if (tipText != null && !tipText.equals(prevTT)) {
            String TPL = "ancestris/templates/" + entity.getTag() + "/popup";
            Renderer render = Renderer.Lookup.lookup(TPL);
            render.put("INDI", entity);
            render.put("FAM", entity);
            StringWriter w = new StringWriter();
            render.render(TPL, w);
            w.flush();
            String tt = w.toString();
            try {
                w.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            theEditorPane.setText(tt);
            prevTT = tipText;
        }
        try {
            if (entity != null) {
                ((HTMLDocument) theEditorPane.getDocument()).setBase(
                        new URL(entity.getGedcom().getOrigin().toString()));
            }
        } catch (MalformedURLException ex) {
        }
        theEditorPane.setCaretPosition(0);
        super.setTipText(tipText);
    }

    @Override
    public void updateUI() {
        setUI(new ToolTipUI() {
        });
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 400);
    }
}
