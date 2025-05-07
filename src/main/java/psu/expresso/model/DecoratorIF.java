/** Code: Luke Stine
 * An interface for Decorating cell objects for different behaviors or styles.
 */

package psu.expresso.model;

public interface DecoratorIF {
    String displayValue(); // Returns a modified string representation of the cell value.

    default void applyTo(CellDataModel<?> cell) {
        cell.setDecorator(this);
    }
}
