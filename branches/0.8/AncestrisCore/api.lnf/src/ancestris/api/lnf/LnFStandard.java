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

package ancestris.api.lnf;

import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(position=0,service=LookAndFeelProvider.class)
public class LnFStandard extends  LookAndFeelProvider{

    private static final String KEY = "lnf_Standard";
    private static LnFStandard instance;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(LnFStandard.class, KEY);
    }

    @Override
    public ImageIcon getSampleImage() {
        return new ImageIcon(getClass().getResource(KEY + ".png"));
    }
    public static LookAndFeelProvider getDefault(){
        if (instance == null){
            instance = new LnFStandard();
        }
        return instance;
    }
}
