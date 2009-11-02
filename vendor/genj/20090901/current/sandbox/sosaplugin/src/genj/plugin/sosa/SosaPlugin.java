/**
 * This GenJ SosaPlugin Source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
//il faut transmettre les instances des autres menus actions dans le setChangeMenuaction class
package genj.plugin.sosa;

/* genj imported classes */
import genj.app.ExtendGedcomClosed;
import genj.app.ExtendGedcomOpened;
import genj.app.ExtendMenubar;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomLifecycleEvent;
import genj.gedcom.GedcomLifecycleListener;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyChild;
import genj.gedcom.PropertyFamilyChild;
import genj.gedcom.PropertyFamilySpouse;
import genj.gedcom.PropertyHusband;
import genj.gedcom.PropertyWife;
import genj.plugin.ExtensionPoint;
import genj.plugin.Plugin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.view.ExtendContextMenu;
/* java imported classes */
import java.util.logging.Logger;

/**
 * A sample plugin that manages Sosa Indexation of individuals
 */
public class SosaPlugin implements Plugin, GedcomLifecycleListener, GedcomListener {

	static final String SOSA_MENU = "Sosa indexation";

	static String SOSA_INFORMATION="Information...";
	static String SOSA_SET="Set indexation with...";
	static String SOSA_CHANGE="Change indexation to...";
	static String SOSA_GET="Get individual from index...";
	static String SOSA_REMOVE="Remove all indexation...";
	private boolean fileRecordedDataFlag = true;

	/* we need this to be fixed as the image cannot be displayed anywhere */
	private final ImageIcon IMG = new ImageIcon(this, "/Sosa.gif");

	/* we need some information on this RESOURCES plugin use */
	private final Resources RESOURCES = Resources.get(this);

	private Logger LOG = Logger.getLogger("genj.plugin.sosa");

	private Registry sosaRegistry;

	private ExtendMenubar menuSosa;

	private MenuActionSETorCHANGE menuActionSetOrChangeIndexation;
	private MenuActionGET menuActionGetIndividualFromIndex;
	private MenuActionREMOVE menuActionRemoveAllIndexation;
	private boolean visibilityStatus;

	private Indi sosaRoot;

	//FIX ME :no way to get access to a non null sosaIndexation from MenuAction
	private SosaIndexation sosaIndexation;

	// private Entity addedEntity;
	// private Indi addedIndi;
	// private Fam addedFam;
	private Indi _toIndi;

	private Indi _fromIndi;

	private Fam _fromFam;

	// private Fam _toFam;
	private String _sosaValue;

	private Property _sosaProperty;

	// private boolean deletedFamcFromIndi=false;
	// private boolean deletedFamsFromIndi=false;
	// private boolean deletedWifeCutFromFam=false;
	// private boolean deletedHusbCutFromFam=false;
	// private boolean childCutFromFam=false;
	// private boolean deletedChildFromFam=false;
	// private boolean famsCutFromIndi=false;
	// private boolean deletedSosaProperty=false;
	// private boolean addedSosaProperty=false;
	// private boolean childAddedToFamc=false;
	// private boolean createIndi=false;
	// private boolean createFam=false;
	// private boolean addWifeToFam=false;
	// private boolean addFamsToIndi=false;
	// private boolean addHusbToFam=false;
	// private boolean _CHILCutFromFAM=false;
	// private boolean _CHILAddedToFAM=false;

	// String family;
	// String individual;
	Indi _indi;

	Fam _fam;

	/* we remove added _SOSA property from indi */
	// private boolean removeSosa=false;
	// private Property propertySosa;
	// private Indi indiSosa;
	private enum interactionType {
		_NULL, _SOSACutFromINDI, _SOSAAddedToINDI, _SOSAModifiedInINDI, _SOSADeletedFromINDI, _SOSASetValueToINDI, _CHILCutFromFAM, _CHILAddedToFAM, _newINDIInFAM, _newFAM
	};

	private interactionType action = interactionType._NULL;

