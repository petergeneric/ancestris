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
package genj.edit.beans;

import genj.gedcom.Gedcom;
import java.awt.Font;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Helper class for Beans
 * @author daniel
 */
public class BeanHelper {

    public static JLabel createTagLabel(String tag) {
        return createTagLabel(tag, null, 0);
    }

    public static JLabel createTagLabel(String tag, String tip, int fontSize) {
        return createLabel(Gedcom.getName(tag), formatToolTip(tag, tip), fontSize);
    }

    public static JLabel createLabel(String label, String tip, int fontSize) {
        JLabel jLabel = new JLabel(label);

        if (tip != null && tip.length() != 0) {
            jLabel.setToolTipText(formatToolTip(null, tip));
        }
        if (fontSize != 0) {
            jLabel.setFont(new Font("DejaVu Sans", 0, fontSize)); // NOI18N
        }
        return jLabel;
    }

    public static String formatToolTip(String tag, String tip) {
        if (tag != null) {
            if (tip != null && tip.length() == 0) {
                tip = PropertyBean.RESOURCES.getString("HINT_" + tag, false); // NOI18N
            }
            if (tip == null) {
                tip = Gedcom.getInfo(tag);
            }
        }
        if (tip != null) {
            if (!tip.startsWith("<html>")) {
                // return text wrapped to 200 pixels
                tip = "<html><table width=200><tr><td>" + tip + "</td></tr></table></html";
            }
        }
        return tip;
    }

    /**
     * display get a checkbox to show/hide a JComponent
     * @param title
     * @param component
     * @return
     */
    public static JCheckBox createShowHide(
            String title,
            String tip,
            final JComponent component) {
        final JCheckBox jcb = new JCheckBox();

        jcb.setFont(new Font("DejaVu Sans", 0, 10)); // NOI18N
        jcb.setText(title);
        jcb.setToolTipText(formatToolTip(null, tip));
        jcb.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jcb.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                component.setVisible(jcb.isSelected());
            }
        });
        return jcb;
    }
}
