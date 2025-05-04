/** Code: Luke Stine
 * An interface for Filtering cell objects.
 */

package psu.expresso.model;

public interface CellFilterIF {

    /**
     * Checks whether the cell meets the filter condition.
     *
     * @param cell The cell to evaluate.
     * @return true if the cell passes the filter.
     */
    boolean filter(CellDataModel<?> cell); // Filters a cell based on a set condition.
}
