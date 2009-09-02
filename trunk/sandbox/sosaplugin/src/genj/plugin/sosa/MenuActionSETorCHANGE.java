/**
 * This GenJ MenuACtionSETorCHANGE source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genj.plugin.sosa;

/* genj imported classes */
import genj.gedcom.Gedcom;
import genj.util.swing.Action2;
import genj.util.Resources;
import genj.window.WindowManager;
import genj.gedcom.Indi;
//import genj.util.swing.ChoiceWidget;
import genj.common.SelectEntityWidget;
/* java imported classes */
import java.util.logging.Logger;

/**
 * MenuAction
 */

public class MenuActionSETorCHANGE extends Action2 {

	private String menuItem;
	private boolean status;
	private Gedcom gedcom;

	private Indi sosaRoot;

	private SosaIndexation sosaIndexation;

	private MenuActionGET menuActionGetIndividualFromIndex;
	private MenuActionREMOVE menuActionRemoveAllIndexation;
	private Logger LOG = Logger.getLogger("genj.plugin.sosa");

	private final Resources RESOURCES = Resources.get(this);

	public enum myMenuEnum {

		/* we initialise enum constants */
		SET(SosaPlugin.SOSA_SET), CHANGE(SosaPlugin.SOSA_CHANGE);

		private String item;

		/* Constructor */
		myMenuEnum(String item) {
			this.item = item;
		}

		/* Method to retrieve constant value */
		public String getItem() {
			return item;
		}

		/* Method to returning myMenuEnum instance */
		public static myMenuEnum valueFor(String item) {
			for (myMenuEnum instance : values()) {
				if (instance.item.equals(item)) {
					return instance;
				}
			}
			return null;
		}
	}

	/**
	 * Set Sosa Indexation
	 */
	public void setSosaIndexationValue(SosaIndexation indexation) {
		LOG.fine("sosaIndexation= " + indexation.toString());
		this.sosaIndexation=indexation;
	}

	/**
	 * Set MenuActionGET instance
	 */
	public void setInstanceOfGetIndividualFromSosaIndexMenuAction(MenuActionGET instance) {
		menuActionGetIndividualFromIndex=instance;
	}

	/**
	 * Set MenuActionREMOVE instance
	 */
	public void setInstanceOfRemoveIndexationMenuAction(MenuActionREMOVE instance) {
		 menuActionRemoveAllIndexation=instance;
	}

	/**
	 * Menu action constructor
		*
		* @param menuItem label of menu item
		* @param status visibility status
		* @param sosaIndexation instance of sosa indexation
		* @param gedcom data
	 * 
	 */
	public MenuActionSETorCHANGE(String menuItem, boolean status, SosaIndexation sosaIndexation, Gedcom gedcom) {
		//this.menuItem = menuItem;
		this.status = status;
		this.sosaIndexation = sosaIndexation;
		this.gedcom = gedcom;
		LOG.fine("Set menu item = " + menuItem);
		//setText(RESOURCES.getString(menuItem));
		setString(menuItem);
	}

	/**
	 * Set label of menu item
	 */
	public void setString(String menuItem) {
		this.menuItem = menuItem;
		setText(RESOURCES.getString(menuItem));
	}
	
	/**
	 * Set visibility status
	 */
	public void	setVisibilityStatus(boolean status) {
		this.status=status;
	}

	/**
	 * Get visibility status
	 */
	public boolean getVisibilityStatus() {
		return status;
	}

	/**
	 * Execute click on menu item
	 */
	protected void execute() {
		LOG.fine("Passe SOSA_SETorCHANGE");
			/* we set select SelectEntityWidget */
			SelectEntityWidget select = new SelectEntityWidget(gedcom,
					Gedcom.INDI, null);
			int rc;
			/* we check which menu item is displayed */
			switch (myMenuEnum.valueFor(menuItem)) {
			case SET:
				if (sosaIndexation == null) {
					LOG.fine("Serious problem !");
				}
				/* we get Decujus to set Sosa indexation */
				rc = WindowManager.getInstance(getTarget()).openDialog(null,
						"Select Sosa Root", WindowManager.QUESTION_MESSAGE,
						select, Action2.okCancel(), getTarget());
				if (rc != 0) {
					LOG.fine("No selection");
				} else {
					sosaRoot = (Indi) select.getSelection();
					/* we set sosa root */
					sosaIndexation.setSosaRoot(sosaRoot);
					/* we set sosa gedcom */
					sosaIndexation.setSosaGedcom(gedcom);
					/* we build sosa indexation */
					sosaIndexation.setSosaIndexation(sosaRoot);
					LOG.fine("Indexation Sosa construite with :"
							+ sosaRoot.toString());
					/* we up-date menu items */
					LOG.fine("passe ici***");
					setString(myMenuEnum.CHANGE.getItem());
					menuActionGetIndividualFromIndex.setVisibilityStatus(true);
					menuActionGetIndividualFromIndex.setSosaIndexationValue(sosaIndexation);
					menuActionGetIndividualFromIndex.setEnabled(true);
					menuActionRemoveAllIndexation.setVisibilityStatus(true);
					menuActionRemoveAllIndexation.setSosaIndexationValue(sosaIndexation);
					menuActionRemoveAllIndexation.setEnabled(true);
				}
				break;
			case CHANGE:
				/* we change sosa indexation */
				LOG.fine("Need here ask for DeCujus");
				rc = WindowManager.getInstance(getTarget()).openDialog(null,
						"Select Sosa Root", WindowManager.QUESTION_MESSAGE,
						select, Action2.okCancel(), getTarget());
				sosaRoot = rc == 0 ? (Indi) select.getSelection() : null;
				if (sosaRoot != null) {
					LOG.fine("Sosa root=" + sosaRoot.toString());
					if (sosaRoot != sosaIndexation.getSosaRoot()) {
						/*
						 * we remove previous indexation including Sosa
						 * properties and map entry
						 */
						sosaIndexation.removeSosaIndexationFromIndi(
								sosaIndexation.getSosaRoot(), 1);
						/* we set sosa root */
						sosaIndexation.setSosaRoot(sosaRoot);
						/* we set sosa gedcom */
						sosaIndexation.setSosaGedcom(gedcom);
						/* we build sosa indexation */
						sosaIndexation.setSosaIndexation(sosaRoot);
						LOG.fine("Indexation Sosa built with :"
								+ sosaRoot.toString());
					}
				}
				break;
			}
	}
}
