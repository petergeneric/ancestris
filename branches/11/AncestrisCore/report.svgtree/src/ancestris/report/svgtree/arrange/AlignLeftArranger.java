/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ancestris.report.svgtree.arrange;

import ancestris.report.svgtree.IndiBox;
import ancestris.report.svgtree.IndiBox.Direction;

/**
 * Aligns the tree to the left edge.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class AlignLeftArranger extends AbstractArranger {

    /**
     * Constructs the object.
     *
     * @param spacing minimal gap between boxes and lines
     */
	public AlignLeftArranger(int spacing) {
		super(spacing);
	}

    public void filter(IndiBox indibox) {
        indibox.wPlus = indibox.width;
        indibox.hPlus = 1;
        super.filter(indibox);
    }

    protected void arrangeSpouse(IndiBox indibox, IndiBox spouse) {
        spouse.wPlus = spouse.width;
		spouse.x = indibox.width;
	}

	protected void arrangeChildren(IndiBox indibox) {
		int currentX = 0;
		if (indibox.getDir() == Direction.PARENT)
            currentX = indibox.prev.width / 2 - indibox.x + spacing;

		for (int i = 0; i < indibox.children.length; i++) {
			IndiBox child = indibox.children[i];
			child.y = 1;
			filter(child);
            child.x = currentX + child.wMinus;
			currentX += child.wMinus + child.wPlus + spacing;
		}
		//int min = indibox.children[0].x - indibox.children[0].wMinus;
		int childrenWidth = currentX - spacing;
            int parentWidth = indibox.wMinus + indibox.wPlus;
        
        if (parentWidth > childrenWidth) {
        	int diff = (parentWidth - childrenWidth) / 2 - indibox.wMinus;
	        for (int i = 0; i < indibox.children.length; i++) {
    	        IndiBox child = indibox.children[i];
        	    child.x += diff;
        	}
        }
	}

	protected void arrangeNextMarriages(IndiBox indibox, IndiBox next) {
		filter(next);
        next.x = indibox.wPlus + next.wMinus + spacing;
        if (indibox.spouse != null && indibox.spouse.nextMarriage == next)
            next.x -= indibox.spouse.x;
	}

	protected void arrangeSpouseParent(IndiBox indibox, IndiBox parent) {
		filter(parent);
		parent.x = parent.wMinus;
		parent.y = -parent.hPlus;
	}

	protected void arrangeParent(IndiBox indibox, IndiBox parent) {
		filter(parent);
		parent.y = -indibox.hMinus - parent.hPlus;
	}
}