	/**
	 * Our change to enrich an extension point
	 * 
	 * @see genj.plugin.Plugin#extend(genj.plugin.ExtensionPoint)
	 * 
	 */
	public void extend(ExtensionPoint ep) {
		/* we get the gedcom */
		LOG.fine("EP= :"+ep);

		if (ep instanceof ExtendMenubar) {
			/* we pass here : */
			/* - at the very beginning when no data has been up-loaded yet (gedcom = null) */
			/* - each time a gedcom is selected in first window by highlighting */
			Gedcom gedcom = ((ExtendMenubar) ep).getGedcom();
			LOG.fine("A : ON INSTALLE LES MENUS DU GEDCOM  avec gedcom = "+gedcom);
			menuSosa = (ExtendMenubar) ep;
			LOG.fine("A-1 : installation menuSosa = " + menuSosa);
			/* we add sub-menu info */
			/* this menu item is not gedcom dependant therefore may not be necessary to reinstanciate all the time */
				LOG.fine("A-1a : Addition of Info sub-menu");
				/* we display info */
				menuSosa.addAction(SOSA_MENU, new Action2(RESOURCES.getString("Info"), true));
				/* we go though here when installing menus gedcom dependant for the first time */
				LOG.fine("A-1b : Addition of menuActionSetOrChangeIndexation menu-item");
				visibilityStatus=false;
				/* we create menu item action menuActionSetOrChangeIndexation */
				menuActionSetOrChangeIndexation = new MenuActionSETorCHANGE(SOSA_SET, visibilityStatus, null, gedcom);
				/* we display menuActionSetOrChangeIndexation */
				menuSosa.addAction(SOSA_MENU, menuActionSetOrChangeIndexation);
				/* we shadow menu item menuActionSetOrChangeIndexation menu item */
				menuActionSetOrChangeIndexation.setEnabled(false);
				LOG.fine("A-1c : Addition of menuActionGetIndividualFromIndex sub-menu");
				/* we add sub-menu menuActionGetIndividualFromIndex */
				menuActionGetIndividualFromIndex = new MenuActionGET(SOSA_GET, visibilityStatus, null);
				/* we display menuActionSetOrChangeIndexation */
				menuSosa.addAction(SOSA_MENU, menuActionGetIndividualFromIndex);
				 /* we shadow menu item menuActionGetIndividualFromIndex menu item */
				menuActionGetIndividualFromIndex.setEnabled(false);
				LOG.fine("A-1d : Addition of menuActionRemoveAllIndexation sub-menu");
				/* we add sub-menu menuActionRemoveAllIndexation */
				LOG.fine("A-1e : Addition of MenuActionREMOVE sub-menu");
				menuActionRemoveAllIndexation = new MenuActionREMOVE(SOSA_REMOVE, visibilityStatus, null);
				menuActionRemoveAllIndexation.setInstanceOfSetOrChangeIndexationMenuAction(menuActionSetOrChangeIndexation);
				menuActionRemoveAllIndexation.setInstanceOfGetIndividualFromSosaIndexMenuAction(menuActionGetIndividualFromIndex);
				/* we display menuActionRemoveAllIndexationn */
				menuSosa.addAction(SOSA_MENU, menuActionRemoveAllIndexation);
				/* we shadow menuActionRemoveAllIndexation menu item */
				menuActionRemoveAllIndexation.setEnabled(false);
		}

		if (ep instanceof ExtendGedcomOpened) {
			/* we pass here each time a gedcom  is up-loaded through the file selector*/
		    Gedcom gedcom = ((ExtendGedcomOpened)ep).getGedcom();
			LOG.fine("B : ON ATTACHE LE GEDCOM :"+gedcom);
			LOG.fine("Flag= " + isExtendSosaIndexation());
			/* we attach the plugin to gedcom */
			gedcom.addLifecycleListener(this);
			gedcom.addGedcomListener(this);
			LOG.fine("B-1-Ouverture Plugin");
			LOG.fine("B-2-Vérification sosa.root");
			sosaRegistry = new Registry();
			sosaRegistry = genj.util.Registry.lookup(gedcom);
			/* we get sosa.root */
			String registryValue = sosaRegistry.get("sosa.root", (String) null);
			LOG.fine("sosaRegistry = "+registryValue);
			// note value to be removed
			//registryValue = "tagada tsouin tsoin (I2)";
			// note : after plugin installation sosa.root is not be initialized
			if (registryValue == null) {
				/* no sosa.root : first installation of plugin */
				LOG.fine("B-2a-Première installation : pas d'indexation Sosa");
				/* we set sosa indexation */
				sosaRoot = null;
				//sosaIndexation = null;
				/* we set sosa indexation */
				sosaIndexation = new SosaIndexation(sosaRoot, gedcom);
				menuActionSetOrChangeIndexation.setSosaIndexationValue(sosaIndexation);
				/* we up-date menu items */
				menuActionSetOrChangeIndexation.setString(SOSA_SET);
				//menuActionSetOrChangeIndexation.setSosaIndexationValue(sosaIndexation);
				menuActionSetOrChangeIndexation.setEnabled(true);
				//menuActionGetIndividualFromIndex.setSosaIndexationValue(sosaIndexation);
				menuActionGetIndividualFromIndex.setEnabled(false);
				//menuActionRemoveAllIndexation.setSosaIndexationValue(sosaIndexation);
				menuActionRemoveAllIndexation.setEnabled(false);
				menuActionSetOrChangeIndexation.setInstanceOfGetIndividualFromSosaIndexMenuAction(menuActionGetIndividualFromIndex);
				menuActionSetOrChangeIndexation.setInstanceOfRemoveIndexationMenuAction(menuActionRemoveAllIndexation);
				/* we set here a sub-menu = "Install indexation" */
				//menuActionSetOrChangeIndexation.setString(SOSA_SET);
				//menuSosa.addAction(SOSA_MENU,menuActionSetOrChangeIndexation);
			} else {
				LOG.fine("B-2b-on a une indexation Sosa");
				/* we have sosa.root : we check for value recorded */
				if (registryValue.equals("")) {
					/*
					 * we have sosa.root = blank -> we install a "Set
					 * indexation" menu item
					 */
					// note : this test is necessary if we cannot remove
					// sosa.root and therefore
					// it may have to be blanked ; to be confirmed
					LOG.fine("B-2ba : Pas d'indexation Sosa");
					/* we set here a sub-menu = "Install indexation" */
					menuActionSetOrChangeIndexation.setString(SOSA_SET);
					//menuSosa.addAction(MenuActionSETorCHANGE.SOSA_MENU,menuActionSetOrChangeIndexation);
					menuSosa.addAction(SOSA_MENU,menuActionSetOrChangeIndexation);
					// done
				} else {
					/* there is a sosa.root = DeCujus */
					LOG.fine("B-2bb : sosa.root = " + registryValue);
					/* we extract DeCujus individual */
					sosaRoot = (Indi) gedcom.getEntity(Gedcom.INDI,	registryValue.substring(registryValue.lastIndexOf("(") + 1, registryValue.lastIndexOf(")")));
					LOG.fine("Sosa root=" + sosaRoot);
					/* we check for recorded sosa indexation */
					boolean setIndexationFlag;
					if (fileRecordedDataFlag) {
						LOG.fine("B-2bba Enregistrement indexation Sosa = "+ fileRecordedDataFlag);
						LOG.fine("Rien à faire");
						setIndexationFlag = false;
					} else {
						LOG.fine("B-2bbb Enregistrement indexation Sosa = "+ fileRecordedDataFlag);
						/* we set sosa indexation */
						LOG.fine("Indexation Sosa construite");
						LOG.fine("We have to set Sosa from DeCuJus");
						setIndexationFlag = true;
					}
					/* we set sosa indexation */
					sosaIndexation = new SosaIndexation(sosaRoot, gedcom);
					/* we up-date menu items */
					LOG.fine("Addition of menuActionSetOrChangeIndexation sub-menu");
					menuActionSetOrChangeIndexation.setString(SOSA_CHANGE);
					menuActionSetOrChangeIndexation.setSosaIndexationValue(sosaIndexation);
					menuActionSetOrChangeIndexation.setEnabled(true);
					menuActionGetIndividualFromIndex.setSosaIndexationValue(sosaIndexation);
					menuActionGetIndividualFromIndex.setEnabled(true);
					menuActionRemoveAllIndexation.setSosaIndexationValue(sosaIndexation);
					menuActionRemoveAllIndexation.setEnabled(true);
					menuActionSetOrChangeIndexation.setInstanceOfGetIndividualFromSosaIndexMenuAction(menuActionGetIndividualFromIndex);
					menuActionSetOrChangeIndexation.setInstanceOfRemoveIndexationMenuAction(menuActionRemoveAllIndexation);
				}
			}
			// done
			return;
		}

		if (ep instanceof ExtendGedcomClosed) {
			/* we pass here each time a gedcom  is closed */
			Gedcom gedcom = ((ExtendGedcomClosed) ep).getGedcom();
			/* we detach plugin from gedcom */
			LOG.fine("C : ON DÉTACHE LE GEDCOM :"+gedcom);
			/* we save sosaRoot */
			sosaRoot=sosaIndexation.getSosaRoot();
			LOG.fine("sosaRoot=" + sosaRoot);
			LOG.fine("sosaRegistry=" + sosaRegistry);
			sosaRegistry.put("sosa.root", sosaRoot.toString());
			/* we check whether _SOSA tags must be saved or not */
			// ne marche pas : à revoir
			if (!fileRecordedDataFlag) {
				/* we remove all _SOSA tags from gedcom */
				sosaIndexation.removeSosaIndexationFromAllIndis();
				LOG.fine("Pas de sauvegarde des index");
			}
			gedcom.removeLifecycleListener(this);
			gedcom.removeGedcomListener(this);
			LOG.fine("Fermeture gedcom="+gedcom);
			/* we remove the gedcom from the list of opened geedcoms */
				/* if there are no more gedcom opened we shadow menu tems data dependent */
				LOG.fine("PASSE********");
				menuActionSetOrChangeIndexation.setEnabled(false);
				menuActionGetIndividualFromIndex.setEnabled(false);
				menuActionRemoveAllIndexation.setEnabled(false);
			// done
			return;
		}

		if (ep instanceof ExtendContextMenu) {
			// FIXME not implemented yet
			// show a context related sosa action
			ExtendContextMenu _menuSosa = (ExtendContextMenu) ep;
			LOG.fine("passe dans ExtendContextMenu");
		}


	}

