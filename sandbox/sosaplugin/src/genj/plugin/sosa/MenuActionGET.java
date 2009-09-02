/**
 * This GenJ MenuActionGET source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genj.plugin.sosa;

/* genj imported classes */
//import genj.gedcom.Gedcom;
import genj.util.swing.Action2;
import genj.util.Resources;
import genj.window.WindowManager;
//import genj.gedcom.Indi;
import genj.util.swing.ChoiceWidget;
/* java imported classes */
import java.util.logging.Logger;

/**
 * MenuAction
 */

public class MenuActionGET extends Action2 {

	private boolean status;

	private SosaIndexation sosaIndexation;

	private Logger LOG = Logger.getLogger("genj.plugin.sosa");

	private final Resources RESOURCES = Resources.get(this);

	/**
	 * Menu action constructor
		* @param menuItem label of menu item
		* @param status visibility status
		* @param sosaIndexation instance of sosa indexation
		* @param gedcom data
	 */
	public MenuActionGET(String menuItem, boolean status, SosaIndexation sosaIndexation) {
		this.status = status;
		this.sosaIndexation = sosaIndexation;
		LOG.fine("Set menu item GET========= = " + menuItem);
		setText(RESOURCES.getString(menuItem));
	}

	/**
	 * Set Sosa Indexation
	 */
	public void setSosaIndexationValue(SosaIndexation indexation) {
		this.sosaIndexation=indexation;
	}

	/**
	 * Get visibility status
	 */
	public boolean getVisibilityStatus() {
		return status;
	}

	/**
	 * Set visibility status
	 */
	public void	setVisibilityStatus(boolean status) {
		this.status=status;
	}


	/**
	 * Execute click on menu item
	 */
	protected void execute() {
		LOG.fine("Passe SOSA_GET");
		/* we get Sosa index of individual */
		ChoiceWidget choice = new ChoiceWidget(sosaIndexation
				.getSosaIndexArray(),
				sosaIndexation.getSosaIndexArray().length > 0 ? sosaIndexation
						.getSosaIndexArray()[0] : "");
		int rc = WindowManager.getInstance(getTarget()).openDialog(null,
				"Choisir un index", WindowManager.QUESTION_MESSAGE, choice,
				Action2.okCancel(), getTarget());
		String result = rc == 0 ? choice.getText() : null;
		if (result != null) {
			LOG.fine("individual is : "
					+ sosaIndexation.getSosaMap().get(Integer.parseInt(result))
							.toString());
		}
	}
}
