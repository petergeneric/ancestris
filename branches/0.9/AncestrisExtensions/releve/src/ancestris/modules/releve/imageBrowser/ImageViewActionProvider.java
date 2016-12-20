package ancestris.modules.releve.imageBrowser;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.AncestrisActionProvider;
import ancestris.gedcom.PropertyNode;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySource;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Action affichée dans le menu contextuel de tous les composants Ancestris
 * Cette action affiche la photo de l'acte associé à un évènement de l'individu courant
 * @author Michel
 */

@ServiceProvider(service=ancestris.core.actions.AncestrisActionProvider.class)
public class ImageViewActionProvider implements AncestrisActionProvider {
    
    static ImageIcon actionIcon = new javax.swing.ImageIcon(ImageViewActionProvider.class.getResource("/ancestris/modules/releve/images/Camera.png"));
    
    @Override
    public List<Action> getActions(boolean hasFocus, org.openide.nodes.Node[] nodes) {
        List<Action> actions = new ArrayList<Action>();
        
        if (BrowserOptionsPanel.getViewMenuVisible()) {
        
            if (nodes.length == 1) {
                if (nodes[0] instanceof PropertyNode) {
                    PropertyNode node = (PropertyNode) nodes[0];
                    Property property = node.getProperty();
                    // je cherche un ascendant de type Entity ou PropertyEvent en remontant 
                    // les parents de la propriété
                    while (property != null) {
                        if (property instanceof Entity) {
                            createAction((Entity) property, actions);
                            break;
                        } else if (property instanceof PropertyEvent) {
                            createAction((PropertyEvent) property, actions);
                            break;
                        } else {
                            property = property.getParent();
                        }
                    }
                }
            }
        }
        return actions;
    }
    
    
    private void createAction(Entity entity, List<Action> actionList) {
        List<PropertyEvent> propertyEventList = entity.getProperties(PropertyEvent.class); 
        
        for(PropertyEvent propertyEvent : propertyEventList  ) {
            createAction(propertyEvent, actionList);
        }        
    }
    
    private void createAction(PropertyEvent propertyEvent, List<Action> actionList) {
        // je recupere le lieu
        PropertyPlace propertyPlace = (PropertyPlace) propertyEvent.getProperty("PLAC");
        if (propertyPlace != null && propertyPlace.isValid()) {
            String city = propertyPlace.getCity();

            // je recupere la cote et la page
            Property[] sourceProperties = propertyEvent.getProperties("SOUR");
            for (Property sourcePropertie : sourceProperties) {
            // remarque : verification de classe PropertySource avant de faire le cast en PropertySource pour eliminer
                // les cas anormaux , par exemple une source "multiline"
                if (sourcePropertie instanceof PropertySource) {
                //Source source = (Source) ((PropertySource) sourceProperties[i]).getTargetEntity();
                    // je verifie si elle contient le meme numero de page ou la meme cote
                    Property pageProperty = sourcePropertie.getProperty("PAGE");
                    if (pageProperty != null && !pageProperty.getValue().isEmpty()) {
                        String[] pageSplit = pageProperty.getValue().split(",");
                        String cote;
                        String page;
                        if (pageSplit.length == 1) {
                            cote = "";
                            page = pageSplit[0].trim();
                        } else {
                            cote = pageSplit[0].trim();
                            page = pageSplit[1].trim();

                        }
                        Action action = createAction(propertyEvent.getTag(), city, cote, page);
                        actionList.add(action);
                    }
                }
            }
        }

    }
    
    
    
    private Action createAction(final String tag, final String city, final String cote, final String page) {
        AbstractAncestrisAction action = new AbstractAncestrisAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                System.out.println("ImageViewActionProvider.createAction tag=" + tag + " city=" + city + " cote=" + cote + " page=" + page);
                BrowserFrame.showEventImage(city, cote, page);
            }
        };

        action.setText(NbBundle.getMessage(BrowserOptionsPanel.class, "ImageViewActionProvider.actionLabel")+ ": "+ Gedcom.getName(tag));
        action.setImage(actionIcon);
        return action;
        
    }
    
    
}
