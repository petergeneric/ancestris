/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.arrange;

import tree.IndiBox;
import tree.IndiBox.Direction;

/**
 * Aligns the tree to the right edge.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class AlignRightArranger extends AbstractArranger {

    /**
     * Constructs the object.
     *
     * @param spacing minimal gap between boxes and lines
     */
    public AlignRightArranger(int spacing) {
        super(spacing);
    }

    public void filter(IndiBox indibox) {
        indibox.wPlus = indibox.width;
        indibox.hPlus = 1;
        super.filter(indibox);
    }

    protected void arrangeSpouse(IndiBox indibox, IndiBox spouse) {
        spouse.wPlus = spouse.width;
        spouse.x = -spouse.width;
    }

    protected void arrangeChildren(IndiBox indibox) {
        int currentX = indibox.width;
        if (indibox.getDir() == Direction.PARENT)
            currentX = indibox.prev.width / 2 - indibox.x - spacing;
        for (int i = 0; i < indibox.children.length; i++) {
            IndiBox child = indibox.children[i];
            child.x = currentX - child.width;
            child.y = 1;
            filter(child);
            currentX -= child.wPlus + child.wMinus + spacing;
        }
        if (indibox.children.length == 1) {
            IndiBox child = indibox.children[0];
            int parentWidth = indibox.wMinus + indibox.wPlus;
            int childWidth = child.wMinus + child.wPlus;
            int centerX = (parentWidth - childWidth) / 2 - indibox.wMinus + child.wMinus;
            if (child.x > centerX)
                child.x = centerX;
        }
    }

    protected void arrangeNextMarriages(IndiBox indibox, IndiBox next) {
        next.x = -indibox.wMinus - next.width - spacing;
        if (indibox.spouse != null && indibox.spouse.nextMarriage == next)
            next.x -= indibox.spouse.x;
        filter(next);
    }

    protected void arrangeSpouseParent(IndiBox indibox, IndiBox parent) {
        parent.x = indibox.width - parent.width;
        filter(parent);
        parent.y = -parent.hPlus;
    }

    protected void arrangeParent(IndiBox indibox, IndiBox parent) {
        parent.x = indibox.width - parent.width;
        filter(parent);
        parent.y = -indibox.hMinus - parent.hPlus;
    }
}
