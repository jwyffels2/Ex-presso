/** Code: Luke Stine
 * A filter that checks if a cell's value is of a specified Java type.
 */

package psu.expresso.model;

public class TypeCellFilter implements CellFilterIF {

    private final Class<?> targetType;

    /**
     * Constructs a filter to match cells whose values are of a specific class type.
     *
     * @param targetType the Java class to match (e.g., String.class, Integer.class)
     */
    public TypeCellFilter(Class<?> targetType) {
        this.targetType = targetType;
    }

    // Returns true if the cell's value is non-null and an instance of the specified type.
    @Override
    public boolean filter(CellDataModel<?> cell) {
        Object value = cell.getValue();
        return value != null && targetType.isInstance(value);
    }
}