	public void handleLifecycleEvent(GedcomLifecycleEvent event) {
		/* more stuff to clarify with Nils */
		// HEADER_CHANGED = 0,
		// WRITE_LOCK_ACQUIRED = 1,
		// BEFORE_UNIT_OF_WORK = 2,
		// AFTER_UNIT_OF_WORK = 3,
		// WRITE_LOCK_RELEASED = 4;
		LOG.fine("Lifecycle event ID = " + event.getId());
		if (event.getId() == GedcomLifecycleEvent.AFTER_UNIT_OF_WORK) {
			switch (action) {
			case _CHILCutFromFAM:
				sosaIndexation.restoreSosaInChildCutFromFam(_toIndi, _fromFam);
				action = interactionType._NULL;
				break;
			case _CHILAddedToFAM:
				sosaIndexation.restoreSosaInChildAddedToFam(_toIndi, _fromFam);
				action = interactionType._NULL;
				break;
			case _SOSACutFromINDI:
				action = interactionType._SOSAAddedToINDI;
				sosaIndexation.restoreSosaValueToIndi(_fromIndi, _sosaValue);
				action = interactionType._NULL;
				break;
			case _SOSAAddedToINDI:
				action = interactionType._SOSACutFromINDI;
				sosaIndexation.deleteExistingSosaIndexFromIndi(_toIndi, _sosaProperty);
				action = interactionType._NULL;
				break;
			case _SOSADeletedFromINDI:
				_toIndi.delProperty(_sosaProperty);
				action = interactionType._NULL;
				break;
			case _SOSASetValueToINDI:
				// on ne pase pas ici
				LOG.fine("passe ici coucou");
				_sosaProperty.setValue(_sosaValue);
				action = interactionType._NULL;
				break;
			case _newINDIInFAM:
				// something to be done
				action = interactionType._NULL;
				break;
			case _newFAM:
				// something to be done
				action = interactionType._NULL;
				break;
			default:
				LOG.fine("2- Lifecycle event ID = " + event.getId());
				break;
			}
		}
	}

