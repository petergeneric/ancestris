package ancestris.modules.commonAncestor;

import ancestris.core.actions.AbstractAncestrisContextAction;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Action pour creer une intance de TodoTopComponent.
 *
 * TodoOpenAction est associee au menu
 * ancestris-modules-todoreport-TodoOpenActionNew.shadow" dans layer.xml .
 *
 * Une instance de TodoOpenAction est cree par layer.xml au chargement de la
 * librairie (voir attribut delegate)
 *
 * @author Michel
 */
@ActionID(id = "ancestris.modules.commonAncestor.CommonAncestorOpenAction", category = "Tools")
@ActionRegistration(
        displayName = "#CTL_CommonAncestorAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Tools", name = "CommonAncestorOpenAction", position = 100)
public final class CommonAncestorOpenAction extends AbstractAncestrisContextAction {

    /**
     * contructeur par défaut requis par l'attribut "delegate" dans layer.xml
     */
    public CommonAncestorOpenAction() {
        super();
        setImage("ancestris/modules/commonAncestor/CommonAncestor.png");
        setText(NbBundle.getMessage(CommonAncestorOpenAction.class, "CTL_CommonAncestorAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent e) {
        Context context = getContext();
        if (context != null) {
            CommonAncestorTopComponent.createInstance(context);
        }
    }
}
