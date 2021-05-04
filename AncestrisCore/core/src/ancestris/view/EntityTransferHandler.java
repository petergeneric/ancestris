/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2021 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.view;

import ancestris.core.TextOptions;
import ancestris.core.actions.CommonActions;
import ancestris.core.actions.DNDAction;
import ancestris.core.resources.Images;
import ancestris.gedcom.PropertyNode;
import ancestris.util.GedcomUtilities;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.MergeEntityPanel;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.Submitter;
import genj.gedcom.time.Delta;
import genj.io.PropertyTransferable;
import genj.util.swing.ImageIcon;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.COPY;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import swingx.dnd.ObjectTransferable;

/**
 *
 * @author frederic
 */
public class EntityTransferHandler extends TransferHandler {

    private Entity sourceEntity = null;
    private Entity newEntity = null;

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    public void setEntity(Entity entity) {
        this.sourceEntity = entity;
    }

    @Override
    public Transferable createTransferable(JComponent c) {
        return sourceEntity != null ? new ObjectTransferable(sourceEntity) : null;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        support.setDropAction(COPY);

        Entity importedEntity = getSourceEntity(support.getTransferable());
        Property targetProperty = getPropertyFromTargetComponent(support.getComponent(), support.getDropLocation().getDropPoint());
        Entity targetEntity = targetProperty != null ? targetProperty.getEntity() : null;

        Transferable t = support.getTransferable();
        if (t.isDataFlavorSupported(DelegatedTransferable.DELEGATED_FLAVOR) && (targetEntity != null)) {
            return support.getComponent() instanceof JComponent;
        }
        
        boolean isSameEntity = importedEntity == targetEntity;
        boolean isIndi = importedEntity instanceof Indi;
        boolean isFam = importedEntity instanceof Fam;
        boolean isSubm = importedEntity instanceof Submitter;
        boolean isTargetIndi = targetEntity instanceof Indi;
        boolean isTargetFam = targetEntity instanceof Fam;
        boolean isTargetSubm = targetEntity instanceof Submitter;
        boolean isAttribute = !isIndi && !isFam && !isSubm;
        boolean isTargetAttribute = !isTargetIndi && !isTargetFam && ! isTargetSubm;
        boolean isSameGedcom = importedEntity != null && targetEntity != null ? importedEntity.getGedcom().compareTo(targetEntity.getGedcom()) == 0 : false;
        boolean isSameEntityType = importedEntity != null && targetEntity != null && importedEntity.getTag().equals(targetEntity.getTag());

        String importedTag = importedEntity != null ? importedEntity.getTag() : "";
        
        boolean allowed =  (isIndi && isTargetIndi) 
                        || (isIndi && isTargetFam) 
                        || (isFam && isTargetIndi) 
                        || (isFam && isTargetFam) 
                        || !isSameGedcom 
                        || (isSameGedcom && isSameEntityType)
                        || isAttribute;
        
        boolean refused =  isSameEntity
                        || importedTag.isEmpty()
                        || (isSameGedcom && (isIndi || isFam) && (isTargetAttribute || isTargetSubm))
                        || (isSameGedcom && isAttribute && !isSameEntityType && !targetEntity.getMetaProperty().allows(importedTag))
                        || (isSameGedcom && isSubm && !isSameEntityType);
        
        return support.getComponent() instanceof JComponent && allowed && !refused;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {

        // Get all the key data elements
        Transferable t = support.getTransferable();
        Entity importedEntity = getSourceEntity(t);
        Gedcom importedGedcom = importedEntity != null ? importedEntity.getGedcom() : null;
        Property targetProperty = getPropertyFromTargetComponent(support.getComponent(), support.getDropLocation().getDropPoint());
        Entity targetEntity = targetProperty != null ? targetProperty.getEntity() : null;
        Gedcom targetGedcom = targetEntity != null ? targetEntity.getGedcom() : null;
        TopComponent tc = getTopComponentFromTargetComponent(support.getComponent());

        // If external DND, delegate action to external module
        if (t.isDataFlavorSupported(DelegatedTransferable.DELEGATED_FLAVOR)) {
            boolean result = false;
            try {
                DelegatedTransferable dt = (DelegatedTransferable) t.getTransferData(DelegatedTransferable.DELEGATED_FLAVOR);
                result = dt.runDelegation(tc, targetGedcom, targetEntity, support);
            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return result;
        }

        // Else manage import from here
        try {
            if (importedEntity != null && targetEntity != null) {
                return manageTransfer(importedEntity, importedGedcom, targetProperty, targetEntity, targetGedcom, tc, support.getComponent(), support.getDropLocation().getDropPoint());
            }
        } catch (Exception e) {
                Exceptions.printStackTrace(e);
        }
        
        return false;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
    }

    private Property getPropertyFromTargetComponent(Component targetComponent, Point point) {
        
        
        // First try with PropertyProvider available in the ancestors of target component
        Property property = PropertyProvider.getPropertyFromComponent(targetComponent, point);
        if (property != null) {
            return property;
        }
        
        // No PropertyProvider found, then default to context
        ExplorerManager em = ExplorerHelper.lookupExplorerManager(targetComponent);
        property = ExplorerHelper.getPropertyFromNodes(em.getSelectedNodes());
        Component parentComponent = targetComponent.getParent();
        while (property == null && parentComponent != null) {
            em = ExplorerHelper.lookupExplorerManager(parentComponent);
            property = ExplorerHelper.getPropertyFromNodes(em.getSelectedNodes());
            parentComponent = parentComponent.getParent();
        }
        return property;
    }

    private TopComponent getTopComponentFromTargetComponent(Component targetComponent) {
        Component parentComponent = targetComponent.getParent();
        while (parentComponent != null && !(parentComponent instanceof TopComponent)) {
            parentComponent = parentComponent.getParent();
        }
        if ((parentComponent instanceof TopComponent)) {
            return (TopComponent) parentComponent;
        }
        return null;
    }

    private Entity getSourceEntity(Transferable t) {
        Entity entity = null;
        if (t != null) {
            DataFlavor[] dfs = t.getTransferDataFlavors();
            String mimeType = "";
            if (dfs.length > 0) {
                mimeType = dfs[0].getMimeType();
            }

            // Check if coming from Gedcom editor
            if (t.isDataFlavorSupported(PropertyTransferable.VMLOCAL_FLAVOR)) {
                try {
                    List<Property> props = (List<Property>) t.getTransferData(PropertyTransferable.VMLOCAL_FLAVOR);
                    for (Property prop : props) {
                        entity = prop.getEntity();
                        break;
                    }
                } catch (UnsupportedFlavorException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

                // Check if coming from Gedcom explorer
            } else if ("application/x-java-openide-nodednd; mask=1; class=org.openide.nodes.Node".equals(mimeType)) {
                Node node = NodeTransfer.node(t, NodeTransfer.DND_COPY_OR_MOVE);
                if (node instanceof PropertyNode) {
                    entity = ((PropertyNode) node).getProperty().getEntity();
                }

                // Check if coming from another TopComponent
            } else if (t.isDataFlavorSupported(ObjectTransferable.localFlavor)) {
                try {
                    Object o = ObjectTransferable.getObject(t);
                    if (o instanceof Entity) {
                        entity = (Entity) o;
                    }
                } catch (UnsupportedFlavorException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return entity;
    }

    /**
     * Manage Transfer of entity
     * 
     * @param importedEntity
     * @param importedGedcom
     * @param targetProperty
     * @param targetEntity
     * @param targetGedcom
     * @param tc
     * @param droppedComponent
     * @param droppedPoint
     * @return 
     */
    
    private static int REL_PARENT = 0; 
    private static int REL_SPOUSE = 1; 
    private static int REL_SIBLING = 2; 
    private static int REL_CHILD = 3; 
    
    private boolean manageTransfer(final Entity importedEntity, final Gedcom importedGedcom, Property targetProperty, Entity targetEntity, Gedcom targetGedcom, 
            TopComponent tc, Component droppedComponent, Point droppedPoint) {

        /**
         * Create Contextual DND popup Menu to get the chosen user action among most relevant possible actions
         * Structure is rather constant so build it directly as list of predefined actions
         * The dynalic part lies on the links which are disabled or not based on situation
         * 
         * Our list of possible actions: Three types of actions : Link (parent, spouse, sibling, child), Create, Merge
         */
        
        // Get criteria
        boolean isIndi = importedEntity instanceof Indi;
        boolean isFam = importedEntity instanceof Fam;
        boolean isSubm = importedEntity instanceof Submitter;
        boolean isTargetIndi = targetEntity instanceof Indi;
        boolean isTargetFam = targetEntity instanceof Fam;
        boolean isAttribute = !isIndi && !isFam && !isSubm;
        boolean isSameGedcom = importedEntity.getGedcom().compareTo(targetEntity.getGedcom()) == 0;
        boolean isSameEntityType = importedEntity.getTag().equals(targetEntity.getTag());

        // Use short names for entities in action names
        Indi importedIndi = isIndi ? (Indi) importedEntity : null;
        Indi targetIndi = isTargetIndi ? (Indi) targetEntity : null; 
        Fam importedFamily = isFam ? (Fam) importedEntity : null;
        Fam targetFamily = isTargetFam ? (Fam) targetEntity : null;

        String name1 = ""; 
        if (isIndi) {
            name1 = importedIndi.getFirstName();
        } else if (isFam) {
            name1 = getFamilyName(importedFamily);
        } else {
            name1 = importedEntity.getDisplayTitle();
        }

        String name2 = "";
        if (isTargetIndi) {
            name2 = targetIndi.getFirstName();
        } else if (isFam) {
            name2 = getFamilyName(targetFamily);
        } else {
            name2 = targetEntity.getDisplayTitle();
        }


        // For Indi, description depends on sex
        int sex = isIndi ? ((Indi)importedEntity).getSex() : -1;
        boolean isMale = sex == PropertySex.MALE || sex == PropertySex.UNKNOWN;
        boolean isFemale = sex == PropertySex.FEMALE || sex == PropertySex.UNKNOWN;
        

        // Title
        List<Action> actions = new ArrayList<>();
        actions.add(CommonActions.createTitleAction(CommonActions.TYPE_DND_MENU, targetProperty, importedGedcom, importedEntity, targetGedcom, targetEntity));
        JPopupMenu popup = Utilities.actionsToPopup(actions.toArray(new Action[0]), droppedComponent);

        // Case of 1 indi moved to another indi
        if (isIndi && isTargetIndi) {

            // Ensure names are different
            if (name1.equals(name2)) {
                name1 = importedIndi.getName();
                name2 = targetIndi.getName();
            }
            if (name1.equals(name2)) {
                name1 = importedIndi.getDisplayTitle(true);
                name2 = targetIndi.getDisplayTitle(true);
            }
            
            Fam[] targetParentFams = targetIndi.getFamiliesWhereChild();
            Fam[] targetSpouseFams = targetIndi.getFamiliesWhereSpouse(true);

            // Link actions Parents (a person can be unknown, hence it can be either male or female)
            if (isMale) {
                create_Indi_Indi_Actions(popup, name1, name2, "FATHER_OF", "IndiMale", targetParentFams, importedIndi, targetIndi, REL_PARENT);
            }
            if (isFemale) {
                create_Indi_Indi_Actions(popup, name1, name2, "MOTHER_OF", "IndiFemale", targetParentFams, importedIndi, targetIndi, REL_PARENT);
            }
            popup.addSeparator();

            // Link actions Spouse
            if (isMale) {
                create_Indi_Indi_Actions(popup, name1, name2, "HUSBAND_OF", "Marriage", targetSpouseFams, importedIndi, targetIndi, REL_SPOUSE);
            }
            if (isFemale) {
                create_Indi_Indi_Actions(popup, name1, name2, "WIFE_OF", "Marriage", targetSpouseFams, importedIndi, targetIndi, REL_SPOUSE);
            }
            popup.addSeparator();

            // Link actions Sibling
            if (isMale) {
                create_Indi_Indi_Actions(popup, name1, name2, "BROTHER_OF", "Child", targetParentFams, importedIndi, targetIndi, REL_SIBLING);
            }
            if (isFemale) {
                create_Indi_Indi_Actions(popup, name1, name2, "SISTER_OF", "Child", targetParentFams, importedIndi, targetIndi, REL_SIBLING);
            }
            popup.addSeparator();

            // Link actions Child
            if (isMale) {
                create_Indi_Indi_Actions(popup, name1, name2, "SON_OF", "Birth", targetSpouseFams, importedIndi, targetIndi, REL_CHILD);
            }
            if (isFemale) {
                create_Indi_Indi_Actions(popup, name1, name2, "DAUGHTER_OF", "Birth", targetSpouseFams, importedIndi, targetIndi, REL_CHILD);
            }
            popup.addSeparator();
        }
        
        
        // Case of 1 indi moved to 1 fam
        if (isIndi && isTargetFam) {
            
            // Link actions Spouse
            if (isMale) {
                create_Indi_Fam_Actions(popup, name1, name2, "HUSBAND_IN", "Marriage", importedIndi, targetFamily, REL_SPOUSE);
            }
            if (isFemale) {
                create_Indi_Fam_Actions(popup, name1, name2, "WIFE_IN", "Marriage", importedIndi, targetFamily, REL_SPOUSE);
            }
            
            popup.addSeparator();

            // Link actions Sibling
            if (isMale) {
                create_Indi_Fam_Actions(popup, name1, name2, "SON_IN", "Child", importedIndi, targetFamily, REL_CHILD);
            }
            if (isFemale) {
                create_Indi_Fam_Actions(popup, name1, name2, "DAUGHTER_IN", "Child", importedIndi, targetFamily, REL_CHILD);
            }
            
            if (!isSameGedcom) {
                popup.addSeparator();
            }

        }
        
        // Case of 1 fam moved to 1 indi
        if (isFam && isTargetIndi) {

            // Sex shoud be the one of the target Indi in this case
            sex = targetIndi.getSex();
            isMale = sex == PropertySex.MALE || sex == PropertySex.UNKNOWN;
            isFemale = sex == PropertySex.FEMALE || sex == PropertySex.UNKNOWN;
            
            // Link actions Spouse
            if (isMale) {
                create_Fam_Indi_Actions(popup, name1, name2, isSameGedcom ? "HUSBAND_IN" : "COPY_HUSBAND_IN", "Marriage", importedFamily, targetIndi, REL_SPOUSE);
            }
            if (isFemale) {
                create_Fam_Indi_Actions(popup, name1, name2, isSameGedcom ? "WIFE_IN" : "COPY_WIFE_IN", "Marriage", importedFamily, targetIndi, REL_SPOUSE);
            }
            
            popup.addSeparator();

            // Link actions Sibling
            if (isMale) {
                create_Fam_Indi_Actions(popup, name1, name2, isSameGedcom ? "SON_IN" : "COPY_SON_IN", "Child", importedFamily, targetIndi, REL_CHILD);
            }
            if (isFemale) {
                create_Fam_Indi_Actions(popup, name1, name2, isSameGedcom ? "DAUGHTER_IN" : "COPY_DAUGHTER_IN", "Child", importedFamily, targetIndi, REL_CHILD);
            }
            
            if (isSameGedcom && isSameEntityType || !isSameGedcom) {
                popup.addSeparator();
            }

        }
        
        
        // Case of one attribute moved to something
        if (isAttribute) {

            String importedTag = importedEntity.getTag();
            
            if (isTargetIndi || isTargetFam) {
                create_Attr_Events_Actions(popup, name1, name2, "ATTACH_TO_EVENT", Gedcom.getEntityImage(importedEntity.getTag()), importedEntity, targetEntity);
                
            } else if (targetEntity.getMetaProperty().allows(importedTag)) {
                String pName = importedEntity.getPropertyName().toLowerCase();
                boolean isError = false;
                popup.add(new DNDAction(isError, NbBundle.getMessage(DNDAction.class, "ATTACH_ENTITY", pName, name1, name2), Gedcom.getEntityImage(importedEntity.getTag())) {
                    @Override
                    public Entity dropActionPerformed(ActionEvent e) {
                        return GedcomUtilities.attach(importedEntity, targetEntity);
                    }
                });
            }
            
            if ((isSameGedcom && isSameEntityType) || !isSameGedcom) {
                popup.addSeparator();
            }

        }



        // Copy actions 
        if (!isSameGedcom) {

            boolean isWarning = false;
            String tooltip = "";
            ImageIcon icon = Gedcom.getEntityImage(importedEntity.getTag());
            String pName = importedEntity.getPropertyName().toLowerCase();

            // Issue warning if targetEntity already exists (name is the same) in target Gedcom
            if (existEntity(targetGedcom, importedEntity)) {
                isWarning = true;
                tooltip = NbBundle.getMessage(DNDAction.class, "REL_ENTITY_ALREADY_EXITS", name1, targetGedcom.getDisplayName());
            }
            
            String nameAction = NbBundle.getMessage(DNDAction.class, "COPY_ENTITY", pName, name1, targetGedcom.getDisplayName());
            JMenuItem mi = popup.add(new DNDAction(false, nameAction, isWarning ? icon.getOverLayed(Images.imgWngOver) : icon) {
                @Override
                public Entity dropActionPerformed(ActionEvent e) {
                    return GedcomUtilities.copyEntity(importedEntity, targetGedcom, false);  // dry copy
                }
            });
            if (tooltip != null && !tooltip.isEmpty()) {
                mi.setToolTipText(tooltip);
            }

            nameAction = NbBundle.getMessage(DNDAction.class, "COPY_ENTITY_FULL", pName, name1, targetGedcom.getDisplayName());
            if (!isWarning) {
                tooltip = NbBundle.getMessage(DNDAction.class, "COPY_ENTITY_FULL_TIP");
            }
            mi = popup.add(new DNDAction(false, nameAction, isWarning ? icon.getOverLayed(MetaProperty.IMG_LINK).getOverLayed(Images.imgWngOver) : icon) {
                @Override
                public Entity dropActionPerformed(ActionEvent e) {
                    return GedcomUtilities.copyEntity(importedEntity, targetGedcom, true);  // rich copy
                }
            });
            if (tooltip != null && !tooltip.isEmpty()) {
                mi.setToolTipText(tooltip);
            }
        }

        
        // Merge action 
        if (isSameGedcom && isSameEntityType) {
            
            boolean isWarning = false;
            String tooltip = "";
            ImageIcon icon = Gedcom.getEntityImage(importedEntity.getTag());
            String pName = importedEntity.getPropertyName().toLowerCase();
            
            if (isIndi) {
                int targetSex = ((Indi)targetEntity).getSex();
                if (sex != PropertySex.UNKNOWN && targetSex != PropertySex.UNKNOWN && sex != targetSex) {
                    isWarning = true;
                    tooltip = NbBundle.getMessage(DNDAction.class, "REL_DIFFERENT_SEX", name1, name2);
                }
            }
            
            String nameAction = NbBundle.getMessage(DNDAction.class, "MERGE_ENTITY_ADD", pName, name1, name2);
            JMenuItem mi = popup.add(new DNDAction(false, nameAction, isWarning ? icon.getOverLayed(Images.imgWngOver) : icon.getOverLayed(Images.imgNew)) {
                @Override
                public Entity dropActionPerformed(ActionEvent e) {
                    GedcomUtilities.MergeEntities(targetEntity, importedEntity, true);
                    return targetEntity;
                }
            });
            if (tooltip != null && !tooltip.isEmpty()) {
                mi.setToolTipText(tooltip);
            }
            nameAction = NbBundle.getMessage(DNDAction.class, "MERGE_ENTITY_OVERWRITE", pName, name1, name2);
            mi = popup.add(new DNDAction(false, nameAction, isWarning ? icon.getOverLayed(Images.imgWngOver) : icon.getOverLayed(Images.imgOverwrite)) {
                @Override
                public Entity dropActionPerformed(ActionEvent e) {
                    GedcomUtilities.MergeEntities(targetEntity, importedEntity, false);
                    return targetEntity;
                }
            });
            if (tooltip != null && !tooltip.isEmpty()) {
                mi.setToolTipText(tooltip);
            }
            nameAction = NbBundle.getMessage(DNDAction.class, "MERGE_ENTITY_ASSIST", pName, name1, name2);
            final String title = NbBundle.getMessage(DNDAction.class, "MERGE_ENTITY_ASSIST_TITLE", name1, name2);
            popup.add(new DNDAction(false, nameAction, Images.imgMerge) {
                @Override
                public Entity dropActionPerformed(ActionEvent e) {
                    MergeEntityPanel panel = new MergeEntityPanel(targetGedcom);
                    panel.setEntities(targetEntity, importedEntity, false);
                    if (DialogManager.OK_OPTION == DialogManager.create(title, panel).setOptionType(DialogManager.OK_CANCEL_OPTION).show()) {
                        GedcomUtilities.MergeEntities(targetEntity, importedEntity, panel.getSelectedProperties());
                        return targetEntity;
                    }
                    return null;
                }
            });
        }
        

        /**
         * Init action variables and show popup
         */
        initActions(popup, tc, importedEntity, targetEntity);
        popup.setPopupSize(popup.getPreferredSize().width, popup.getPreferredSize().height);
        popup.show(droppedComponent, droppedPoint.x - popup.getPreferredSize().width/2, droppedPoint.y - 100);

        return true;
        
    }

    private String getFamilyName(Fam fam) {
        String husb = "???";
        String wife = "???";
        if (fam.getHusband() != null) {
            husb = fam.getHusband().getLastName();
        }
        if (fam.getWife() != null) {
            wife = fam.getWife().getLastName();
        }
        return husb + " " + TextOptions.getInstance().getMarriageSymbol() + " " + wife + " (" + fam.getId() + ")";
    }

    
    private void create_Indi_Indi_Actions(JPopupMenu popup, String name1, String name2, String descName, String imageName, Fam[] fams, Indi indi1, Indi indi2, int relation) {

        // For all actions
        String nameAction = NbBundle.getMessage(DNDAction.class, descName, name1, name2);
        String suffAction = "";
        ImageIcon iconCreate = MetaProperty.loadImage(imageName).getOverLayed(Images.imgNew);
        ImageIcon iconUpdate = MetaProperty.loadImage(imageName).getOverLayed(MetaProperty.IMG_LINK);
        
        // Detect errors and warnings of actions for each action regardless of intended family link
        boolean isError = false;
        boolean isWarning = false;
        String tooltip = "";
        boolean anotherFamilyAllowed = true;
        boolean anotherFamilyWarning = false;
        String anotherFamilyTooltip = "";

        // If both individuals are already involved in a relationship (ancestors from eachother (including parent and child), or spouse of eachother or sibling), generate a warning
        if (indi1.isAncestorOf(indi2)) {
            isWarning = true;
            tooltip = NbBundle.getMessage(DNDAction.class, "REL_ANCESTOR_EXISTS", name1, name2);
            anotherFamilyWarning = true;
            anotherFamilyTooltip = anotherFamilyTooltip.isEmpty() ? tooltip : anotherFamilyTooltip;
        } else if (indi1.isDescendantOf(indi2)) {
            isWarning = true;
            tooltip = NbBundle.getMessage(DNDAction.class, "REL_DESCENDANT_EXISTS", name1, name2);
            anotherFamilyWarning = true;
            anotherFamilyTooltip = anotherFamilyTooltip.isEmpty() ? tooltip : anotherFamilyTooltip;
        } else if (indi1.isSpouseOf(indi2)) {
            isWarning = true;
            tooltip = NbBundle.getMessage(DNDAction.class, "REL_SPOUSE_ALREADY_EXISTS", name1, name2);
            anotherFamilyWarning = true;
            anotherFamilyTooltip = anotherFamilyTooltip.isEmpty() ? tooltip : anotherFamilyTooltip;
        } else if (indi1.isSiblingOf(indi2, false)) {
            isWarning = true;
            tooltip = NbBundle.getMessage(DNDAction.class, "REL_SIBLING_ALREADY_EXISTS", name1, name2);
            anotherFamilyWarning = true;
            anotherFamilyTooltip = anotherFamilyTooltip.isEmpty() ? tooltip : anotherFamilyTooltip;

        // If ages are too much apart or too close according to the validation settings, generate a warning
        } else if (isAgeAnomaly(relation, indi1, indi2)) {
            isWarning = true;
            tooltip = NbBundle.getMessage(DNDAction.class, "REL_AGE_ANOMALY");
            anotherFamilyWarning = true;
            anotherFamilyTooltip = anotherFamilyTooltip.isEmpty() ? tooltip : anotherFamilyTooltip;
        }

        
        // Create link to existing families actions, overwriting with an error if any, for each specific family action
        JMenuItem mi = null;
        for (Fam fam : fams) {
            
            if (relation == REL_PARENT || relation == REL_SPOUSE) {
                
                // If the exact same link already exists, generate an error
                // - REL_PARENT and indi1 parent of indi2
                // - REL_SPOUSE and indi1 spouse of indi2
                if (indi1.isSpouseIn(fam)) {
                    isError = true;
                    isWarning = false;
                    tooltip = NbBundle.getMessage(DNDAction.class, relation == REL_PARENT ? "REL_PARENT_ALREADY_EXISTS" : "REL_SPOUSE_ALREADY_EXISTS", name1, name2);
                    anotherFamilyAllowed = false;
                    
                // If indi2 has already got another individual as the relationship, generate an error
                } else if ((relation == REL_PARENT && indi1.getSex() == PropertySex.MALE && fam.getHusband() != null) || 
                           (relation == REL_PARENT && indi1.getSex() == PropertySex.FEMALE && fam.getWife() != null) ||
                           (relation == REL_PARENT && indi1.getSex() == PropertySex.UNKNOWN && fam.getHusband() != null && fam.getWife() != null)) {
                    isError = true;
                    isWarning = false;
                    tooltip = NbBundle.getMessage(DNDAction.class, "REL_PARENT_BOTH_EXISTS", name2);
                    anotherFamilyWarning = true;
                    anotherFamilyTooltip = anotherFamilyTooltip.isEmpty() ? tooltip : anotherFamilyTooltip;
                } else if ((relation == REL_SPOUSE && fam.getOtherSpouse(indi2) != null)) {
                    isError = true;
                    isWarning = false;
                    anotherFamilyWarning = true;
                    tooltip = NbBundle.getMessage(DNDAction.class, "REL_SPOUSE_EXISTS", name2);
                    anotherFamilyTooltip = anotherFamilyTooltip.isEmpty() ? tooltip : anotherFamilyTooltip;
                }
                
                
            } else if (relation == REL_SIBLING || relation == REL_CHILD) {
                
                // If the exact same link already exists, generate an error
                // - REL_SIBLING and indi1 sibling of indi2
                // - REL_CHILD and indi1 child of indi2
                for (Fam fam1 : indi1.getFamiliesWhereChild()) { 
                    if (fam1 == fam) {
                        isError = true;
                        tooltip = NbBundle.getMessage(DNDAction.class, relation == REL_SIBLING ? "REL_SIBLING_ALREADY_EXISTS" : "REL_CHILD_ALREADY_EXISTS", name1, name2);
                        break;
                    } else {
                        isWarning = true; // only a warning as one can be a child in two different families
                        tooltip = NbBundle.getMessage(DNDAction.class, "REL_ALREADY_CHILD", name1);
                        anotherFamilyWarning = true;
                        anotherFamilyTooltip = anotherFamilyTooltip.isEmpty() ? tooltip : anotherFamilyTooltip;
                    }
                }
                
            }

            
            suffAction = NbBundle.getMessage(DNDAction.class, "VIA_FAMILLY", getFamilyName(fam));
            mi = popup.add(new DNDAction(isError, nameAction + " " + suffAction, isWarning ? MetaProperty.loadImage(imageName).getOverLayed(Images.imgWngOver) : iconUpdate) {
                @Override
                public Entity dropActionPerformed(ActionEvent e) {
                    if (relation == REL_PARENT || relation == REL_SPOUSE) {
                        boolean isHusband = descName.equals("FATHER_OF") || descName.equals("HUSBAND_OF");
                        return GedcomUtilities.createParent(isHusband, indi1, fam, fam.getGedcom());
                    }
                    if (relation == REL_SIBLING || relation == REL_CHILD) {
                        return GedcomUtilities.createChild(indi1, fam, fam.getGedcom());
                    }
                    return null;
                }
            });
            if (tooltip != null && !tooltip.isEmpty()) {
                mi.setToolTipText(tooltip);
            }
        }
        
        
        // Create new family action
        if (anotherFamilyAllowed) {
            isError = false;
            isWarning = anotherFamilyWarning;
            suffAction = NbBundle.getMessage(DNDAction.class, "VIA_NEW_FAMILLY");
            mi = popup.add(new DNDAction(isError, nameAction + " " + suffAction, isWarning ? MetaProperty.loadImage(imageName).getOverLayed(Images.imgWngOver) : iconCreate) {
                @Override
                public Entity dropActionPerformed(ActionEvent e) {
                    // Create family with proper place for indi2
                    Indi husb = null, wife = null, child = null;
                    if (relation == REL_PARENT || relation == REL_SIBLING) {
                        child = indi2;
                    } else if (relation == REL_SPOUSE || relation == REL_CHILD) {
                        if (descName.equals("HUSBAND_OF") || indi2.getSex() == PropertySex.FEMALE) {
                            wife = indi2;
                        } else {
                            husb = indi2;
                        }
                    }
                    Fam fam = GedcomUtilities.createFamily(indi2.getGedcom(), husb, wife, child);
                    if (relation == REL_PARENT || relation == REL_SPOUSE) {
                        boolean isHusband = descName.equals("FATHER_OF") || descName.equals("HUSBAND_OF");
                        return GedcomUtilities.createParent(isHusband, indi1, fam, fam.getGedcom());
                    }
                    if (relation == REL_SIBLING || relation == REL_CHILD) {
                        return GedcomUtilities.createChild(indi1, fam, fam.getGedcom());
                    }
                    return null;
                }
            });
            if (tooltip != null && !tooltip.isEmpty()) {
                mi.setToolTipText(tooltip);
            }
        }
        
    }

    private void create_Indi_Fam_Actions(JPopupMenu popup, String name1, String name2, String descName, String imageName, Indi indi1, Fam fam2, int relation) {
        create_Indi_Fam_Actions(popup, name1, name2, descName, imageName, indi1, fam2, relation, false);
    }

    private void create_Fam_Indi_Actions(JPopupMenu popup, String name1, String name2, String descName, String imageName, Fam fam1, Indi indi2, int relation) {
        create_Indi_Fam_Actions(popup, name2, name1, descName, imageName, indi2, fam1, relation, true);
    }

    /**
     * Create actions for Indi-Fam or Fam-Indi DND
     * @param popup
     * @param name1
     * @param name2
     * @param descName
     * @param imageName
     * @param indi1
     * @param fam2
     * @param relation
     * @param reversed : if same gedcom, this has no impact. If different Gedcoms, has an impact: 
     *                   indi1 is copied to gedcom of fam2 if false, fam2 is copied to gedcom of indi1 if true
     */
    private void create_Indi_Fam_Actions(JPopupMenu popup, String name1, String name2, String descName, String imageName, Indi indi1, Fam fam2, int relation, boolean reversed) {

        // For all actions
        String nameAction = NbBundle.getMessage(DNDAction.class, descName, name1, name2);
        ImageIcon iconUpdate = MetaProperty.loadImage(imageName).getOverLayed(MetaProperty.IMG_LINK);
        
        // Detect errors and warnings of actions for each action regardless of intended family link
        boolean isError = false;
        boolean isWarning = false;
        String tooltip = "";

        // If individual is already involved in the family (ancestors of, descendant of, spouse, child of), generate a warning or an error
        if (indi1.isSpouseIn(fam2)) {
            isError = true;
            tooltip = NbBundle.getMessage(DNDAction.class, "REL_SPOUSE_ALREADY_EXISTS", name1, name2);
        } else if (indi1.isChildIn(fam2)) {
            isError = true;
            tooltip = NbBundle.getMessage(DNDAction.class, "REL_CHILD_ALREADY_EXISTS", name1, name2);
        } else if (indi1.isAncestorOf(fam2)) {
            isWarning = true;
            tooltip = NbBundle.getMessage(DNDAction.class, "REL_ANCESTOR_EXISTS", name1, name2);
        } else if (indi1.isDescendantOf(fam2)) {
            isWarning = true;
            tooltip = NbBundle.getMessage(DNDAction.class, "REL_DESCENDANT_EXISTS", name1, name2);

        // If ages are too much apart or too close according to the validation settings, generate a warning
        } else if (isAgeAnomaly(relation, indi1, fam2)) {
            isWarning = true;
            tooltip = NbBundle.getMessage(DNDAction.class, "REL_AGE_ANOMALY");
        }


        // Perform more controls
        if (!isError && relation == REL_SPOUSE) {

            // If fam2 has already got the corresponding individual as the parent, generate an error
            if ((indi1.getSex() == PropertySex.MALE && fam2.getHusband() != null) || 
                       (indi1.getSex() == PropertySex.FEMALE && fam2.getWife() != null) ||
                       (indi1.getSex() == PropertySex.UNKNOWN && fam2.getHusband() != null && fam2.getWife() != null)) {
                isError = true;
                isWarning = false;
                tooltip = NbBundle.getMessage(DNDAction.class, "REL_SPOUSE_IN_FAMILY_EXISTS", name2);
            }

        } else if (!isError && relation == REL_CHILD) {

            // If the individual is already a child in another family, generate a warning
            for (Fam fam1 : indi1.getFamiliesWhereChild()) { 
                if (fam1 != fam2) {
                    isWarning = true; // only a warning as one can be a child in two different families
                    tooltip = NbBundle.getMessage(DNDAction.class, "REL_ALREADY_CHILD", name1);
                }
            }
        }
        
        // Create link to family, overwriting with an error if any
        JMenuItem mi = null;
            
        mi = popup.add(new DNDAction(isError, nameAction, isWarning ? MetaProperty.loadImage(imageName).getOverLayed(Images.imgWngOver) : iconUpdate) {
            @Override
            public Entity dropActionPerformed(ActionEvent e) {
                if (!reversed) {
                    if (relation == REL_SPOUSE) {
                        boolean isHusband = descName.equals("HUSBAND_IN");
                        return GedcomUtilities.createParent(isHusband, indi1, fam2, fam2.getGedcom());
                    }
                    if (relation == REL_CHILD) {
                        return GedcomUtilities.createChild(indi1, fam2, fam2.getGedcom());
                    }
                } else {
                    if (relation == REL_SPOUSE) {
                        boolean isHusband = descName.equals("HUSBAND_IN");
                        return GedcomUtilities.createParent(isHusband, indi1, fam2, indi1.getGedcom());
                    }
                    if (relation == REL_CHILD) {
                        return GedcomUtilities.createChild(indi1, fam2, indi1.getGedcom());
                    }
                }
                return null;
            }
        });
        if (tooltip != null && !tooltip.isEmpty()) {
            mi.setToolTipText(tooltip);
        }
    }

    
    private void create_Attr_Events_Actions(JPopupMenu popup, String name1, String name2, String descName, ImageIcon imageName, Entity importedEntity, Entity targetEntity) {
        
        boolean isError = false;
        String pName = importedEntity.getPropertyName().toLowerCase();

        JMenu attachToEventsMenu = new JMenu(NbBundle.getMessage(DNDAction.class, "ATTACH_MENU", pName, name1, name2));
        attachToEventsMenu.setIcon(imageName);
        popup.add(attachToEventsMenu);
        
        String eventName = NbBundle.getMessage(DNDAction.class, "ATTACH_TO_GENERAL_EVENT");
        attachToEventsMenu.add(new DNDAction(isError, eventName, targetEntity.getImage()) {
            @Override
            public Entity dropActionPerformed(ActionEvent e) {
                return GedcomUtilities.attach(importedEntity, targetEntity);
            }
        });

        for (Property event : targetEntity.getEvents()) {
            String eventStr = event.getPropertyName();
            String dateStr = "?";
            String placeStr = "?";
            if (event.getTag().equals("EVEN")) {
                Property type = event.getProperty("TYPE");
                if (type != null && !type.getDisplayValue().trim().isEmpty()) {
                    eventStr = type.getDisplayValue().trim();
                }
            }
            Property date = event.getProperty("DATE");
            if (date != null) {
                dateStr = date.getDisplayValue();
            }
            Property place = event.getProperty("PLAC");
            if (place != null) {
                placeStr = place.getDisplayValue();
            }
            eventName = NbBundle.getMessage(DNDAction.class, "ATTACH_TO_EVENT", eventStr, dateStr, placeStr);
            attachToEventsMenu.add(new DNDAction(isError, eventName, event.getImage()) {
                @Override
                public Entity dropActionPerformed(ActionEvent e) {
                    return GedcomUtilities.attach(importedEntity, event);
                }
            });
        }
        
        
    }

    
    
    
    private boolean isAgeAnomaly(int relation, Indi indi1, Indi indi2) {
        
        PropertyDate pDate1 = indi1.getBirthDate();
        PropertyDate pDate2 = indi2.getBirthDate();
        if (pDate1 == null || pDate2 == null) {
            return false;
        }
        Delta delta = Delta.get(pDate1.getStart(), pDate2.getStart());
        if (delta == null) {
            return false;
        }
        int ageDiff = delta.getYears();
        
        Preferences modulePreferences = NbPreferences.forModule(Gedcom.class);
        int minAgeFather = modulePreferences.getInt("minAgeFather", 14);
        int minAgeMother = modulePreferences.getInt("minAgeMother", 10);
        int maxAgeMother = modulePreferences.getInt("maxAgeMother", 48);
        int maxDiffAgeSibling = modulePreferences.getInt("maxDiffAgeSibling", 21);
        int maxDiffAgeSpouses = modulePreferences.getInt("maxDiffAgeSpouses", 20);
        
        if ((relation == REL_PARENT || relation == REL_CHILD) && (ageDiff < minAgeFather || ageDiff < minAgeMother || ageDiff > maxAgeMother)) {
            return true;
        }
        if (relation == REL_SPOUSE && ageDiff > maxDiffAgeSpouses) {
            return true;
        }
        if (relation == REL_SIBLING && ageDiff > maxDiffAgeSibling) {
            return true;
        }
        
        return false;
        
    }

    private boolean isAgeAnomaly(int relation, Indi indi1, Fam fam2) {

        Indi spouse = fam2.getHusband();
        if (spouse == null) {
            spouse = fam2.getWife();
        }
        return spouse == null ? false : isAgeAnomaly(relation, indi1, spouse);
        
    }

    private void initActions(JPopupMenu popup, TopComponent tc, Entity importedEntity, Entity targetEntity) {
        
        for (MenuElement me : popup.getSubElements()) {
            if (me instanceof JMenu) {
                JMenu menu = (JMenu) me;
                JPopupMenu pm = menu.getPopupMenu();
                if (pm != null) {
                    initActions(pm, tc, importedEntity, targetEntity);
                }
            }
            if (me instanceof AbstractButton) {
                Action a = ((AbstractButton)me).getAction();
                if (a instanceof DNDAction) {
                    ((DNDAction)a).init(tc, importedEntity, targetEntity);
                }
            }
        }
    }

    private boolean existEntity(Gedcom targetGedcom, Entity entity) {
        boolean ret = false;
        
        String name = entity.toString(false);
        for (Entity ent : targetGedcom.getEntities(entity.getTag())) {
            if (ent.toString(false).equals(name)) {
                return true;
            }
        }
        
        return ret;
    }


    
}