	public void gedcomPropertyLinked(Gedcom gedcom, Property from, Property to) {
		LOG.fine("Link Property from : " + from.getValue());
		LOG.fine("Link Property to : " + to.getValue());
		LOG.fine("Link Property from : " + from.getEntity());
		LOG.fine("Link Property to : " + to.getEntity());
		if (from instanceof PropertyChild) {
			/* case CHIL added to FAM */
			_toIndi = (Indi) to.getEntity();
			_fromFam = (Fam) from.getEntity();
			action = interactionType._CHILAddedToFAM;
		} else {
			if (from instanceof PropertyFamilyChild) {
				/* case FAM added to INDI */
				LOG.fine("PASS:PropertyFamilyChild");
			} else {
				if (from instanceof PropertyFamilySpouse) {
					/* case FAMS added to INDI */
					LOG.fine("PASS:PropertyFamilySpouse");
				} else {
					if (from instanceof PropertyHusband) {
						/* case HUB added to FAM */
						LOG.fine("PASS:PropertyHusband");
					} else {
						if (from instanceof PropertyWife) {
							/* case WIFE added to FAM */
							LOG.fine("PASS:PropertyWife");
						}
					}
				}
			}
		}
	}

	public void gedcomPropertyUnlinked(Gedcom gedcom, Property from, Property to) {
		LOG.fine("Unlink Property from : " + from.getValue());
		LOG.fine("Unlink Property to : " + to.getValue());
		LOG.fine("Unlink Property from : " + from.getEntity());
		LOG.fine("Unlink Property to : " + to.getEntity());
		action = interactionType._NULL;
		if (from instanceof PropertyChild) {
			/* case CHIL cut from FAM */
			_toIndi = (Indi) to.getEntity();
			_fromFam = (Fam) from.getEntity();
			action = interactionType._CHILCutFromFAM;
		} else {
			if (from instanceof PropertyFamilyChild) {
				/* case FAM cut from INDI */
				LOG.fine("PASS:PropertyFamilyChild");
			} else {
				if (from instanceof PropertyFamilySpouse) {
					/* case FAMS cut from INDI */
					LOG.fine("PASS:PropertyFamilySpouse");
				} else {
					if (from instanceof PropertyHusband) {
						/* case HUSB cut from FAM */
						LOG.fine("PASS:PropertyHusband");
					} else {
						if (from instanceof PropertyWife) {
							/* case WIFE cut from FAM */
							LOG.fine("PASS:PropertyWife");
						}
					}
				}
			}
		}
	}

