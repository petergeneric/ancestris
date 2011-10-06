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
import javax.swing.JLabel;
import org.apache.commons.lang.WordUtils;

/**
 * Helper class for Beans
 * @author daniel
 */
public class BeanHelper {

    public static JLabel createTagLabel(PropertyBean bean, String tag) {
        return createTagLabel(bean, tag, null, 0);
    }

    public static JLabel createTagLabel(PropertyBean bean, String tag, String tip, int fontSize) {
        return createLabel(bean, Gedcom.getName(tag), formatToolTip(bean, tag, tip), fontSize);
    }

    public static JLabel createLabel(PropertyBean bean, String label, String tip, int fontSize) {
        JLabel jLabel = new JLabel(label);

        if (tip != null && tip.length() != 0) {
            jLabel.setToolTipText(formatToolTip(bean, null, tip));
        }
        if (fontSize != 0) {
            jLabel.setFont(new Font("DejaVu Sans", 0, fontSize)); // NOI18N
        }
        return jLabel;
    }

    public static String formatToolTip(PropertyBean bean, String tag, String tip){
        if (tag != null) {
            if (tip != null && tip.length() == 0) {
                tip = bean.RESOURCES.getString("HINT_" + tag, false); // NOI18N
            }
            if (tip == null) {
                tip = Gedcom.getInfo(tag);
            }
        }
        if (tip != null){
            if (!tip.startsWith("<html>")){
                tip = "<html>"+WordUtils.wrap(tip,40, "<br/>", false)+"</html>";
            }
        }
        return tip;
    }

}
