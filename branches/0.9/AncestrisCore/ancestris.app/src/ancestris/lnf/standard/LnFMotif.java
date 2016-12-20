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

package ancestris.lnf.standard;

import ancestris.api.lnf.LookAndFeelProvider;
import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(position=200,service=LookAndFeelProvider.class)
public class LnFMotif extends LookAndFeelProvider{

    private static final String KEY = "lnf_Motif";

    @Override
    public String getName() {
        return MotifLookAndFeel.class.getCanonicalName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(LnFMotif.class, KEY);
    }

    @Override
    public ImageIcon getSampleImage() {
        return new ImageIcon(getClass().getResource(KEY + ".png"));
    }

}
