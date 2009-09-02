/* This GenJ MenuActionREMOVE source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genj.plugin.sosa;

/* genj imported classes */
import genj.util.swing.Action2;
import genj.util.Resources;
/* java imported classes */
import java.util.logging.Logger;

/**
 * MenuAction
 */

public class MenuActionREMOVE extends Action2 {

	private String menuItem;

	private boolean status;

	private SosaIndexation sosaIndexation;

	private MenuActionSETorCHANGE menuActionSetOrChangeIndexation;

	private MenuActionGET menuActionGetIndividualFromIndex;

	private Logger LOG = Logger.getLogger("genj.plugin.sosa");

	private final Resources RESOURCES = Resources.get(this);

	/**
	 * Menu action constructor
	 * 
	 * @param menuItem string label
	 * @param status visibility status
	 * @param sosaIndexation instance of sosa indexation
	 */
	public MenuActionREMOVE(String menuItem, boolean status, SosaIndexation sosaIndexation) {
		this.status = status;
		this.sosaIndexation = sosaIndexation;
		LOG.fine("Set menu item = " + menuItem);
		setString(menuItem);
	}

	/**
	 * Change label of menu item
	 */
	public void setString(String menuItem) {
		this.menuItem = menuItem;
		setText(RESOURCES.getString(menuItem));
	}

	/**
	 * Set Sosa indexation
	 */
	public void setSosaIndexationValue(SosaIndexation indexation) {
		this.sosaIndexation = indexation;
	}

	/**
	 * Set visibility status
	 */
	public void setVisibilityStatus(boolean status) {
		this.status = status;
	}

	/**
	 * Get visibility status
	 */
	public boolean getVisibilityStatus() {
		return status;
	}

	/**
	 * Set MenuActionSETorCHANGE instance
	 */
	public void setInstanceOfSetOrChangeIndexationMenuAction(MenuActionSETorCHANGE instance) {
		menuActionSetOrChangeIndexation=instance;
	}

	/**
	 * Set MenuActionGET instance
	 */
	public void setInstanceOfGetIndividualFromSosaIndexMenuAction(MenuActionGET instance) {
		menuActionGetIndividualFromIndex=instance;
	}

	/**
	 * Execute click on menu item
	 */
	protected void execute() {
		LOG.fine("Passe SOSA_REMOVE");
		/* we check which menu item is displayed */
		/* we remove all Sosa index of gedcom */
		if (sosaIndexation == null) {
			LOG.fine("GROS PROBLEM !");
		}
		/* we remove sosa indexation */
		sosaIndexation.removeSosaIndexationFromAllIndis();
		/* we update menu items */
		menuActionSetOrChangeIndexation.setString(SosaPlugin.SOSA_SET);
		menuActionGetIndividualFromIndex.setEnabled(false);
		menuActionGetIndividualFromIndex.setVisibilityStatus(false);
		status = false;
		setEnabled(status);
	}
}
