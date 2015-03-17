package ancestris.lnf.jtattoo;

import ancestris.api.lnf.LookAndFeelProvider;
import java.io.File;
import javax.swing.ImageIcon;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lemovice
 */
@ServiceProvider(position = 306, service = LookAndFeelProvider.class)
public class LnFJtattooHiFi extends LookAndFeelProvider {

    private static final String KEY = "lnf_JtattooHiFi";

    @Override
    public String getName() {
        return "com.jtattoo.plaf.hifi.HiFiLookAndFeel";
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(LnFJtattooSmart.class, KEY);
    }

    @Override
    public ImageIcon getSampleImage() {
        return new ImageIcon(getClass().getResource(KEY + ".png"));
    }

    @Override
    public String getClassPath() {
        File jar = InstalledFileLocator.getDefault().locate("modules/ext/JTattoo.jar", null, false);
        return jar == null ? null : jar.getAbsolutePath();
    }
}
