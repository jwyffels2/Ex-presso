/** Code: Luke Stine
 * Filters cells based on specific criteria such as value ranges, types, or formulas.
 */

package psu.expresso.model;

public class ValueCellFilter implements CellFilterIF {

    private final Object targetValue; // The value used for cells to match.

    public ValueCellFilter(Object value) {
        this.targetValue = value; // The value to filter cells.
    }

    /**
     * Checks whether the given cell's value equals the target value.
     *
     * @param cell The cell to evaluate.
     * @return true if the cell's value matches the target.
     */
    @Override
    public boolean filter(CellDataModel<?> cell) {
        return targetValue.equals(cell.getValue()); // Returns true if the values match.
    }
}
