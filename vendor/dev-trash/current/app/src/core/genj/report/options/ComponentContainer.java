/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genj.report.options;

import java.util.List;

/**
 * Interface for components containing other components.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public interface ComponentContainer
{
    List<Object> getComponents();
}
