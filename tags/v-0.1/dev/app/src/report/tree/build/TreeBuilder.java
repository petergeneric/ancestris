/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.build;

import tree.IndiBox;
import genj.gedcom.Indi;

/**
 * Interface for classes building the family tree by creating connected IndiBox
 * classes. The builder class does not determine the positions of boxes but only
 * generates the tree structure.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public interface TreeBuilder {
	public IndiBox build(Indi indi);
}