	/**
	 * notification that an entity has been added
	 * 
	 * @see GedcomListener#gedcomEntityAdded(Gedcom, Entity)
	 */

	public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
		// more stuff to clarify with Nils
		/* we test here the type of entity added */
		LOG.fine("Entity added : " + entity);
		if (entity.getTag().equals(Gedcom.INDI)) {

			// FIXME
			// addedIndi=(Indi)entity;
			action = interactionType._newINDIInFAM;
		} else {
			if (entity.getTag().equals(Gedcom.FAM)) {
				// FIXME
				// addedFam=(Fam)entity;
				// action=interactionType._NULL;
				action = interactionType._newFAM;
			}
		}
	}

	/**
	 * notification that an entity has been deleted
	 * 
	 * @see GedcomListener#gedcomEntityDeleted(Gedcom, Entity)
	 */

	public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
		// more stuff to clarify with Nils\"
		LOG.fine("Entity deleted : " + entity);
	}

	/**
	 * notification that a property has been added
	 * 
	 * @see GedcomListener#gedcomPropertyAdded(Gedcom, Property, int, Property)
	 */

	public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos,
			Property added) {
		// more stuff to clarify with Nils
		/**
		 * notification that a property has been deleted we track here the
		 * following cut actions : - add _SOSA to an individual : sequence _SOSA
		 * to INDI (A) - cut a child from a family : sequence FAMC from INDI
		 * (1b) + sequence CHIL from FAM (2a) - cut a husband from a family :
		 * sequence FAMS from INDI (3c) + sequence HUSB from FAM (4a) - cut a
		 * wife from a family : sequence FAMS from INDI (3c) + sequence HUSB
		 * from FAM (5a) - cut a individual mariage from a male individual :
		 * sequence HUB from FAM (4b) + sequence FAMS from FAM (3a) - cut a
		 * individual mariage from a female individual : sequence WIFE from FAM
		 * (5a) + sequence FAMS from FAM (3a) - cut a parent mariage from a
		 * individual : sequence HUB from FAM (4b) + sequence FAMS from FAM (3a)
		 * 
		 * @see GedcomListener#gedcomPropertyDeleted(Gedcom, Property, int,
		 *      Property)
		 */
		String propertyTag = property.getTag();
		String addedTag = added.getTag();
		// following line just to help building code to be removed
		LOG.fine("((addedTag.equals(\"" + addedTag
				+ "\")) && (propertyTag.equals(\"" + propertyTag + "\")))");
		// --BEGIN add action of _SOSA tag----
		// here we prevent users from adding _SOSA property
		if (addedTag.equals("_SOSA") && (propertyTag.equals("INDI"))) {
			/* (A) case added _SOSA to INDI */
			switch (action) {
			case _NULL:
				boolean b = false;
				if (b) {
					LOG
							.fine("1 - Sorry addition of _SOSA tag is not possible !");
					_toIndi = (Indi) added.getEntity();
					_sosaProperty = added;
					/* we set action for process */
					action = interactionType._SOSAAddedToINDI;
				} else {
					// new possibility
					LOG
							.fine("1 - Sorry addition of _SOSA tag is not possible !");
					_toIndi = (Indi) added.getEntity();
					_sosaProperty = added;
					// sosaIndexation.deleteSosaIndexFromIndi(_toIndi);
					action = interactionType._SOSAAddedToINDI;
				}
				break;
			case _SOSAAddedToINDI:
				LOG.fine("Addition is ok");
				break;
			default:
				LOG.fine("Cut action is cancelled");
				break;
			}
		}
		// --END add action on _SOSA----
	}

	/**
	 * notification that a property has been changed
	 * 
	 * @see GedcomListener#gedcomPropertyChanged(Gedcom, Property)
	 */

	public void gedcomPropertyChanged(Gedcom gedcom, Property property) {

		// more stuff to clarify with Nils
		LOG.fine("sosa indexation " + isExtendSosaIndexation());
		LOG.fine("Property modified = " + property.getTag());
		LOG.fine("Property value = " + property.getValue());
		if (property.getTag().equals("_SOSA")) {
			LOG.fine("_SOSA modified");
			switch (action) {
			case _SOSAModifiedInINDI:
				/*
				 * we go though this when modifying Sosa properties in an after
				 * 3 cycle
				 */
				action = interactionType._NULL;
				LOG.fine("_SOSA modification : confirmed");
				break;
			default:
				LOG.fine("_SOSA : passe ici");
				break;
			}
		}
	}

	/**
	 * notification that a property has been deleted we track here the following
	 * cut actions : - cut a _SOSA from an individual : sequence _SOSA from INDI
	 * (A)
	 * 
	 * @see GedcomListener#gedcomPropertyDeleted(Gedcom, Property, int,
	 *      Property)
	 */

	public void gedcomPropertyDeleted(Gedcom gedcom, Property property,
			int pos, Property deleted) {
		// more stuff to clarify with Nils
		String propertyTag = property.getTag();
		String deletedTag = deleted.getTag();
		LOG.fine("((deletedTag.equals(\"" + deletedTag
				+ "\")) && (propertyTag.equals(\"" + propertyTag + "\")))");
		// --BEGIN cut action on _SOSA----
		// here we prevent users from cutting _SOSA property
		if (deletedTag.equals("_SOSA") && (propertyTag.equals("INDI"))) {
			/* (A) case deleted _SOSA from INDI */
			switch (action) {
			case _NULL:
				LOG.fine("Sorry cut of _SOSA tag is not possible !");
				_fromIndi = (Indi) deleted.getEntity();
				_sosaValue = deleted.getValue();
				action = interactionType._SOSACutFromINDI;
				break;
			case _SOSADeletedFromINDI:
				LOG.fine("_SOSA removal is confirmed");
				action = interactionType._NULL;
				break;
			default:
				LOG.fine("Add action is cancelled");
				break;
			}
		}
		//--END cut action on _SOSA---- 
	}

	/**
	 * Check whether sosa indexation is actually turned on by user
	 */
	private boolean isExtendSosaIndexation() {
		return SosaOptions.getInstance().isExtendSosaIndexation;
	}
}