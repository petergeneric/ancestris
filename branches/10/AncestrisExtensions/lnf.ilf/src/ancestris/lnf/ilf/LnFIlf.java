package ancestris.lnf.ilf;

import ancestris.api.lnf.LookAndFeelProvider;
import java.io.File;
import javax.swing.ImageIcon;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author francois
 */
@ServiceProvider(position = 290, service = LookAndFeelProvider.class)
public class LnFIlf extends LookAndFeelProvider {

    private static final String KEY = "lnf_ilf";

    @Override
    public String getName() {
        return "net.infonode.gui.laf.InfoNodeLookAndFeel";
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(LnFIlf.class, KEY);
    }

    @Override
    public ImageIcon getSampleImage() {
        return new ImageIcon(getClass().getResource(KEY + ".png"));
    }

    @Override
    public String getClassPath() {
        File jar = InstalledFileLocator.getDefault().locate("modules/ext/ilf-gpl.jar", null, false);
        return jar == null ? null : jar.getAbsolutePath();
    }
}
